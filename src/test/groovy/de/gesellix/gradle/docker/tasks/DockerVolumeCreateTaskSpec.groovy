package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerVolumeCreateTaskSpec extends Specification {

    def project
    def task
    def dockerClient = Mock(DockerClient)

    def setup() {
        project = ProjectBuilder.builder().build()
        task = project.task('dockerCreateVolume', type: DockerVolumeCreateTask)
        task.dockerClient = dockerClient
    }

    def "delegates to dockerClient and saves result"() {
        given:
        def config = [Name: "foo"]
        task.configure {
            volumeConfig = config
        }

        when:
        task.execute()

        then:
        1 * dockerClient.createVolume(config) >> [status: [code: 201]]

        and:
        task.response == [status: [code: 201]]
    }
}
