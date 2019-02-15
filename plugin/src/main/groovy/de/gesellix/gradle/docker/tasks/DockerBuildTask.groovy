package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.image.BuildConfig
import de.gesellix.gradle.docker.worker.BuildcontextArchiver
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

class DockerBuildTask extends GenericDockerTask {

    def buildContextDirectory

    @Input
    @Optional
    def imageName

    @Input
    @Optional
    InputStream buildContext

    @InputDirectory
    @Optional
    File getBuildContextDirectory() {
        buildContextDirectory ? project.file(this.buildContextDirectory) : null
    }

    @Input
    @Optional
    def buildParams

    @Input
    @Optional
    def buildOptions

    @Input
    @Optional
    def enableBuildLog = false

    @Internal
    File targetFile

    @Internal
    def imageId

    @Internal
    WorkerExecutor workerExecutor

    @Inject
    DockerBuildTask(WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor

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

    @TaskAction
    def build() {
        logger.info "docker build"

        if (getBuildContextDirectory()) {
            // only one of buildContext and buildContextDirectory shall be provided
            assert !getBuildContext()
            buildContext = createBuildContextFromDirectory()
        }

        // at this point we need the buildContext
        assert getBuildContext()

        // Add tag to build params
        def buildParams = getBuildParams() ?: [rm: true]
        if (getImageName()) {
            buildParams.putIfAbsent("rm", true)
            if (buildParams.t) {
                logger.warn "Overriding build parameter \"t\" with imageName as both were given"
            }
            buildParams.t = getImageName() as String
        }

        def buildOptions = getBuildOptions() ?: [:]

        // TODO this one needs some beautification
        if (getEnableBuildLog()) {
            imageId = getDockerClient().buildWithLogs(getBuildContext(), new BuildConfig(query: buildParams, options: buildOptions)).imageId
        } else {
            imageId = getDockerClient().build(getBuildContext(), new BuildConfig(query: buildParams, options: buildOptions)).imageId
        }

        return imageId
    }

    @Internal
    def getNormalizedImageName() {
        if (!getImageName()) {
            return UUID.randomUUID().toString()
        }
        return getImageName().replaceAll("\\W", "_")
    }

    InputStream createBuildContextFromDirectory() {
        targetFile = new File(getTemporaryDir(), "buildContext_${getNormalizedImageName()}.tar.gz")
//            outputs.file(targetFile.absolutePath)
//            outputs.upToDateWhen { false }
        workerExecutor.submit(BuildcontextArchiver) { config ->
            config.isolationMode = IsolationMode.NONE
            config.params(getBuildContextDirectory(), targetFile)
        }
        workerExecutor.await()

        logger.info "temporary buildContext: ${targetFile}"
        return new FileInputStream(targetFile as File)
    }
}
