package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.client.EnvFileParser;
import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
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

  private final MapProperty<String, Object> containerConfiguration;

  @Input
  @Optional
  public MapProperty<String, Object> getContainerConfiguration() {
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
    containerConfiguration = objectFactory.mapProperty(String.class, Object.class);
    env = objectFactory.listProperty(String.class);
    environmentFiles = objectFactory.listProperty(File.class);
  }

  @TaskAction
  public EngineResponse create() {
    getLogger().info("docker create");

    Map<String, ?> containerConfig = getActualContainerConfig();
    Map<String, String> query = new HashMap<>(1);
    query.put("name", getContainerName().getOrElse(""));
    result = getDockerClient().createContainer(containerConfig, query);
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
  public Map<String, Object> getActualContainerConfig() {
    final Map<String, Object> containerConfig = new HashMap<>(getContainerConfiguration().getOrElse(new HashMap<>()));
    containerConfig.put("Image", getImageNameWithTag());
    containerConfig.putIfAbsent("HostConfig", new HashMap<>());
    Map<String, Object> hostConfig = (Map<String, Object>) containerConfig.get("HostConfig");
    if (!getEnvironmentFiles().get().isEmpty()) {
      containerConfig.putIfAbsent("Env", new ArrayList<>());
      final List<String> env = (List<String>) containerConfig.get("Env");
      getEnvironmentFiles().get().forEach((File file) -> {
        List<String> parsedEnv = envFileParser.parse(file);
        env.addAll(parsedEnv);
      });
    }
    if (!getEnv().get().isEmpty()) {
      containerConfig.putIfAbsent("Env", new ArrayList<>());
      final List<String> env = (List<String>) containerConfig.get("Env");
      env.addAll(getEnv().get());
    }

    if (!getPorts().get().isEmpty()) {
      containerConfig.putIfAbsent("ExposedPorts", new HashMap<>());
      final Map<String, Object> exposedPorts = (Map<String, Object>) containerConfig.get("ExposedPorts");
      hostConfig.putIfAbsent("PortBindings", new HashMap<>());
      final Map<String, Object> portBindings = (Map<String, Object>) hostConfig.get("PortBindings");
      getPorts().get().forEach((String portMapping) -> {
        // format: ip:hostPort:containerPort | ip::containerPort | hostPort:containerPort | containerPort
        final String[] splittedPortMapping = portMapping.split(":");
        if (splittedPortMapping.length != 2) {
          throw new UnsupportedOperationException("please use the plain `containerConfig.ExposedPorts and containerConfig.HostConfig.PortBindings` properties");
        }
        String hostPort = splittedPortMapping[0];
        String containerPort = splittedPortMapping[1] + "/tcp";
        exposedPorts.put(containerPort, new HashMap<>());

        Map<String, Object> hostBinding = new HashMap<>();
        hostBinding.put("HostIp", "0.0.0.0");
        hostBinding.put("HostPort", hostPort);
        portBindings.put(containerPort, Collections.singletonList(hostBinding));
      });
    }

    getLogger().info("effective container config: " + containerConfig);
    return containerConfig;
  }

  /**
   * @see #getImageName()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setImageName(String imageName) {
    this.imageName.set(imageName);
  }

  /**
   * @see #getImageTag()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setTag(String tag) {
    this.imageTag.set(tag);
  }

  /**
   * @see #getContainerName()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setContainerName(String containerName) {
    this.containerName.set(containerName);
  }

  /**
   * @see #getPorts()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setPorts(List<String> ports) {
    this.ports.set(ports);
  }

  /**
   * @see #getContainerConfiguration()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setContainerConfiguration(Map<String, Object> containerConfiguration) {
    this.containerConfiguration.set(containerConfiguration);
  }

  /**
   * @see #getEnv()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setEnv(List<String> env) {
    this.env.set(env);
  }

  /**
   * @see #getEnvironmentFiles()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setEnvironmentFiles(List<File> environmentFiles) {
    this.environmentFiles.set(environmentFiles);
  }
}
