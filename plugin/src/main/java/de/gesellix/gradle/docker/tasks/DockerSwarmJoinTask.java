package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class DockerSwarmJoinTask extends GenericDockerTask {

  private final MapProperty<String, Object> config;

  @Input
  @Optional
  public MapProperty<String, Object> getConfig() {
    return config;
  }

  private EngineResponse response;

  @Internal
  public EngineResponse getResponse() {
    return response;
  }

  /**
   * @see #getConfig()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setConfig(Map<String, Object> config) {
    this.config.set(config);
  }

  @Inject
  public DockerSwarmJoinTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Join a swarm as a node and/or manager");

    config = objectFactory.mapProperty(String.class, Object.class);
  }

  @TaskAction
  public EngineResponse joinSwarm() {
    getLogger().info("docker swarm join");

    response = getDockerClient().joinSwarm(new HashMap<>(getConfig().get()));
    return response;
  }
}
