package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
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

    def "start new non-existing container"() {
        when:
        task.targetState = "started"
        task.image = "testImage:latest"
        task.containerName = "example"
        task.execute()

        then:
        3 * dockerClient.ps([filters: [name: ["example"]]]) >>> [
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
        task.changed == true
    }

    def "start already created container that is not running"() {
        when:
        task.targetState = "started"
        task.image = "testImage:latest"
        task.containerName = "example"
        task.execute()

        then:
        1 * dockerClient.ps([filters: [name: ["example"]]]) >> [
                content: [[ Names: [ "/example" ], Id: "123" ]]
        ]
        1 * dockerClient.startContainer(_) >> [
                status: [ success: true ],
                content: [ Image: task.image, State: [ Running: true ] ]
        ]
        2 * dockerClient.inspectContainer("123") >>> [
                [ content: [ Image: task.image, State: [ Running: false ] ] ],
                [ content: [ Image: task.image, State: [ Running: true ] ] ]
        ]

        and:
        task.changed == true
    }

    def "start already running container"() {
        when:
        task.targetState = "started"
        task.image = "testImage:latest"
        task.containerName = "example"
        task.execute()

        then:
        1 * dockerClient.ps([filters: [name: ["example"]]]) >>> [
                [ content: [[ Names: [ "/example" ], Id: "123" ]]]
        ]
        1 * dockerClient.inspectContainer("123") >> [
                content: [ Image: task.image, State: [ Running: true ] ]
        ]

        and:
        task.changed == false
    }

    def "stop running container"() {
        when:
        task.targetState = "stopped"
        task.image = "testImage:latest"
        task.containerName = "example"
        task.execute()

        then:
        1 * dockerClient.ps([filters: [name: ["example"]]]) >>> [
                [ content: [[ Names: [ "/example" ], Id: "123" ]]]
        ]
        2 * dockerClient.inspectContainer("123") >>> [
                [ content: [ Image: task.image, State: [ Running: true ] ] ],
                [ content: [ Image: task.image, State: [ Running: false ] ] ],
        ]
        1 * dockerClient.stop("123") >> [
                status: [ success: true ]
        ]

        and:
        task.changed == true
    }

    def "stop already stopped container"() {
        when:
        task.targetState = "stopped"
        task.image = "testImage:latest"
        task.containerName = "example"
        task.execute()

        then:
        1 * dockerClient.ps([filters: [name: ["example"]]]) >>> [
                [ content: [[ Names: [ "/example" ], Id: "123" ]]]
        ]
        1 * dockerClient.inspectContainer("123") >> [
                content: [ Image: task.image, State: [ Running: false ] ]
        ]

        and:
        task.changed == false
    }

    def "remove running container"() {
        when:
        task.targetState = "absent"
        task.image = "testImage:latest"
        task.containerName = "example"
        task.execute()

        then:
        1 * dockerClient.ps([filters: [name: ["example"]]]) >>> [
                [ content: [[ Names: [ "/example" ], Id: "123" ]]]
        ]
        2 * dockerClient.inspectContainer("123") >>> [
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
        task.changed == true
    }

    def "remove stopped container"() {
        when:
        task.targetState = "absent"
        task.image = "testImage:latest"
        task.containerName = "example"
        task.execute()

        then:
        1 * dockerClient.ps([filters: [name: ["example"]]]) >>> [
                [ content: [[ Names: [ "/example" ], Id: "123" ]]]
        ]
        1 * dockerClient.inspectContainer("123") >> [
                content: [ Image: task.image, State: [ Running: false ] ]
        ]
        1 * dockerClient.rm("123") >> [
                status: [ success: true ]
        ]

        and:
        task.changed == true
    }

    def "remove non-existing container"() {
        when:
        task.targetState = "absent"
        task.image = "testImage:latest"
        task.containerName = "example"
        task.execute()

        then:
        1 * dockerClient.ps([filters: [name: ["example"]]]) >> [
                content: []
        ]

        and:
        task.changed == false
    }

    def "reload new non-existing container"() {
        when:
        task.targetState = "reloaded"
        task.image = "testImage:latest"
        task.containerName = "example"
        task.execute()

        then:
        4 * dockerClient.ps([filters: [name: ["example"]]]) >>> [
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
        task.changed == true
    }

    def "reload non-running container"() {
        when:
        task.targetState = "reloaded"
        task.image = "testImage:latest"
        task.containerName = "example"
        task.execute()

        then:
        2 * dockerClient.ps([filters: [name: ["example"]]]) >>> [
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
        2 * dockerClient.inspectContainer("123") >>> [
                [ content: [ Image: task.image, State: [ Running: false ] ] ],
                [ content: [ Image: task.image, State: [ Running: false ] ] ]
        ]
        2 * dockerClient.inspectContainer("234") >>> [
                [ content: [ Image: task.image, State: [ Running: false ] ] ],
                [ content: [ Image: task.image, State: [ Running: true ] ] ]
        ]

        and:
        task.changed == true
    }

    def "reload running container with different image"() {
        when:
        task.targetState = "reloaded"
        task.image = "testImage:latest"
        task.containerName = "example"
        task.execute()

        then:
        2 * dockerClient.ps([filters: [name: ["example"]]]) >>> [
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
        2 * dockerClient.inspectContainer("123") >>> [
                [ content: [ Image: "image1", State: [ Running: true ] ] ],
                [ content: [ Image: "image1", State: [ Running: true ] ] ]
        ]
        2 * dockerClient.inspectContainer("234") >>> [
                [ content: [ Image: task.image, State: [ Running: false ] ] ],
                [ content: [ Image: task.image, State: [ Running: true ] ] ]
        ]
        1 * dockerClient.inspectImage("testImage:latest") >> [
                status: [ success: true ],
                content: [ Id: "image0" ]
        ]

        and:
        task.changed == true
    }

    // TODO: HealthCheck tests
}
