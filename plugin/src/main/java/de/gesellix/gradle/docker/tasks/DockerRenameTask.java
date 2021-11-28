package de.gesellix.gradle.docker.tasks;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerRenameTask extends GenericDockerTask {

  private final Property<String> containerId;

  @Input
  public Property<String> getContainerId() {
    return containerId;
  }

  private final Property<String> newName;

  @Input
  public Property<String> getNewName() {
    return newName;
  }

  @Inject
  public DockerRenameTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Rename an existing container");

    containerId = objectFactory.property(String.class);
    newName = objectFactory.property(String.class);
  }

  @TaskAction
  public void rename() {
    getLogger().info("docker rename");
    getDockerClient().rename(getContainerId().get(), getNewName().get());
  }
}
