package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static de.gesellix.docker.client.BuildContextBuilder.archiveTarFilesRecursively

class DockerBuildTask extends AbstractDockerTask {

  private static Logger logger = LoggerFactory.getLogger(DockerBuildTask)

  @Input
  @Optional
  def imageName
  @Input
  @Optional
  def buildContext
  @InputDirectory
  @Optional
  File buildContextDirectory

  @OutputFile
  @Optional
  File temporaryBuildContext

  DockerBuildTask() {
    description = "builds an image from the given build context"

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
    logger.info "running build..."

    if (getBuildContextDirectory()) {
      // only one of buildContext and buildContextDirectory shall be provided
      assert !getBuildContext()

      temporaryBuildContext = createTemporaryBuildContext()
      archiveTarFilesRecursively(getBuildContextDirectory(), getTemporaryBuildContext())
      buildContext = new FileInputStream(getTemporaryBuildContext())
    }

    // at this point we need the buildContext
    assert getBuildContext()

    def imageId = getDockerClient().build(getBuildContext())
    if (getImageName()) {
      logger.info "tag $imageId as '${getImageName()}'..."
      getDockerClient().tag(imageId, getImageName())
    }
    return imageId
  }

  def createTemporaryBuildContext() {
    def temporaryBuildContext = new File(getTemporaryDir(), "buildContext_${getNormalizedImageName()}")
    project.buildDir.mkdirs()
    temporaryBuildContext.createNewFile()
    return temporaryBuildContext
  }

  def getNormalizedImageName() {
    if (!getImageName()) {
      return UUID.randomUUID().toString()
    }
    return getImageName().replaceAll("\\W", "_")
  }
}
