*** OwsSupervisor ***

This is a small web application (for Tomcat Servlet containers) to supervise OGC Web Service implementations.

In the current stat, it supports:
 - scheduling of service checks
 - Java file based check creation (see org.n52.owsSupervisor.data for an example)
 - a web front end to inspect the latest checks
 - notification of failures via email
 
Future tasks:
 - XML based / property file based check data structure
 - ...

Please see /build/build.xml for an ANT build script.

Please see gnu-gplv2.txt for license information.

Developer Contact: Daniel Nüst <daniel.nuest@uni-muenster.de>