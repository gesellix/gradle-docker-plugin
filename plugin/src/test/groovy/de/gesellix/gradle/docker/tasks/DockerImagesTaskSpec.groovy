package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.engine.EngineResponse
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerImagesTaskSpec extends Specification {

    def project
    def task
    def dockerClient = Mock(DockerClient)

    def setup() {
        project = ProjectBuilder.builder().build()
        task = project.task('dockerImages', type: DockerImagesTask)
        task.dockerClient = dockerClient
    }

    def "delegates to dockerClient and saves result"() {
        when:
        task.images()

        then:
        1 * dockerClient.images() >> new EngineResponse(content: ["image"])

        and:
        task.images.content == ["image"]
    }
}
