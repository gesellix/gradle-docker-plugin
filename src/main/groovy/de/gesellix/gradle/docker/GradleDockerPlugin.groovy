package de.gesellix.gradle.docker

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class GradleDockerPlugin implements Plugin<Project> {

  def Logger logger = LoggerFactory.getLogger(GradleDockerPlugin)

  @Override
  void apply(Project project) {
    logger.info("adding gradle-docker extension");
    def extension = project.extensions.create("gradle-docker", GradleDockerPluginExtension)

    logger.info("adding gradle-docker tasks");
    project.afterEvaluate {
      project.task("dockerDeploy", type: DockerDeployTask)
    }
  }
}
