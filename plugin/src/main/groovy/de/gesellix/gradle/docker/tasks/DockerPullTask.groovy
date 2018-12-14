package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class DockerPullTask extends DockerTask {

    @Input
    def imageName
    @Input
    @Optional
    def tag
    @Input
    @Optional
    def registry

    def imageId

    DockerPullTask() {
        description = "Pull an image or a repository from a Docker registry server"
        group = "Docker"
    }

    @TaskAction
    def pull() {
        logger.info "docker pull"

        def query = [fromImage: getImageName(),
                     tag      : getTag()]
        if (getRegistry()) {
            query.fromImage = "${getRegistry()}/${getImageName()}".toString()
        }

        def options = [EncodedRegistryAuth: getAuthConfig()]

        def response = dockerClient.create(query, options)
        if (response.status.success) {
            imageId = dockerClient.findImageId(query.fromImage, query.tag)
        }
        else {
            imageId = null
        }
        return imageId
    }
}
