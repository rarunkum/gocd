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

package com.thoughtworks.go.domain;

import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.hamcrest.number.OrderingComparison.greaterThan;

public class AgentStatusTest {
    @Test
    public void shouldCompareStatusAsExpected() { 
        AgentStatus statusInOrder[] = new AgentStatus[] {AgentStatus.Pending, AgentStatus.LostContact, AgentStatus.Missing,
                AgentStatus.Building, AgentStatus.Cancelled, AgentStatus.Idle, AgentStatus.Disabled};
        AgentStatus previous = null;

        for(AgentStatus status : statusInOrder) {
            if(previous != null) {
                assertThat(previous.compareTo(status), is(lessThan(0)));
                assertThat(status.compareTo(previous), is(greaterThan(0)));
            }
            previous = status;
        }
    }

}
