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

package org.dslab.gradle.ecore.ext

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author Adrian Rumpold (a.rumpold@ds-lab.org)
 */
class EcoreExtension {
    private static final String DEFAULT_BUILD_SCRIPT = 'build.xml'
    private static final String DEFAULT_MODEL_PATH = 'model'
    private static final Path DEFAULT_OUTPUT_PATH = Paths.get('src', 'generated', 'java')
    private static final Path DEFAULT_RESOURCES_PATH = Paths.get('src', 'generated', 'resources')

    /**
     * The Eclipse binary to be invoked for code generation
     */
    final RegularFileProperty eclipsePath

    /**
     * The Ant build script used to invoke the Ecore2Java code generation task
     * <p/>
     * This file is generated by the {@link org.dslab.gradle.ecore.tasks.GenerateBuildDescriptorTask} task.
     */
    final RegularFileProperty buildScript

    /**
     * The location of the Eclipse workspace containing the modeling project
     */
    final DirectoryProperty workspacePath

    /**
     * The input directory containing the Ecore model(s)
     */
    final DirectoryProperty modelPath

    /**
     * The target directory for generated code
     */
    final DirectoryProperty outputDir

    /**
     * The target directory for generated resources
     */
    final DirectoryProperty resourcesDir

    /**
     * Should EMF compile-time dependencies be automatically added?
     */
    final Property<Boolean> addDependencies

    /**
     * Determine handling of genmodel updates
     */
    final Property<String> reconcileGenmodel

    /**
     * Clean the output directory before generating sources automatically
     */
    final Property<Boolean> autoclean

    /**
     * Should OSGi-related generated files be removed?
     */
    final Property<Boolean> removeOSGiDescriptors

    final Project project

    EcoreExtension(Project project) {
        this.project = project

        eclipsePath = project.layout.fileProperty()

        workspacePath = project.layout.directoryProperty()
        workspacePath.set(project.rootDir)

        buildScript = project.layout.fileProperty()
        buildScript.set(project.file(DEFAULT_BUILD_SCRIPT))

        modelPath = project.layout.directoryProperty()
        modelPath.set(project.file(DEFAULT_MODEL_PATH))

        outputDir = project.layout.directoryProperty()
        outputDir.set(project.file(DEFAULT_OUTPUT_PATH))

        resourcesDir = project.layout.directoryProperty()
        resourcesDir.set(project.file(DEFAULT_RESOURCES_PATH))

        addDependencies = project.objects.property(Boolean)
        addDependencies.set(true)

        reconcileGenmodel = project.objects.property(String)
        reconcileGenmodel.set("reload")

        autoclean = project.objects.property(Boolean)
        autoclean.set(true)

        removeOSGiDescriptors = project.objects.property(Boolean)
        removeOSGiDescriptors.set(true)
    }
}
