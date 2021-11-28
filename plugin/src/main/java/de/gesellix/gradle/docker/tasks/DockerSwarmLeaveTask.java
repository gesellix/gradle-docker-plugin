package de.gesellix.gradle.docker.tasks;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerSwarmLeaveTask extends GenericDockerTask {

  private final Property<Boolean> force;

  @Input
  @Optional
  public Property<Boolean> getForce() {
    return force;
  }

  @Inject
  public DockerSwarmLeaveTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Leave the swarm");

    force = objectFactory.property(Boolean.class);
  }

  @TaskAction
  public void leaveSwarm() {
    getLogger().info("docker swarm leave");
    getDockerClient().leaveSwarm(getForce().get());
  }
}
