package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerNetworkCreateTaskSpec extends Specification {

    def project
    def task
    def dockerClient = Mock(DockerClient)

    def setup() {
        project = ProjectBuilder.builder().build()
        task = project.task('createNetwork', type: DockerNetworkCreateTask)
        task.dockerClient = dockerClient
    }

    def "delegates to dockerClient and saves result"() {
        given:
        task.networkName = "a-network"
        task.networkConfig = [
                Driver: "overlay",
                "IPAM": ["Driver": "default"]
        ]

        when:
        task.execute()

        then:
        1 * dockerClient.createNetwork("a-network", [
                Driver: "overlay",
                "IPAM": [
                        "Driver": "default"
                ]]) >> [content: "result"]

        and:
        task.response == [content: "result"]
    }
}
