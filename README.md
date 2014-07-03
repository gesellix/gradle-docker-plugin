# Gradle-Docker-Plugin

[![Build Status](https://travis-ci.org/gesellix-docker/gradle-docker-plugin.svg)](https://travis-ci.org/gesellix-docker/gradle-docker-plugin)
[![Coverage Status](https://coveralls.io/repos/gesellix-docker/gradle-docker-plugin/badge.png)](https://coveralls.io/r/gesellix-docker/gradle-docker-plugin)
[![Latest version](https://api.bintray.com/packages/gesellix/docker-utils/gradle-docker-plugin/images/download.png) ](https://bintray.com/gesellix/docker-utils/gradle-docker-plugin/_latestVersion)

[![Gradle logo](img/gradle-logo.png)](http://www.gradle.org/)
[![Docker logo](img/docker-logo.png)](http://www.docker.com/)

Yet another Gradle plugin making it easy for your build scripts to talk to a Docker daemon.
Each task delegates to the [Docker-Client](https://github.com/gesellix-docker/docker-client), which connects
to the Docker remote API via HTTP.

For basic usage please have a look at the tests or the [example project](https://github.com/gesellix-docker/gradle-docker-plugin-example).
