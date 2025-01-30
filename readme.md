# Sample Banking Application

A Scala project with Play framework and Postgres database to implement the problem statement in [docs](docs/readme.md).

## Pre-requisites
1. Expecting that the user already knows how to setup git and clone the repository
2. sbt, Java and Scala are available in the system.
3. Postgres is downloaded and running, either in docker or as application.

## How to run

1. Connect to postgres-sql. Validate the connection:
```curl
curl -v http://localhost:5432
```
If you see connection succeeded, then postgres is up and running.

2. Run the application in terminal.
```sbt
sbt clean compile
```
Once it's compiled, run the project using below command.

```sbt
sbt run
```
