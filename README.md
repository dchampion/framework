# framework
## Summary
In order to exercise the features of the framework contained in this repo, you will need to install both the framework itself, and a separate project&mdash;the <i><a href=https://github.com/dchampion/framework-demo target="_blank">framework-demo</a></i>&mdash;that uses it.

Follow the instructions below to install both the <i>framework</i> and the <i>framework-demo</i>.

For a full descritpion of the features of this framework, consult the <a href=https://github.com/dchampion/framework/wiki/Java-Tools-Framework target="_blank">wiki</a>.

## Project Requirements

* The Java Development Kit (JDK), version 8 or greater, must be installed on your computer.

## Install the <i>framework</i> and <i>framework-demo</i> Projects
Use one of the following methods to install the framework and framework-demo projects, and their build and runtime dependencies:

* If Git is installed, navigate to a clean directory on your file system and type <code>git clone https<nolink>://github.com/dchampion/framework.git</code>. This will install the Framework in a subdirectory called <code>framework</code>.

* From the same directory in which you executed the <code>clone</code> in the previous step, type <code>git clone https<nolink>://github.com/dchampion/framework-demo.git</code>. This will install the Framework Demo Project in a subdirectory called <code>framework-demo</code>.

* Alternatively, if <code>Git</code> is not installed, or you do not wish to use it, use links on this page and the <a href=https://github.com/dchampion/framework-demo target="_blank">framework-demo home page</a> to download and extract zipped versions of the repos into a clean file system directory.

## Build the <i>framework</i> and <i>framework-demo</i> Projects
* Using a command (Windows) or bash (Linux or Mac) shell, change into the <code>framework</code> directory and type <code>./mvnw clean install</code> (if using Windows, type <code>mvnw clean install</code>&mdash;that is, omit the <code>./</code>).

* Change into the <code>framework-demo</code> directory and type <code>./mvnw clean install</code> (again, if using Windows, type <code>mvnw clean install</code>).

* (Optional) To generate Javadocs for either project, type <code>./mvnw javadoc:javadoc</code> from the project root directory. To browse the Javadocs, open <code>target/site/apidocs/index.html</code> (relative to the project root directory) in a web browser. (Note this path will differ for the framework-demo Project; there it is <code>server/target/site/apidocs/index.html</code>.)

## Run the <i>framework-demo</i> Project
* From the root directory of the framework-demo project, type <code>java -jar server/target/server-1.0.0.jar</code> (the Java Development Kit (JDK) version 8 or greater must be in your search path for this command to work).

* Type <code>http<nolink>://localhost:8080</code> in the address bar of a web browser of your choice.

* Experiment with the capabilities of the framework by manipulating the controls in its simple browser interface.

* For finer-grained detail on the interactions between the browser and the server, load the browser developer tools (provided with most web browsers) and navigate to the <code>Network</code> view to inspect client requests, server responses, status codes, header values and other useful information.