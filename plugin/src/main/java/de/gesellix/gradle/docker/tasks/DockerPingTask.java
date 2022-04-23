package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.client.EngineResponseContent;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerPingTask extends GenericDockerTask {

  private EngineResponseContent<String> result;

  @Internal
  public EngineResponseContent<String> getResult() {
    return result;
  }

  @Inject
  public DockerPingTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Ping the docker server");
  }

  @TaskAction
  public EngineResponseContent<String> ping() {
    getLogger().info("docker ping");
    return result = getDockerClient().ping();
  }
}
