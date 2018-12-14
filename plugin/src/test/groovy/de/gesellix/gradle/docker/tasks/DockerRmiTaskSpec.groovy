package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerRmiTaskSpec extends Specification {

    def project
    def task
    def dockerClient = Mock(DockerClient)

    def setup() {
        project = ProjectBuilder.builder().build()
        task = project.task('dockerRmi', type: DockerRmiTask)
        task.dockerClient = dockerClient
    }

    def "delegates to dockerClient"() {
        given:
        task.imageId = "4712"

        when:
        task.rmi()

        then:
        1 * dockerClient.rmi("4712")
    }
}
