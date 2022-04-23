package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.client.EngineResponseContent;
import de.gesellix.docker.remote.api.SwarmInitRequest;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerSwarmInitTask extends GenericDockerTask {

  private final Property<SwarmInitRequest> swarmconfig;

  @Input
  public Property<SwarmInitRequest> getSwarmconfig() {
    return swarmconfig;
  }

  private EngineResponseContent<String> response;

  @Internal
  public EngineResponseContent<String> getResponse() {
    return response;
  }

  @Inject
  public DockerSwarmInitTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Initialize a swarm");

    swarmconfig = objectFactory.property(SwarmInitRequest.class);
  }

  @TaskAction
  public EngineResponseContent<String> initSwarm() {
    getLogger().info("docker swarm init");
    response = getDockerClient().initSwarm(getSwarmconfig().get());
    return response;
  }
}
