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
        when:
        task.targetState = "started"
        task.image = "testImage:latest"
        task.containerName = "example"
        def upToDate = task.checkIfUpToDate()
        task.execute()

        then:
        3 * dockerClient.ps([filters: [name: ["example"]]]) >> [
                content: [[Names: ["/example"], Id: "123"]]
        ]
        3 * dockerClient.inspectContainer("123") >> [
                content: [Image: task.image, State: [Running: true]]
        ]

        and:
        upToDate == true
        task.changed == false
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

    def "stop running container"() {
        when:
        task.targetState = "stopped"
        task.image = "testImage:latest"
        task.containerName = "example"
        def upToDate = task.checkIfUpToDate()
        task.execute()

        then:
        3 * dockerClient.ps([filters: [name: ["example"]]]) >> [
                content: [[Names: ["/example"], Id: "123"]]
        ]
        4 * dockerClient.inspectContainer("123") >>> [
                [content: [Image: task.image, State: [Running: true]]],
                [content: [Image: task.image, State: [Running: true]]],
                [content: [Image: task.image, State: [Running: true]]],
                [content: [Image: task.image, State: [Running: false]]],
        ]
        1 * dockerClient.stop("123") >> [
                status: [success: true]
        ]

        and:
        upToDate == false
        task.changed == true
    }

    def "stop already stopped container"() {
        when:
        task.targetState = "stopped"
        task.image = "testImage:latest"
        task.containerName = "example"
        def upToDate = task.checkIfUpToDate()
        task.execute()

        then:
        3 * dockerClient.ps([filters: [name: ["example"]]]) >>> [
                [content: [[Names: ["/example"], Id: "123"]]]
        ]
        3 * dockerClient.inspectContainer("123") >> [
                content: [Image: task.image, State: [Running: false]]
        ]

        and:
        upToDate == true
        task.changed == false
    }

    def "remove running container"() {
        when:
        task.targetState = "absent"
        task.image = "testImage:latest"
        task.containerName = "example"
        def upToDate = task.checkIfUpToDate()
        task.execute()

        then:
        3 * dockerClient.ps([filters: [name: ["example"]]]) >> [
                content: [[Names: ["/example"], Id: "123"]]
        ]
        4 * dockerClient.inspectContainer("123") >>> [
                [content: [Image: task.image, State: [Running: true]]],
                [content: [Image: task.image, State: [Running: true]]],
                [content: [Image: task.image, State: [Running: true]]],
                [content: [Image: task.image, State: [Running: false]]],
        ]
        1 * dockerClient.stop("123") >> [
                status: [success: true]
        ]
        1 * dockerClient.rm("123") >> [
                status: [success: true]
        ]

        and:
        upToDate == false
        task.changed == true
    }

    def "remove stopped container"() {
        when:
        task.targetState = "absent"
        task.image = "testImage:latest"
        task.containerName = "example"
        def upToDate = task.checkIfUpToDate()
        task.execute()

        then:
        3 * dockerClient.ps([filters: [name: ["example"]]]) >> [
                content: [[Names: ["/example"], Id: "123"]]
        ]
        3 * dockerClient.inspectContainer("123") >> [
                content: [Image: task.image, State: [Running: false]]
        ]
        1 * dockerClient.rm("123") >> [
                status: [success: true]
        ]

        and:
        upToDate == false
        task.changed == true
    }

    def "remove non-existing container"() {
        when:
        task.targetState = "absent"
        task.image = "testImage:latest"
        task.containerName = "example"
        def upToDate = task.checkIfUpToDate()
        task.execute()

        then:
        3 * dockerClient.ps([filters: [name: ["example"]]]) >> [
                content: []
        ]

        and:
        upToDate == true
        task.changed == false
    }

    def "reload new non-existing container"() {
        when:
        task.targetState = "reloaded"
        task.image = "testImage:latest"
        task.containerName = "example"
        def upToDate = task.checkIfUpToDate()
        task.execute()

        then:
        5 * dockerClient.ps([filters: [name: ["example"]]]) >>> [
                [content: []],
                [content: []],
                [content: []],
                [content: []],
                [content: [[Names: ["/example"], Id: "123"]]]
        ]
        1 * dockerClient.createContainer(_, [name: "example"]) >> [
                status : [success: true],
                content: [id: "123"]
        ]
        1 * dockerClient.startContainer(_) >> [
                status : [success: true],
                content: [Image: task.image, State: [Running: true]]
        ]
        2 * dockerClient.inspectContainer("123") >> [
                content: [Image: task.image, State: [Running: true]]
        ]

        and:
        upToDate == false
        task.changed == true
    }

    def "reload non-running container"() {
        when:
        task.targetState = "reloaded"
        task.image = "testImage:latest"
        task.containerName = "example"
        def upToDate = task.checkIfUpToDate()
        task.execute()

        then:
        4 * dockerClient.ps([filters: [name: ["example"]]]) >>> [
                [content: [[Names: ["/example"], Id: "123"]]],
                [content: [[Names: ["/example"], Id: "123"]]],
                [content: [[Names: ["/example"], Id: "123"]]],
                [content: [[Names: ["/example"], Id: "234"]]]
        ]
        1 * dockerClient.rm("123") >> [
                status: [success: true]
        ]
        1 * dockerClient.createContainer(_, [name: "example"]) >> [
                status : [success: true],
                content: [id: "234"]
        ]
        1 * dockerClient.startContainer(_) >> [
                status : [success: true],
                content: [Image: task.image, State: [Running: true]]
        ]
        4 * dockerClient.inspectContainer("123") >> [
                content: [Image: task.image, State: [Running: false]]
        ]
        2 * dockerClient.inspectContainer("234") >>> [
                [content: [Image: task.image, State: [Running: false]]],
                [content: [Image: task.image, State: [Running: true]]]
        ]

        and:
        upToDate == false
        task.changed == true
    }

    def "reload running container with different image"() {
        when:
        task.targetState = "reloaded"
        task.image = "testImage:latest"
        task.containerName = "example"
        def upToDate = task.checkIfUpToDate()
        task.execute()

        then:
        4 * dockerClient.ps([filters: [name: ["example"]]]) >>> [
                [content: [[Names: ["/example"], Id: "123"]]],
                [content: [[Names: ["/example"], Id: "123"]]],
                [content: [[Names: ["/example"], Id: "123"]]],
                [content: [[Names: ["/example"], Id: "234"]]]
        ]
        1 * dockerClient.rm("123") >> [
                status: [success: true]
        ]
        1 * dockerClient.createContainer(_, [name: "example"]) >> [
                status : [success: true],
                content: [id: "234"]
        ]
        1 * dockerClient.startContainer(_) >> [
                status : [success: true],
                content: [Image: task.image, State: [Running: true]]
        ]
        4 * dockerClient.inspectContainer("123") >> [
                content: [Image: "image1", State: [Running: true]],
        ]
        2 * dockerClient.inspectContainer("234") >>> [
                [content: [Image: task.image, State: [Running: false]]],
                [content: [Image: task.image, State: [Running: true]]]
        ]
        3 * dockerClient.inspectImage("testImage:latest") >> [
                status : [success: true],
                content: [Id: "image0"]
        ]

        and:
        upToDate == false
        task.changed == true
    }

    def "reload running container with everything same"() {
        when:
        task.targetState = "reloaded"
        task.image = "testImage:latest"
        task.containerName = "example"
        task.ports = ["80:8080"]
        task.env = ["TMP=1"]
        task.cmd = ["jar", "myjarfile.jar"]
        task.links = ["mycontainer:myalias"]
        task.volumes = [
                "/mnt/data:/data",
                "/mnt/readonly:/input:ro"
        ]
        task.extraHosts = ["dockerhost:127.0.0.1"]
        def upToDate = task.checkIfUpToDate()
        task.execute()

        then:
        3 * dockerClient.ps([filters: [name: ["example"]]]) >> [
                content: [[Names: ["/example"], Id: "123"]]
        ]
        3 * dockerClient.inspectContainer("123") >> [
                content: [
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
                        ]]
        ]
        3 * dockerClient.inspectImage("testImage:latest") >> [
                status : [success: true],
                content: [
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
        ]

        and:
        upToDate == true
        task.changed == false
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
