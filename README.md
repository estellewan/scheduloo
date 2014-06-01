# JAX-RS Template Application

This is a template for a lightweight RESTful API using JAX-RS. The sample code is a call for getting the current time.
    
## Running the application locally

First build with:

    $mvn clean install

Then run it with:

    $ java -cp target/classes:target/dependency/* com.example.Main

## Sample Test

GET: Fetch list of all registered courses:

    http://secret-oasis-8161.herokuapp.com/services/course

POST: Add a course to database:

    1. Easier way to test this is to get a REST API Client e.g. RESTClient add-on for FireFox (https://addons.mozilla.org/en-US/firefox/addon/restclient/)

    2. Message body: {"id":"2001", "subjectCode":"AMATH","subjectCatalog":"332","section":"LEC 001"}

    3. Make sure headers contain: "Content-Type: application/json"
