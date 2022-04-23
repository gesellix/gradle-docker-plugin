package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.client.EngineResponseContent;
import de.gesellix.docker.remote.api.ContainerInspectResponse;
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

  private EngineResponseContent<ContainerInspectResponse> containerInfo;

  @Internal
  public EngineResponseContent<ContainerInspectResponse> getContainerInfo() {
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
}
