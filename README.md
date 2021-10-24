# Double Entry

__Double Entry__ is a bookkeeping application making the power of the double entry system accessible through an intuitive 
user interface designed to be usable regardless of accounting knowledge. 

![Double Entry dashboard](https://github.com/joern-kalz/double-entry/blob/master/img/dashboard.png)

## How it works

__Double Entry__ is a web application with a Spring Boot back end and an Angular front end. The interface between back
end and front end is specified with OpenAPI employing the API-first approach. 

## Getting Started

The following steps describe how to set up the application.

### Prerequisites

#### Java

Java at least Version 11 is installed.

#### Optional: Database

This step is optional, but keep in mind that without setting up the database all data will be 
stored in memory only and lost after terminating the application.

For setting up the database install PostgreSQL. Create a database user `double_entry`. 
Create a database `double_entry` owned by the user `double_entry`. 

Configure the following environment variables

| Name | Value |
| ---- | ----- |
| spring_datasource_url | JDBC connection string, e.g. jdbc:postgresql://192.168.1.32:5432/double_entry |
| spring_datasource_username | `double_entry` |
| spring_datasource_password | the password you defined for the user double_entry |
| spring_jpa_hibernate_ddl_auto | `update` |
| spring_jpa_database_platform | `org.hibernate.dialect.PostgreSQLDialect` |

### Build

Build the application with

```bash
./gradlew build
```

### Run 

Run the application with

```bash
java -jar server/build/libs/server-0.0.1-SNAPSHOT.jar
```

### Usage

Open the browser and navigate to [http://localhost:8080](http://localhost:8080).

Click _Create new account_ and enter a username and password. To start with some test data click _Select_ in the 
_Restore backup_ section and select the file _test-data/build/backup.json_ located in this repository. Click _Sign up_.

You can now enter transactions by clicking one of the buttons in the upper left or browse the example data by clicking 
one of the charts.

## Continuous Integration

The repository includes a Jenkinsfile that builds, tests and analysis front end as well as back end and pushes a 
Docker container containing the application to a configured Docker registry.

### Prerequisites

Jenkins, Docker registry and SonarQube are deployed.

### Configuration

Configure the environment variables in Jenkins

| Name | Value |
| ---- | ----- |
| DOCKER_REGISTRY | host and port of the docker registry separated by a colon, e.g. 192.168.2.232:5000 |
| JIB_OPTIONS | additional options for Jib if required |
| SONARQUBE_TOKEN | The access token for SonarQube |
| SONARQUBE_URL | The url of your SonarQube server, e.g. http://192.168.1.30:9000 |

Create a new job of type _Multibranch Pipeline_. Set _Project Repository_ to this repository and leave all other 
parameters as default. 
