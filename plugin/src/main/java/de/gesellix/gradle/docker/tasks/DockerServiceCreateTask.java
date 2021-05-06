package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class DockerServiceCreateTask extends GenericDockerTask {

  private final MapProperty<String, Object> serviceConfig;

  @Input
  public MapProperty<String, Object> getServiceConfig() {
    return serviceConfig;
  }

  private EngineResponse response;

  @Internal
  public EngineResponse getResponse() {
    return response;
  }

  @Inject
  public DockerServiceCreateTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Create a service");

    serviceConfig = objectFactory.mapProperty(String.class, Object.class);
  }

  @TaskAction
  public void createService() {
    getLogger().info("docker service create");
    response = getDockerClient().createService(new HashMap<>(getServiceConfig().get()));
  }

  /**
   * @see #getServiceConfig()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setServiceConfig(Map<String, Object> serviceConfig) {
    this.serviceConfig.set(serviceConfig);
  }
}
