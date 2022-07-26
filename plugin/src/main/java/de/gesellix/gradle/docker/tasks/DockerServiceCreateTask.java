package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.client.EngineResponseContent;
import de.gesellix.docker.remote.api.ServiceCreateRequest;
import de.gesellix.docker.remote.api.ServiceCreateResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerServiceCreateTask extends GenericDockerTask {

  private final Property<ServiceCreateRequest> serviceConfig;

  @Input
  public Property<ServiceCreateRequest> getServiceConfig() {
    return serviceConfig;
  }

  private EngineResponseContent<ServiceCreateResponse> response;

  @Internal
  public EngineResponseContent<ServiceCreateResponse> getResponse() {
    return response;
  }

  @Inject
  public DockerServiceCreateTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Create a service");

    serviceConfig = objectFactory.property(ServiceCreateRequest.class);
  }

  @TaskAction
  public void createService() {
    getLogger().info("docker service create");
    response = getDockerClient().createService(getServiceConfig().get());
  }
}
