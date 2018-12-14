package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.engine.EngineResponse
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerInspectImageTaskSpec extends Specification {
    def project
    def task
    def dockerClient = Mock(DockerClient)

    def setup() {
        project = ProjectBuilder.builder().build()
        task = project.task('dockerInspect', type: DockerInspectImageTask)
        task.dockerClient = dockerClient
    }

    def "delegates to dockerClient and returns result"() {
        given:
        task.imageId = "my.image:dev"
        def expectedResponse = new EngineResponse(content: ["id": "sha256:1234"])

        when:
        task.inspect()

        then:
        1 * dockerClient.inspectImage("my.image:dev") >> expectedResponse
        and:
        task.imageInfo == expectedResponse
    }
}
