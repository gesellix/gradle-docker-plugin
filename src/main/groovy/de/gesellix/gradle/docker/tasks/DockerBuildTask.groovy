package de.gesellix.gradle.docker.tasks

import org.gradle.api.Task
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Tar
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.gradle.api.tasks.bundling.Compression.GZIP

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
      tarOfBuildcontextTask = project.task([type: Tar, group: getGroup()], "tarBuildcontextFor${name.capitalize()}") {
        description = "creates a tar of the buildcontext"
        from getBuildContextDirectory()
        compression = GZIP
        baseName = "buildContext_${getNormalizedImageName()}"
        destinationDir getTemporaryDir()
      }
      tarOfBuildcontextTask.exclude {
        it.file == tarOfBuildcontextTask.archivePath
      }
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
      logger.info "temporary buildContext: ${tarOfBuildcontextTask.archivePath}"
      buildContext = new FileInputStream(tarOfBuildcontextTask.archivePath as File)
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
