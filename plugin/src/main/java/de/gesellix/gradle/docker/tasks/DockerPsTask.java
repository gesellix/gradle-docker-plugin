package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerPsTask extends GenericDockerTask {

  private EngineResponse containers;

  @Internal
  public EngineResponse getContainers() {
    return containers;
  }

  @Inject
  public DockerPsTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("List containers");
  }

  @TaskAction
  public void ps() {
    getLogger().info("docker ps");
    containers = getDockerClient().ps();
  }
}
