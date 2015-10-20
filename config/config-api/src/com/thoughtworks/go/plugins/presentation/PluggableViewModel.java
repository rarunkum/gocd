/*************************GO-LICENSE-START*********************************
 * Copyright 2014 ThoughtWorks, Inc.
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
 *************************GO-LICENSE-END***********************************/

package com.thoughtworks.go.plugins.presentation;

import java.util.Map;

public interface PluggableViewModel<T> {
    String FACTORY_CONTEXT_KEY = "__FactoryContext__";
    String LOCAL_CONTEXT_KEY = "__LocalContext__";

    String getRenderingFramework();

    String getTemplatePath();

    Map<String, Object> getParameters();

    T getModel();

    void setModel(T model);

    String getTypeForDisplay();

    String getTaskType();
}
