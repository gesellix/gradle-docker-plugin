package de.gesellix.gradle.docker.worker;

import de.gesellix.docker.builder.BuildContextBuilder;
import org.gradle.workers.WorkAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public abstract class BuildcontextArchiver implements WorkAction<BuildcontextArchiverWorkParameters> {

  private static final Logger log = LoggerFactory.getLogger(BuildcontextArchiver.class);

  @Override
  public void execute() {
    final File sourceDirectory = getParameters().getSourceDirectory().getAsFile().get();
    final File targetFile = getParameters().getArchivedTargetFile().getAsFile().get();
    log.info("archiving " + sourceDirectory + " into " + targetFile + "...");
    targetFile.getParentFile().mkdirs();
    try {
      BuildContextBuilder.archiveTarFilesRecursively(sourceDirectory, targetFile);
    }
    catch (IOException e) {
      throw new RuntimeException("Archiving failed", e);
    }
    log.info("archiving finished");
  }
}
