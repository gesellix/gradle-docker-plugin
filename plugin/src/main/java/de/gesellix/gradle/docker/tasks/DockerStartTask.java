package de.gesellix.gradle.docker.tasks;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerStartTask extends GenericDockerTask {

  private final Property<String> containerId;

  @Input
  public Property<String> getContainerId() {
    return containerId;
  }

  @Inject
  public DockerStartTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Start a stopped container");

    containerId = objectFactory.property(String.class);
  }

  @TaskAction
  public void start() {
    getLogger().info("docker start");
    getDockerClient().startContainer(getContainerId().get());
  }
}
