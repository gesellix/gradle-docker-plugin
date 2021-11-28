package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.client.EnvFileParser;
import de.gesellix.docker.engine.EngineResponse;
import de.gesellix.docker.remote.api.ContainerCreateRequest;
import de.gesellix.docker.remote.api.HostConfig;
import de.gesellix.docker.remote.api.PortBinding;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DockerCreateTask extends GenericDockerTask {

  private final Property<String> imageName;

  @Input
  public Property<String> getImageName() {
    return imageName;
  }

  private final Property<String> imageTag;

  @Input
  @Optional
  public Property<String> getImageTag() {
    return imageTag;
  }

  private final Property<String> containerName;

  @Input
  @Optional
  public Property<String> getContainerName() {
    return containerName;
  }

  private final ListProperty<String> ports;

  /**
   * Accepts a list of port mappings with the following pattern: `hostPort:containerPort`.
   * More sophisticated patterns are only supported via plain containerConfig.
   */
  @Input
  @Optional
  public ListProperty<String> getPorts() {
    return ports;
  }

  private final Property<ContainerCreateRequest> containerConfiguration;

  @Input
  @Optional
  public Property<ContainerCreateRequest> getContainerConfiguration() {
    return containerConfiguration;
  }

  private final ListProperty<String> env;

  @Input
  @Optional
  public ListProperty<String> getEnv() {
    return env;
  }

  private final ListProperty<File> environmentFiles;

  @Input
  @Optional
  public ListProperty<File> getEnvironmentFiles() {
    return environmentFiles;
  }

  private EngineResponse result;

  @Internal
  public EngineResponse getResult() {
    return result;
  }

  private final EnvFileParser envFileParser = new EnvFileParser();

  @Inject
  public DockerCreateTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Create a new container");

    imageName = objectFactory.property(String.class);
    imageTag = objectFactory.property(String.class);
    imageTag.convention("");
    containerName = objectFactory.property(String.class);
    containerName.convention("");
    ports = objectFactory.listProperty(String.class);
    containerConfiguration = objectFactory.property(ContainerCreateRequest.class);
    containerConfiguration.convention(new ContainerCreateRequest());
    env = objectFactory.listProperty(String.class);
    environmentFiles = objectFactory.listProperty(File.class);
  }

  @TaskAction
  public EngineResponse create() {
    getLogger().info("docker create");

    ContainerCreateRequest containerConfig = getActualContainerConfig();
    result = getDockerClient().createContainer(containerConfig, getContainerName().getOrElse(""), getEncodedAuthConfig());
    return result;
  }

  private String getImageNameWithTag() {
    if (getImageTag().isPresent() && !getImageTag().get().isEmpty()) {
      return getImageName().get() + ":" + getImageTag().get();
    }
    else {
      return getImageName().get();
    }
  }

  @Internal
  public ContainerCreateRequest getActualContainerConfig() {
    ContainerCreateRequest containerCreateRequest = getContainerConfiguration().getOrElse(new ContainerCreateRequest());
    if (containerCreateRequest.getHostConfig() == null) {
      containerCreateRequest.setHostConfig(new HostConfig());
    }

    containerCreateRequest.setImage(getImageNameWithTag());
    if (!getEnvironmentFiles().get().isEmpty()) {
      if (containerCreateRequest.getEnv() == null) {
        containerCreateRequest.setEnv(new ArrayList<>());
      }
      List<String> env = containerCreateRequest.getEnv();
      getEnvironmentFiles().get().forEach((File file) -> {
        List<String> parsedEnv = envFileParser.parse(file);
        env.addAll(parsedEnv);
      });
    }
    if (!getEnv().get().isEmpty()) {
      if (containerCreateRequest.getEnv() == null) {
        containerCreateRequest.setEnv(new ArrayList<>());
      }
      List<String> env = containerCreateRequest.getEnv();
      env.addAll(getEnv().get());
    }

    if (!getPorts().get().isEmpty()) {
      if (containerCreateRequest.getExposedPorts() == null) {
        containerCreateRequest.setExposedPorts(new HashMap<>());
      }
      final Map<String, Object> exposedPorts = containerCreateRequest.getExposedPorts();
      if (containerCreateRequest.getHostConfig().getPortBindings() == null) {
        containerCreateRequest.getHostConfig().setPortBindings(new HashMap<>());
      }
      final Map<String, List<PortBinding>> portBindings = containerCreateRequest.getHostConfig().getPortBindings();
      getPorts().get().forEach((String portMapping) -> {
        // format: ip:hostPort:containerPort | ip::containerPort | hostPort:containerPort | containerPort
        final String[] splittedPortMapping = portMapping.split(":");
        if (splittedPortMapping.length != 2) {
          throw new UnsupportedOperationException("please use the plain `containerConfig.ExposedPorts and containerConfig.HostConfig.PortBindings` properties");
        }
        String hostPort = splittedPortMapping[0];
        String containerPort = splittedPortMapping[1] + "/tcp";
        exposedPorts.put(containerPort, new HashMap<>());

        PortBinding hostBinding = new PortBinding("0.0.0.0", hostPort);
        portBindings.put(containerPort, Collections.singletonList(hostBinding));
      });
    }

    getLogger().info("effective container config: " + containerCreateRequest);
    return containerCreateRequest;
  }
}
