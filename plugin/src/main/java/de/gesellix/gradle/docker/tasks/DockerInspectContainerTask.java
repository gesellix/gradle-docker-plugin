package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerInspectContainerTask extends GenericDockerTask {

  private final Property<String> containerId;

  @Input
  public Property<String> getContainerId() {
    return containerId;
  }

  private EngineResponse containerInfo;

  @Internal
  public EngineResponse getContainerInfo() {
    return containerInfo;
  }

  @Inject
  public DockerInspectContainerTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Return low-level information on a container");

    containerId = objectFactory.property(String.class);
  }

  @TaskAction
  public void inspect() {
    getLogger().info("docker inspect");

    containerInfo = getDockerClient().inspectContainer(getContainerId().get());
  }

  /**
   * @see #getContainerId()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setContainerId(String containerId) {
    this.containerId.set(containerId);
  }
}
