package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerCleanupTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('dockerCleanup', type: DockerCleanupTask)
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient"() {
    given:
    def containerPredicate = { container ->
      container.Names.any { String name ->
        name.replaceAll("^/", "").matches(".*data.*")
      }
    }
    task.shouldKeepContainer = containerPredicate

    when:
    task.execute()

    then:
    1 * dockerClient.cleanupStorage(containerPredicate)
  }
}
