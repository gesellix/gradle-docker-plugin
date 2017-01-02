package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerSwarmInitTaskSpec extends Specification {

    def project
    def task
    def dockerClient = Mock(DockerClient)

    def setup() {
        project = ProjectBuilder.builder().build()
        task = project.task('initSwarm', type: DockerSwarmInitTask)
        task.dockerClient = dockerClient
    }

    def "delegates to dockerClient and saves result"() {
        given:
        task.config = [
                "ListenAddr": "0.0.0.0:80"
        ]

        when:
        task.execute()

        then:
        1 * dockerClient.initSwarm([
                "ListenAddr": "0.0.0.0:80"
        ]) >> [content: "swarm-result"]

        and:
        task.response == [content: "swarm-result"]
    }
}
