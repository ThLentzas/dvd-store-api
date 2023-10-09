# DVD Store Service

This is a Java Spring Boot application that provides a simple Restful API service for a DVD store. The API allows users to perform CRUD operations on DVD records. The application includes user authentication and authorization using Spring Security. It also includes Docker for containerization and portability and can be run on a Kubernetes cluster using Minikube

# Features
* User registration and login with JWT authentication
* Caching of DVD data with Redis
* Docker support
* Kubernetes support

The application also provides the following DVD-related operations:

* Retrieve a list of all DVDs
* Search for DVDs by title
* Retrieve details about a specific DVD
* Add new DVDs to the database
* Update existing DVDs
* Delete DVDs from the database

# Technologies
* Java 17
* Spring Boot 3.1.0
* Spring Security
* PostgreSQL
* Redis
* JWT
* Docker
* Kubernetes
* TestContainers
* Junit5
* Mockito

# Getting Started
To get started with this project, you can choose either to run it locally your host machine, on docker or in a single node Kubernetes cluster using minikube:
## Locally
You will need to have the following installed on your machine:

* Java 17+
* Maven 3.9.1+
* PostgreSQL 15.2+
* Redis 7.0.10+

To build and run the project, follow these steps:

* Clone the repository to your local machine: https://github.com/ThLentzas/dvd-store-api.git
* Navigate to the project directory
* Build the project: `mvn clean install -DskipTests`
* Run the project: `mvn spring-boot:run`

## Docker
To run the project on Docker, make sure you have Docker installed on your machine.

### Option 1: Manual Docker Build
* Clone the repository to your local machine: https://github.com/ThLentzas/dvd-store-api.git
* Navigate to the `scripts` folder in the`docker` directory.
* Run the `build.bat` file.This will create a Docker image for the application: `./build.bat`
* Start the containers(dvd-store, psql, redis) by running the `start.bat` file: `./start.bat`
* Stop the containers and remove them along with the images,networks and volumes by running the `stop.bat` file: `./stop.bat`

### Option 2: Docker Compose
* Clone the repository to your local machine: https://github.com/ThLentzas/dvd-store-api.git
* Navigate to the `docker` directory
* Start the containers by running the Docker Compose file: `docker-compose up`

## Kubernetes
To run the project on Minikube, make sure you have Minikube, Docker and kubectl installed on your machine.
* Clone the repository to your local machine: https://github.com/ThLentzas/dvd-store-api.git
* Navigate to the `scripts` folder in the `kubernetes` directory
* Run the `setup.bat` file 
* Get the minikube IP address.
* Access the application's endpoints at the given IP address

# Endpoints

## Authentication
* POST `/api/v1/auth/signup` - creates a new user account and returns an authentication token.
* POST `/api/v1/auth/login` - logs a user into the system and returns an authentication token.
## DVDs
* GET `/api/v1/dvds` - retrieves a list of all DVDs.
* GET `/api/v1/dvds?title=title` - retrieves a list of DVDs that match the specified title.
* GET `/api/v1/dvds/{dvdId}` - retrieves details about a specific DVD.
* POST `/api/v1/dvds` - adds a new DVD to the database.
* PUT `/api/v1/dvds/{dvdID}` - updates the quantity and genre of an existing DVD.
* DELETE `/api/v1/dvds/{dvdId}` - deletes a DVD from the database.

# Error Handling

The application includes exception handling for several types of errors to provide a better user experience and give
appropriate responses to requests. The following exceptions are handled:

* IllegalArgumentException - This exception is thrown when an attempt is made to add or update a DVD with invalid information. The response for this exception is a 400 Bad Request status code along with an error message indicating the cause of the exception.
* ResourceNotFoundException - This exception is thrown when a requested resource, such as a specific DVD, is not found in the database or the system. The response for this exception is a 404 Bad Request status code along with an error message indicating the cause of the exception.
* DuplicateResourceException - This exception is thrown when an attempt is made to add a DVD or a user with an email  that already exists in the database. The response for this exception is a 400 Bad Request status code along with an  error message indicating that the resource already exists.
* BadCredentialsException - This exception is thrown when a user provides incorrect login credentials. The response for this exception is a 401 Unauthorized status code along with an error message indicating that the credentials are invalid.
* AccessDeniedException - This exception is thrown when a user does not have the required permission to access a resource. The response for this exception is a 403 Forbidden status code along with an error message indicating that the user is not authorized to access the resource.
* ServerErrorException - This exception is thrown when an unexpected error occurs on the server while processing a request. The response for this exception is a 500 Internal Server Error status code along with an error message indicating that something went wrong on the server. 
