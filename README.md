# user-roles

A generic service featuring RBAC (Role Based Access Control) using AWS Cognito

### Running locally

1. Start the database

   ```bash
   docker-compose up -d
   ```

1. Build and run the application

   All of these will skip tests to be quick.

    1. Option 1: In IDE

       Run the `Main` file.

    1. Option 2: Via Maven

       ```bash
       ./build-and-run.sh
       ```

    1. Option 3: Package and run with the actual Docker image

       ```bash
       # See the script for details.
       ./build-and-run-docker.sh
       ```

1. Access the service at http://localhost:8080/health

### Linting

ktlint is used. Prefer using `mvn verify` to check linting.

Due to using Maven multi-module project, there will be an error if running
ktlint commands directly. This can be resolved by also compiling the project
so that it properly resolves the dependency tree.

To only check linting (no tests etc):

```bash
mvn compile ktlint:check
```

To format (does not fail on lint errors):

```bash
mvn compile ktlint:format
```
