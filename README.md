# KFoundation: JVM/JS Edition

KFoundation is a library of essentials for modern programming needs. 
It is designed based on the concept of universality, both in 
technological and geographical senses. It is available on most platforms; it 
has a very small footprint with no dependency on third-party libraries, and it is 
highly optimized for CPU and memory usage so that it works smoothly on small 
devices. It natively supports UTF-8 (as opposed to the host platform's string 
format), and comes with a  rich internationalization toolset.

This repository contains the JVM/JS edition. The main code is written in Scala,
which is cross-compiled for JVM and JS, and wrapped inside friendly native 
Java and JS APIs for the convenience of the users of those languages. 

The JVM/JS edition is mostly concerned with semantics of data exchange. For example, 
the (de)serialization module can read and write objects from and to XML, YAML, 
JSON, and K4 out of the box, and can be extended for more. The native 
[C++ edition](https://github.com/hkhandan/kfoundation) 
is more extensive as it completely replaces (and does not depend on) C and C++ 
standard libraries. 

## Home Page
Contains all resources, API documentations, dependency management, etc.

http://kfoundation.net/



## Complete Tutorial Video
Everything you need to know to get started and make the best use of this library.

https://mscp.co/resouces/video/kfoundation-tutorial.html


## Patreon Page
If you like this project, and want to show your support.

https://www.patreon.com/kfoundation
