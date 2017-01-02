package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerNetworkDisconnectTaskSpec extends Specification {

    def project
    def task
    def dockerClient = Mock(DockerClient)

    def setup() {
        project = ProjectBuilder.builder().build()
        task = project.task('disconnectNetwork', type: DockerNetworkDisconnectTask)
        task.dockerClient = dockerClient
    }

    def "delegates to dockerClient and saves result"() {
        given:
        task.networkName = "a-network"
        task.containerName = "a-container"

        when:
        task.execute()

        then:
        1 * dockerClient.disconnectNetwork("a-network", "a-container") >> [content: "result"]

        and:
        task.response == [content: "result"]
    }
}
