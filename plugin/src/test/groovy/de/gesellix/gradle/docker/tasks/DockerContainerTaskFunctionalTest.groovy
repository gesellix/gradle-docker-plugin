package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.gradle.docker.PortFinder
import de.gesellix.gradle.docker.engine.DockerEngineHttpHandler
import de.gesellix.gradle.docker.engine.ExpectedRequestWithResponse
import de.gesellix.gradle.docker.engine.HttpTestServer
import groovy.json.JsonOutput
import org.gradle.api.GradleException
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class DockerContainerTaskFunctionalTest extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile

    DockerClient dockerClient

    HttpTestServer testServer
    InetSocketAddress testServerAddress
    DockerEngineHttpHandler dockerEngineHttpHandler

    // Also requires './gradlew :plugin:pluginUnderTestMetadata' to be run before performing the tests.
    def setup() {
        testServer = new HttpTestServer()
        dockerEngineHttpHandler = new DockerEngineHttpHandler()
        testServerAddress = testServer.start("/", dockerEngineHttpHandler)

        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'de.gesellix.docker'
            }
            
            docker {
                dockerHost = 'tcp://localhost:${testServerAddress.port}'
            }
        """
        dockerEngineHttpHandler.expectedRequests = []
    }

    def cleanup() {
        testServer?.stop()
    }

    def "fails when container name is missing"() {
        given:
        buildFile << """
          task dockerContainer(type: de.gesellix.gradle.docker.tasks.DockerContainerTask) {
              targetState = "started"
              image = "testImage:latest"
          }
        """

        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('dockerContainer')
                .withPluginClasspath()
                .build()

        then:
        thrown(Exception)
    }

    def "start new non-existing container"() {
        given:
        buildFile << """
          task dockerContainer(type: de.gesellix.gradle.docker.tasks.DockerContainerTask) {
              targetState = "started"
              image = "testImage:latest"
              containerName = "example"
              doLast {
                  logger.lifecycle("Done.")
              }
          }
        """
        dockerEngineHttpHandler.expectedRequests = [
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([])
                ),
                new ExpectedRequestWithResponse(
                        request: "POST /containers/create?name=example",
                        response: JsonOutput.toJson([status: [success: true], content: [id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: false]])
                ),
                new ExpectedRequestWithResponse(
                        request: "POST /containers/123/start",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: true]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: true]])
                ),
        ]

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('dockerContainer')
                .withPluginClasspath()
                .build()

        then:
        result.task(':dockerContainer').outcome != TaskOutcome.UP_TO_DATE
        result.output.contains("Done.")
        dockerEngineHttpHandler.expectedRequests.empty
    }

    def "start already created container that is not running"() {
        given:
        buildFile << """
          task dockerContainer(type: de.gesellix.gradle.docker.tasks.DockerContainerTask) {
              targetState = "started"
              image = "testImage:latest"
              containerName = "example"
              doLast {
                  logger.lifecycle("Done.")
              }
          }
        """
        dockerEngineHttpHandler.expectedRequests = [
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: false]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: false]])
                ),
                new ExpectedRequestWithResponse(
                        request: "POST /containers/123/start",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: false]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: true]])
                ),
        ]

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('dockerContainer')
                .withPluginClasspath()
                .build()

        then:
        result.task(':dockerContainer').outcome != TaskOutcome.UP_TO_DATE
        result.output.contains("Done.")
        dockerEngineHttpHandler.expectedRequests.empty
    }

    def "start already running container"() {
        given:
        buildFile << """
          task dockerContainer(type: de.gesellix.gradle.docker.tasks.DockerContainerTask) {
              targetState = "started"
              image = "testImage:latest"
              containerName = "example"
              doLast {
                  logger.lifecycle("Done.")
              }
          }
        """
        dockerEngineHttpHandler.expectedRequests = [
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: true]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: true]])
                ),
        ]

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('dockerContainer')
                .withPluginClasspath()
                .build()

        then:
        result.task(':dockerContainer').outcome != TaskOutcome.UP_TO_DATE
        result.output.contains("Done.")
        dockerEngineHttpHandler.expectedRequests.empty
    }

    def "stop running container"() {
        given:
        buildFile << """
          task dockerContainer(type: de.gesellix.gradle.docker.tasks.DockerContainerTask) {
              targetState = "stopped"
              image = "testImage:latest"
              containerName = "example"
              doLast {
                  logger.lifecycle("Done.")
              }
          }
        """
        dockerEngineHttpHandler.expectedRequests = [
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: true]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: true]])
                ),
                new ExpectedRequestWithResponse(
                        request: "POST /containers/123/stop?t=10",
                        response: JsonOutput.toJson([:])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: false]])
                ),
        ]

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('dockerContainer')
                .withPluginClasspath()
                .build()

        then:
        result.task(':dockerContainer').outcome != TaskOutcome.UP_TO_DATE
        result.output.contains("Done.")
        dockerEngineHttpHandler.expectedRequests.empty
    }

    def "stop already stopped container"() {
        given:
        buildFile << """
          task dockerContainer(type: de.gesellix.gradle.docker.tasks.DockerContainerTask) {
              targetState = "stopped"
              image = "testImage:latest"
              containerName = "example"
              doLast {
                  logger.lifecycle("Done.")
              }
          }
        """
        dockerEngineHttpHandler.expectedRequests = [
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: false]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: false]])
                ),
        ]

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('dockerContainer')
                .withPluginClasspath()
                .build()

        then:
        result.task(':dockerContainer').outcome == TaskOutcome.SUCCESS
//        result.task(':dockerContainer').outcome == TaskOutcome.UP_TO_DATE
        result.output.contains("Done.")
        dockerEngineHttpHandler.expectedRequests.empty
    }

    def "remove running container"() {
        given:
        buildFile << """
          task dockerContainer(type: de.gesellix.gradle.docker.tasks.DockerContainerTask) {
              targetState = "absent"
              image = "testImage:latest"
              containerName = "example"
              doLast {
                  logger.lifecycle("Done.")
              }
          }
        """
        dockerEngineHttpHandler.expectedRequests = [
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: true]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: true]])
                ),
                new ExpectedRequestWithResponse(
                        request: "POST /containers/123/stop?t=10",
                        response: JsonOutput.toJson([:])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: false]])
                ),
                new ExpectedRequestWithResponse(
                        request: "DELETE /containers/123",
                        response: JsonOutput.toJson([:])
                ),
        ]

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('dockerContainer')
                .withPluginClasspath()
                .build()

        then:
        result.task(':dockerContainer').outcome != TaskOutcome.UP_TO_DATE
        result.output.contains("Done.")
        dockerEngineHttpHandler.expectedRequests.empty
    }

    def "remove stopped container"() {
        given:
        buildFile << """
          task dockerContainer(type: de.gesellix.gradle.docker.tasks.DockerContainerTask) {
              targetState = "absent"
              image = "testImage:latest"
              containerName = "example"
              doLast {
                  logger.lifecycle("Done.")
              }
          }
        """
        dockerEngineHttpHandler.expectedRequests = [
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: false]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: false]])
                ),
                new ExpectedRequestWithResponse(
                        request: "DELETE /containers/123",
                        response: JsonOutput.toJson([:])
                ),
        ]

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('dockerContainer')
                .withPluginClasspath()
                .build()

        then:
        result.task(':dockerContainer').outcome != TaskOutcome.UP_TO_DATE
        result.output.contains("Done.")
        dockerEngineHttpHandler.expectedRequests.empty
    }

    def "remove non-existing container"() {
        given:
        buildFile << """
          task dockerContainer(type: de.gesellix.gradle.docker.tasks.DockerContainerTask) {
              targetState = "absent"
              image = "testImage:latest"
              containerName = "example"
              doLast {
                  logger.lifecycle("Done.")
              }
          }
        """
        dockerEngineHttpHandler.expectedRequests = [
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([])
                ),
        ]

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('dockerContainer')
                .withPluginClasspath()
                .build()

        then:
        result.task(':dockerContainer').outcome == TaskOutcome.SUCCESS
//        result.task(':dockerContainer').outcome == TaskOutcome.UP_TO_DATE
        result.output.contains("Done.")
        dockerEngineHttpHandler.expectedRequests.empty
    }

    def "reload new non-existing container"() {
        given:
        buildFile << """
          task dockerContainer(type: de.gesellix.gradle.docker.tasks.DockerContainerTask) {
              targetState = "reloaded"
              image = "testImage:latest"
              containerName = "example"
              doLast {
                  logger.lifecycle("Done.")
              }
          }
        """
        dockerEngineHttpHandler.expectedRequests = [
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([])
                ),
                new ExpectedRequestWithResponse(
                        request: "POST /containers/create?name=example",
                        response: JsonOutput.toJson([id: "123"])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: false]])
                ),
                new ExpectedRequestWithResponse(
                        request: "POST /containers/123/start",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: true]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: true]])
                ),
        ]

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('dockerContainer')
                .withPluginClasspath()
                .build()

        then:
        result.task(':dockerContainer').outcome != TaskOutcome.UP_TO_DATE
        result.output.contains("Done.")
        dockerEngineHttpHandler.expectedRequests.empty
    }

    def "reload non-running container"() {
        given:
        buildFile << """
          task dockerContainer(type: de.gesellix.gradle.docker.tasks.DockerContainerTask) {
              targetState = "reloaded"
              image = "testImage:latest"
              containerName = "example"
              doLast {
                  logger.lifecycle("Done.")
              }
          }
        """
        dockerEngineHttpHandler.expectedRequests = [
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: false]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: false]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: false]])
                ),
                new ExpectedRequestWithResponse(
                        request: "DELETE /containers/123",
                        response: JsonOutput.toJson([:])
                ),
                new ExpectedRequestWithResponse(
                        request: "POST /containers/create?name=example",
                        response: JsonOutput.toJson([id: "234"])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "234"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/234/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: false]])
                ),
                new ExpectedRequestWithResponse(
                        request: "POST /containers/234/start",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: true]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/234/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: true]])
                ),
        ]

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('dockerContainer')
                .withPluginClasspath()
                .build()

        then:
        result.task(':dockerContainer').outcome != TaskOutcome.UP_TO_DATE
        result.output.contains("Done.")
        dockerEngineHttpHandler.expectedRequests.empty
    }

    def "reload running container with different image"() {
        given:
        buildFile << """
          task dockerContainer(type: de.gesellix.gradle.docker.tasks.DockerContainerTask) {
              targetState = "reloaded"
              image = "testImage:latest"
              containerName = "example"
              doLast {
                  logger.lifecycle("Done.")
              }
          }
        """
        dockerEngineHttpHandler.expectedRequests = [
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "image1", State: [Running: true]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /images/testImage:latest/json",
                        response: JsonOutput.toJson([Id: "image0"])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "image1", State: [Running: true]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /images/testImage:latest/json",
                        response: JsonOutput.toJson([Id: "image0"])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson([Image: "image1", State: [Running: true]])
                ),
                new ExpectedRequestWithResponse(
                        request: "POST /containers/123/stop",
                        response: JsonOutput.toJson([:])
                ),
                new ExpectedRequestWithResponse(
                        request: "DELETE /containers/123",
                        response: JsonOutput.toJson([:])
                ),
                new ExpectedRequestWithResponse(
                        request: "POST /containers/create?name=example",
                        response: JsonOutput.toJson([id: "234"])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "234"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/234/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: false]])
                ),
                new ExpectedRequestWithResponse(
                        request: "POST /containers/234/start",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: true]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/234/json",
                        response: JsonOutput.toJson([Image: "testImage", State: [Running: true]])
                ),
        ]

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('dockerContainer')
                .withPluginClasspath()
                .build()

        then:
        result.task(':dockerContainer').outcome != TaskOutcome.UP_TO_DATE
        result.output.contains("Done.")
        dockerEngineHttpHandler.expectedRequests.empty
    }

    def "reload running container with everything same"() {
        given:
        buildFile << """
          task dockerContainer(type: de.gesellix.gradle.docker.tasks.DockerContainerTask) {
              targetState = "reloaded"
              image = "testImage:latest"
              containerName = "example"
              ports = ["80:8080"]
              env = ["TMP=1"]
              cmd = ["jar", "myjarfile.jar"]
              links = ["mycontainer:myalias"]
              volumes = [
                  "/mnt/data:/data",
                  "/mnt/readonly:/input:ro"
              ]
              extraHosts = ["dockerhost:127.0.0.1"]
              doLast {
                  logger.lifecycle("Done.")
              }
          }
        """
        def containerConfig = [
                Image     : "image1",
                State     : [
                        Running: true
                ],
                Config    : [
                        ExposedPorts: ["8080/tcp": []],
                        Volumes     : [
                                "/data" : [],
                                "/spec" : [],
                                "/input": []
                        ],
                        Env         : ["TMP=1", "MYVAR=myval"],
                        Cmd         : ["java", "jar", "myjarfile.jar"]
                ],
                HostConfig: [
                        Binds       : [
                                "/mnt/data:/data",
                                "/mnt/readonly:/input:ro"
                        ],
                        Links       : ["mycontainer:myalias"],
                        ExtraHosts  : ["dockerhost:127.0.0.1"],
                        Privileged  : false,
                        PortBindings: ["8080/tcp": [
                                [
                                        HostIp  : "0.0.0.0",
                                        HostPort: "80"
                                ]
                        ]]
                ]
        ]
        def imageConfig = [
                Id             : "image1",
                ContainerConfig: [
                        ExposedPorts: [],
                        Volumes     : ["/spec": []],
                        Env         : ["MYVAR=myval"]
                ],
                Config         : [
                        Entrypoint: "java"
                ]
        ]
        dockerEngineHttpHandler.expectedRequests = [
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson(containerConfig)
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /images/testImage:latest/json",
                        response: JsonOutput.toJson(imageConfig)
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/json?filters=%7B%22name%22%3A%5B%22example%22%5D%7D&all=true&size=false",
                        response: JsonOutput.toJson([[Names: ["/example"], Id: "123"]])
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /containers/123/json",
                        response: JsonOutput.toJson(containerConfig)
                ),
                new ExpectedRequestWithResponse(
                        request: "GET /images/testImage:latest/json",
                        response: JsonOutput.toJson(imageConfig)
                ),
        ]

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('dockerContainer')
                .withPluginClasspath()
                .build()

        then:
        result.task(':dockerContainer').outcome == TaskOutcome.SUCCESS
//        result.task(':dockerContainer').outcome == TaskOutcome.UP_TO_DATE
        result.output.contains("Done.")
        dockerEngineHttpHandler.expectedRequests.empty
    }

    def project
    def task

    def "dockerHost from tcp://127.0.0.1"() {
        expect:
        new URI("tcp://127.0.0.1").getHost() == "127.0.0.1"
    }

    def "dockerHost from tcp://127.0.0.1:999"() {
        expect:
        new URI("tcp://127.0.0.1:999").getHost() == "127.0.0.1"
    }

    // Health check tests

    def "healthcheck on already running container - port which is not exposed"() {
        when:
        task.dockerHost = "tcp://127.0.0.1:999"
        task.targetState = "started"
        task.image = "testImage:latest"
        task.containerName = "example"
        task.healthChecks = [[
                                     containerPort: 8080
                             ]]
        task.execute()

        then:
        1 * dockerClient.ps([filters: [name: ["example"]]]) >> [
                content: [[Names: ["/example"], Id: "123"]]
        ]
        2 * dockerClient.inspectContainer("123") >> [
                content: [
                        Image          : task.image,
                        State          : [Running: true],
                        HostConfig     : [
                                PortBindings: []
                        ],
                        NetworkSettings: [
                                Ports: []
                        ]
                ]
        ]

        and:
        task.changed == false

        and:
        def e = thrown(GradleException)
        e.cause.class == IllegalArgumentException
        e.cause.message.endsWith("is not bound to host.")
    }

    def "healthcheck on already running container - timeout"() {
        given:
        int port = PortFinder.findFreePort()

        when:
        task.dockerHost = "tcp://127.0.0.1:999"
        task.targetState = "started"
        task.image = "testImage:latest"
        task.containerName = "example"
        task.healthChecks = [
                [
                        containerPort: 8080,
                        timeout      : 1,
                        retries      : 1
                ]]
        task.execute()

        then:
        1 * dockerClient.ps([filters: [name: ["example"]]]) >>> [
                [content: [[Names: ["/example"], Id: "123"]]]
        ]
        2 * dockerClient.inspectContainer("123") >> [
                content: [
                        Image          : task.image,
                        State          : [Running: true],
                        HostConfig     : [
                                PortBindings: ["8080/tcp": [
                                        [
                                                HostIp  : "0.0.0.0",
                                                HostPort: port.toString()
                                        ]
                                ]]
                        ],
                        NetworkSettings: [
                                Ports: ["8080/tcp": [
                                        [
                                                HostIp  : "0.0.0.0",
                                                HostPort: port.toString()
                                        ]
                                ]]
                        ]
                ]
        ]

        and:
        task.changed == false

        and:
        def e = thrown(GradleException)
        e.cause.class == IllegalStateException
        e.cause.message == "HealthCheck: Container not healthy."
    }

    def "healthcheck on already running container"() {
        given:
        Thread server = new Thread() {

            boolean initialized = false
            int serverPort = 0
            boolean stopped = false

            @Override
            void run() {
                ServerSocket ss
                synchronized (this) {
                    serverPort = PortFinder.findFreePort()
                    ss = new ServerSocket(serverPort)
                    initialized = true
                    notify()
                }
                while (!stopped) {
                    ss.accept()
                }
            }
        }

        def port
        server.start()
        synchronized (server) {
            if (!server.initialized) {
                server.wait()
            }
            port = server.serverPort
        }

        when:
        task.dockerHost = "tcp://127.0.0.1:999"
        task.targetState = "started"
        task.image = "testImage:latest"
        task.containerName = "example"
        task.healthChecks = [
                [
                        containerPort: 8080,
                        timeout      : 100,
                        retries      : 1
                ]]
        task.execute()

        then:
        2 * dockerClient.ps([filters: [name: ["example"]]]) >> [
                content: [[Names: ["/example"], Id: "123"]]
        ]
        4 * dockerClient.inspectContainer("123") >> [
                content: [
                        Image          : task.image,
                        State          : [Running: true],
                        HostConfig     : [
                                PortBindings: ["8080/tcp": [
                                        [
                                                HostIp  : "0.0.0.0",
                                                HostPort: port.toString()
                                        ]
                                ]]
                        ],
                        NetworkSettings: [
                                Ports: ["8080/tcp": [
                                        [
                                                HostIp  : "0.0.0.0",
                                                HostPort: port.toString()
                                        ]
                                ]]
                        ]
                ]
        ]

        and:
        task.changed == false

        cleanup:
        server.stopped = true
        server.stop()
    }
}
