package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.client.EngineResponseContent;
import de.gesellix.docker.remote.api.SystemInfo;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerInfoTask extends GenericDockerTask {

  private EngineResponseContent<SystemInfo> info;

  @Internal
  public EngineResponseContent<SystemInfo> getInfo() {
    return info;
  }

  @Inject
  public DockerInfoTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Display system-wide information");
  }

  @TaskAction
  public EngineResponseContent<SystemInfo> info() {
    getLogger().info("docker info");
    return info = getDockerClient().info();
  }
}
