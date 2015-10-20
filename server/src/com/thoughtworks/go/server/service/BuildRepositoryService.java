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

package com.thoughtworks.go.server.service;

import com.thoughtworks.go.domain.JobIdentifier;
import com.thoughtworks.go.domain.JobInstance;
import com.thoughtworks.go.domain.JobResult;
import com.thoughtworks.go.domain.JobState;
import com.thoughtworks.go.remote.work.InvalidAgentException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BuildRepositoryService {
    @Autowired private StageService stageService;
    @Autowired private ScheduleService scheduleService;
    @Autowired public JobInstanceService jobInstanceService;
    @Autowired private AgentService agentService;
    private static final Logger LOGGER = Logger.getLogger(BuildRepositoryService.class);

    public BuildRepositoryService() {
    }

    BuildRepositoryService(StageService stageService,
                           JobInstanceService jobInstanceService,
                           ScheduleService scheduleService) {
        this.stageService = stageService;
        this.jobInstanceService = jobInstanceService;
        this.scheduleService = scheduleService;
    }

    public void completing(JobIdentifier jobIdentifier, JobResult result, String agentUuid) {
        scheduleService.jobCompleting(jobIdentifier, result, agentUuid);
    }

    public void updateStatusFromAgent(JobIdentifier jobIdentifier, JobState jobState, String agentUuid) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Changing status of job instance with identifier %s to %s from agent[%s]", jobIdentifier, jobState, agentUuid));
        }
        checkAgentUUID(jobIdentifier, agentUuid);

        scheduleService.updateJobStatus(jobIdentifier, jobState);
    }

    private void checkAgentUUID(JobIdentifier jobIdentifier, String agentUuid) {
        JobInstance job = jobInstanceService.buildByIdWithTransitions(jobIdentifier.getBuildId());
        if (!StringUtils.equals(job.getAgentUuid(), agentUuid)) {
            LOGGER.error("Build Instance is using agent [" + job.getAgentUuid()
                    + "] but status updating from agent [" + agentUuid + "]");
            throw new InvalidAgentException("AgentUUID has changed in the middle of a job. AgentUUID:"
                    + agentUuid + ", Build: " + job.toString());
        }
    }

    public boolean isCancelledOrRescheduled(Long buildInstanceId) {
        JobInstance instance = jobInstanceService.buildByIdWithTransitions(buildInstanceId);
        if (instance.isNull()) {
            return false;
        }
        boolean cancelled = instance.getResult() == JobResult.Cancelled;
        boolean rescheduled = instance.getState() == JobState.Rescheduled;
        return cancelled || rescheduled;
    }
}
