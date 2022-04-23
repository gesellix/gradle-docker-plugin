package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.client.EngineResponseContent;
import de.gesellix.docker.remote.api.Network;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.List;

public class DockerNetworksTask extends GenericDockerTask {

  private EngineResponseContent<List<Network>> networks;

  @Internal
  public EngineResponseContent<List<Network>> getNetworks() {
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
