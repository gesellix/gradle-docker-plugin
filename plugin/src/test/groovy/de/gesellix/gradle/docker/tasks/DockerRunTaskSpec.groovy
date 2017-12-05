package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerRunTaskSpec extends Specification {

    def project
    def task
    def dockerClient = Mock(DockerClient)

    def setup() {
        project = ProjectBuilder.builder().build()
        task = project.task('dockerRun', type: DockerRunTask)
        task.dockerClient = dockerClient
    }

    def "delegates to dockerClient"() {
        given:
        task.imageName = "anImage"
        task.tag = "aTag"
        task.containerName = "aContainerName"
        task.containerConfiguration = [
                "ExposedPorts": [
                        "8889/tcp": [],
                        "9300/tcp": []],
                HostConfig    : ["PortBindings": [
                        "8889/tcp": [
                                ["HostIp"  : "0.0.0.0",
                                 "HostPort": "8889"]]
                ]]
        ]

        when:
        task.execute()

        then:
        1 * dockerClient.run(
                "anImage",
                ["ExposedPorts": [
                        "8889/tcp": [],
                        "9300/tcp": []],
                 "HostConfig"  : [
                         "PortBindings": [
                                 "8889/tcp": [
                                         ["HostIp"  : "0.0.0.0",
                                          "HostPort": "8889"]]
                         ]]],
                "aTag",
                "aContainerName",
                "")
    }

    def "parses env-file to containerConfig.Env"() {
        URL envfile = getClass().getResource('/env-files/env-test.properties')

        given:
        task.imageName = "anImage"
        task.containerConfiguration = [
                "Env"       : null,
                "HostConfig": ["PublishAllPorts": false]
        ]
        task.environmentFiles = [new File(envfile.toURI())]

        when:
        task.execute()

        then:
        1 * dockerClient.run(
                "anImage",
                ["Env"       : ['THE_WIND=CAUGHT_IT', 'FOO=BAR Baz'],
                 "HostConfig": ["PublishAllPorts": false]],
                '',
                '',
                ''
        )
    }

    def "maps env and port properties to actual containerConfig"() {
        given:
        task.imageName = "anImage"
        task.tag = "aTag"
        task.containerName = "aContainerName"
        task.env = ["foo=bar"]
        task.ports = ["8080:80", "8889:8889"]

        when:
        task.execute()

        then:
        1 * dockerClient.run(
                "anImage",
                [Env         : ["foo=bar"],
                 ExposedPorts: [
                         "80/tcp"  : [:],
                         "8889/tcp": [:]],
                 HostConfig  : [
                         PortBindings: [
                                 "80/tcp"  : [
                                         [HostIp  : "0.0.0.0",
                                          HostPort: "8080"]],
                                 "8889/tcp": [
                                         [HostIp  : "0.0.0.0",
                                          HostPort: "8889"]]
                         ]]],
                "aTag",
                "aContainerName",
                '')
    }

    def "passes auth config to docker client"() {
        given:
        task.imageName = "anImage"
        task.tag = "theTag"
        task.containerName = "anotherContainerName"
        task.authConfigPlain = [foo: "bar"]

        when:
        task.execute()

        then:
        1 * dockerClient.encodeAuthConfig([foo: "bar"]) >> "encoded-auth"
        1 * dockerClient.run(
                "anImage",
                [HostConfig: [:]],
                "theTag",
                "anotherContainerName",
                "encoded-auth")
    }
}
