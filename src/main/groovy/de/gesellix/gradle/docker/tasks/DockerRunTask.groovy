package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerRunTask extends AbstractDockerTask {

  private static Logger logger = LoggerFactory.getLogger(DockerRunTask)

  @Input
  def imageName

  // docker -H tcp://${targetHost} run -d -p 8889:8889 -p 9300:9300 --name $containerName ${imageName}
  @TaskAction
  def run() {
    logger.info "running run..."
    getDockerClient().run(getImageName(), ["ExposedPorts": [
        "8889/tcp": [],
        "9300/tcp": []
    ]])
  }
}
