package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.authentication.AuthConfig
import de.gesellix.gradle.docker.DockerPluginExtension
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class GenericDockerTaskSpec extends Specification {

    def task

    def setup() {
        def project = ProjectBuilder.builder().build()
        task = project.task('dockerTask', type: TestTask)
    }

    def "creates dockerClient only once"() {
        def clientMock = Mock(DockerClient)
        given:
        task.dockerClient = clientMock

        when:
        def dockerClient = task.dockerClient

        then:
        dockerClient == clientMock
    }

    def "delegates to dockerClient with default dockerHost"() {
        when:
        def dockerClient = task.dockerClient

        then:
        dockerClient.env.dockerHost == new DockerPluginExtension(task.project as Project).dockerHost ?: 'http://127.0.0.1:2375'
    }

    def "delegates to dockerClient with configured dockerHost"() {
        when:
        task.dockerHost = "http://example.org:4243"
        def dockerClient = task.dockerClient

        then:
        dockerClient.env.dockerHost == "http://example.org:4243"
    }

    def "delegates to dockerClient with configured certPath"() {
        when:
        task.certPath = "/path/to/certs"
        def dockerClient = task.dockerClient

        then:
        dockerClient.env.certPath.endsWith "/path/to/certs".replaceAll('/', "\\${File.separator}")
    }

    def "getAuthConfig with plain Map (deprecated)"() {
        when:
        task.authConfigPlain = [identitytoken: "foo"]

        then:
        task.getAuthConfig() == "eyJpZGVudGl0eXRva2VuIjoiZm9vIn0="
    }

    def "getAuthConfig with plain AuthConfig"() {
        when:
        task.authConfigPlain = new AuthConfig(identitytoken: "foo")

        then:
        task.getAuthConfig() == "eyJpZGVudGl0eXRva2VuIjoiZm9vIn0="
    }

    def "getAuthConfig with encoded AuthConfig"() {
        when:
        task.authConfigEncoded = "--auth.base64--"

        then:
        task.getAuthConfig() == "--auth.base64--"
    }

    def "getAuthConfig without AuthConfig"() {
        when:
        task.authConfigPlain = null
        task.authConfigEncoded = null

        then:
        task.getAuthConfig() == ''
    }
}
