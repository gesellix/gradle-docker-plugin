package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.remote.api.ExecConfig;
import de.gesellix.docker.remote.api.ExecStartConfig;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

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

    List<String> commandline = (!cmds.get().isEmpty()) ? cmds.get() : Arrays.asList("sh", "-c", cmd.getOrNull());
    ExecConfig execCreateConfig = new ExecConfig();
    execCreateConfig.setAttachStdin(false);
    execCreateConfig.setAttachStdout(true);
    execCreateConfig.setAttachStderr(true);
    execCreateConfig.setTty(false);
    execCreateConfig.setCmd(commandline);
    getLogger().debug("exec cmd: '" + execCreateConfig.getCmd() + "'");
    String execId = getDockerClient().createExec(containerId.get(), execCreateConfig).getContent().getId();

    ExecStartConfig execStartConfig = new ExecStartConfig(false, false, null);
    getDockerClient().startExec(execId, execStartConfig, null, null);
  }
}
