# Pre-req

In order to run the application the following tools need to be installed
1) Java 21
2) Docker
3) Maven

# Running the application

You can run the application in a number of ways;

# Deploying the application

You can deploy the application to a dev environment using the [.github/workflows/build_and_deploy.yml](.github/workflows/first_workflow.yml) workflow. This will build and push the image to the central ACR in the `devopsutils` Resource Group. It will then deploy this image to the Web App. For more information see; [confluence](https://goddard-smith.atlassian.net/wiki/spaces/nomad/pages/22020097/Platform)

Requirements;
1) The Nomad infrastructure must be deployed. See [confluence](https://goddard-smith.atlassian.net/wiki/spaces/nomad/pages/24739951/Deployment+steps)

# Local development

### Using Docker-Compose

A docker-compose.yml file has been created in the root directory. This compose file;

- Builds the nomad java app using [./Dockerfile](./Dockerfile)
- Starts up a neo4j container with a health check (to ensure it has properly started)
- Runs the nomad app in a container once the neo4j container has tarted successfully.

Now you are ready to use docker-compose:

```
docker-compose up --build
```
> Note; you can omit the --build flag if you don't want to re-build the image (which includes a mvn build).

The application should now be running.

### Running the application with mvn or Intellij

You will need a Neo4j container running locally.


```
docker run --publish=7474:7474 --publish=7687:7687 -e 'NEO4J_AUTH=neo4j/mypassword' neo4j:5
```


# Accessing Neo4j terminal

Sometimes it is useful to visualize the data in neo4j. You can get a terminal to the neo4j instance using 

```
http://localhost:7474/browser/
```

The pasword will be `mypassword`
