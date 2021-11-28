package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.remote.api.ContainerInspectResponse;
import de.gesellix.docker.remote.api.core.ClientException;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.net.HttpURLConnection;
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
    ContainerInspectResponse containerDetails;
    try {
      containerDetails = getDockerClient().inspectContainer(containerId).getContent();
    }
    catch (ClientException e) {
      if (e.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
        getLogger().info("couldn't dispose container because it doesn't exist");
        return;
      }
      throw e;
    }

    getDockerClient().stop(containerId);
    getDockerClient().wait(containerId);
    Map<String, Object> query = new HashMap<>(1);
    query.put("v", getRemoveVolumes().getOrElse(false) ? 1 : 0);
    getDockerClient().rm(containerId, query);
    if (getRmiParentImage().getOrElse(false)) {
      getDockerClient().rmi(containerDetails.getImage());
    }
  }
}
