# framework
## Summary
In order to run this framework on your computer, you will need to install both the framework itself, and a separate project [framework&dash;demo&dash;vanilla&dash;js](https://github.com/dchampion/framework-demo-vanilla-js) that demonstrates its features. Follow the instructions below to install both.

For a full descritpion of the features of this framework, consult the [wiki](https://github.com/dchampion/framework/wiki/Web-Application-Framework).

## Project Requirements

* The Java Development Kit (JDK), version 17 or greater, must be installed on your computer. Type `java -version` at a command prompt to check your Java version. If it is less than 17, or it is not installed, you can git it [here](https://www.oracle.com/java/technologies/downloads/).

## Install the _framework_ and _framework&dash;demo&dash;vanilla&dash;js_ Projects
Use one of the following methods to install the framework and framework&dash;demo&dash;vanilla&dash;js projects:

* If Git is installed, navigate to a clean directory on your file system and type `git clone https://github.com/dchampion/framework.git`. This will install the framework in a subdirectory called `framework`.

* From the same directory in which you executed the `clone` in the previous step, type `git clone https://github.com/dchampion/framework-demo-vanilla-js.git`. This will install the companion framework demo project in a subdirectory called `framework-demo-vanilla-js`.

* If `Git` is not installed, or you do not wish to use it, click the `Code` button on this page and select `Download ZIP` to download and extract a zipped version of the project into a clean file system directory. Do the same on the  [framework&dash;demo&dash;vanilla&dash;js](https://github.com/dchampion/framework-demo-vanilla-js) page.

## Build the _framework_ and _framework&dash;demo&dash;vanilla&dash;js_ Projects
* Using an operating system command&dash;line shell&mdash;e.g., `cmd` (Windows) or `bash` (Linux or MacOS)&mdash;change into the `framework` directory and type `./mvnw clean install` (if using Windows, type `mvnw clean install`&mdash;that is, omit the `./`).

* Change into the `framework-demo-vanilla-js` directory and type `./mvnw clean package` (again, if using Windows, type `mvnw clean package`).

* (Optional) To generate Javadocs for either project, type `./mvnw javadoc:javadoc` from the project root directory. To browse the Javadocs, open `target/site/apidocs/index.html` (relative to the project root directory) in a web browser.

## Run the _framework&dash;demo&dash;vanilla&dash;js_ Project
* From the root directory of the framework&dash;demo&dash;vanilla&dash;js project, type `java -jar target/framework-demo-vanilla-js-2.0.0.jar` (the Java Development Kit (JDK) version 17 or greater must be in your search path for this command to work).

* Type `http://localhost:8080` in the address bar of a web browser of your choice.

* Experiment with the capabilities of the framework by manipulating the controls in its simple browser interface.

* For finer&dash;grained detail on the interactions between the browser and the server, load the browser developer tools (provided with most web browsers) and navigate to the `Network` view to inspect client requests, server responses, status codes, header values and other useful information.