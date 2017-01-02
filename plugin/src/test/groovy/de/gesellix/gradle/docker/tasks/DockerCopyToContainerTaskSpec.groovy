package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerCopyToContainerTaskSpec extends Specification {

    def project
    def task
    def dockerClient = Mock(DockerClient)

    def setup() {
        project = ProjectBuilder.builder().build()
        task = project.task('dockerCpToContainer', type: DockerCopyToContainerTask)
        task.dockerClient = dockerClient
    }

    def "delegates archive upload to dockerClient"() {
        given:
        task.container = "4711"
        task.targetPath = "/tmp/."

        def stream = new ByteArrayInputStream('--'.bytes)
        task.tarInputStream = stream

        when:
        task.execute()

        then:
        1 * dockerClient.putArchive("4711", "/tmp/.", stream)
    }
}
