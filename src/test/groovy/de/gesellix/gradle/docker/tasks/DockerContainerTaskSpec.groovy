package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.util.AvailablePortFinder
import spock.lang.Specification

class DockerContainerTaskSpec extends Specification {
    def project
    def task
    def dockerClient = Mock(DockerClient)

    def setup() {
        project = ProjectBuilder.builder().build()
        task = project.task('dockerContainer', type: DockerContainerTask)
        task.dockerClient = dockerClient
    }

    def "dockerHost from tcp://127.0.0.1"() {
        expect:
        new URI("tcp://127.0.0.1").getHost() == "127.0.0.1"
    }

    def "dockerHost from tcp://127.0.0.1:999"() {
        expect:
        new URI("tcp://127.0.0.1:999").getHost() == "127.0.0.1"
    }

    def "no container name"() {
        when:
        task.targetState = "started"
        task.image = "testImage:latest"
        task.execute()

        then:
        thrown(GradleException)
    }

    def "start new non-existing container"() {
        when:
        task.targetState = "started"
        task.image = "testImage:latest"
        task.containerName = "example"
        def upToDate = task.checkIfUpToDate()
        task.execute()

        then:
        5 * dockerClient.ps([filters: [name: ["example"]]]) >>> [
                [ content: [] ],
                [ content: [] ],
                [ content: [] ],
                [ content: [] ],
                [ content: [[ Names: [ "/example" ], Id: "123" ]]]
        ]
        1 * dockerClient.createContainer(_, [name: "example"]) >> [
                status: [ success: true ],
                content: [ id: "123"]
        ]
        1 * dockerClient.startContainer(_) >> [
                status: [ success: true ],
                content: [ Image: task.image, State: [ Running: true ] ]
        ]
        1 * dockerClient.inspectContainer("123") >> [
                content: [ Image: task.image, State: [ Running: true ] ]
        ]

        and:
        upToDate == false
        task.changed == true
    }

    def "start already created container that is not running"() {
        when:
        task.targetState = "started"
        task.image = "testImage:latest"
        task.containerName = "example"
        def upToDate = task.checkIfUpToDate()
        task.execute()

        then:
        3 * dockerClient.ps([filters: [name: ["example"]]]) >> [
                content: [[ Names: [ "/example" ], Id: "123" ]]
        ]
        1 * dockerClient.startContainer(_) >> [
                status: [ success: true ],
                content: [ Image: task.image, State: [ Running: true ] ]
        ]
        4 * dockerClient.inspectContainer("123") >>> [
                [ content: [ Image: task.image, State: [ Running: false ] ] ],
                [ content: [ Image: task.image, State: [ Running: false ] ] ],
                [ content: [ Image: task.image, State: [ Running: false ] ] ],
                [ content: [ Image: task.image, State: [ Running: true ] ] ]
        ]

        and:
        upToDate == false
        task.changed == true
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
                content: [[ Names: [ "/example" ], Id: "123" ]]
        ]
        3 * dockerClient.inspectContainer("123") >> [
                content: [ Image: task.image, State: [ Running: true ] ]
        ]

        and:
        upToDate == true
        task.changed == false
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
                content: [[ Names: [ "/example" ], Id: "123" ]]
        ]
        4 * dockerClient.inspectContainer("123") >>> [
                [ content: [ Image: task.image, State: [ Running: true ] ] ],
                [ content: [ Image: task.image, State: [ Running: true ] ] ],
                [ content: [ Image: task.image, State: [ Running: true ] ] ],
                [ content: [ Image: task.image, State: [ Running: false ] ] ],
        ]
        1 * dockerClient.stop("123") >> [
                status: [ success: true ]
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
                [ content: [[ Names: [ "/example" ], Id: "123" ]]]
        ]
        3 * dockerClient.inspectContainer("123") >> [
                content: [ Image: task.image, State: [ Running: false ] ]
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
                content: [[ Names: [ "/example" ], Id: "123" ]]
        ]
        4 * dockerClient.inspectContainer("123") >>> [
                [ content: [ Image: task.image, State: [ Running: true ] ] ],
                [ content: [ Image: task.image, State: [ Running: true ] ] ],
                [ content: [ Image: task.image, State: [ Running: true ] ] ],
                [ content: [ Image: task.image, State: [ Running: false ] ] ],
        ]
        1 * dockerClient.stop("123") >> [
                status: [ success: true ]
        ]
        1 * dockerClient.rm("123") >> [
                status: [ success: true ]
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
                content: [[ Names: [ "/example" ], Id: "123" ]]
        ]
        3 * dockerClient.inspectContainer("123") >> [
                content: [ Image: task.image, State: [ Running: false ] ]
        ]
        1 * dockerClient.rm("123") >> [
                status: [ success: true ]
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
                [ content: [] ],
                [ content: [] ],
                [ content: [] ],
                [ content: [] ],
                [ content: [[ Names: [ "/example" ], Id: "123" ]]]
        ]
        1 * dockerClient.createContainer(_, [name: "example"]) >> [
                status: [ success: true ],
                content: [ id: "123"]
        ]
        1 * dockerClient.startContainer(_) >> [
                status: [ success: true ],
                content: [ Image: task.image, State: [ Running: true ] ]
        ]
        2 * dockerClient.inspectContainer("123") >> [
                content: [ Image: task.image, State: [ Running: true ] ]
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
                [ content: [[ Names: [ "/example" ], Id: "123" ]]],
                [ content: [[ Names: [ "/example" ], Id: "123" ]]],
                [ content: [[ Names: [ "/example" ], Id: "123" ]]],
                [ content: [[ Names: [ "/example" ], Id: "234" ]]]
        ]
        1 * dockerClient.rm("123") >> [
                status: [ success: true ]
        ]
        1 * dockerClient.createContainer(_, [name: "example"]) >> [
                status: [ success: true ],
                content: [ id: "234"]
        ]
        1 * dockerClient.startContainer(_) >> [
                status: [ success: true ],
                content: [ Image: task.image, State: [ Running: true ] ]
        ]
        4 * dockerClient.inspectContainer("123") >> [
                content: [ Image: task.image, State: [ Running: false ] ]
        ]
        2 * dockerClient.inspectContainer("234") >>> [
                [ content: [ Image: task.image, State: [ Running: false ] ] ],
                [ content: [ Image: task.image, State: [ Running: true ] ] ]
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
                [ content: [[ Names: [ "/example" ], Id: "123" ]]],
                [ content: [[ Names: [ "/example" ], Id: "123" ]]],
                [ content: [[ Names: [ "/example" ], Id: "123" ]]],
                [ content: [[ Names: [ "/example" ], Id: "234" ]]]
        ]
        1 * dockerClient.rm("123") >> [
                status: [ success: true ]
        ]
        1 * dockerClient.createContainer(_, [name: "example"]) >> [
                status: [ success: true ],
                content: [ id: "234"]
        ]
        1 * dockerClient.startContainer(_) >> [
                status: [ success: true ],
                content: [ Image: task.image, State: [ Running: true ] ]
        ]
        4 * dockerClient.inspectContainer("123") >> [
                content: [ Image: "image1", State: [ Running: true ] ],
        ]
        2 * dockerClient.inspectContainer("234") >>> [
                [ content: [ Image: task.image, State: [ Running: false ] ] ],
                [ content: [ Image: task.image, State: [ Running: true ] ] ]
        ]
        3 * dockerClient.inspectImage("testImage:latest") >> [
                status: [ success: true ],
                content: [ Id: "image0" ]
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
        task.ports = [ "80:8080" ]
        task.env = [ "TMP=1" ]
        task.cmd = [ "jar", "myjarfile.jar" ]
        task.links = [ "mycontainer:myalias" ]
        task.volumes = [
                "/mnt/data:/data",
                "/mnt/readonly:/input:ro"
        ]
        task.extraHosts = [ "dockerhost:127.0.0.1" ]
        def upToDate = task.checkIfUpToDate()
        task.execute()

        then:
        3 * dockerClient.ps([filters: [name: ["example"]]]) >> [
                content: [[ Names: [ "/example" ], Id: "123" ]]
        ]
        3 * dockerClient.inspectContainer("123") >> [
                content: [
                        Image: "image1",
                        State: [
                                Running: true
                        ],
                        Config: [
                                ExposedPorts: [ "8080/tcp": [] ],
                                Volumes: [
                                        "/data": [],
                                        "/spec": [],
                                        "/input": []
                                ],
                                Env: [ "TMP=1", "MYVAR=myval" ],
                                Cmd: [ "java", "jar", "myjarfile.jar" ]
                        ],
                        HostConfig: [
                                Binds: [
                                        "/mnt/data:/data",
                                        "/mnt/readonly:/input:ro"
                                ],
                                Links: [ "mycontainer:myalias" ],
                                ExtraHosts: [ "dockerhost:127.0.0.1" ],
                                Privileged: false,
                                PortBindings: [ "8080/tcp" : [
                                        [
                                                HostIp: "0.0.0.0",
                                                HostPort: "80"
                                        ]
                                ]]
                        ]]
        ]
        3 * dockerClient.inspectImage("testImage:latest") >> [
                status: [ success: true ],
                content: [
                        Id: "image1",
                        ContainerConfig: [
                                ExposedPorts: [],
                                Volumes: [ "/spec": [] ],
                                Env: [ "MYVAR=myval" ]
                        ],
                        Config: [
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
                content: [[ Names: [ "/example" ], Id: "123" ]]
        ]
        2 * dockerClient.inspectContainer("123") >> [
                content: [
                        Image: task.image,
                        State: [ Running: true ],
                        HostConfig: [
                                PortBindings: [ ]
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
        int port = AvailablePortFinder.createPrivate().nextAvailable

        when:
        task.dockerHost = "tcp://127.0.0.1:999"
        task.targetState = "started"
        task.image = "testImage:latest"
        task.containerName = "example"
        task.healthChecks = [
                [
                        containerPort: 8080,
                        timeout: 1,
                        retries: 1
                ]]
        task.execute()

        then:
        1 * dockerClient.ps([filters: [name: ["example"]]]) >>> [
                [ content: [[ Names: [ "/example" ], Id: "123" ]]]
        ]
        2 * dockerClient.inspectContainer("123") >> [
                content: [
                        Image: task.image,
                        State: [ Running: true ],
                        HostConfig: [
                                PortBindings: [ "8080/tcp" : [
                                        [
                                                HostIp: "0.0.0.0",
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
        int port = 0

        Thread server = new Thread() {
            boolean initialized = false
            int serverPort = 0

            public void run() {
                ServerSocket ss;
                synchronized (this) {
                    serverPort = AvailablePortFinder.createPrivate().nextAvailable
                    ss = new ServerSocket(serverPort);
                    initialized = true
                    notify()
                }
                while (true) {
                    Socket client = ss.accept();
                }
            }
        }

        server.start()
        synchronized(server) {
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
                        timeout: 1,
                        retries: 1
                ]]
        task.execute()

        then:
        2 * dockerClient.ps([filters: [name: ["example"]]]) >> [
                content: [[ Names: [ "/example" ], Id: "123" ]]
        ]
        4 * dockerClient.inspectContainer("123") >> [
                content: [
                        Image: task.image,
                        State: [ Running: true ],
                        HostConfig: [
                                PortBindings: [ "8080/tcp" : [
                                        [
                                                HostIp: "0.0.0.0",
                                                HostPort: port.toString()
                                        ]
                                ]]
                        ]
                ]
        ]

        and:
        task.changed == false

        cleanup:
        server.stop();
    }
}
