# ARCHIVED

This project is no longer maintained and will not receive any further updates. If you plan to continue using it, please be aware that future security issues will not be addressed.

# Supervisor

Website: https://wiki.52north.org/bin/view/Sensornet/OwsSupervisor

## About

The Supervisor is a Java server application with a simple user interface and API to supervise the state of web services. Originally focussing on standardized OGC services, the Supervisor is now a tool to support maintainers of web services to get a quick overview of the current state of the deployments.

The basic procedure is as follows: A check is activated in Supervisor and runs at a specific interval. The check has a specific results, for example "passed", "neutral" or "fail", and it can be accessed via an API and a browser-based user interface.

## Configuration

You can add your own configuration easily by putting your own copy of supervisor.properties into the user home of the service that executes your servlet container, e.g. ``/home/tomcat/.Supervisor/supervisor.properties``. If you are unsure where to put this file, take a look at the log file and search for ``ConfigModule``, which will tell you where exactly Supervisor looks for the files.

## Developer documentation

### Code structure

The functionality is organised in the following packages and uses Guice for modularization:

* ``org.n52.supervisor``: core classes for configuration and initialization, the actual servlets, and initialisation of modules
* ``org.n52.supervisor.api``: core API for checks and check runners (not only interfaces, but also abstract classes)
* ``org.n52.supervisor.checks``: basic implementations of checks (data storage layer) and check runners (business logic)
* ``org.n52.supervisor.checks.ows``: checks for OGC web services
* ``org.n52.supervisor.checks.util``: utility checks (e.g. self check)
* ``org.n52.supervisor.db``: (non-) persistent storage of checks and check results 
* ``org.n52.supervisor.id``: module for identifier generation
* ``org.n52.supervisor.notification``: module for notifications (e.g. e-mail)
* ``org.n52.supervisor.resources``: JAX-RS web resources (i.e. the API endpoints) 
* ``org.n52.supervisor.tasks``: module for (scheduled) execution of checks
* ``org.n52.supervisor.util``: utility classes

### How a check works

A check basically consists of two classes. The actual ``Check`` implementation captures all the settings and configuration of a check, such as the name, the author, the identifier, the resource to be checked.
The ``CheckRunner`` is handed this information and executes the check, so this class contains the actual check and notification logic and stores the results of checks during it's lifetime. The runner is controlled by a ``CheckTask``, an executable class that can be run manually or at a time interval.
The ``CheckTask`` executes the methods of the runner in the required order: ``check()`` and then depending on the result ``notifySuccess()`` or ``notifyFailure()``.

### Important Interfaces

**IServiceChecker**: The main interface, which is eventually wrapped in a task that is scheduled. It contains all information and logic to run a specific check (like execution interval) on a service and the respective actions to take in successful or unsuccessful cases. It provides a template for the generic check procedure, which is (i) =check()=, and (ii) depending of the outcome either =notifyFailure()= or =notifySuccess()=. *Implementing classes:*

<img alt="OwsSupervisor-classes.png" src="https://wiki.52north.org/pub/SensorWeb/OwsSupervisor/OwsSupervisor-classes.png" />

A **CheckerCollection** can consolidate several checkers. This is currently not available via configuration file settings.

*ICheckResult**: The outcome of a check, which has an identifier, a certain type (positive or negative), and a textual description. This result can be used to generate different notification outputs (!HTML page, email, !SMS).

**ICheckerFactory**: A factory interface to be used for compiled checks with =CHECK_CLASSES=, see above. Implement this interface if the existing checkers do not suffice for your needs.

**IJobScheduler**: Wraps the whole tasking component. The task management and execution is done in an extra servlet, =Timer=. It provides an implementation of !IJobScheduler which provides methods for submitting and cancelling an object of class !IServiceChecker.



## License

Supervisor is published under The Apache Software License, Version 2.0.

### Java libraries

See the NOTICE file.

### Javascript libraries

* jQuery, https://jquery.org/license/ - MIT License
* AngularJS, https://github.com/angular/angular.js/blob/master/LICENSE - MIT License
* Bootstrap, https://github.com/twbs/bootstrap/blob/master/LICENSE - MIT License

## Contact

Daniel Nüst <d.nuest@52north.org>
