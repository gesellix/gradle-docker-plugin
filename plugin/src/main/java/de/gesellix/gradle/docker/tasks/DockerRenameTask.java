package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
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

  private EngineResponse result;

  @Internal
  public EngineResponse getResult() {
    return result;
  }

  @Inject
  public DockerRenameTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Rename an existing container");

    containerId = objectFactory.property(String.class);
    newName = objectFactory.property(String.class);
  }

  @TaskAction
  public EngineResponse rename() {
    getLogger().info("docker rename");
    result = getDockerClient().rename(getContainerId().get(), getNewName().get());
    return result;
  }

  /**
   * @see #getContainerId()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setContainerId(String containerId) {
    this.containerId.set(containerId);
  }

  /**
   * @see #getNewName()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setNewName(String newName) {
    this.newName.set(newName);
  }
}
