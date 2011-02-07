====================================
Welcome to the Cordys Script Connector
====================================
Script Connector is an application connector that allows web services to be implemented in JavaScript. 
The script runs inside the application connector (i.e. it has no relation to the front-end or the browser). 
This way a prototype of a real web service can be modeled quickly and usually this connector is used for 
creating web service stubs where the script returns a fixed response that emulates the real back-end.

A typical use case is creating stub methods for an external web service. In this case the Cordys method set
 for these external methods is created using the UDDI method generator, this method set is attached to the
 Script Connector SOAP node and scripts are created for these methods. When the real web service is available,
 the method set can be attached to the UDDI SOAP node and the real web service can be accessed.

License (see also LICENSE.txt)
==============================
Collective work: Copyright 2004 Cordys R&D B.V.

Licensed to Cordys R&D B.V. under one or more contributor license agreements.  
See the NOTICE file distributed with this work for additional information 
regarding copyright ownership.

Cordys licenses this product to you under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Latest development
==================
The latest Cordys Script Connector source code is available via Subversion at

   http://code.google.com/p/cordysscriptcon/

Credits
=======
See http://code.google.com/p/cordysscriptcon//people/list for the list of connector 
committers and main contributors. Special credits to Phillip Gussow and Mikko Poyhonen, the 
original developer. 