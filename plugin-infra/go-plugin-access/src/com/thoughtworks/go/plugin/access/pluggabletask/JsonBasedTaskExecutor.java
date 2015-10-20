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

package com.thoughtworks.go.plugin.access.pluggabletask;

import com.thoughtworks.go.plugin.access.PluginInteractionCallback;
import com.thoughtworks.go.plugin.access.PluginRequestHelper;
import com.thoughtworks.go.plugin.api.response.execution.ExecutionResult;
import com.thoughtworks.go.plugin.api.task.*;
import com.thoughtworks.go.plugin.infra.Action;
import com.thoughtworks.go.plugin.infra.PluginManager;
import com.thoughtworks.go.plugin.infra.plugininfo.GoPluginDescriptor;
import com.thoughtworks.go.plugin.internal.api.LoggingService;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonBasedTaskExecutor implements TaskExecutor {
    private String pluginId;
    private PluginRequestHelper pluginRequestHelper;
    private HashMap<String, JsonBasedTaskExtensionHandler> handlerMap;

    public JsonBasedTaskExecutor(String pluginId, PluginRequestHelper pluginRequestHelper, HashMap<String, JsonBasedTaskExtensionHandler> handlerMap) {
        this.pluginId = pluginId;
        this.pluginRequestHelper = pluginRequestHelper;
        this.handlerMap = handlerMap;
    }

    @Override
    public ExecutionResult execute(final TaskConfig config, final TaskExecutionContext taskExecutionContext) {
        return pluginRequestHelper.submitRequest(pluginId, JsonBasedTaskExtension.EXECUTION_REQUEST, new PluginInteractionCallback<ExecutionResult>() {
            @Override
            public String requestBody(String resolvedExtensionVersion) {
                return handlerMap.get(resolvedExtensionVersion).getTaskExecutionBody(config, taskExecutionContext);
            }

            @Override
            public Map<String, String> requestParams(String resolvedExtensionVersion) {
                return null;
            }

            @Override
            public ExecutionResult onSuccess(String responseBody, String resolvedExtensionVersion) {
                return handlerMap.get(resolvedExtensionVersion).toExecutionResult(responseBody);
            }
        });
    }
}

