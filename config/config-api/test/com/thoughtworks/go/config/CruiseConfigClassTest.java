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

package com.thoughtworks.go.config;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class CruiseConfigClassTest {

    private ConfigCache configCache = new ConfigCache();

    @Test
    public void shouldFindAllFields() {
        GoConfigClassWriter fooBarClass = new GoConfigClassWriter(FooBar.class, configCache, null);
        assertThat(fooBarClass.getAllFields(new FooBar()).size(), is(3));
    }

    @Test
    public void shouldFindAllFieldsInBaseClass() {
        GoConfigClassWriter fooBarClass = new GoConfigClassWriter(DerivedFooBar.class, configCache, null);
        assertThat(fooBarClass.getAllFields(new DerivedFooBar()).size(), is(4));
    }

}
