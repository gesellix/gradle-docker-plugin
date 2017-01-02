package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerVolumesTaskSpec extends Specification {

    def project
    def task
    def dockerClient = Mock(DockerClient)

    def setup() {
        project = ProjectBuilder.builder().build()
        task = project.task('dockerVolumes', type: DockerVolumesTask)
        task.dockerClient = dockerClient
    }

    def "delegates to dockerClient and saves result"() {
        when:
        task.execute()

        then:
        1 * dockerClient.volumes([:]) >> [["Name": "id"]]

        and:
        task.volumes == [["Name": "id"]]
    }

    def "delegates with query to dockerClient and saves result"() {
        when:
        task.configure {
            query = [filters: [dangling: ["true"]]]
        }
        task.execute()

        then:
        1 * dockerClient.volumes([filters: [dangling: ["true"]]]) >> [["Name": "id"]]

        and:
        task.volumes == [["Name": "id"]]
    }
}
