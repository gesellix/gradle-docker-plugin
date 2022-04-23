package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.client.EngineResponseContent;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class DockerPsTask extends GenericDockerTask {

  private EngineResponseContent<List<Map<String, Object>>> containers;

  @Internal
  public EngineResponseContent<List<Map<String, Object>>> getContainers() {
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
