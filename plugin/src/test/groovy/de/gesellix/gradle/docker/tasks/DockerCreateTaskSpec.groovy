package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerCreateTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('dockerCreate', type: DockerCreateTask)
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
    task.create()

    then:
    1 * dockerClient.createContainer(
        ["ExposedPorts": [
            "8889/tcp": [],
            "9300/tcp": []],
         "HostConfig"  : [
             "PortBindings": [
                 "8889/tcp": [
                     ["HostIp"  : "0.0.0.0",
                      "HostPort": "8889"]]
             ]],
         "Image"       : "anImage:aTag"],
        [name: "aContainerName"])
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
    task.create()

    then:
    1 * dockerClient.createContainer(
        ["Env"       : ['THE_WIND=CAUGHT_IT', 'FOO=BAR Baz'],
         "HostConfig": ["PublishAllPorts": false],
         "Image"     : "anImage"],
        [name: '']
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
    task.create()

    then:
    1 * dockerClient.createContainer(
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
             ]],
         "Image"     : "anImage:aTag"],
        [name: "aContainerName"])
  }
}
