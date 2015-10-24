# Supported Features

*feature set based on the [Docker Remote API v1.21](https://docs.docker.com/reference/api/docker_remote_api_v1.21/)*

Since the Docker remote api tends to be backwards compatible,
the underlying Docker-Client currently supports most other api versions, too.

Current api coverage (25/58): ![Remote API Coverage Status](http://progressed.io/bar/43)

This project tends to support most api endpoints, but only if there's an actual use case. If you're missing a feature, please file
a [new issue](https://github.com/gesellix/gradle-docker-plugin/issues) or a [pull request](https://github.com/gesellix/gradle-docker-plugin/pulls)
and we'll add it as soon as the private free time allows. This plugin relies on the [docker-client](https://github.com/gesellix/docker-client) while
there's a [similar Gradle Docker plugin](https://github.com/bmuschko/gradle-docker-plugin) based
on the [Java Docker API Client](https://github.com/docker-java/docker-java) available, too.

## Containers

* [x] `docker ps`: List containers
* [x] `docker create`: Create a container
* [x] `docker inspect <container>`: Inspect a container
* [ ] `docker top <container>`: List processes running inside a container
* [ ] `docker logs <container>`: Get container logs
* [ ] `docker diff <container>`: Inspect changes on a container's filesystem
* [ ] `docker export <container>`: Export a container
* [ ] `docker stats <container>`: Get container stats based on resource usage
* [ ] Resize a container TTY
* [x] `docker start <container>`: Start a container
* [x] `docker stop <container>`: Stop a container
* [x] `docker restart <container>`: Restart a container
* [x] `docker kill <container>`: Kill a container
* [x] `docker rename <container>`: Rename a container
* [x] `docker pause <container>`: Pause a container
* [x] `docker unpause <container>`: Unpause a container
* [ ] `docker attach <container>`: Attach to a container - _interactive tty via socket hijacking currently not supported_
* [ ] Attach to a container (websocket)
* [x] `docker wait <container>`: Wait a container
* [x] `docker rm <container>`: Remove a container
* [x] `docker cp <container>:<path> <hostpath>`: Copy files or folders from a container - _deprecated_
* [ ] Retrieve information about files and folders in a container
* [x] `docker cp <container>:<path> <hostpath>`: Get an archive of a filesystem resource in a container
* [x] `docker cp <hostpath> <container>:<path>`: Extract an archive of files or folders to a directory in a container

## Images

* [x] `docker images`: List Images
* [x] `docker build`: Build image from a Dockerfile
* [x] `docker pull`: Create an image (from the registry)
* [ ] `docker import`: Create an image (by import from url)
* [ ] `docker import`: Create an image (by import from stream)
* [ ] `docker inspect <image>`: Inspect an image
* [ ] `docker history <image>`: Get the history of an image
* [x] `docker push <image>`: Push an image on the registry
* [x] `docker tag <image> <repository>`: Tag an image into a repository
* [x] `docker rmi <image>`: Remove an image
* [ ] `docker search <term>`: Search images

## Misc

* [ ] Check auth configuration
* [x] `docker info`: Display system-wide information
* [x] `docker version`: Show the docker version information
* [ ] Ping the docker server
* [ ] `docker commit <container>`: Create a new image from a container's changes
* [ ] `docker events`: Monitor Docker's events
* [ ] `docker save <image>`: Get a tarball containing all images in a repository
* [ ] `docker save <image> [<image> ...]`: Get a tarball containing all images.
* [ ] `docker load`: Load a tarball with a set of images and tags into docker
* [x] Exec Create
* [x] Exec Start (`docker exec <container> <command>`) - _interactive tty via socket hijacking currently not supported_
* [ ] Exec Resize
* [ ] Exec Inspect

## Volumes

* [ ] `docker volume ls`: List volumes from all volume drivers
* [ ] `docker volume create`: Create a volume
* [ ] `docker volume inspect`: Return low-level information on a volume
* [ ] `docker volume rm`: Remove a volume

## Networks

* [ ] `docker network ls`: Lists all networks
* [ ] `docker network inspect`: Display detailed information on a network
* [ ] `docker network create`: Create a new network
* [ ] `docker network connect`: Connect a container to a network
* [ ] `docker network disconnect`: Disconnect a container from a network
* [ ] `docker network rm`: Remove a network
