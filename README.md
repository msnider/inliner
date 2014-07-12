Inliner
=======

A typical Cascading Style Sheet presents several problems if you want to store
a snapshot of a web page and display it later, possibly offline.

Inliner is a small application that hopes to solve these problems. Inliner acts
as an HTTP server in which a style sheet is requested through it (like a proxy)
and it attempts to:
1. Merge all @import statements into the same document
2. Convert all external resources (images, fonts, etc) into Data-URIs
3. Given a User Agent & related browser information, remove styles that would 
    have been invisible due to media queries
4. Preserve the cache headers so the resource should have the same cache lifetime
    as the underlying resource

**Summary:** Given a Style Sheet & User Agent, Inliner should produce a stylesheet
that should render identically in the browser, but request 0 external resources
and not rely on any media queries (so it will look the same on all devices).

## Requirements

To run the built application:
* Java 1.7+

To build:
* JDK 1.7+
* Maven 3

## Run

Inliner is built with Spring Boot, so it can easily be run via Maven:

`mvn spring-boot:run`

Or it can be built into a runnable jar:

`mvn package`

Which can be run:

`java -jar target/mymodule-0.0.1-SNAPSHOT.jar`
