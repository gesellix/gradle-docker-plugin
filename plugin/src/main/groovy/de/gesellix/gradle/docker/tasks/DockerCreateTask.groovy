package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.EnvFileParser
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class DockerCreateTask extends GenericDockerTask {

    @Input
    def imageName
    @Input
    @Optional
    def tag = ""
    @Input
    @Optional
    def containerName = ""
    /**
     * Accepts a list of port mappings with the following pattern: `hostPort:containerPort`.
     * More sophisticated patterns are only supported via plain containerConfig.
     */
    @Input
    @Optional
    def ports = []
    @Input
    @Optional
    def containerConfiguration = [:]
    @Input
    @Optional
    def env = []
    @Input
    @Optional
    def environmentFiles = []

    def envFileParser = new EnvFileParser()

    def result

    DockerCreateTask() {
        description = "Create a new container"
        group = "Docker"
    }

    @TaskAction
    def create() {
        logger.info "docker create"
        def containerConfig = getActualContainerConfig()
        result = getDockerClient().createContainer(containerConfig, [name: getContainerName()])
        return result
    }

    def getActualContainerConfig() {

        def containerConfig = getContainerConfiguration() ?: [:]
        containerConfig.Image = getImageName() + (getTag() ? ":${getTag()}" : "")
        containerConfig.HostConfig = containerConfig.HostConfig ?: [:]
        if (getEnvironmentFiles()) {
            containerConfig.Env = containerConfig.Env ?: []
            getEnvironmentFiles().each { File file ->
                def parsedEnv = envFileParser.parse(file)
                containerConfig.Env.addAll(parsedEnv)
            }
            logger.info "effective container.env: ${containerConfig.Env}"
        }
        if (getEnv()) {
            containerConfig.Env = containerConfig.Env ?: []
            getEnv().each {
                containerConfig.Env += it
            }
        }
        if (getPorts()) {
            containerConfig.ExposedPorts = containerConfig.ExposedPorts ?: [:]
            containerConfig.HostConfig.PortBindings = containerConfig.HostConfig.PortBindings ?: [:]
            getPorts().each { String portMapping ->
                // format: ip:hostPort:containerPort | ip::containerPort | hostPort:containerPort | containerPort
                def splittedPortMapping = portMapping.split(":")
                if (splittedPortMapping.size() != 2) {
                    throw new UnsupportedOperationException("please use the plain `containerConfig.ExposedPorts and containerConfig.HostConfig.PortBindings` properties")
                }
                def hostPort = splittedPortMapping[0]
                def containerPort = "${splittedPortMapping[1]}/tcp".toString()
                containerConfig.ExposedPorts[containerPort] = [:]
                containerConfig.HostConfig.PortBindings[containerPort] = [[HostIp  : "0.0.0.0",
                                                                           HostPort: hostPort]]
            }
        }
        return containerConfig
    }
}
