package de.gesellix.gradle.docker.tasks

import org.gradle.api.Task
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class DockerPublishTask extends GenericDockerTask {

    def buildContextDirectory

    @Input
    @Optional
    def buildContext

    @InputDirectory
    @Optional
    File getBuildContextDirectory() {
        buildContextDirectory ? project.file(this.buildContextDirectory) : null
    }

    @Input
    def imageName

    @Input
    @Optional
    def imageTag

    @Internal
    String imageNameWithTag

    @Input
    @Optional
    def targetRegistries

    DockerPublishTask() {
        description = "builds and publishes an image"
        group = "Docker"
    }

    @Override
    Task configure(Closure closure) {
        def configureResult = super.configure(closure)

        imageNameWithTag = "${configureResult.getImageName()}"
        if (configureResult.getImageTag()) {
            imageNameWithTag += ":${configureResult.getImageTag()}"
        }

        def buildImageTask = configureBuildImageTask()
        configureResult.getDependsOn().each { parentTaskDependency ->
            if (buildImageTask != parentTaskDependency) {
                buildImageTask.mustRunAfter parentTaskDependency
            }
        }
        configureResult.dependsOn buildImageTask

        if ((getTargetRegistries() ?: [:]).isEmpty()) {
            logger.warn("No targetRegistries configured, image won't be pushed to any registry.")
        }
        getTargetRegistries().each { name, targetRegistry ->
            def pushTask = createUniquePushTask(name, targetRegistry, imageNameWithTag, getAuthConfig())
            pushTask.dependsOn buildImageTask
            configureResult.dependsOn pushTask
        }

        return configureResult
    }

    def createUniquePushTask(name, targetRegistry, imageNameWithTag, authConfig) {
        def pushTask = createUniqueTask(DockerPushTask, "pushImageTo${name.capitalize()}Internal").configure {
            repositoryName = imageNameWithTag
            registry = targetRegistry
            authConfigEncoded = authConfig
        }
        def rmiTask = createUniqueTask(DockerRmiTask, "rmi${name.capitalize()}Image").configure {
            imageId = "${targetRegistry}/${imageNameWithTag}"
        }
        pushTask.finalizedBy rmiTask
    }

    private def configureBuildImageTask() {
        def buildImageTaskName = "buildImageFor${this.name.capitalize()}"
        def buildImageTask = createUniqueTask(DockerBuildTask, buildImageTaskName).configure {
            imageName = imageNameWithTag
            buildContext = this.getBuildContext()
            buildContextDirectory = this.getBuildContextDirectory()
        }
        return buildImageTask
    }

    def createUniqueTask(taskType, String name) {
        def existingTasks = project.tasks.findAll {
            taskType.isInstance(it) && it.name == name
        }
        if (existingTasks.size() > 1) {
            throw new IllegalStateException("unique task expected, found ${existingTasks.size()}.")
        }

        if (existingTasks) {
            return existingTasks.first()
        }
        else {
            return project.task([type: taskType, group: getGroup()], name)
        }
    }

    @TaskAction
    def publish() {
        logger.info "running publish..."
    }
}
