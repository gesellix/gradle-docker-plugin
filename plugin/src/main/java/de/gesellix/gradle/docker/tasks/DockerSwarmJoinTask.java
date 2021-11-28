package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.remote.api.SwarmJoinRequest;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerSwarmJoinTask extends GenericDockerTask {

  private final Property<SwarmJoinRequest> config;

  @Input
  @Optional
  public Property<SwarmJoinRequest> getConfig() {
    return config;
  }

  @Inject
  public DockerSwarmJoinTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Join a swarm as a node and/or manager");

    config = objectFactory.property(SwarmJoinRequest.class);
  }

  @TaskAction
  public void joinSwarm() {
    getLogger().info("docker swarm join");
    getDockerClient().joinSwarm(getConfig().get());
  }
}
