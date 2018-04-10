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

package org.dslab.gradle.ecore

import org.dslab.gradle.ecore.ext.EcoreExtension
import org.dslab.gradle.ecore.tasks.EmfCodegenTask
import org.dslab.gradle.ecore.tasks.GenerateBuildDescriptorTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Provides code generation from Ecore models and integration with non-generated Java sources.
 *
 * @author Adrian Rumpold (a.rumpold@ds-lab.org)
 */
class EcorePlugin implements Plugin<Project> {
    static final String EXTENSION_NAME = 'ecore'
    static final String TASK_GROUP = "EMF Ecore code generation tasks"

    private EcoreExtension extension
    private Project project

    @Override
    void apply(Project project) {
        this.project = project
        extension = project.extensions.create(EXTENSION_NAME, EcoreExtension, project)

        /* Set up plugin dependency to Java plugin */
        if (!project.plugins.findPlugin(org.gradle.api.plugins.JavaPlugin)) {
            project.pluginManager.apply(org.gradle.api.plugins.JavaPlugin)
        }

        /* Tasks for Ant build script generation */
        def buildDescriptorTask = project.tasks.create("buildDescriptor", GenerateBuildDescriptorTask) { task ->
            task.descriptor = extension.buildScript
            task.modelPath = extension.modelPath
            task.reconcileGenmodel = extension.reconcileGenmodel
            task.outputDir = extension.outputDir
        }
        buildDescriptorTask.group = TASK_GROUP
        buildDescriptorTask.description = "Generate an Ant build script for Ecore2Java code generation"

        /* Ecore2Java code generation tasks */
        def codegenTask = project.tasks.create("emfCodegen", EmfCodegenTask) { task ->
            // Ant build descriptor must be generated before code generation can run
            task.dependsOn buildDescriptorTask

            task.buildScript = extension.buildScript
            task.eclipseCommand = extension.eclipsePath
            task.workspacePath = extension.workspacePath
            task.modelDir = extension.modelPath
            task.outputDir = extension.outputDir
            task.resourcesDir = extension.resourcesDir
        }
        codegenTask.group = TASK_GROUP
        codegenTask.description = "Run the Ecore2Java code generation using a headless Eclipse instance"

        /* Integration with other Gradle tasks */
        // Generated sources are treated as a separate Java source set
        project.sourceSets.create("generated", {
            java.srcDirs = [extension.outputDir.get()]
        })
        project.tasks['compileGeneratedJava'].dependsOn codegenTask

        // Java compilation depends on the generated code
        project.dependencies {
            implementation project.sourceSets.generated.output
        }

        project.afterEvaluate {
            // Optionally add EMF compilation dependencies
            if (extension.addDependencies.get()) {
                project.dependencies.add("generatedImplementation", "org.eclipse.emf:org.eclipse.emf.common:2.12.0")
                project.dependencies.add("generatedImplementation", "org.eclipse.emf:org.eclipse.emf.ecore:2.12.0")
                project.dependencies.add("generatedImplementation", "org.eclipse.emf:org.eclipse.emf.ecore.xmi:2.12.0")
            }

            // Generated classes must be bundled with the resulting Jar
            project.jar {
                from project.sourceSets.main.output
                from project.sourceSets.generated.output
            }

            // Clean up includes removing the build descriptor and generated sources
            project.tasks['clean'].dependsOn 'cleanEmfCodegen', 'cleanBuildDescriptor'
            if (extension.autoclean.get()) {
                project.logger.info("Cleaning up output folder before code generation")
                // Clean up the output directory before generating sources to prevent problems with renamed/deleted model elements
                project.tasks['emfCodegen'].dependsOn project.tasks['cleanEmfCodegen']
            }
        }
    }
}
