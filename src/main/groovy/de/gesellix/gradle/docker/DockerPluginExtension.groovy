package de.gesellix.gradle.docker

import org.gradle.api.Project

/** Provides project extension class for configuring default values for all Docker tasks.
*/
class DockerPluginExtension {

    /** Docker host url. Will default to the system property {@code docker.host.url} if that is provided,
     * otherwise it will try to use the environment variable {@code DOCKER_HOST}. Failign that it will be
     * null
     *
     */
    String dockerHost = System.getProperty("docker.host.url") ?: System.env["DOCKER_HOST"]

    def proxy
    def authConfigPlain
    def authConfigEncoded

    DockerPluginExtension(Project project) {
      this.project = project
    }

    /** Allows for the setting of the path where certificates can be found.
     *
     * @param path ANy path object that can be resolved via {@code project.file()}
     */
    void setCertPath(Object path) {
        this.certPath = path
    }

    /** Returns the certificate path as an abolute path to the certificate.
     * If {@code certPath} was not configured it will attempt to check for system property
     * {@code docker.cert.path} first and then for the {@code DOCKER_CERT_PATH} environment variable.
     *
     * @return Absolute path as a string or null.
    */
    String getCertPath() {
      certPath ? project.file(certPath).absolutePath : null
    }

    private Object certPath = System.getProperty("docker.cert.path") ?: System.env["DOCKER_CERT_PATH"]
    private Project project
}
