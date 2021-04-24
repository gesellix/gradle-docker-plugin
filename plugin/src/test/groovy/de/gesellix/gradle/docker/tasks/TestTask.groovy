package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class TestTask extends GenericDockerTask {

  @Inject
  TestTask(ObjectFactory objectFactory) {
    super(objectFactory)
  }

  @TaskAction
  def run() {
  }
}
