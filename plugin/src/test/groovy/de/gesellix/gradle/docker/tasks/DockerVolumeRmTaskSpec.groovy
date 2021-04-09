package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.engine.EngineResponse
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerVolumeRmTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('dockerRmVolume', type: DockerVolumeRmTask)
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient and saves result"() {
    given:
    task.configure {
      volumeName = "foo"
    }
    def expectedResult = new EngineResponse(status: [code: 204])

    when:
    task.rmVolume()

    then:
    1 * dockerClient.rmVolume("foo") >> expectedResult

    and:
    task.response == expectedResult
  }
}
