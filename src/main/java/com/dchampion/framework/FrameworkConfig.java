package com.dchampion.framework;

import org.springframework.context.annotation.Configuration;

/**
 * Any module using this framework should add this class to its configuration via the
 * module's entrypoint. For example, in an entrypoint class called FrameworkConsumer,
 * you would add parameters to the @SpringBootApplication annotatation, as in:
 *
 * <pre>
 * %40SpringBootApplication(scanBasePackageClasses={FrameworkConsumer.class,FrameworkConfig.class})
 * public class FrameworkConsumer {
 *   public static void main(String[] args) {
 *     SpringApplication.run(FrameworkConsumer.class, args);
 *   }
 * }
 * </pre>
 *
 */
@Configuration
public class FrameworkConfig {
    // Spring component-scan hook (with optional additional configuration).
}