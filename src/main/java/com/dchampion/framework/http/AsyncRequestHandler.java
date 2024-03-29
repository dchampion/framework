package com.dchampion.framework.http;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Use an instance of this class to execute a long-running HTTP method
 * asynchronously, to report on the status of the method while it is executing,
 * and to return the results of the method when it is complete.
 * <p>
 * Use the {@link #submit(Callable, int) submit} method to submit for asynchronous
 * execution a long-running task wrapped in a {@link Callable} instance. Then
 * use the {@link #poll(String) poll} method to check the status of the long-running
 * task at periodic intervals. If the long-running task has completed by the
 * time {@link #poll(String) poll} is called, then {@link #poll(String) poll} will return
 * a status of {@link TaskStatus#COMPLETE COMPLETE}, along with the results of the
 * long-running task. Otherwise {@link #poll(String) poll} will return a status of
 * {@link TaskStatus#PENDING PENDING}.
 * <p>
 * The cardinality of runtime instances of this class should be one instance per
 * long-running REST controller method. That is, when using this class, declare
 * a separate instance of it for each long-running HTTP method you wish to use
 * it with.
 * <p>
 * <b>Important:</b> By default, instances of this class that are suitable for
 * singleton processes will be instantiated by the Spring runtime. Such
 * instances are preferable in the singleton-process use case because they will be
 * very performant. However, if your process is designed to scale to more than a
 * single instance, say in a containerized runtime environment, then you must
 * use a version of this class suitable for multiple process instances. This
 * is done in two steps, as specified here:
 * <ol>
 * <li>Declare a property {@code async.response_cache.scope=shared} in your
 * {@code application.properties} file (note that by default this property is
 * {@code async.response_cache.scope=in-process}).</li>
 * <li>Assuming you are using an RDBMS for persistent data storage, create a
 * table in your database with the following definition:</li>
 * </ol>
 * <pre>
 * <br>
 * CREATE TABLE RESPONSE_CACHE
 * (
 *   UUID               CHAR(36 BYTE),
 *   HEADERS            VARCHAR2(512 BYTE),
 *   BODY               CLOB
 * )
 * </pre>
 * Below is an example usage of this class, inside a REST controller class containing
 * a call to a long-running method:
 * <pre>
 * <br>
 * &#64;Autowired
 * private AsyncRequestHandler&lt;List&lt;String&gt;&gt; handler;
 * 
 * &#64;PostMapping("/submit")
 * public ResponseEntity&lt;List&lt;String&gt;&gt; submit() {
 *     // Submit the task with a timeout of 10 minutes (600 seconds). This is a non-blocking call.
 *     return handler.submit(() -&gt; longRunningTask(), 600);
 * }
 *
 * &#64;GetMapping("/poll/{id}")
 * public ResponseEntity&lt;List&lt;String&gt;&gt; poll(&#64;PathVariable String id) {
 *     // Check the status of the task.
 *     return handler.poll(id);
 * }
 *
 * // A long-running task.
 * private List&lt;String&gt; longRunningTask() {
 *     Thread.sleep(10000);
 *     return Arrays.asList("Hello", "Client!");
 * }
 * </pre>
 * Below is the HTTP client's perspective of the above implementation. The first call
 * submits the long-running task for asynchronous execution:
 * <pre>
 * <br>
 * <b>Reqeust</b>
 * URL:             http://localhost:8080/submit
 * Method:          POST
 * Body (Optional): {"timeout":600}
 *
 * <b>Response</b>
 * Status Code:     202 (Accepted)
 * Header Values:   Task-Status=submitted
 *                  Task-Id=cf645961-9b2c-4a60-b994-a9f093e5ac56
 * Body:            null
 * </pre>
 * Subsequent calls poll the previously submitted long-running task for its
 * status, supplying the {@code Task-Id} returned by the first call to this method:
 * <pre>
 * <br>
 * <b>Request</b>
 * URL:             http://localhost:8080/poll/cf645961-9b2c-4a60-b994-a9f093e5ac56
 * Method:          GET
 *
 * <b>Response</b>
 * Status Codes:    200 (OK) if Task-Status is "pending" or "complete"
 *                  400 (BAD_REQUEST) if Task-Status is "unsubmitted"
 *                  500 (INTERNAL_SERVER_ERROR) if Task-Status is "error" or "timedout"
 * Header Values:   Task-Status=pending
 *                  Task-Status=complete
 *                  Task-Status=unsubmitted
 *                  Task-Status=error
 *                  Task-Status=timedout
 *Body:             ["Hello","Client!"] if Task-Status is "complete"
 *                  null if Task-Status is any other value.
 * </pre>
 * If {@code Task-Status=complete}, the body of the {@link ResponseEntity} returned by the
 * REST call will contain the results of the long-running task.
 *
 * @param <T> the body of the HTTP response returned by the long-running task
 *            executed by an instance of this class.
 */
@Component
public class AsyncRequestHandler<T> {

    /** Task status indicators. */
    public enum TaskStatus {
        /** The task has been submitted for execution. */
        SUBMITTED("submitted"),
        /** The task is in progress. */
        PENDING("pending"),
        /** The task has completed. */
        COMPLETE("complete"),
        /** The task has either never been submitted, or completed on a previous call to poll. */
        UNSUBMITTED("unsubmitted"),
        /** The task failed to complete due to an error. */
        ERROR("error"),
        /** The task failed to complete before the timeout period expired. */
        TIMEDOUT("timedout");

        private final String status;

        TaskStatus(String status) {
            this.status = status;
        }

        /**
         * Return a string-ified version of this enum.
         *
         * @return a string-ified version of this enum.
         */
        public String getStatus() {
            return status;
        }
    }

    private static final String TASK_STATUS_KEY = "Task-Status";

    private static final Logger log = LoggerFactory.getLogger(AsyncRequestHandler.class);

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Autowired
    ResponseCache<T> responseCache;

    /**
     * Submits a long-running task, wrapped in a {@link Callable} instance, for
     * asynchronous execution from a REST controller method. This method returns
     * immediately; that is, it does not wait for the task to complete.
     * <p>
     * See {@link AsyncRequestHandler class-level Javadoc} for an example usage of this
     * method.
     *
     * @param task     The {@link Callable} instance containing the long-running task.
     *                 The type parameter of the {@link Callable} instance, and its return
     *                 value, will comprise the contents of the HTTP response body when the
     *                 task is complete.
     * @param timeout  The time allowed, in seconds, for the long-running task to complete. If
     *                 the task does not complete within this duration, this class'
     *                 {@link #poll(String) poll} method will report a status of
     *                 {@link TaskStatus#TIMEDOUT TIMEDOUT} in its header's {@code Task-Status} field.
     *                 This value must be a positive integer.
     *
     * @return a {@link ResponseEntity} containing no body, but with a header
     *         containing a {@code Task-Status} of {@link TaskStatus#SUBMITTED SUBMITTED}, and a
     *         {@code Task-Id} to be used in subsequent calls to this class'
     *         {@link #poll(String) poll} method. This {@link ResponseEntity} should be
     *         returned immediately to the client making the long-running REST call.
     *         An HTTP status of {@code 202 (Accepted)} is included in the response to
     *         further indicate to the client that the request has been received but
     *         not yet fulfilled.
     *
     * @throws NullPointerException     if arg {@code task} is {@code null}.
     * @throws IllegalArgumentException if arg {@code timeout} is not a positive integer.
     */
    public ResponseEntity<T> submit(Callable<T> task, int timeout) {

        // Validate args.
        Objects.requireNonNull(task);
        if (timeout < 1) {
            throw new IllegalArgumentException("Timeout must be a positive integer.");
        }

        // Generate unique task ID for task tracking.
        UUID uuid = UUID.randomUUID();
        
        // Build an initial response to return immediately to the caller.
        ResponseEntity<T> response = ResponseEntity
            .accepted()
            .cacheControl(CacheControl.noStore())
            .header("Task-Id", uuid.toString())
            .header(TASK_STATUS_KEY, TaskStatus.SUBMITTED.getStatus())
            .build();
        
        // Cache it.
        responseCache.put(uuid, response);

        // Submit the task for asynchronous execution (this call doesn't block).
        executorService.execute(() -> doTask(uuid, task, timeout));

        // Return the response.
        return response;
    }

    /**
     * Executes long-running task and waits for it to finish.
     * 
     * @param uuid A unique task ID.
     * @param task The task.
     * @param timeout The timeout value for the task.
     */
    private void doTask(UUID uuid, Callable<T> task, int timeout) {
        try {
            // Execute the task and wait for it to finish.
            T body = executorService.submit(task).get(timeout, TimeUnit.SECONDS);
            
            // Build a response containing the results of the task.
            ResponseEntity<T> response = ResponseEntity
                .ok()
                .cacheControl(CacheControl.noStore())
                .header(TASK_STATUS_KEY, TaskStatus.COMPLETE.getStatus())
                .body(body);
            
            // Cache it.
            responseCache.put(uuid, response);

        } catch (InterruptedException e) {
            log.warn("Task for id " + uuid.toString() + " interrupted.", e);
            buildErrorResponse(uuid, TaskStatus.ERROR, e);
            Thread.currentThread().interrupt();

        } catch (ExecutionException e) {
            log.error("Task for id " + uuid.toString() + " threw an exception.", e);
            buildErrorResponse(uuid, TaskStatus.ERROR, e);

        } catch (TimeoutException e) {
            log.warn("Task for id " + uuid.toString() + " timed out.");
            buildErrorResponse(uuid, TaskStatus.TIMEDOUT, e);
        }
    }

    /**
     * {@link #doTask(UUID, Callable, int) doTask} helper method to build up an error response.
     * 
     * @param uuid The task ID.
     * @param status The {@link TaskStatus}.
     * @param t The {@link Throwable} thrown by the task.
     */
    private void buildErrorResponse(UUID uuid, TaskStatus status, Throwable t) {
        
        // Get the original cause if ExecutionException.
        if (t instanceof ExecutionException) {
            t = t.getCause();
        }

        // Build a response.
        ResponseEntity<T> response = ResponseEntity
            .status(HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
            .cacheControl(CacheControl.noStore())
            .header(TASK_STATUS_KEY, status.getStatus())
            .header("Task-Error-Type", t.getClass() != null ? t.getClass().getName() : "Unknown")
            .header("Task-Error-Message", t.getMessage() != null ? t.getMessage() : "None")
            .build();
        
        // Cache it.
        responseCache.put(uuid, response);
    }

    /**
     * Polls the status of a long-running task previously submitted to this class'
     * {@link #submit(Callable, int) submit} method, given its {@code Task-Id}.
     * <p>
     * One of five possible statuses is returned in the header of the
     * {@link ResponseEntity} returned by this method, depending on the state of the
     * long-running task:
     * <ol>
     * <li>{@code Task-Status}={@link TaskStatus#UNSUBMITTED UNSUBMITTED}: No task was found for the
     * supplied id; either no such task was ever submitted, or the task has since
     * completed.</li>
     * <li>{@code Task-Status}={@link TaskStatus#PENDING PENDING}: The task is currently
     * running.</li>
     * <li>{@code Task-Status}={@link TaskStatus#COMPLETE COMPLETE}: The task is complete.</li>
     * <li>{@code Task-Status}={@link TaskStatus#ERROR ERROR}: The task threw an exception, or was
     * interrupted by another thread.</li>
     * <li>{@code Task-Status}={@link TaskStatus#TIMEDOUT TIMEDOUT}: The task did not complete before
     * the timeout value specified in the submit() call expired.</li>
     * </ol>
     * <p>
     * If the {@code Task-Status} is {@link TaskStatus#COMPLETE COMPLETE}, then the results of the
     * long-running task will be included in the body of the {@link ResponseEntity}
     * returned by this method. In all other cases, the body will be {@code null}.
     * <p>
     * If {@code Task-Status} is {@link TaskStatus#ERROR ERROR}, then two additional headers
     * will be included in the response indicating the type of error
     * ({@code Task-Error-Type}) and an error message ({@code Task-Error-Message}).
     * <p>
     * See {@link AsyncRequestHandler class-level Javadoc} for sample usages of this
     * method.
     *
     * @param id the id of the long-running task. This id is supplied in the
     *           {@code Task-Id} header of the {@link ResponseEntity} returned by this
     *           class' {@link #submit(Callable, int) submit} method.
     *
     * @return a {@link ResponseEntity} containing a {@code Task-Status} header indicating
     *         the status of the long-running task and, if the status is
     *         {@link TaskStatus#COMPLETE COMPLETE}, the results of the long-running task in its
     *         body.
     *
     * @throws NullPointerException if arg {@code id} supplied to this method is {@code null}.
     */
    public ResponseEntity<T> poll(String id) {

        Objects.requireNonNull(id);

        // Get the current state of the task from the response cache.
        UUID uuid = UUID.fromString(id);
        ResponseEntity<T> response = responseCache.get(uuid);
        
        if (response == null) {
            
            // We have no response for the supplied task ID.
            response = ResponseEntity
                .status(HttpStatusCode.valueOf(HttpStatus.BAD_REQUEST.value()))
                .cacheControl(CacheControl.noStore())
                .header(TASK_STATUS_KEY, TaskStatus.UNSUBMITTED.getStatus())
                .build();

        } else {
            
            // If we have a response body we know the task has finished.
            T body = response.getBody();
            if (body != null) {

                // Remove the response from the cache as we are done.
                responseCache.remove(uuid);

                // Build up a response containing the results of the task.
                response = ResponseEntity
                    .ok()
                    .cacheControl(CacheControl.noStore())
                    .header(TASK_STATUS_KEY, TaskStatus.COMPLETE.getStatus())
                    .body(body);

            } else {
                
                // Task has not finished; adjust headers accordingly.
                HttpHeaders headers = response.getHeaders();
                String status = headers.getFirst(TASK_STATUS_KEY);
                if (status.equals(TaskStatus.SUBMITTED.getStatus())) {
                    
                    // This is the first time we've been polled; tell the caller
                    // the task is still pending.
                    response = ResponseEntity
                        .ok()
                        .cacheControl(CacheControl.noStore())
                        .header(TASK_STATUS_KEY, TaskStatus.PENDING.getStatus())
                        .build();

                    responseCache.put(uuid, response);
                } else if (status.equals(TaskStatus.ERROR.getStatus())
                        || status.equals(TaskStatus.TIMEDOUT.getStatus())) {

                    // In the case of an error or a timeout, remove the
                    // response from the cache.
                    responseCache.remove(uuid);
                }
            }
        }

        return response;
    }
}
