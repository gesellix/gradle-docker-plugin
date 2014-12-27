package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerExecTask extends AbstractDockerTask {

  private static Logger logger = LoggerFactory.getLogger(DockerExecTask)

  @Input
  def containerId
  @Input
  def commandLine

  def result

  DockerExecTask() {
    description = "run a command in a running container"
    group = "Docker"
  }

  @TaskAction
  def exec() {
    logger.info "running exec..."
    def execCreateResult = dockerClient.createExec(containerId, [
        "AttachStdin" : false,
        "AttachStdout": true,
        "AttachStderr": true,
        "Tty"         : false,
        "Cmd"         : commandLine instanceof Collection<String> ? commandLine : commandLine.toString().split(' ')
    ])
    result = dockerClient.startExec(execCreateResult.Id, [
        "Detach": false,
        "Tty"   : false])
  }
}
