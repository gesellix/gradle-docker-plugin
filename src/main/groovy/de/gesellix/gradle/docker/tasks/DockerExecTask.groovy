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
    def actualCommand = getCommandLine() instanceof Collection<String> ? getCommandLine() : "sh -c '${getCommandLine()}'"
    logger.debug("exec '$actualCommand'")
    def execCreateResult = dockerClient.createExec(getContainerId(), [
        "AttachStdin" : false,
        "AttachStdout": true,
        "AttachStderr": true,
        "Tty"         : false,
        "Cmd": actualCommand
    ])
    result = dockerClient.startExec(execCreateResult.Id, [
        "Detach": false,
        "Tty"   : false])
  }
}
