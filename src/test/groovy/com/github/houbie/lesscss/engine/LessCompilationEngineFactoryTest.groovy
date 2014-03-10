/*
 * Copyright (c) 2013 Houbrechts IT
 *
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

package com.github.houbie.lesscss.engine

import spock.lang.Specification

class LessCompilationEngineFactoryTest extends Specification {
    def "test create default engine"() {
        expect:
        LessCompilationEngineFactory.create() instanceof RhinoLessCompilationEngine
    }

    def "test create commandline engine"() {
        expect:
        LessCompilationEngineFactory.create('commandline', 'path/to/lessc').executable == 'path/to/lessc'
    }

}
