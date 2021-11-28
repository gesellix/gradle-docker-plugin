package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerCommitTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('dockerCommit', type: DockerCommitTask)
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient"() {
    given:
    task.repo = "your.local.repo"
    task.tag = "container-changed:1.0"
    task.containerId = "a-container"
    task.author = "Tue Dissing <tue@somersault.dk>"
    task.comment = "a test"
    task.changes.set("change description")
    task.pauseContainer = true

    when:
    task.commit()

    then:
    1 * dockerClient.commit("a-container", [
        repo   : 'your.local.repo',
        tag    : 'container-changed:1.0',
        comment: 'a test',
        author : 'Tue Dissing <tue@somersault.dk>',
        changes: "change description",
        pause  : true
    ])
  }
}
