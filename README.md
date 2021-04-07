[![Build Status](https://img.shields.io/github/workflow/status/gesellix/gradle-docker-plugin/Publish?style=for-the-badge)](https://github.com/gesellix/gradle-docker-plugin/actions)
[![Maven Central](https://img.shields.io/maven-central/v/de.gesellix/gradle-docker-plugin.svg?style=for-the-badge&maxAge=86400)](https://search.maven.org/search?q=g:de.gesellix%20AND%20a:gradle-docker-plugin)
[![API Coverage](https://img.shields.io/static/v1?label=Gradle%20Plugin%20Portal&message=latest%20version&color=blue&style=for-the-badge)](https://plugins.gradle.org/plugin/de.gesellix.docker)

# Gradle-Docker-Plugin

[![Gradle logo](https://github.com/gesellix/gradle-docker-plugin/raw/master/img/gradle-logo.png)](http://www.gradle.org/)
[![Docker logo](https://github.com/gesellix/gradle-docker-plugin/raw/master/img/docker-logo.png)](http://www.docker.com/)

Yet another Gradle plugin making it easy for your build scripts to talk to a Docker daemon.
Each task delegates to the [Docker-Client](https://github.com/gesellix/docker-client), which connects
to the Docker remote API via HTTP.

For basic usage please have a look at the tests or the [example project](https://github.com/gesellix/gradle-docker-plugin-example).

## Publishing/Release Workflow

See RELEASE.md

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
