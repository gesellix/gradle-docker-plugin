package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.engine.EngineResponse
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerCopyFromContainerTaskSpec extends Specification {

    def project
    def task
    def dockerClient = Mock(DockerClient)

    def setup() {
        project = ProjectBuilder.builder().build()
        task = project.task('dockerCpFromContainer', type: DockerCopyFromContainerTask)
        task.dockerClient = dockerClient
    }

    def "delegates archive download from dockerClient and saves result"() {
        given:
        task.container = "4711"
        task.sourcePath = "/file.txt"
        def expectedResponse = new EngineResponse(content: "file-content")

        when:
        task.copyFromContainer()

        then:
        1 * dockerClient.getArchive("4711", "/file.txt") >> expectedResponse
        and:
        task.content == expectedResponse
    }
}
