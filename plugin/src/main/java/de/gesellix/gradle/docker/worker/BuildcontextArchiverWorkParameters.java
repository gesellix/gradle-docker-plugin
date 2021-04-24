package de.gesellix.gradle.docker.worker;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.workers.WorkParameters;

public interface BuildcontextArchiverWorkParameters extends WorkParameters {

  DirectoryProperty getSourceDirectory();

  RegularFileProperty getArchivedTargetFile();
}
