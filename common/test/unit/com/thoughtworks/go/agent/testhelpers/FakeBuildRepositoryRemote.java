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

package com.thoughtworks.go.agent.testhelpers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.thoughtworks.go.domain.AgentRuntimeStatus;
import com.thoughtworks.go.domain.JobIdentifier;
import com.thoughtworks.go.domain.JobResult;
import com.thoughtworks.go.domain.JobState;
import com.thoughtworks.go.remote.AgentInstruction;
import com.thoughtworks.go.remote.BuildRepositoryRemote;
import com.thoughtworks.go.remote.AgentIdentifier;
import com.thoughtworks.go.remote.work.Work;
import com.thoughtworks.go.server.service.AgentRuntimeInfo;
import com.thoughtworks.go.util.SystemEnvironment;
import org.apache.log4j.Logger;

public class FakeBuildRepositoryRemote implements BuildRepositoryRemote {
    public final static List<AgentRuntimeStatus> AGENT_STATUS = new ArrayList<AgentRuntimeStatus>();

    private static final Logger LOGGER = Logger.getLogger(FakeBuildRepositoryRemote.class);

    public static final String PIPELINE_NAME = "studios";
    public static final String PIPELINE_LABEL = "100";
    public static final String STAGE_NAME = "pipeline";
    public static final String JOB_PLAN_NAME = "cruise-test-data";

    private static BlockingQueue<Boolean> buildResult = new LinkedBlockingQueue<Boolean>();

    public AgentInstruction ping(AgentRuntimeInfo info) {
        AGENT_STATUS.add(info.getRuntimeStatus());
        return new AgentInstruction(false);
    }

    public Work getWork(AgentRuntimeInfo runtimeInfo) {
        String className = SystemEnvironment.getProperty("WORKCREATOR", DefaultWorkCreator.class.getCanonicalName());
        Class<? extends WorkCreator> aClass = null;
        try {
            aClass = (Class<? extends WorkCreator>) Class.forName(className);
            return aClass.newInstance().work(runtimeInfo.getIdentifier());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void reportCurrentStatus(AgentRuntimeInfo agentRuntimeInfo, JobIdentifier jobIdentifier, JobState jobState) {
        LOGGER.info("Current status of build instance with id " + jobIdentifier + " is " + jobState);
        if (jobState.isCompleted()) {
            buildResult.offer(Boolean.TRUE);
        }
    }

    public void reportCompleting(AgentRuntimeInfo agentRuntimeInfo, JobIdentifier jobIdentifier, JobResult result) {
        LOGGER.info("Build result of project " + jobIdentifier + " is " + result);
    }

    @Override public void reportCompleted(AgentRuntimeInfo agentRuntimeInfo, JobIdentifier jobId, JobResult result) {
        LOGGER.info("Completed Build");
    }

    public boolean isIgnored(JobIdentifier jobIdentifier) {
        return false;
    }

    public String getCookie(AgentIdentifier identifier, String location) {
        throw new UnsupportedOperationException("Not implemented");
    }


    public static void waitUntilBuildCompleted() throws InterruptedException {
        while (!isBuildCompleted()) {
            Thread.sleep(1000);
        }
    }

    private static boolean isBuildCompleted() throws InterruptedException {
        Boolean aBoolean = buildResult.poll(1, TimeUnit.SECONDS);
        return aBoolean != null && aBoolean;
    }


}
