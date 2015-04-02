package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.BuildContextBuilder
import org.gradle.api.Task
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
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
  @Optional
  def buildContext
  @InputDirectory
  @Optional
  File buildContextDirectory

  def tarOfBuildcontextTask
  def targetFile

  def imageId

  DockerBuildTask() {
    description = "Build an image from a Dockerfile"
    group = "Docker"

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

  @Override
  Task configure(Closure closure) {
    def configureResult = super.configure(closure)
    if (getBuildContextDirectory()) {
      configureTarBuildContextTask()
      configureResult.getDependsOn().each { parentTaskDependency ->
        if (tarOfBuildcontextTask != parentTaskDependency) {
          tarOfBuildcontextTask.mustRunAfter parentTaskDependency
        }
      }
    }
    return configureResult
  }

  private def configureTarBuildContextTask() {
    if (tarOfBuildcontextTask == null) {
      targetFile = new File(getTemporaryDir(), "buildContext_${getNormalizedImageName()}.tar.gz")
      tarOfBuildcontextTask = project.task([group: getGroup()], "tarBuildcontextFor${name.capitalize()}").doLast {
        BuildContextBuilder.archiveTarFilesRecursively(getBuildContextDirectory(), targetFile)
      }
      tarOfBuildcontextTask.outputs.file(targetFile.absolutePath)
      tarOfBuildcontextTask.outputs.upToDateWhen { false }
      dependsOn tarOfBuildcontextTask
    }
  }

  @TaskAction
  def build() {
    logger.info "docker build"

    if (getBuildContextDirectory()) {
      // only one of buildContext and buildContextDirectory shall be provided
      assert !getBuildContext()

      assert tarOfBuildcontextTask
      logger.info "temporary buildContext: ${targetFile}"
      buildContext = new FileInputStream(targetFile as File)
    }

    // at this point we need the buildContext
    assert getBuildContext()

    imageId = getDockerClient().build(getBuildContext())
    if (getImageName()) {
      logger.info "tag $imageId as '${getImageName()}'..."
      getDockerClient().tag(imageId, getImageName(), true)
    }
    return imageId
  }

  def getNormalizedImageName() {
    if (!getImageName()) {
      return UUID.randomUUID().toString()
    }
    return getImageName().replaceAll("\\W", "_")
  }
}
