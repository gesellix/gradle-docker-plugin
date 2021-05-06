package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerNetworksTask extends GenericDockerTask {

  private EngineResponse networks;

  @Internal
  public EngineResponse getNetworks() {
    return networks;
  }

  @Inject
  public DockerNetworksTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Lists all networks");
  }

  @TaskAction
  public void networks() {
    getLogger().info("docker network ls");
    networks = getDockerClient().networks();
  }
}
