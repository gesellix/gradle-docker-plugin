package de.gesellix.gradle.docker.tasks;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;

@DisableCachingByDefault(because = "Delegate caching to Docker")
public class DockerNetworkConnectTask extends GenericDockerTask {

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

  @Inject
  public DockerNetworkConnectTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Connects a container to a network");

    networkName = objectFactory.property(String.class);
    containerName = objectFactory.property(String.class);
  }

  @TaskAction
  public void connectNetwork() {
    getLogger().info("docker network connect");
    getDockerClient().connectNetwork(getNetworkName().get(), getContainerName().get());
  }
}
