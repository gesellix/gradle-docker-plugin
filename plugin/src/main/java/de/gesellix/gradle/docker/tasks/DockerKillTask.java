package de.gesellix.gradle.docker.tasks;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerKillTask extends GenericDockerTask {

  private final Property<String> containerId;

  @Input
  public Property<String> getContainerId() {
    return containerId;
  }

  @Inject
  public DockerKillTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Kill a running container");

    containerId = objectFactory.property(String.class);
  }

  @TaskAction
  public void kill() {
    getLogger().info("docker kill");
    getDockerClient().kill(getContainerId().get());
  }
}
