# Java Tools Framework
### Summary
In order to exercise the <i>Java Tools Framework</i> contained in this repo, you will need to install both the Framework itself, and a separate project&mdash;the <i>Framework Demo Project</i>&mdash;that demonstrates the Framework in action.

Follow the instructions below to install both the <i>Java Tools Framework</i> and the <i>Framework Demo Project</i>.

### Project Requirements

* The Java Development Kit (JDK), version 12 or greater, must be installed on your computer.

### Install the Java Tools Framework and Demo Project
Use either of the following methods to install the Framework and  Demo Project, and their build and runtime dependencies, on your computer:

* If Git is installed, navigate to a clean directory on your file system and type <code>git clone https<nolink>://github.com/dchampion/framework.git</code>. This will install the Framework in a subdirectory called <code>framework</code>.

* From the same directory in which you executed the <code>clone</code> in the previous step, type <code>git clone https<nolink>://github.com/dchampion/framework-demo.git</code>. This will install the Framework Demo Project in a subdirectory called <code>framework-demo</code>.

* Alternatively, if <code>Git</code> is not installed, or you do not wish to use it, use links on this page and the <a href=https://github.com/dchampion/framework-demo target="_blank">Project Demo home page</a> to download and extract zipped versions of the repos into a clean file system directory.

### Build the Java Tools Framework and Demo Project
* Using a command (Windows) or bash (Linux or Mac) shell, change into the <code>framework</code> directory and type <code>./mvnw clean install</code> (if using Windows, type <code>mvnw clean install</code>&mdash;that is, omit the <code>./</code>).

* Change into the <code>framework-demo</code> directory and type <code>./mvnw clean install</code> (again, if using Windows, type <code>mvnw clean install</code>).

* (Optional) To generate Javadocs for either project, type <code>./mvnw javadoc:javadoc</code> from the project root directory. To browse the Javadocs, open <code>target/site/apidocs/index.html</code> (relative to the project root directory) in a web browser. (Note this path will differ for the Framework Demo Project. There it is <code>server/target/site/apidocs/index.html</code>.)

### Run the Framework Demo
* From the root directory of the Framework Demo Project, type <code>java -jar server/target/server-1.0.0.jar</code> (the Java Development Kit (JDK) version 12 or greater must be in your search path for this command to work).

* Type <code>http<nolink>://localhost:8080</code> in the address bar of a web browser of your choice.

* Experiment with the capabilities of the long-call framework by manipulating the controls in its simple browser interface.

* For finer-grained detail on the interations between the browser and the server, load the browser developer tools (provided with most web browsers) and navigate to the <code>Network</code> view to inspect client requests, server responses, status codes, header values and other useful information.