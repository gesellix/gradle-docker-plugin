package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.EngineResponseContent
import de.gesellix.docker.remote.api.VolumeListResponse
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerVolumesTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('dockerVolumes', DockerVolumesTask).get()
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient and saves result"() {
    given:
    def expectedResult = new EngineResponseContent(new VolumeListResponse(
        [], []
    ))

    when:
    task.volumes()

    then:
    1 * dockerClient.volumes([:]) >> expectedResult

    and:
    task.volumes == expectedResult
  }

  def "delegates with query to dockerClient and saves result"() {
    given:
    def expectedResult = new EngineResponseContent(new VolumeListResponse(
        [], []
    ))

    when:
    task.configure {
      query = [filters: [dangling: ["true"]]]
    }
    task.volumes()

    then:
    1 * dockerClient.volumes([filters: [dangling: ["true"]]]) >> expectedResult

    and:
    task.volumes == expectedResult
  }
}
