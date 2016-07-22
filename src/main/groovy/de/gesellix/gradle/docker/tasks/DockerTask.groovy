package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.docker.client.config.DockerEnv
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.util.CollectionUtils

import static de.gesellix.gradle.docker.DockerPlugin.EXTENSION_NAME
import static java.net.Proxy.NO_PROXY

/**
 * The base class for all Docker-related tasks.
 */
class DockerTask extends DefaultTask {

    private Object dockerHost
    private Object certPath
    private Object proxy
    private Object authConfigPlain
    private Object authConfigEncoded

    /**
     * Sets a Docker host other than the one in the {@code docker} project extension
     *
     * @param host Any object can be converted to a string, including a closure that will resolve to a string.
     */
    void setDockerHost(def host) {
        this.dockerHost = host
    }

    /**
     * Obtains the docker host for the task instance. If a host was not set, the one that has been set in the
     * {@code docker} project extension will be returned.
     * @return A string containing the URL of the Docker host. (Could return the DockerClient's default if no project extension is defined).
     */
    @Input
    @Optional
    String getDockerHost() {
        def host = dockerHost ?: project.extensions.findByName(EXTENSION_NAME)?.dockerHost
        host ? CollectionUtils.stringize([host])[0] : new DockerEnv().dockerHost
    }

    /**
     * Sets a certificate path other than the one in the {@code docker} project extension.
     *
     * @param certPath Any object can be converted to a string, including a closure that will resolve to a string.
     */
    void setCertPath(def certPath) {
        this.certPath = certPath
    }

    /**
     * Obtains the certificate path for the task instance. If a certificate path was not set, the one that has been set
     * in the {@code docker} project extension will be returned.
     * @return The certificate path as an absolute path string. (Could return null if no project extension is defined).
     */
    @Input
    @Optional
    String getCertPath() {
        this.certPath ? project.file(this.certPath).absolutePath : project.extensions.findByName(EXTENSION_NAME)?.certPath
    }

    /**
     * Sets a proxy other than the one in the {@code docker} project extension.
     *
     * @param proxy A proxy object as understood by {@link de.gesellix.docker.client.DockerClientImpl}.
     */
    void setProxy(def proxy) {
        this.proxy = proxy
    }

    /**
     * Obtains the proxy to be used for this task. If the proxy was not set, the default from {@code docker}
     * project extension is returned.
     *
     * @return The proxy object or null if proxy has not been set in project extension (or if project extension is not
     * defined).
     */
    @Input
    @Optional
    def getProxy() {
        this.proxy ?: project.extensions.findByName(EXTENSION_NAME)?.proxy
    }

    /**
     * Sets a configuration object which will be encoded.
     * An auth configuration is an object with the structure described at
     * <a href="https://docs.docker.com/engine/reference/api/docker_remote_api/#authentication">docs.docker.com</a>.
     * {@link #setAuthConfigPlain} and {@link #setAuthConfigEncoded} are mutually exclusive. Both cannot be set on the
     * same task.
     *
     * @param authConfigPlain an object with the properties "username", "password", "email", and "serveraddress".
     *
     * @see #setAuthConfigEncoded(java.lang.Object)
     */
    void setAuthConfigPlain(def authConfigPlain) {
        this.authConfigPlain = authConfigPlain
    }

    /**
     * Obtains a configuration object that requires encoding. If this is not set, the default from {@code docker}
     * extension will be returned.
     *
     * @return Authentication in plain text
     */
    @Input
    @Optional
    def getAuthConfigPlain() {
        this.authConfigPlain ?: project.extensions.findByName(EXTENSION_NAME)?.authConfigPlain
    }

    /**
     * Sets a base64 encoded authentication object.
     * {@link #setAuthConfigPlain} and {@link #setAuthConfigEncoded} are mutually exclusive. Both cannot be set on the
     * same task.
     *
     * @param authConfigEncoded a base64 authentication object
     *
     * @see #setAuthConfigPlain(java.lang.Object)
     */
    void setAuthConfigEncoded(def authConfigEncoded) {
        this.authConfigEncoded = authConfigEncoded
    }

    /**
     * Obtains a configuration string that is already encoded. If this is not set, the default from {@code docker}
     * extension will be returned.
     *
     * @return Encoded authentication string
     */
    @Input
    @Optional
    def getAuthConfigEncoded() {
        this.authConfigEncoded ?: project.extensions.findByName(EXTENSION_NAME)?.authConfigEncoded
    }

    DockerClient dockerClient

    def getDockerClient() {
        if (!dockerClient) {
            if (getDockerHost() || getCertPath()) {
                def dockerEnv = new DockerEnv()
                if (getDockerHost()) {
                    dockerEnv.dockerHost = getDockerHost()
                }
                if (getCertPath()) {
                    dockerEnv.certPath = getCertPath()
                }
                dockerClient = new DockerClientImpl(
                        env: dockerEnv,
                        proxy: getProxy() ?: NO_PROXY)
            } else {
                dockerClient = new DockerClientImpl()
            }
        }
        dockerClient
    }

    /**
     * Returns an object that is suitable for authentication or an empty string if no authentication was set up.
     *
     * @return Authentication object
     */
    def getAuthConfig() {
        // NOTE: To keep behaviour from previous versions we need to access the fields directly.
        //       Once we know the field is set we can proceed as per normal as we'll obtain the
        //       same result. In the older versions values were pushed down to task instances from
        //       the settings in the extension. In order to be sure that we obtain the defaults
        //       from the extension we need to call the getters, which will return the extension values
        if (this.authConfigPlain) {
            assert !this.authConfigEncoded
            return getDockerClient().encodeAuthConfig(this.authConfigPlain)
        }
        if (this.authConfigEncoded) {
            return authConfigEncoded
        }
        if (getAuthConfigPlain()) {
            return getDockerClient().encodeAuthConfig(getAuthConfigPlain())
        }
        return getAuthConfigEncoded() ?: ''
    }
}
