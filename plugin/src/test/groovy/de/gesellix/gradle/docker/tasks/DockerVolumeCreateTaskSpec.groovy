package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.EngineResponseContent
import de.gesellix.docker.remote.api.Volume
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerVolumeCreateTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('dockerCreateVolume', DockerVolumeCreateTask).get()
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient and saves result"() {
    given:
    def config = [Name: "foo"]
    task.configure {
      volumeConfig = config
    }
    def expectedResult = new EngineResponseContent(new Volume(
        "foo", "overlay", "", null, null, null, null, null, null
    ))

    when:
    task.createVolume()

    then:
    1 * dockerClient.createVolume(config) >> expectedResult

    and:
    task.response == expectedResult
  }
}
