package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.EngineResponseContent
import de.gesellix.docker.remote.api.GraphDriverData
import de.gesellix.docker.remote.api.Image
import de.gesellix.docker.remote.api.ImageRootFS
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerInspectImageTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('dockerInspect', type: DockerInspectImageTask)
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient and returns result"() {
    given:
    task.imageId = "my.image:dev"
    def expectedResponse = new EngineResponseContent(new Image(
        "sha256:1234", "parent", "", "", "", "", "", "", "",
        0, 0, new GraphDriverData("", [:]), new ImageRootFS("", [], ""),
        null, null, null, null, null, null
    ))

    when:
    task.inspect()

    then:
    1 * dockerClient.inspectImage("my.image:dev") >> expectedResponse
    and:
    task.imageInfo == expectedResponse
  }
}
