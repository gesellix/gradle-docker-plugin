package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerServiceRmTask extends DockerTask {

    @Input
    def serviceName = [:]

    def response

    DockerServiceRmTask() {
        description = "Remove a service"
        group = "Docker"
    }

    @TaskAction
    def rmService() {
        logger.info "docker service rm"
        response = getDockerClient().rmService(getServiceName())
    }
}
