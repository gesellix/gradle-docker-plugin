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
    description = "Run a command in a running container"
    group = "Docker"
  }

  @TaskAction
  def exec() {
    logger.info "docker exec"

    def execCreateConfig = [
        "AttachStdin" : false,
        "AttachStdout": true,
        "AttachStderr": true,
        "Tty"         : false,
        "Cmd"         : getCommandLine()
    ]
    if (!(getCommandLine() instanceof Collection<String>)) {
      execCreateConfig.Cmd = ['sh', '-c', getCommandLine()?.toString()]
    }
    logger.debug("exec cmd: '${execCreateConfig.Cmd}'")
    def execCreateResult = dockerClient.createExec(getContainerId(), execCreateConfig)

    def execStartConfig = [
        "Detach": false,
        "Tty"   : false]
    result = dockerClient.startExec(execCreateResult.Id, execStartConfig)
  }
}
