package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.client.EngineResponseContent;
import de.gesellix.docker.remote.api.ContainerSummary;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.List;

public class DockerPsTask extends GenericDockerTask {

  private EngineResponseContent<List<ContainerSummary>> containers;

  @Internal
  public EngineResponseContent<List<ContainerSummary>> getContainers() {
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
