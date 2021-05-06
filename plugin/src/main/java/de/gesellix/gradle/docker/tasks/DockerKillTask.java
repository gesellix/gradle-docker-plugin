package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerKillTask extends GenericDockerTask {

  private final Property<String> containerId;

  @Input
  public Property<String> getContainerId() {
    return containerId;
  }

  private EngineResponse result;

  @Internal
  public EngineResponse getResult() {
    return result;
  }

  @Inject
  public DockerKillTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Kill a running container");

    containerId = objectFactory.property(String.class);
  }

  @TaskAction
  public EngineResponse kill() {
    getLogger().info("docker kill");
    result = getDockerClient().kill(getContainerId().get());
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
}
