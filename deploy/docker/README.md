# OpenFlower docker image

Included Dockerfile can be used to build an **all-in-one** image with all required services installed and running within one container, or separate images for frontend and backend services.

For examples on running the all-in-one image or the multi image deployment see **deploy/docker/docker-compose.yaml** and **deploy/docker/docker-compose-multi.yaml**


## all-in-one image

This image contains all services needed to run OpenFlower platform in one container.

### Building the image

This is the default target and can be built by running following command from project root:

```
DOCKER_BUILDKIT=1 docker build -f deploy/docker/Dockerfile -t flowerappengorg/openflower .
```

### Configuration

Image can be configured by setting environment variables.

| Environment variable                | Description                                                             | Default-Value                                                 |
|-------------------------------------| ----------------------------------------------------------------------- | ----------------------------------------------------- |
| `LOWCODER_REDIS_ENABLED`            | If **true** redis server is started in the container                    | `true`                                                |
| `LOWCODER_MONGODB_ENABLED`          | If **true** mongo database is started in the container                  | `true`                                                |
| `LOWCODER_MONGODB_EXPOSED`          | If **true** mongo database accept connections from outside the docker   | `false`                                               |
| `LOWCODER_API_SERVICE_ENABLED`      | If **true** lowcoder api-service is started in the container            | `true`                                                |
| `LOWCODER_NODE_SERVICE_ENABLED`     | If **true** lowcoder node-service is started in the container           | `true`                                                |
| `LOWCODER_FRONTEND_ENABLED`         | If **true** lowcoder web frontend is started in the container           | `true`                                                |
| `LOWCODER_PUID`                     | ID of user running services. It will own all created logs and data.     | `9001`                                                |
| `LOWCODER_PGID`                     | ID of group of the user running services.                               | `9001`                                                |
| `LOWCODER_MONGODB_URL`              | Mongo database connection string                                        | `mongodb://localhost:27017/lowcoder?authSource=admin` |
| `LOWCODER_REDIS_URL`                | Redis server URL                                                        | `redis://localhost:6379`                              |
| `LOWCODER_DB_ENCRYPTION_PASSWORD`   | Encryption password                                                     | `lowcoder.org`                                        |
| `LOWCODER_DB_ENCRYPTION_SALT`       | Salt used for encrypting password                                       | `lowcoder.org`                                        |
| `LOWCODER_CORS_DOMAINS`             | CORS allowed domains                                                    | `*`                                                   |
| `LOWCODER_PUBLIC_URL`               | The URL of the public User Interface                                    | `localhost:3000`                                      |
| `LOWCODER_MAX_REQUEST_SIZE`         | Lowcoder max request size                                               | `20m`                                                 |
| `LOWCODER_MAX_QUERY_TIMEOUT`        | Lowcoder max query timeout (in seconds)                                 | `120`                                                 |
| `LOWCODER_DEFAULT_QUERY_TIMEOUT`    | Lowcoder default query timeout (in seconds)                             | `10`                                                  |
| `LOWCODER_API_RATE_LIMIT`           | Number of max Request per Second                                        | `100`                                                 |
| `LOWCODER_API_SERVICE_URL`          | OpenFlower API service URL                                                | `http://localhost:8080`                               |
| `LOWCODER_NODE_SERVICE_URL`         | OpenFlower Node service (js executor) URL                                 | `http://localhost:6060`                               |
| `LOWCODER_MAX_ORGS_PER_USER`        | Default maximum organizations per user                                  | `100`                                                 |
| `LOWCODER_MAX_MEMBERS_PER_ORG`      | Default maximum members per organization                                | `1000`                                                |
| `LOWCODER_MAX_GROUPS_PER_ORG`       | Default maximum groups per organization                                 | `100`                                                 |
| `LOWCODER_MAX_APPS_PER_ORG`         | Default maximum applications per organization                           | `1000`                                                |
| `LOWCODER_MAX_DEVELOPERS`           | Default maximum developers                                              | `100`                                                 |
| `LOWCODER_WORKSPACE_MODE`           | SAAS to activate, ENTERPRISE to switch off - Workspaces                 | `SAAS`                                                |
| `LOWCODER_EMAIL_SIGNUP_ENABLED`     | Control if users create their own Workspace automatic when Sign Up      | `true`                                                |
| `LOWCODER_CREATE_WORKSPACE_ON_SIGNUP` | IF LOWCODER_WORKSPACE_MODE = SAAS, controls if a own workspace is created for the user after sign up   | `true`               |
| `LOWCODER_MARKETPLACE_PRIVATE_MODE` | Control if not to show Apps on the local Marketplace to anonymous users | `true`                                                |
| `LOWCODER_SUPERUSER_USERNAME`       | Username of the Super-User of an Lowcoder Installation | `admin@localhost`                                                      |
| `LOWCODER_SUPERUSER_PASSWORD`       | Password of the Super-User, if not present or empty, it will be generated | `generated and printed into log file                |



Also you should set the API-KEY secret, whcih should be a string of at least 32 random characters. (from Lowcoder v2.3.x on)
On linux/mac, generate one eg. with: head /dev/urandom | head -c 30 | shasum -a 256

| Environment variable                | Description                                                             | Default-Value                                         |
|-------------------------------------| ----------------------------------------------------------------------- | ----------------------------------------------------- |
| `LOWCODER_API_KEY_SECRET`           | String to encrypt/sign API Keys that users may create                   |                                                       |


To enable secure Password Reset flow for the users, you need to configure your own SMTP Server. You can do this with the following Variables (from Lowcoder v2.4.x on):

| Environment Variable                      | Description                                             | Default Value        |
|-------------------------------------------|---------------------------------------------------------|----------------------|
| `LOWCODER_ADMIN_SMTP_HOST`                | SMTP Hostname of your Mail Relay Server                 |                      |
| `LOWCODER_ADMIN_SMTP_PORT`                | Port number for the SMTP service                        | `587`                |
| `LOWCODER_ADMIN_SMTP_USERNAME`            | Username for SMTP authentication                        |                      |
| `LOWCODER_ADMIN_SMTP_PASSWORD`            | Password for SMTP authentication                        |                      |
| `LOWCODER_ADMIN_SMTP_AUTH`                | Enable SMTP authentication                              | `true`               |
| `LOWCODER_ADMIN_SMTP_SSL_ENABLED`         | Enable SSL encryption                                   | `false`              |
| `LOWCODER_ADMIN_SMTP_STARTTLS_ENABLED`    | Enable STARTTLS encryption                              | `true`               |
| `LOWCODER_ADMIN_SMTP_STARTTLS_REQUIRED`   | Require STARTTLS encryption                             | `true`               |
| `LOWCODER_LOST_PASSWORD_EMAIL_SENDER`     | "from" Email address of the password Reset Email Sender | `service@yourhost.com` |


## Building api-service image

Standalone OpenFlower api-service image.

### Building the image

From project root run:

```
DOCKER_BUILDKIT=1 docker build -f deploy/docker/Dockerfile -t flowerappengorg/openflower-api-service --target openflower-api-service .
```

### Configuration

Image can be configured by setting environment variables.

| Environment variable            | Description                                                         | Default-Value                                                 |
| --------------------------------| --------------------------------------------------------------------| ------------------------------------------------------|
| `LOWCODER_PUID`                 | ID of user running services. It will own all created logs and data. | `9001`                                                |
| `LOWCODER_PGID`                 | ID of group of the user running services.                           | `9001`                                                |
| `LOWCODER_MONGODB_URL`          | Mongo database connection string                                    | `mongodb://localhost:27017/lowcoder?authSource=admin` |
| `LOWCODER_REDIS_URL`            | Redis server URL                                                    | `redis://localhost:6379`                              |
| `LOWCODER_DB_ENCRYPTION_PASSWORD`           | Encryption password                                     | `lowcoder.org`                                        |
| `LOWCODER_DB_ENCRYPTION_SALT`               | Salt used for encrypting password                       | `lowcoder.org`                                        |
| `LOWCODER_CORS_DOMAINS`         | CORS allowed domains                                                | `*`                                                   |
| `LOWCODER_PUBLIC_URL`           | The URL of the public User Interface                                | `localhost:3000`                                      |
| `LOWCODER_MAX_ORGS_PER_USER`    | Default maximum organizations per user                              | `100`                                                 |
| `LOWCODER_MAX_MEMBERS_PER_ORG`  | Default maximum members per organization                            | `1000`                                                |
| `LOWCODER_MAX_GROUPS_PER_ORG`   | Default maximum groups per organization                             | `100`                                                 |
| `LOWCODER_MAX_APPS_PER_ORG`     | Default maximum applications per organization                       | `1000`                                                |
| `LOWCODER_MAX_DEVELOPERS`       | Default maximum developers                                          | `100`                                                 |
| `LOWCODER_MAX_REQUEST_SIZE`     | Lowcoder max request size                                           | `20m`                                                 |
| `LOWCODER_MAX_QUERY_TIMEOUT`    | Lowcoder max query timeout (in seconds)                             | `120`                                                 |
| `LOWCODER_DEFAULT_QUERY_TIMEOUT`| Lowcoder default query timeout (in seconds)                         | `10`                                                  |
| `LOWCODER_WORKSPACE_MODE`       | SAAS to activate, ENTERPRISE to switch off - Workspaces             | `SAAS`                                                |
| `LOWCODER_EMAIL_SIGNUP_ENABLED` | Control is users can create their own Workspace when Sign Up        | `true`                                                |
| `LOWCODER_CREATE_WORKSPACE_ON_SIGNUP` | IF LOWCODER_WORKSPACE_MODE = SAAS, controls if a own workspace is created for the user after sign up   | `true`               |
| `LOWCODER_MARKETPLACE_PRIVATE_MODE` | Control if not to show Apps on the local Marketplace to anonymous users | `true`                                                |
| `LOWCODER_SUPERUSER_USERNAME` | Username of the Super-User of an Lowcoder Installation | `admin@localhost`                                                    |
| `LOWCODER_SUPERUSER_PASSWORD` | Password of the Super-User, if not present or empty, it will be generated | `generated and printed into log file              |


Also you should set the API-KEY secret, whcih should be a string of at least 32 random characters. (from Lowcoder v2.3.x on)
On linux/mac, generate one eg. with: head /dev/urandom | head -c 30 | shasum -a 256

| Environment variable                | Description                                                             | Default-Value                                                 |
|-------------------------------------| ----------------------------------------------------------------------- | ----------------------------------------------------- |
| `LOWCODER_API_KEY_SECRET`           | String to encrypt/sign API Keys that users may create                   |                                                       |


To enable secure Password Reset flow for the users, you need to configure your own SMTP Server. You can do this with the following Variables (from Lowcoder v2.4.x on):

| Environment Variable                      | Description                                             | Default Value        |
|-------------------------------------------|---------------------------------------------------------|----------------------|
| `LOWCODER_ADMIN_SMTP_HOST`                | SMTP Hostname of your Mail Relay Server                 |                      |
| `LOWCODER_ADMIN_SMTP_PORT`                | Port number for the SMTP service                        | `587`                |
| `LOWCODER_ADMIN_SMTP_USERNAME`            | Username for SMTP authentication                        |                      |
| `LOWCODER_ADMIN_SMTP_PASSWORD`            | Password for SMTP authentication                        |                      |
| `LOWCODER_ADMIN_SMTP_AUTH`                | Enable SMTP authentication                              | `true`               |
| `LOWCODER_ADMIN_SMTP_SSL_ENABLED`         | Enable SSL encryption                                   | `false`              |
| `LOWCODER_ADMIN_SMTP_STARTTLS_ENABLED`    | Enable STARTTLS encryption                              | `true`               |
| `LOWCODER_ADMIN_SMTP_STARTTLS_REQUIRED`   | Require STARTTLS encryption                             | `true`               |
| `LOWCODER_LOST_PASSWORD_EMAIL_SENDER`     | "from" Email address of the password Reset Email Sender | `service@yourhost.com` |

## Building node-service image

Standalone OpenFlower node-service (JS executor) image.

### Building the image

From project root run:

```
DOCKER_BUILDKIT=1 docker build -f deploy/docker/Dockerfile -t flowerappengorg/openflower-node-service --target openflower-node-service .
```

### Configuration

Image can be configured by setting environment variables.

| Environment variable            | Description                                                         | Default-Value                                                   |
| --------------------------------| --------------------------------------------------------------------| ------------------------------------------------------- |
| `LOWCODER_PUID`                 | ID of user running services. It will own all created logs and data. | `9001`                                                  |
| `LOWCODER_PGID`                 | ID of group of the user running services.                           | `9001`                                                  |
| `LOWCODER_API_SERVICE_URL`      | OpenFlower API service URL                                            | `http://localhost:8080`                                 |

## Building web frontend image

Standalone OpenFlower web frontend image.

### Building the image

From project root run:

```
DOCKER_BUILDKIT=1 docker build -f deploy/docker/Dockerfile -t flowerappengorg/openflower-frontend --target openflower-frontend .
```
### Configuration

Image can be configured by setting environment variables.

| Environment variable            | Description                                                         | Default-Value                                                   |
| --------------------------------| --------------------------------------------------------------------| ------------------------------------------------------- |
| `LOWCODER_PUID`                 | ID of user running services. It will own all created logs and data. | `9001`                                                  |
| `LOWCODER_PGID`                 | ID of group of the user running services.                           | `9001`                                                  |
| `LOWCODER_MAX_QUERY_TIMEOUT`    | OpenFlower max query timeout (in seconds)                             | `120`                                                 |
| `LOWCODER_MAX_REQUEST_SIZE`     | OpenFlower max request size                                           | `20m`                                                   |
| `LOWCODER_API_SERVICE_URL`      | OpenFlower API service URL                                            | `http://localhost:8080`                                 |
| `LOWCODER_NODE_SERVICE_URL`     | OpenFlower Node service (js executor) URL                             | `http://localhost:6060`                                 |


