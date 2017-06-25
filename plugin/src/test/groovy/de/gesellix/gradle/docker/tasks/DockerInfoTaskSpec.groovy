package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.engine.EngineResponse
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerInfoTaskSpec extends Specification {

    def project
    def task
    def dockerClient = Mock(DockerClient)

    def setup() {
        project = ProjectBuilder.builder().build()
        task = project.task('dockerInfo', type: DockerInfoTask)
        task.dockerClient = dockerClient
    }

    def "delegates to dockerClient and saves result"() {
        given:
        def response = new EngineResponse()

        when:
        task.execute()

        then:
        1 * dockerClient.info() >> response

        and:
        task.info == response
    }
}
