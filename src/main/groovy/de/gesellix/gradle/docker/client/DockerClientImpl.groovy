package de.gesellix.gradle.docker.client

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseDecorator
import org.apache.commons.io.IOUtils
import org.codehaus.groovy.runtime.MethodClosure
import org.mortbay.util.ajax.JSON
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerClientImpl implements DockerClient {

  private static Logger logger = LoggerFactory.getLogger(DockerClientImpl)

  def hostname
  def port
  def HTTPBuilder client

  DockerClientImpl(hostname = "172.17.42.1", port = 4243) {
    this.hostname = hostname
    this.port = port
    this.client = new HTTPBuilder("http://$hostname:$port/")
  }

  @Override
  def build(InputStream buildContext) {
    logger.info "build image..."
    def responseHandler = new ChunkedResponseHandler()
    client.handler.'200' = new MethodClosure(responseHandler, "handleResponse")
    client.post([path              : "/build",
                 body: IOUtils.toByteArray(buildContext),
                 requestContentType: ContentType.BINARY])

    def lastResponseDetail = responseHandler.lastResponseDetail
    logger.info "${lastResponseDetail}"
    return lastResponseDetail.stream.trim()
  }

  @Override
  def tag(imageId, repositoryName) {
    logger.info "tag image"
    client.post([path : "/images/${imageId}/tag".toString(),
                 query: [repo : repositoryName,
                         force: 0]]) { response ->
      logger.info "${response.statusLine}"
      return response.statusLine.statusCode
    }
  }

  @Override
  def push() {
    logger.info "push image"
  }

  @Override
  def pull(imageName) {
    logger.info "pull image '${imageName}'..."

    def responseHandler = new ChunkedResponseHandler()
    client.handler.'200' = new MethodClosure(responseHandler, "handleResponse")
    client.post([path : "/images/create",
                 query: [fromImage: imageName]])

    def lastResponseDetail = responseHandler.lastResponseDetail
    logger.info "${lastResponseDetail}"
    return lastResponseDetail.id
  }

  @Override
  def stop() {
    logger.info "stop container"
  }

  @Override
  def rm() {
    logger.info "rm container"
  }

  @Override
  def rmi() {
    logger.info "rm image"
  }

  @Override
  def run() {
    logger.info "run container"
  }

  @Override
  def ps() {
    logger.info "list containers"
  }

  @Override
  def images() {
    logger.info "list images"
  }

  static class ChunkedResponseHandler {

    def completeResponse = ""

    def handleResponse(HttpResponseDecorator response) {
      new InputStreamReader(response.entity.content).each { chunk ->
        logger.debug("received chunk: '${chunk}'")
        completeResponse += chunk
      }
    }

    def getLastResponseDetail() {
      logger.debug("find last detail in: '${completeResponse}'")
      def lastResponseDetail = completeResponse.substring(completeResponse.lastIndexOf("}{") + 1)
      return JSON.parse(lastResponseDetail)
    }
  }
}
