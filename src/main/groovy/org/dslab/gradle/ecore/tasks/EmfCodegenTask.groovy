/*
 * Copyright 2018 Adrian Rumpold
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dslab.gradle.ecore.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.*

/**
 * Generate Java sources from an Ecore model using a headless Eclipse instance.
 * <p/>
 * This task uses the Ant build script produced by the {@link GenerateBuildDescriptorTask} task
 * and invokes the Eclipse Ant runner in headless mode to generate the source files.
 *
 * @author Adrian Rumpold (a.rumpold@ds-lab.org)
 */
class EmfCodegenTask extends DefaultTask {
    private LogLevel level = LogLevel.DEBUG

    @Internal
    RegularFileProperty eclipseCommand = project.objects.newInputFile()

    @Internal
    DirectoryProperty workspacePath = project.objects.newInputDirectory()

    @InputDirectory
    @SkipWhenEmpty
    DirectoryProperty modelDir = project.objects.newInputDirectory()

    @InputFile
    RegularFileProperty buildScript = project.objects.newInputFile()

    @OutputDirectory
    DirectoryProperty outputDir = project.objects.newOutputDirectory()

    @OutputDirectory
    DirectoryProperty resourcesDir = project.objects.newOutputDirectory()

    @TaskAction
    void generate() {
        logger.log(level, "---- emfCodegen() ----")
        logger.log(level, "eclipseCommand: {}", eclipseCommand)
        logger.log(level, "modelDir: {}", modelDir)
        logger.log(level, "buildScript: {}", buildScript)
        logger.log(level, "outputDir: {}", outputDir)
        logger.log(level, "resourcesDir: {}", resourcesDir)

        assert eclipseCommand.asFile.getOrNull()?.canExecute()

        project.exec {
            workingDir project.projectDir
            commandLine "${eclipseCommand.get()}",
                    '-noSplash',
                    '-data', workspacePath.get(),
                    '-application', 'org.eclipse.ant.core.antRunner',
                    '-buildfile', buildScript.asFile.get()
        }
    }
}
