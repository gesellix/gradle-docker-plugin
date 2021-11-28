package de.gesellix.gradle.docker.tasks;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerPauseTask extends GenericDockerTask {

  private final Property<String> containerId;

  @Input
  public Property<String> getContainerId() {
    return containerId;
  }

  @Inject
  public DockerPauseTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Pause a running container");

    containerId = objectFactory.property(String.class);
  }

  @TaskAction
  public void pause() {
    getLogger().info("docker pause");
    getDockerClient().pause(getContainerId().get());
  }
}
