# How to run the project

- Execute the sql script [createSchema.sql](repository-jdbi/src/main/sql/createSchema.sql) to create the database and the tables.

After this you can run the project by two ways:
- In intellij idea, you can run the project by clicking the green arrow on the left of the [main function](gomoku/src/main/kotlin/pt/isel/daw/GomokuApplication.kt)
  and then [run the javascript code](../js/README.md).


- To run the docker version you should move to this folder jvm folder:
```bash 
cd code/jvm
 ``` 

## Build the docker images:
- Builds the JVM image with ChImp backend HTTP API:
  ```bash
  ./gradlew buildImageJvm
  ```
- Builds the Postgres image for testing:
  ```bash
  ./gradlew buildImagePostgresTest
  ```
- Builds the Nginx image:
  ```bash
  ./gradlew buildImageNginx
  ```
- Builds all images:
  ```bash
  ./gradlew buildImageAll
  ```

## Start, scale, and stop services:
- Starts all services:
  ```bash
  ./gradlew allUp
  ```
- Stops all services:
  ```bash
  ./gradlew allDown
  ```
- To scale the dynamic JVM service:
  - First move to host folder:
    ```bash
    cd host
    ```
    - Then run the following command:
    ```bash
      docker-compose up --scale talkrooms-jvm=3
    ```
