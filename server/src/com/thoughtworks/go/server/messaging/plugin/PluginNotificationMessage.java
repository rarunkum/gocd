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

package com.thoughtworks.go.server.messaging.plugin;

import com.thoughtworks.go.server.messaging.GoMessage;

import java.util.Map;

public class PluginNotificationMessage implements GoMessage {
    private final String requestName;
    private final Map requestData;

    public PluginNotificationMessage(String requestName, Map requestData) {
        this.requestName = requestName;
        this.requestData = requestData;
    }

    public String getRequestName() {
        return requestName;
    }

    public Map getRequestData() {
        return requestData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PluginNotificationMessage that = (PluginNotificationMessage) o;

        if (requestData != null ? !requestData.equals(that.requestData) : that.requestData != null) return false;
        if (requestName != null ? !requestName.equals(that.requestName) : that.requestName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = requestName != null ? requestName.hashCode() : 0;
        result = 31 * result + (requestData != null ? requestData.hashCode() : 0);
        return result;
    }
}
