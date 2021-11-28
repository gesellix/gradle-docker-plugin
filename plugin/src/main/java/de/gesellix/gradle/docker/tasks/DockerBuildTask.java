package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.authentication.AuthConfig;
import de.gesellix.docker.remote.api.BuildInfo;
import de.gesellix.docker.remote.api.ImageID;
import de.gesellix.docker.remote.api.client.BuildInfoExtensionsKt;
import de.gesellix.docker.remote.api.core.Cancellable;
import de.gesellix.docker.remote.api.core.StreamCallback;
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
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

  public Duration buildTimeout = Duration.of(10, ChronoUnit.MINUTES);

  @Internal
  public Duration getBuildTimeout() {
    return buildTimeout;
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

    if (getAuthConfig().isPresent()) {
      getLogger().info("Docker Build requires a Map of AuthConfig by registry name. The configured 'authConfig' will be ignored." +
                       " Please use the 'authConfigs' (plural form) task parameter if you need to override the DockerClient's default behaviour.");
    }

    String encodedRegistryConfig = (String) getBuildOptions().getting("EncodedRegistryConfig").getOrNull();
    if (encodedRegistryConfig == null && !getAuthConfigs().get().isEmpty()) {
      encodedRegistryConfig = getDockerClient().encodeAuthConfigs(getAuthConfigs().get());
    }

    Map<String, Object> buildParams = new HashMap<>(getBuildParams().getOrElse(new HashMap<>()));
    String tag = (String) buildParams.getOrDefault("t", null);
    if (getImageName().isPresent()) {
      if (tag != null) {
        getLogger().warn("Overriding build parameter \"t\" with imageName, because both were given");
      }
      tag = getImageName().get();
    }

    List<BuildInfo> infos = new ArrayList<>();
    CountDownLatch buildFinished = new CountDownLatch(1);
    StreamCallback<BuildInfo> callback = new StreamCallback<BuildInfo>() {
      Cancellable cancellable;

      @Override
      public void onStarting(Cancellable cancellable) {
        this.cancellable = cancellable;
      }

      @Override
      public void onNext(BuildInfo element) {
        if (element != null) {
          if (getEnableBuildLog().getOrElse(false)) {
            getLogger().info(element.toString());
          }
        }
        infos.add(element);
      }

      @Override
      public void onFailed(Exception e) {
        getLogger().error("Build failed", e);
        buildFinished.countDown();
        cancellable.cancel();
      }

      @Override
      public void onFinished() {
        getLogger().info("Build finished");
        buildFinished.countDown();
      }
    };

    getDockerClient().build(
        callback,
        buildTimeout,
        (String) buildParams.getOrDefault("dockerfile", null),
        tag,
        null,
        null,
        null,
        (boolean) buildParams.getOrDefault("rm", true),
        null,
        null,
        encodedRegistryConfig,
        null,
        actualBuildContext);
    try {
      getLogger().debug("Waiting " + buildTimeout + " for the build to finish...");
      buildFinished.await(buildTimeout.toMillis(), TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException e) {
      getLogger().error("Build didn't finish before timeout of " + buildTimeout, e);
    }
    ImageID imageId = BuildInfoExtensionsKt.getImageId(infos);
    this.imageId = imageId == null ? null : imageId.getID();
    return this.imageId;
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
}
