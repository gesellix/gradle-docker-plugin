package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerServiceCreateTaskSpec extends Specification {

    def project
    def task
    def dockerClient = Mock(DockerClient)

    def setup() {
        project = ProjectBuilder.builder().build()
        task = project.task('createService', type: DockerServiceCreateTask)
        task.dockerClient = dockerClient
    }

    def "delegates to dockerClient and saves result"() {
        given:
        task.serviceConfig = [
                "Name"        : "a-service",
                "TaskTemplate": ["ContainerSpec": ["Image": "nginx"]]
        ]

        when:
        task.execute()

        then:
        1 * dockerClient.createService([
                "Name"        : "a-service",
                "TaskTemplate": ["ContainerSpec": ["Image": "nginx"]]
        ]) >> [content: "service-result"]

        and:
        task.response == [content: "service-result"]
    }
}
