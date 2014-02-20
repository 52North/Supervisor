# Supervisor

Build status: [![Build Status](https://travis-ci.org/52North/Supervisor.png)](https://travis-ci.org/52North/Supervisor)

Website: https://wiki.52north.org/bin/view/Sensornet/OwsSupervisor

See https://wiki.52north.org/bin/view/SensorWeb/OwsSupervisor

## About

The Supervisor is a Java server application with a simple user interface and API to supervise the state of web services. Originally focussing on standardized OGC services, the Supervisor is now a tool to support maintainers of web services to get a quick overview of the current state of the deployments.

The basic procedure is as follows: A check is activated in Supervisor and runs at a specific interval. The check has a specific results, for example "passed", "neutral" or "fail", and it can be accessed via an API and a browser-based user interface.

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