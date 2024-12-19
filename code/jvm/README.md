# How to run the project

- Execute the sql script [createSchema.sql](repository-jdbi/src/main/sql/createSchema.sql) to create the database and the tables.

After this you can run the project by two ways:
- In intellij idea, you can run the project by clicking the green arrow on the left of the [main function](gomoku/src/main/kotlin/pt/isel/daw/GomokuApplication.kt)
  and then [run the javascript code](../js/README.md).


- To run the docker version you should move to this folder jvm folder:
```bash 
cd code/jvm
 ``` 
- To start the docker:
```bash 
docker-compose up
 ``` 

