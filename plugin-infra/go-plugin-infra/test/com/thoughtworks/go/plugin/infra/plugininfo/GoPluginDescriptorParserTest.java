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

package com.thoughtworks.go.plugin.infra.plugininfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class GoPluginDescriptorParserTest {
    @Test
    public void shouldPerformPluginXsdValidationAndFailWhenIDIsNotPresent() throws Exception {
        InputStream pluginXml = IOUtils.toInputStream("<go-plugin version=\"1\"></go-plugin>");
        try {
            GoPluginDescriptorParser.parseXML(pluginXml, "/tmp/", new File("/tmp/"), true);
            fail("xsd validation should have failed");
        } catch (SAXException e) {
           assertThat(e.getMessage(), is("XML Schema validation of Plugin Descriptor(plugin.xml) failed"));
        }
    }

    @Test
    public void shouldPerformPluginXsdValidationAndFailWhenVersionIsNotPresent() throws Exception {
        InputStream pluginXml = IOUtils.toInputStream("<go-plugin id=\"some\"></go-plugin>");
        try {
            GoPluginDescriptorParser.parseXML(pluginXml, "/tmp/", new File("/tmp/"), true);
            fail("xsd validation should have failed");
        } catch (SAXException e) {
           assertThat(e.getMessage(), is("XML Schema validation of Plugin Descriptor(plugin.xml) failed"));
        }
    }
}
