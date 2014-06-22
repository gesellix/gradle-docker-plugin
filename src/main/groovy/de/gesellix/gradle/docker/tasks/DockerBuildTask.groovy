package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static de.gesellix.docker.client.TarFileBuilder.archiveTarFilesRecursively

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

  @TaskAction
  def build() {
    logger.info "running build..."

    if (getBuildContextDirectory()) {
      // only one of buildContext and buildContextDirectory shall be provided
      assert !getBuildContext()

      def tarFile = archiveTarFilesRecursively(buildContextDirectory, "buildContext")
      buildContext = new FileInputStream(tarFile)
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
}
