/*
 * Copyright {yyyy} {name of copyright owner}
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

package org.dslab.gradle.ecore

import org.dslab.gradle.ecore.ext.EcoreExtension
import org.dslab.gradle.ecore.tasks.EmfCodegenTask
import org.dslab.gradle.ecore.tasks.GenerateBuildDescriptorTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

/**
 * @author Adrian Rumpold (a.rumpold@ds-lab.org)
 */
class EcorePlugin implements Plugin<Project> {
    static final String EXTENSION_NAME = 'ecore'
    private EcoreExtension extension
    private Project project

    @Override
    void apply(Project project) {
        this.project = project
        extension = project.extensions.create(EXTENSION_NAME, EcoreExtension, project)

        /* Set up plugin dependency to Java plugin */
        project.pluginManager.apply(org.gradle.api.plugins.JavaPlugin)

        /* Tasks for Ant build script generation */
        def buildDescriptorTask = project.tasks.create("buildDescriptor", GenerateBuildDescriptorTask) { task ->
            task.descriptor = extension.buildScript
        }

        /* Ecore2Java code generation tasks */
        def codegenTask = project.tasks.create("emfCodegen", EmfCodegenTask) { task ->
            // Ant build descriptor must be generated before code generation can run
            task.dependsOn buildDescriptorTask

            task.buildScript = extension.buildScript
            task.eclipseCommand = extension.eclipsePath
            task.modelDir = extension.modelPath
            task.outputDir = extension.outputDir
        }

        /* Integration with other Gradle tasks */
        // Java compilation depends on code generation
        project.tasks.withType(JavaCompile) {
            it.dependsOn codegenTask
        }

        project.sourceSets.main.java {
            srcDirs += extension.outputDir.get()
        }
    }
}
