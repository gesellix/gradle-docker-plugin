package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerServiceRmTask extends GenericDockerTask {

  private final Property<String> serviceName;

  @Input
  public Property<String> getServiceName() {
    return serviceName;
  }

  private EngineResponse response;

  @Internal
  public EngineResponse getResponse() {
    return response;
  }

  @Inject
  public DockerServiceRmTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Remove a service");

    serviceName = objectFactory.property(String.class);
  }

  @TaskAction
  public void rmService() {
    getLogger().info("docker service rm");
    response = getDockerClient().rmService(getServiceName().get());
  }

  /**
   * @see #getServiceName()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setServiceName(String serviceName) {
    this.serviceName.set(serviceName);
  }
}
