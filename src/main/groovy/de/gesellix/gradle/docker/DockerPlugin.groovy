package de.gesellix.gradle.docker

import de.gesellix.gradle.docker.tasks.AbstractDockerTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class DockerPlugin implements Plugin<Project> {

  def Logger logger = LoggerFactory.getLogger(DockerPlugin)

  @Override
  void apply(Project project) {
//    project.plugins.apply(BasePlugin)

    logger.debug("adding docker extension");
    def extension = project.extensions.create("docker", DockerPluginExtension)

    project.tasks.withType(AbstractDockerTask) { task ->
      task.dockerHost = extension.dockerHost
      task.authConfigPlain = extension.authConfigPlain
      task.authConfigEncoded = extension.authConfigEncoded
    }

//    logger.info("adding gradle-docker tasks");
//    project.afterEvaluate {
//      project.task("dockerDeploy", type: DockerDeployTask)
//    }
  }
}
