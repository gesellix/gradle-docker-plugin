package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.EngineResponseContent
import de.gesellix.docker.remote.api.ImageSummary
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerImagesTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('dockerImages', DockerImagesTask).get()
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient and saves result"() {
    when:
    task.images()

    then:
    1 * dockerClient.images() >> new EngineResponseContent([new ImageSummary(
        "image", "parent", -1, 1, 10, 0, 1, null, null, null
    )])

    and:
    task.images.content.id == ["image"]
  }
}
