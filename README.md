# Gradle-Docker-Plugin

[![Publish](https://github.com/gesellix/gradle-docker-plugin/actions/workflows/cd.yml/badge.svg)](https://github.com/gesellix/gradle-docker-plugin/actions/workflows/cd.yml)
[Latest version](https://plugins.gradle.org/plugin/de.gesellix.docker)

[![Gradle logo](https://github.com/gesellix/gradle-docker-plugin/raw/master/img/gradle-logo.png)](http://www.gradle.org/)
[![Docker logo](https://github.com/gesellix/gradle-docker-plugin/raw/master/img/docker-logo.png)](http://www.docker.com/)

Yet another Gradle plugin making it easy for your build scripts to talk to a Docker daemon.
Each task delegates to the [Docker-Client](https://github.com/gesellix/docker-client), which connects
to the Docker remote API via HTTP.

For basic usage please have a look at the tests or the [example project](https://github.com/gesellix/gradle-docker-plugin-example).

## Release Workflow

There are multiple GitHub Action Workflows for the different steps in the package's lifecycle:

- CI: Builds and checks incoming changes on a pull request
  - triggered on every push to a non-default branch
- CD: Publishes the Gradle artifacts to GitHub Package Registry
  - triggered only on pushes to the default branch
- Release: Publishes Gradle artifacts to Sonatype and releases them to Maven Central
  - triggered on a published GitHub release using the underlying tag as artifact version, e.g. via `git tag -m "$MESSAGE" v$(date +"%Y-%m-%dT%H-%M-%S")`

## License

MIT License

Copyright 2015-2021 [Tobias Gesellchen](https://www.gesellix.net/) ([@gesellix](https://twitter.com/gesellix))

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
