package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerExecTask extends GenericDockerTask {

  @Input
  Property<String> containerId

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getContainerId()
   */
  @Deprecated
  void setContainerId(String containerId) {
    this.containerId.set(containerId)
  }

  @Input
  @Optional
  ListProperty<String> cmds

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getCmds()
   */
  @Deprecated
  void setCommandLine(Collection<String> commandLine) {
    this.cmds.set(commandLine)
  }

  private Property<String> cmd

  @Input
  @Optional
  Property<String> getCmd() {
    return cmd
  }

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getCmd()
   */
  @Deprecated
  void setCommandLine(String commandLine) {
    this.cmd.set(commandLine)
  }

  @Internal
  def result

  @Inject
  DockerExecTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Run a command in a running container"

    containerId = objectFactory.property(String)
    cmds = objectFactory.listProperty(String)
    cmd = objectFactory.property(String)
  }

  @TaskAction
  def exec() {
    logger.info "docker exec"

    String[] commandline = cmds.get() ?: ["sh", "-c", cmd.getOrNull()]
    Map<String, Object> execCreateConfig = [
        "AttachStdin" : false,
        "AttachStdout": true,
        "AttachStderr": true,
        "Tty"         : false,
        "Cmd"         : commandline
    ]
    logger.debug("exec cmd: '${execCreateConfig.Cmd}'")
    def execCreateResult = dockerClient.createExec(containerId.get(), execCreateConfig)

    String execId = execCreateResult.content.Id
    Map<String, Object> execStartConfig = [
        "Detach": false,
        "Tty"   : false]
    result = dockerClient.startExec(execId, execStartConfig)
  }
}
