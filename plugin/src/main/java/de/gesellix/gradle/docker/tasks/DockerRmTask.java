package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class DockerRmTask extends GenericDockerTask {

  private final Property<String> containerId;

  @Input
  public Property<String> getContainerId() {
    return containerId;
  }

  private final Property<Boolean> removeVolumes;

  @Input
  @Optional
  public Property<Boolean> getRemoveVolumes() {
    return removeVolumes;
  }

  private EngineResponse result;

  @Internal
  public EngineResponse getResult() {
    return result;
  }

  @Inject
  public DockerRmTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Remove one or more containers");

    containerId = objectFactory.property(String.class);
    removeVolumes = objectFactory.property(Boolean.class);
    removeVolumes.convention(false);
  }

  @TaskAction
  public EngineResponse rm() {
    getLogger().info("docker rm");
    Map<String, Object> query = new HashMap<>(1);
    query.put("v", getRemoveVolumes().get() ? 1 : 0);
    result = getDockerClient().rm(getContainerId().get(), query);
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
   * @see #getRemoveVolumes()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setRemoveVolumes(boolean removeVolumes) {
    this.removeVolumes.set(removeVolumes);
  }
}
