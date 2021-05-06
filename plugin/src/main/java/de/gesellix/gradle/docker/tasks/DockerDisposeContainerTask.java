package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.client.DockerClientException;
import de.gesellix.docker.engine.EngineResponse;
import de.gesellix.docker.engine.EngineResponseStatus;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class DockerDisposeContainerTask extends GenericDockerTask {

  private final Property<String> containerId;

  @Input
  public Property<String> getContainerId() {
    return containerId;
  }

  private final Property<Boolean> rmiParentImage;

  @Input
  @Optional
  public Property<Boolean> getRmiParentImage() {
    return rmiParentImage;
  }

  private final Property<Boolean> removeVolumes;

  @Input
  @Optional
  public Property<Boolean> getRemoveVolumes() {
    return removeVolumes;
  }

  @Inject
  public DockerDisposeContainerTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Stops and removes a container and optionally its parent image");

    containerId = objectFactory.property(String.class);
    rmiParentImage = objectFactory.property(Boolean.class);
    rmiParentImage.convention(false);
    removeVolumes = objectFactory.property(Boolean.class);
    removeVolumes.convention(false);
  }

  @TaskAction
  public void dispose() {
    getLogger().info("docker dispose");

    String containerId = getContainerId().get();
    EngineResponse containerDetails;
    try {
      containerDetails = getDockerClient().inspectContainer(containerId);
    }
    catch (DockerClientException e) {
      if (e.getDetail() instanceof EngineResponse) {
        final EngineResponse detail = (EngineResponse) e.getDetail();
        final EngineResponseStatus status = (detail == null ? null : detail.getStatus());
        if (status != null && status.getCode() == 404) {
          getLogger().info("couldn't dispose container because it doesn't exists");
          return;
        }
      }
      throw e;
    }

    getDockerClient().stop(containerId);
    getDockerClient().wait(containerId);
    Map<String, Integer> query = new HashMap<>(1);
    query.put("v", getRemoveVolumes().getOrElse(false) ? 1 : 0);
    getDockerClient().rm(containerId, query);
    if (getRmiParentImage().getOrElse(false)) {
      getDockerClient().rmi((String) ((Map<String, Object>) containerDetails.getContent()).get("Image"));
    }
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
   * @see #getRmiParentImage()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setRmiParentImage(boolean rmiParentImage) {
    this.rmiParentImage.set(rmiParentImage);
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
