package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerVersionTaskSpec extends Specification {

    def project
    def task
    def dockerClient = Mock(DockerClient)

    def setup() {
        project = ProjectBuilder.builder().build()
        task = project.task('dockerVersion', type: DockerVersionTask)
        task.dockerClient = dockerClient
    }

    def "delegates to dockerClient and saves result"() {
        when:
        task.execute()

        then:
        1 * dockerClient.version() >> ["some docker version"]

        and:
        task.version == ["some docker version"]
    }
}
