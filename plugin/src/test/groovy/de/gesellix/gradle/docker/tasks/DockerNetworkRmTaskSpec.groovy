package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.FailsWith
import spock.lang.Specification

class DockerNetworkRmTaskSpec extends Specification {

    def project
    def task
    def dockerClient = Mock(DockerClient)

    def setup() {
        project = ProjectBuilder.builder().build()
        task = project.task('rmNetwork', type: DockerNetworkRmTask)
        task.dockerClient = dockerClient
    }

    def "delegates to dockerClient and saves result"() {
        given:
        task.networkName = "a-network"

        when:
        task.execute()

        then:
        1 * dockerClient.rmNetwork("a-network") >> [content: "result"]

        and:
        task.response == [content: "result"]
    }

    @FailsWith(RuntimeException)
    def "fails on error"() {
        given:
        task.networkName = "a-network"

        when:
        task.execute()

        then:
        1 * dockerClient.rmNetwork("a-network") >> { throw new RuntimeException("expected error") }

        and:
        task.response == null
    }

    def "can ignore errors"() {
        given:
        task.networkName = "a-network"
        task.ignoreError = true

        when:
        task.execute()

        then:
        1 * dockerClient.rmNetwork("a-network") >> { throw new RuntimeException("expected error") }

        and:
        task.response == null
    }
}
