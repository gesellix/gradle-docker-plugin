package de.gesellix.gradle.docker.tasks;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerServiceRmTask extends GenericDockerTask {

  private final Property<String> serviceName;

  @Input
  public Property<String> getServiceName() {
    return serviceName;
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
    getDockerClient().rmService(getServiceName().get());
  }
}
