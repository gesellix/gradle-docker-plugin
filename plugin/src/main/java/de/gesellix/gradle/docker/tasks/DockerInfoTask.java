package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerInfoTask extends GenericDockerTask {

  private EngineResponse info;

  @Internal
  public EngineResponse getInfo() {
    return info;
  }

  @Inject
  public DockerInfoTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Display system-wide information");
  }

  @TaskAction
  public EngineResponse info() {
    getLogger().info("docker info");
    return info = getDockerClient().info();
  }
}
