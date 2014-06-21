package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerBuildTask extends AbstractDockerTask {

  private static Logger logger = LoggerFactory.getLogger(DockerBuildTask)

  @Input
  @Optional
  def imageName
  @Input
  def buildContext

  @TaskAction
  def build() {
    logger.info "running build..."
    def imageId = getDockerClient().build(getBuildContext())
    if (getImageName()) {
      logger.info "tag $imageId as '${getImageName()}'..."
      getDockerClient().tag(imageId, getImageName())
    }
    return imageId
  }
}
