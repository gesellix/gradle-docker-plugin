package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DockerExecTask extends GenericDockerTask {

  private final Property<String> containerId;

  @Input
  public Property<String> getContainerId() {
    return containerId;
  }

  private final ListProperty<String> cmds;

  @Input
  @Optional
  public ListProperty<String> getCmds() {
    return cmds;
  }

  private final Property<String> cmd;

  @Input
  @Optional
  public Property<String> getCmd() {
    return cmd;
  }

  private EngineResponse result;

  @Internal
  public EngineResponse getResult() {
    return result;
  }

  @Inject
  public DockerExecTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Run a command in a running container");

    containerId = objectFactory.property(String.class);
    cmds = objectFactory.listProperty(String.class);
    cmd = objectFactory.property(String.class);
  }

  @TaskAction
  public void exec() {
    getLogger().info("docker exec");

    final List<String> get = cmds.get();
    List<String> commandline = (!cmds.get().isEmpty()) ? cmds.get() : Arrays.asList("sh", "-c", cmd.getOrNull());
    Map<String, Object> execCreateConfig = new HashMap<>(5);
    execCreateConfig.put("AttachStdin", false);
    execCreateConfig.put("AttachStdout", true);
    execCreateConfig.put("AttachStderr", true);
    execCreateConfig.put("Tty", false);
    execCreateConfig.put("Cmd", commandline);
    getLogger().debug("exec cmd: '" + execCreateConfig.get("Cmd") + "'");
    EngineResponse execCreateResult = getDockerClient().createExec(containerId.get(), execCreateConfig);

    String execId = (String) ((Map<String, Object>) execCreateResult.getContent()).get("Id");
    Map<String, Boolean> execStartConfig = new HashMap<>(2);
    execStartConfig.put("Detach", false);
    execStartConfig.put("Tty", false);
    result = getDockerClient().startExec(execId, execStartConfig);
  }

  /**
   * @see #getContainerId()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setContainerId(String containerId) {
    this.containerId.set(containerId);
  }

  /**
   * @see #getCmd()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setCommandLine(String commandLine) {
    this.cmd.set(commandLine);
  }

  /**
   * @see #getCmds()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setCommandLine(Collection<String> commandLine) {
    this.cmds.set(commandLine);
  }
}
