package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.client.authentication.AuthConfig;
import de.gesellix.docker.client.image.BuildConfig;
import de.gesellix.gradle.docker.worker.BuildcontextArchiver;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DockerBuildTask extends GenericDockerTask {

  private final Property<String> imageName;

  @Input
  @Optional
  public Property<String> getImageName() {
    return imageName;
  }

  private final DirectoryProperty buildContextDirectory;

  @InputDirectory
  @Optional
  public DirectoryProperty getBuildContextDirectory() {
    return buildContextDirectory;
  }

  private final Property<InputStream> buildContext;

  @Input
  @Optional
  public Property<InputStream> getBuildContext() {
    return buildContext;
  }

  private final MapProperty<String, Object> buildParams;

  @Input
  @Optional
  public MapProperty<String, Object> getBuildParams() {
    return buildParams;
  }

  private final MapProperty<String, Object> buildOptions;

  @Input
  @Optional
  public MapProperty<String, Object> getBuildOptions() {
    return buildOptions;
  }

  private final MapProperty<String, AuthConfig> authConfigs;

  /**
   * A map of registry URL name to AuthConfig.
   *
   * Only the registry domain name (and port if not the default 443) are required.
   * However, for legacy reasons, the Docker Hub registry must be specified with both a https:// prefix and a /v1/ suffix even though Docker will prefer to use the v2 registry API.
   *
   * See https://docs.docker.com/engine/api/v1.40/#operation/ImageBuild for reference.
   */
  @Input
  @Optional
  public MapProperty<String, AuthConfig> getAuthConfigs() {
    return authConfigs;
  }

  private final Property<Boolean> enableBuildLog;

  @Input
  @Optional
  public Property<Boolean> getEnableBuildLog() {
    return enableBuildLog;
  }

  private String imageId;

  @Internal
  public String getImageId() {
    return imageId;
  }

  WorkerExecutor workerExecutor;
  File targetFile;

  @Inject
  public DockerBuildTask(ObjectFactory objectFactory, WorkerExecutor workerExecutor) {
    super(objectFactory);
    this.workerExecutor = workerExecutor;

    setDescription("Build an image from a Dockerfile");

    imageName = objectFactory.property(String.class);
    buildContextDirectory = objectFactory.directoryProperty();
    buildContext = objectFactory.property(InputStream.class);
    buildParams = objectFactory.mapProperty(String.class, Object.class);
    buildOptions = objectFactory.mapProperty(String.class, Object.class);
    authConfigs = objectFactory.mapProperty(String.class, AuthConfig.class);
    enableBuildLog = objectFactory.property(Boolean.class);
    enableBuildLog.convention(false);

//    addValidator(new TaskValidator() {
//      @Override
//      void validate(TaskInternal task, Collection<String> messages) {
//        if (getBuildContextDirectory() && getBuildContext()) {
//          messages.add("Please provide only one of buildContext and buildContextDirectory")
//        }
//        if (!getBuildContextDirectory() && !getBuildContext()) {
//          messages.add("Please provide either buildContext or buildContextDirectory")
//        }
//      }
//    })
  }

  @TaskAction
  public String build() {
    getLogger().info("docker build");

    InputStream actualBuildContext;
    if (getBuildContextDirectory().isPresent()) {
      if (getBuildContext().isPresent()) {
        throw new IllegalArgumentException("only one of buildContext and buildContextDirectory are allowed");
      }
      actualBuildContext = createBuildContextFromDirectory();
    }
    else {
      actualBuildContext = getBuildContext().get();
    }

    if (actualBuildContext == null) {
      throw new IllegalStateException("neither buildContext nor buildContextDirectory found");
    }

    // Add tag to build params
    Map<String, Object> buildParams = new HashMap<>(getBuildParams().getOrElse(new HashMap<>()));
    buildParams.putIfAbsent("rm", true);
    if (getImageName().isPresent()) {
      if (buildParams.containsKey("t")) {
        getLogger().warn("Overriding build parameter \"t\" with imageName, because both were given");
      }

      buildParams.put("t", getImageName().get());
    }

    if (getAuthConfig().isPresent()) {
      getLogger().info("Docker Build requires a Map of AuthConfig by registry name. The configured 'authConfig' will be ignored." +
                       " Please use the 'authConfigs' (plural form) task parameter if you need to override the DockerClient's default behaviour.");
    }

    Map<String, Object> buildOptions = new HashMap<>(getBuildOptions().get());
    if (!buildOptions.containsKey("EncodedRegistryConfig") && !getAuthConfigs().get().isEmpty()) {
      buildOptions.put("EncodedRegistryConfig", getDockerClient().encodeAuthConfigs(getAuthConfigs().get()));
    }

    BuildConfig config = new BuildConfig();
    config.setQuery(buildParams);
    config.setOptions(buildOptions);
    // TODO this one needs some beautification
    if (getEnableBuildLog().getOrElse(false)) {
      imageId = getDockerClient().buildWithLogs(actualBuildContext, config).getImageId();
    }
    else {
      imageId = getDockerClient().build(actualBuildContext, config).getImageId();
    }

    return imageId;
  }

  @Internal
  public String getNormalizedImageName() {
    if (!getImageName().isPresent()) {
      return UUID.randomUUID().toString();
    }

    return getImageName().get().replaceAll("\\W", "_");
  }

  public InputStream createBuildContextFromDirectory() {
    targetFile = new File(getTemporaryDir(), "buildContext_" + getNormalizedImageName() + ".tar.gz");
//            outputs.file(targetFile.absolutePath)
//            outputs.upToDateWhen { false }
    workerExecutor.noIsolation().submit(BuildcontextArchiver.class, parameters -> {
      parameters.getSourceDirectory().set(getBuildContextDirectory());
      parameters.getArchivedTargetFile().set(targetFile);
    });
    workerExecutor.await();

    getLogger().info("temporary buildContext: " + targetFile);
    try {
      return new FileInputStream(targetFile);
    }
    catch (FileNotFoundException e) {
      throw new RuntimeException("targetFile not found", e);
    }
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
   * @see #getBuildContextDirectory()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setBuildContextDirectory(File buildContextDirectory) {
    this.buildContextDirectory.set(buildContextDirectory);
  }

  /**
   * @see #getBuildContextDirectory()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setBuildContextDirectory(String buildContextDirectory) {
    this.buildContextDirectory.set(getProject().file(buildContextDirectory));
  }

  /**
   * @see #getBuildContext()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setBuildContext(InputStream buildContext) {
    this.buildContext.set(buildContext);
  }

  /**
   * @see #getBuildParams()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setBuildParams(Map<String, Object> buildParams) {
    this.buildParams.set(buildParams);
  }

  /**
   * @see #getBuildOptions()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setBuildOptions(Map<String, Object> buildOptions) {
    this.buildOptions.set(buildOptions);
  }

  /**
   * @see #getAuthConfigs()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setAuthConfigs(Map<String, AuthConfig> authConfigs) {
    this.authConfigs.set(authConfigs);
  }

  /**
   * @see #getEnableBuildLog()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setEnableBuildLog(boolean enableBuildLog) {
    this.enableBuildLog.set(enableBuildLog);
  }
}
