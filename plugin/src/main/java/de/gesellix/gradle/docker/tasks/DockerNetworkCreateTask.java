package de.gesellix.gradle.docker.tasks;

import java.util.Objects;

import de.gesellix.docker.client.EngineResponseContent;
import de.gesellix.docker.remote.api.NetworkCreateRequest;
import de.gesellix.docker.remote.api.NetworkCreateResponse;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerNetworkCreateTask extends GenericDockerTask {

  private final Property<String> networkName;

  @Input
  @Optional
  public Property<String> getNetworkName() {
    return networkName;
  }

  private final Property<NetworkCreateRequest> networkConfig;

  @Input
  @Optional
  public Property<NetworkCreateRequest> getNetworkConfig() {
    return networkConfig;
  }

  private EngineResponseContent<NetworkCreateResponse> response;

  @Internal
  public EngineResponseContent<NetworkCreateResponse> getResponse() {
    return response;
  }

  @Inject
  public DockerNetworkCreateTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Create a new network");

    networkName = objectFactory.property(String.class);
    networkConfig = objectFactory.property(NetworkCreateRequest.class);
  }

  @TaskAction
  public void createNetwork() {
    getLogger().info("docker network create");

    if (networkName.isPresent() && !networkConfig.isPresent()) {
      response = getDockerClient().createNetwork(networkName.get());
      return;
    }

    if (networkName.isPresent() && networkConfig.isPresent()) {
      NetworkCreateRequest networkCreateRequest = networkConfig.get();
      if (networkCreateRequest.getName() == null) {
        networkCreateRequest.setName(networkName.get());
      } else {
        if (!Objects.equals(networkCreateRequest.getName(), networkName.get())) {
          throw new IllegalArgumentException("NetworkName and NetworkConfig are mutually exclusive. Please specify only one of them or keep the network name consistent.");
        }
      }
      response = getDockerClient().createNetwork(networkCreateRequest);
    }

    if (!networkName.isPresent() && networkConfig.isPresent()) {
      response = getDockerClient().createNetwork(networkConfig.get());
    }

    if (!networkName.isPresent() && !networkConfig.isPresent()) {
      throw new IllegalArgumentException("Either NetworkName or NetworkConfig must be specified.");
    }
  }
}
