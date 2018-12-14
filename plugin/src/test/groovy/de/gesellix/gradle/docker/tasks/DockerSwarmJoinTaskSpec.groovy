package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerSwarmJoinTaskSpec extends Specification {

    def project
    DockerSwarmJoinTask task
    def dockerClient = Mock(DockerClient)

    def setup() {
        project = ProjectBuilder.builder().build()
        task = project.task('joinSwarm', type: DockerSwarmJoinTask) as DockerSwarmJoinTask
        task.dockerClient = dockerClient
    }

    def "delegates to dockerClient and saves result"() {
        given:
        task.config = [
                "ListenAddr": "0.0.0.0:4500",
                "RemoteAddr": "node1:4500",
                "Manager"   : false
        ]

        when:
        task.joinSwarm()

        then:
        1 * dockerClient.joinSwarm([
                "ListenAddr": "0.0.0.0:4500",
                "RemoteAddr": "node1:4500",
                "Manager"   : false
        ]) >> [content: "swarm-result"]

        and:
        task.response == [content: "swarm-result"]
    }
}
