/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.j2objc;

import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.util.JdtParser;

import com.google.devtools.j2objc.util.TimeTracker;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Reads source files and extracts header mappings between translated header files and associated
 * Java type declarations. This processor is invoked iff command line flags, "--header-mapping" and
 * "--preserve-full-paths", are both specified.
 *
 * Note that the reason we need to process the type information of all the Java source files
 * first, instead of doing it iteratively during the translation phase for each file, is that we may
 * have cross-referencing among the given Java source files in a single J2ObjC invocation.
 */
public class HeaderMappingPreProcessor extends FileProcessor {
  public HeaderMappingPreProcessor(JdtParser parser) {
    super(parser);
  }

  @Override
  protected void processSource(InputFile file, String source) {
    CompilationUnit unit = getParser().parse(file.getUnitName(), source);
    String headerRelativePath = getHeaderRelativePath(file.getUnitName());

    for (Object type : unit.types()) {
      Options.getHeaderMappings().put(
          getTypeQualifiedName(type, unit), headerRelativePath);
    }
  }

  @Override
  protected void processUnit(
      InputFile file, String source, CompilationUnit unit, TimeTracker ticker) {
  }

  private String getHeaderRelativePath(String path) {
    if (path.endsWith(".java")) {
      return path.substring(0, path.length() - 5) + ".h";
    } else {
      return path;
    }
  }

  private String getTypeQualifiedName(Object type, CompilationUnit unit) {
    AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) type;
    String packageName = unit.getPackage().getName().getFullyQualifiedName();
    if (!packageName.isEmpty()) {
      packageName += ".";
    }
    return packageName + typeDeclaration.getName().getFullyQualifiedName();
  }
}

