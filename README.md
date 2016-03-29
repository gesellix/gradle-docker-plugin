# Gradle-Docker-Plugin

[![Build Status](https://travis-ci.org/gesellix/gradle-docker-plugin.svg)](https://travis-ci.org/gesellix/gradle-docker-plugin)
[![Latest version](https://api.bintray.com/packages/gesellix/docker-utils/gradle-docker-plugin/images/download.svg) ](https://bintray.com/gesellix/docker-utils/gradle-docker-plugin/_latestVersion)

[![Gradle logo](https://github.com/gesellix/gradle-docker-plugin/raw/master/img/gradle-logo.png)](http://www.gradle.org/)
[![Docker logo](https://github.com/gesellix/gradle-docker-plugin/raw/master/img/docker-logo.png)](http://www.docker.com/)

Yet another Gradle plugin making it easy for your build scripts to talk to a Docker daemon.
Each task delegates to the [Docker-Client](https://github.com/gesellix/docker-client), which connects
to the Docker remote API via HTTP.

[Docker for Mac](https://beta.docker.com/) users: you need to configure your `DOCKER_HOST` environment variable
 or `dockerHost` task property to be set to `unix:///var/tmp/docker.sock`.

For basic usage please have a look at the tests or the [example project](https://github.com/gesellix/gradle-docker-plugin-example).
