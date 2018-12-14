package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerWaitTaskSpec extends Specification {

    def project
    def task
    def dockerClient = Mock(DockerClient)

    def setup() {
        project = ProjectBuilder.builder().build()
        task = project.task('dockerWait', type: DockerWaitTask)
        task.dockerClient = dockerClient
    }

    def "delegates to dockerClient"() {
        given:
        task.containerId = "4711"

        when:
        task.awaitStop()

        then:
        1 * dockerClient.wait("4711")
    }
}
