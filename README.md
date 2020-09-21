# KFoundation: JVM/JS Edition

## About
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



## Installation

Of course, you can always clone, change, and build this repository. But, for
your convenience, precompiled code is made available through Maven Central and 
npm.


### Scala (sbt)

### Scala.js (sbt)

### Java (Maven)

### JavaScript (npm)

### Clone & Build



## Reference Documentation