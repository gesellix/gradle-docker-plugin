package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.EngineResponseContent
import de.gesellix.docker.remote.api.ExecConfig
import de.gesellix.docker.remote.api.ExecStartConfig
import de.gesellix.docker.remote.api.IdResponse
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerExecTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('dockerExec', type: DockerExecTask)
    task.dockerClient = dockerClient
  }

  def "delegates plain exec command via 'sh -c' to dockerClient and saves result"() {
    given:
    def containerId = 'foo'
    task.containerId = containerId

    def commandLine = 'echo "foo" > /bar.txt && cat /bar.txt'
    task.cmd = commandLine

    def execConfig = new ExecConfig().tap {
      attachStdin = false
      attachStdout = true
      attachStderr = true
      tty = false
      cmd = ["sh", "-c", commandLine]
    }

    when:
    task.exec()

    then:
    1 * dockerClient.createExec(containerId, execConfig) >> new EngineResponseContent(new IdResponse("exec-id"))
    1 * dockerClient.startExec("exec-id", new ExecStartConfig(false, false), null, null)
  }

  def "delegates exec commands to dockerClient and saves result"() {
    given:
    def containerId = 'foo'
    task.containerId = containerId

    def commands = ['sh', '-c', 'echo "foo" > /baz.txt && cat /baz.txt']
    task.cmds = commands

    def execConfig = new ExecConfig().tap {
      attachStdin = false
      attachStdout = true
      attachStderr = true
      tty = false
      cmd = commands
    }

    when:
    task.exec()

    then:
    1 * dockerClient.createExec(containerId, execConfig) >> new EngineResponseContent(new IdResponse("exec-id"))
    1 * dockerClient.startExec("exec-id", new ExecStartConfig(false, false), null, null)
  }
}
