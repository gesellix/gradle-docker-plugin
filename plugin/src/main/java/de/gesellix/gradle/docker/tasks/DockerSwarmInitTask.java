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

public class DockerSwarmInitTask extends GenericDockerTask {

  private final MapProperty<String, Object> swarmconfig;

  @Input
  public MapProperty<String, Object> getSwarmconfig() {
    return swarmconfig;
  }

  private EngineResponse response;

  @Internal
  public EngineResponse getResponse() {
    return response;
  }

  @Inject
  public DockerSwarmInitTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Initialize a swarm");

    swarmconfig = objectFactory.mapProperty(String.class, Object.class);
  }

  @TaskAction
  public EngineResponse initSwarm() {
    getLogger().info("docker swarm init");

    response = getDockerClient().initSwarm(new HashMap<>(getSwarmconfig().get()));
    return response;
  }

  /**
   * @see #getSwarmconfig()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setSwarmconfig(Map<String, Object> swarmconfig) {
    this.swarmconfig.set(swarmconfig);
  }
}
