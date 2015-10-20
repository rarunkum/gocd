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

import com.thoughtworks.go.server.service.result.ServerHealthStateOperationResult;
import com.thoughtworks.go.util.ClassMockery;
import static org.hamcrest.core.Is.is;
import org.jmock.Expectations;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class StageAuthorizationCheckerTest {
    private ClassMockery mockery;
    private String pipelineName;
    private StageAuthorizationChecker checker;
    private String stageName;
    private String username;
    private SecurityService securityService;

    @Before
    public void setUp() throws Exception {
        mockery = new ClassMockery();
        securityService = mockery.mock(SecurityService.class);
        pipelineName = "cruise";
        stageName = "dev";
        username = "gli";
        checker = new StageAuthorizationChecker(pipelineName, stageName, username, securityService);
    }

    @Test
    public void shouldFailIfUserHasNoPermission() {
        mockery.checking(new Expectations() {
            {
                one(securityService).hasOperatePermissionForStage(pipelineName, stageName, username);
                will(returnValue(false));
            }
        });

        ServerHealthStateOperationResult result = new ServerHealthStateOperationResult();
        checker.check(result);
        assertThat(result.getServerHealthState().isSuccess(), is(false));
        assertThat(result.getServerHealthState().getDescription(),
                is("User gli does not have permission to schedule cruise/dev"));
    }


    @Test
    public void shouldPassIfUserHasPermission() {

        mockery.checking(new Expectations() {
            {
                one(securityService).hasOperatePermissionForStage(pipelineName, stageName, username);
                will(returnValue(true));
            }
        });

        ServerHealthStateOperationResult result = new ServerHealthStateOperationResult();
        checker.check(result);
        assertThat(result.getServerHealthState().isSuccess(), is(true));
    }
}
