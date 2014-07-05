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
            "9300/tcp": []]
    ]
    task.hostConfiguration = [
        "PortBindings": [
            "8889/tcp": [
                ["HostIp"  : "0.0.0.0",
                 "HostPort": "8889"]]
        ]]

    when:
    task.run()

    then:
    1 * dockerClient.run(
        "anImage",
        ["ExposedPorts": [
            "8889/tcp": [],
            "9300/tcp": []]],
        ["PortBindings": [
            "8889/tcp": [
                ["HostIp"  : "0.0.0.0",
                 "HostPort": "8889"]]
        ]],
        "aTag", "aContainerName")
  }
}
