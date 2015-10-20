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

package com.thoughtworks.go.server.controller;

import com.thoughtworks.go.config.CaseInsensitiveString;
import com.thoughtworks.go.config.PipelineConfig;
import com.thoughtworks.go.config.PipelineNotFoundException;
import com.thoughtworks.go.server.controller.actions.JsonAction;
import com.thoughtworks.go.server.domain.Username;
import com.thoughtworks.go.server.scheduling.ScheduleOptions;
import com.thoughtworks.go.server.service.*;
import com.thoughtworks.go.server.service.result.ServerHealthStateOperationResult;
import com.thoughtworks.go.server.util.ErrorHandler;
import com.thoughtworks.go.server.util.UserHelper;
import com.thoughtworks.go.util.json.JsonMap;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.HashMap;

import static com.thoughtworks.go.server.controller.actions.JsonAction.*;
import static com.thoughtworks.go.util.GoConstants.ERROR_FOR_JSON;
import static com.thoughtworks.go.util.json.JsonHelper.addFriendlyErrorMessage;
import static org.apache.commons.lang.StringUtils.isEmpty;

@Controller
public class PipelineStatusController {
    private static final Logger LOGGER = Logger.getLogger(PipelineStatusController.class);

    private static final String FORCE_URI = "/force";

    private final GoConfigService goConfigService;
    private final PipelineScheduler buildCauseProducer;
    private final SecurityService securityService;
    private final JsonCurrentActivityService jsonCurrentActivityService;
    private PipelinePauseService pipelinePauseService;

    @Autowired
    public PipelineStatusController(GoConfigService goConfigService,
                                    PipelineScheduler buildCauseProducer,
                                    SecurityService securityService,
                                    JsonCurrentActivityService jsonCurrentActivityService,
                                    PipelinePauseService pipelinePauseService) {
        this.goConfigService = goConfigService;
        this.buildCauseProducer = buildCauseProducer;
        this.securityService = securityService;
        this.jsonCurrentActivityService = jsonCurrentActivityService;
        this.pipelinePauseService = pipelinePauseService;
    }

    @RequestMapping(value = FORCE_URI, method = RequestMethod.POST)
    public ModelAndView triggerPipeline(@RequestParam(value = "pipelineName", required = true)String pipelineName,
                                        HttpServletResponse response) throws Exception {
        Username username = UserHelper.getUserName();
        PipelineConfig pipelineConfig = goConfigService.pipelineConfigNamed(new CaseInsensitiveString(pipelineName));
        boolean isAuthorized = securityService.hasOperatePermissionForStage(CaseInsensitiveString.str(pipelineConfig.name()), CaseInsensitiveString.str(pipelineConfig.first().name()),
                CaseInsensitiveString.str(username.getUsername()));
        if (isAuthorized) {
            LOGGER.debug("start producing manual build cause");
            ServerHealthStateOperationResult result = new ServerHealthStateOperationResult();
            final HashMap<String, String> revisions = new HashMap<String, String>();
            final HashMap<String, String> environmentVariables = new HashMap<String, String>();
            final HashMap<String, String> secureEnvironmentVariables = new HashMap<String, String>();
            buildCauseProducer.manualProduceBuildCauseAndSave(pipelineName, username, new ScheduleOptions(revisions, environmentVariables, secureEnvironmentVariables), result);
            return JsonAction.from(result.getServerHealthState()).respond(response);
        } else {
            LOGGER.error(username + " is not authorized to force this pipeline");
            return JsonAction.jsonUnauthorized().respond(response);
        }
    }

    @RequestMapping(value = "/**/pipelineStatus.json", method = RequestMethod.GET)
    public ModelAndView list(@RequestParam(value = "pipelineName", required = false)String pipelineName,
                             @RequestParam(value = "useCache", required = false)Boolean useCache,
                             HttpServletResponse response, HttpServletRequest request) throws NamingException {
        String username = CaseInsensitiveString.str(UserHelper.getUserName().getUsername());

        JsonMap json = new JsonMap();
        try {
            json.put("pipelines", jsonCurrentActivityService.pipelinesActivityAsJson(username, pipelineName));
            return jsonFound(json).respond(response);
        } catch (PipelineNotFoundException e) {
            JsonMap jsonLog = new JsonMap();
            jsonLog.put(ERROR_FOR_JSON, e.getMessage());
            return jsonNotFound(jsonLog).respond(response);
        }
    }

    @ErrorHandler
    public ModelAndView handleError(HttpServletRequest request, HttpServletResponse response, Exception e) {
        LOGGER.error("Error happened", e);
        JsonMap json = new JsonMap();
        String pipelineName = request.getParameter("pipelineName");
        if (isEmpty(pipelineName) && isForceBuildRequest(request)) {
            addFriendlyErrorMessage(json, "Cannot schedule: missed pipeline name");
        } else {
            addFriendlyErrorMessage(json, e.getMessage());
        }
        return jsonNotAcceptable(json).respond(response);
    }

    private boolean isForceBuildRequest(HttpServletRequest request) {
        return request.getRequestURI().contains(FORCE_URI);
    }

    String getFullContextPath(HttpServletRequest request) throws URIException {
        String contextPath = request.getContextPath();
        StringBuffer url = request.getRequestURL();
        URI uri = new URI(url.toString());
        uri.setPath(contextPath);
        return uri.toString();
    }
}
