package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DockerNetworkCreateTask extends GenericDockerTask {

  private final Property<String> networkName;

  @Input
  public Property<String> getNetworkName() {
    return networkName;
  }

  private final MapProperty<String, Object> networkConfig;

  @Input
  public MapProperty<String, Object> getNetworkConfig() {
    return networkConfig;
  }

  private EngineResponse response;

  @Internal
  public EngineResponse getResponse() {
    return response;
  }

  @Inject
  public DockerNetworkCreateTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Create a new network");

    networkName = objectFactory.property(String.class);
    networkConfig = objectFactory.mapProperty(String.class, Object.class);
  }

  @TaskAction
  public void createNetwork() {
    getLogger().info("docker network create");
    response = getDockerClient().createNetwork(getNetworkName().get(), new HashMap<>(getNetworkConfig().getOrElse(new LinkedHashMap<String, Object>())));
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
   * @see #getNetworkConfig()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setNetworkConfig(Map<String, Object> networkConfig) {
    this.networkConfig.set(networkConfig);
  }
}
