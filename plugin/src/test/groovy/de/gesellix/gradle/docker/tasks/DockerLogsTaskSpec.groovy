package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import java.time.Duration
import java.time.temporal.ChronoUnit

class DockerLogsTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('dockerLogs', DockerLogsTask).get()
    task.dockerClient = dockerClient
    task.logsTimeout = Duration.of(1, ChronoUnit.SECONDS)
  }

  def "delegates to dockerClient"() {
    given:
    task.containerId = "4711"
    task.logOptions.put("timestamps", true)

    when:
    task.logs()

    then:
    1 * dockerClient.logs("4711",
                          ["follow": false, "timestamps": true],
                          _, _)
  }
}
