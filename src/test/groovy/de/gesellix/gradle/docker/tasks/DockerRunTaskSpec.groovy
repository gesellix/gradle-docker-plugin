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

  // docker -H tcp://${targetHost} run -d -p 8889:8889 -p 9300:9300 --name $containerName ${imageName}
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

    when:
    task.run()

    then:
    1 * dockerClient.run(
        ["ExposedPorts": [
            "8889/tcp": [],
            "9300/tcp": []]],
        "anImage", "aTag", "aContainerName")
  }
}
