# ConnectionHandler

ConnectionHandler is an Open Source Android library project for handling communication with RESTful services. It is based on Apache HttpClient.

ConnectionHandler advantages:

 - Support for both synchronous and asynchronous calls.
 - Support for many configuration options like connection timeouts, using cookies and many more.
 - Simple mechanism for logging entire communication with API. All request's and response's parameters are included: headers, time stamps, status codes, cookies, body descriptions, etc.
 - Predefined views to be used for displaying logs.

Library uses PersistentCookieStore and SerializableCookie classes written by James Smith (<james@loopj.com>).

For information how to use the library please check included sample application and library code.

### Project integration

Add repository reference in your build.gradle file:

```
repositories {
    ...
    maven {
        url 'http://dev.open-rnd.net:30844/content/groups/public/'
    }
    ...
}
```

Add library dependence:

```
dependencies {
    compile group: "pl.openrnd.android", name: "connection-handler", version: "1.0.1"
}
```

### License

2015 (C) Copyright Open-RnD Sp. z o.o.

Licensed under the Apache License, Version 2.0 (the "License");<br />
you may not use this file except in compliance with the License.<br />
You may obtain a copy of the License at<br />

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software<br />
distributed under the License is distributed on an "AS IS" BASIS,<br />
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.<br />
See the License for the specific language governing permissions and<br />
limitations under the License.
