package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerNetworkDisconnectTask extends GenericDockerTask {

  private final Property<String> networkName;

  @Input
  public Property<String> getNetworkName() {
    return networkName;
  }

  private final Property<String> containerName;

  @Input
  public Property<String> getContainerName() {
    return containerName;
  }

  private EngineResponse response;

  @Internal
  public EngineResponse getResponse() {
    return response;
  }

  @Inject
  public DockerNetworkDisconnectTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Disconnects container from a network");

    networkName = objectFactory.property(String.class);
    containerName = objectFactory.property(String.class);
  }

  @TaskAction
  public void disconnectNetwork() {
    getLogger().info("docker network disconnect");
    response = getDockerClient().disconnectNetwork(getNetworkName().get(), getContainerName().get());
  }

  /**
   * @see #getNetworkName()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setNetworkName(String networkName) {
    this.networkName.set(networkName);
  }

  /**
   * @see #getContainerName()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setContainerName(String containerName) {
    this.containerName.set(containerName);
  }
}
