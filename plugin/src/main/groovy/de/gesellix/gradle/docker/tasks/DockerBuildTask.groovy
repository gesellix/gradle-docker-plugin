package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.authentication.AuthConfig
import de.gesellix.docker.client.image.BuildConfig
import de.gesellix.gradle.docker.worker.BuildcontextArchiver
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

class DockerBuildTask extends GenericDockerTask {

  @Input
  @Optional
  Property<String> imageName

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getImageName()
   */
  @Deprecated
  void setImageName(String imageName) {
    this.imageName.set(imageName)
  }

  @InputDirectory
  @Optional
  DirectoryProperty buildContextDirectory

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getBuildContextDirectory()
   */
  @Deprecated
  void setBuildContextDirectory(File buildContextDirectory) {
    this.buildContextDirectory.set(buildContextDirectory)
  }

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getBuildContextDirectory()
   */
  @Deprecated
  void setBuildContextDirectory(String buildContextDirectory) {
    this.buildContextDirectory.set(project.file(buildContextDirectory))
  }

  @Input
  @Optional
  Property<InputStream> buildContext

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getBuildContext()
   */
  @Deprecated
  void setBuildContext(InputStream buildContext) {
    this.buildContext.set(buildContext)
  }

  @Input
  @Optional
  MapProperty<String, Object> buildParams

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getBuildParams()
   */
  @Deprecated
  void setBuildParams(Map<String, Object> buildParams) {
    this.buildParams.set(buildParams)
  }

  @Input
  @Optional
  MapProperty<String, Object> buildOptions

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getBuildOptions()
   */
  @Deprecated
  void setBuildOptions(Map<String, Object> buildOptions) {
    this.buildOptions.set(buildOptions)
  }

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
  MapProperty<String, AuthConfig> authConfigs

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getAuthConfigs()
   */
  @Deprecated
  void setAuthConfigs(Map<String, AuthConfig> authConfigs) {
    this.authConfigs.set(authConfigs)
  }

  @Input
  @Optional
  Property<Boolean> enableBuildLog

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getEnableBuildLog()
   */
  @Deprecated
  void setEnableBuildLog(boolean enableBuildLog) {
    this.enableBuildLog.set(enableBuildLog)
  }

  @Internal
  File targetFile

  @Internal
  String imageId

  @Internal
  WorkerExecutor workerExecutor

  @Inject
  DockerBuildTask(ObjectFactory objectFactory, WorkerExecutor workerExecutor) {
    super(objectFactory)
    this.workerExecutor = workerExecutor

    description = "Build an image from a Dockerfile"

    imageName = objectFactory.property(String)
    buildContextDirectory = objectFactory.directoryProperty()
    buildContext = objectFactory.property(InputStream)
    buildParams = objectFactory.mapProperty(String, Object)
    buildOptions = objectFactory.mapProperty(String, Object)
    authConfigs = objectFactory.mapProperty(String, AuthConfig)
    enableBuildLog = objectFactory.property(Boolean)
    enableBuildLog.convention(false)

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
  def build() {
    logger.info "docker build"

    InputStream actualBuildContext
    if (getBuildContextDirectory().isPresent()) {
      // only one of buildContext and buildContextDirectory shall be provided
      assert !getBuildContext().isPresent()
      actualBuildContext = createBuildContextFromDirectory()
    }
    else {
      actualBuildContext = getBuildContext().get()
    }
    // here we need the buildContext
    assert actualBuildContext

    // Add tag to build params
    def buildParams = new HashMap(getBuildParams().getOrElse([rm: true]))
    if (getImageName().isPresent()) {
      buildParams.putIfAbsent("rm", true)
      if (buildParams.t) {
        logger.warn "Overriding build parameter \"t\" with imageName as both were given"
      }
      buildParams.t = getImageName().get()
    }

    if (getAuthConfig().isPresent()) {
      logger.info("Docker Build requires a Map of AuthConfig by registry name. The configured 'authConfig' will be ignored." +
                  " Please use the 'authConfigs' (plural form) task parameter if you need to override the DockerClient's default behaviour.")
    }
    Map<String, Object> buildOptions = new HashMap<>(getBuildOptions().get())
    if (!buildOptions['EncodedRegistryConfig'] && getAuthConfigs().get() != [:]) {
      buildOptions['EncodedRegistryConfig'] = getDockerClient().encodeAuthConfigs(getAuthConfigs().get())
    }

    // TODO this one needs some beautification
    if (getEnableBuildLog().getOrElse(false)) {
      imageId = getDockerClient().buildWithLogs(actualBuildContext, new BuildConfig(query: buildParams, options: buildOptions)).imageId
    }
    else {
      imageId = getDockerClient().build(actualBuildContext, new BuildConfig(query: buildParams, options: buildOptions)).imageId
    }

    return imageId
  }

  @Internal
  def getNormalizedImageName() {
    if (!getImageName().isPresent()) {
      return UUID.randomUUID().toString()
    }
    return getImageName().get().replaceAll("\\W", "_")
  }

  InputStream createBuildContextFromDirectory() {
    targetFile = new File(getTemporaryDir(), "buildContext_${getNormalizedImageName()}.tar.gz")
//            outputs.file(targetFile.absolutePath)
//            outputs.upToDateWhen { false }
    workerExecutor.noIsolation().submit(BuildcontextArchiver) { parameters ->
      parameters.sourceDirectory.set(getBuildContextDirectory())
      parameters.archivedTargetFile.set(targetFile)
    }
    workerExecutor.await()

    logger.info "temporary buildContext: ${targetFile}"
    return new FileInputStream(targetFile as File)
  }
}
