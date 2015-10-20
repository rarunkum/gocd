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

package com.thoughtworks.go.server.util;

import java.util.Locale;

import com.jezhumble.javasysmon.JavaSysMon;
import com.thoughtworks.go.server.web.GoVelocityView;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ViewResolver;

/**
 * @understands the version of go server
 */
@Component
public class ServerVersion implements InitializingBean {

    private static final Logger LOG = Logger.getLogger(ServerVersion.class);

    private static String goVersion = null;
    private ViewResolver[] viewResolvers;
    private static final String NOT_APPLICABLE = "N/A";

    @Autowired
    public ServerVersion(ViewResolver ...viewResolvers) {
        this.viewResolvers = viewResolvers;
    }

    public String version() {
        if (goVersion == null) {
            try {
                if (viewResolvers.length != 0) {
                    for (ViewResolver viewResolver : viewResolvers) {
                        GoVelocityView view = (GoVelocityView) viewResolver.resolveViewName("admin/admin_version.txt", Locale.getDefault());
                        if (view != null) {
                            goVersion = view.getContentAsString();
                            break;
                        }
                    }
                } else {
                    goVersion = NOT_APPLICABLE;
                }
            } catch (Exception e) {
                LOG.error("Got an exception while computing the Go server version.", e);
                goVersion = NOT_APPLICABLE;
            }
        }
        return goVersion;
    }

    public void afterPropertiesSet() throws Exception {
        LOG.info(String.format("[Startup] Go Version: %s", version()));
        LOG.info(String.format("[Startup] PID: %s", new JavaSysMon().currentPid()));
        LOG.info(String.format("[Startup] JVM properties: %s", System.getProperties()));
        LOG.info(String.format("[Startup] Environment Variables: %s", System.getenv()));
    }

    @Deprecated
    /*
        *   Used in tests to purge cached go version
         */
    protected static void resetCachedGoVersion() {
        goVersion = null;
    }

}
