package de.gesellix.gradle.docker.tasks;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerStopTask extends GenericDockerTask {

  private final Property<String> containerId;

  @Input
  public Property<String> getContainerId() {
    return containerId;
  }

  @Inject
  public DockerStopTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Stop a running container");

    containerId = objectFactory.property(String.class);
  }

  @TaskAction
  public void stop() {
    getLogger().info("docker stop");
    getDockerClient().stop(getContainerId().get());
  }
}
