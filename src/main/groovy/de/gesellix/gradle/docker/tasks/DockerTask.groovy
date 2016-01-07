package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.docker.client.DockerConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

class DockerTask extends DefaultTask {

    @Input
    @Optional
    def dockerHost
    @Input
    @Optional
    def certPath
    @Input
    @Optional
    def proxy
    @Input
    @Optional
    def authConfigPlain
    @Input
    @Optional
    def authConfigEncoded

    DockerClient dockerClient

    def getDockerClient() {
        if (!dockerClient) {
            if (getDockerHost() || getCertPath()) {
                def config = new DockerConfig()
                if (getDockerHost()) {
                    config.dockerHost = getDockerHost()
                }
                if (getCertPath()) {
                    config.certPath = getCertPath()
                }
                dockerClient = new DockerClientImpl(
                        config: config,
                        proxy: getProxy() ?: Proxy.NO_PROXY)
            } else {
                dockerClient = new DockerClientImpl()
            }
        }
        dockerClient
    }

    def getAuthConfig() {
        if (getAuthConfigPlain()) {
            assert !getAuthConfigEncoded()
            return getDockerClient().encodeAuthConfig(getAuthConfigPlain())
        }
        if (getAuthConfigEncoded()) {
            return getAuthConfigEncoded()
        }
        ''
    }
}
