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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.UUID;

import com.thoughtworks.go.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class GoPluginDescriptorBuilderTest {
    private static final File TMP_DIR = new File("./tmp");
    private static final String TESTPLUGIN_ID = "testplugin.descriptorValidator";
    private GoPluginDescriptorBuilder goPluginDescriptorBuilder;
    private File pluginDirectory;
    private File bundleDirectory;

    @Before
    public void setUp() throws Exception {
        pluginDirectory = new File(TMP_DIR + "-plugins-" + UUID.randomUUID().toString());
        FileUtil.recreateDirectory(pluginDirectory);

        bundleDirectory = new File(TMP_DIR + "-bundles-" + UUID.randomUUID().toString());
        FileUtil.recreateDirectory(bundleDirectory);

        goPluginDescriptorBuilder = spy(new GoPluginDescriptorBuilder());
        doReturn(bundleDirectory).when(goPluginDescriptorBuilder).bundlePath();

    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteQuietly(pluginDirectory);
        FileUtils.deleteQuietly(bundleDirectory);
    }

    @Test
    public void shouldCreateThePluginDescriptorFromGivenPluginJarWithPluginXML() throws Exception {
        String pluginJarName = "descriptor-aware-test-plugin.jar";
        copyPluginToThePluginDirectory(pluginDirectory, pluginJarName);
        File pluginJarFile = new File(pluginDirectory, pluginJarName);

        GoPluginDescriptor descriptor = goPluginDescriptorBuilder.build(pluginJarFile, true);

        GoPluginDescriptor expectedDescriptor = buildExpectedDescriptor
                (pluginJarName, pluginJarFile.getAbsolutePath());
        assertThat(descriptor, CoreMatchers.is(expectedDescriptor));
        assertThat(descriptor.isInvalid(), CoreMatchers.is(false));
        assertThat(descriptor.isBundledPlugin(), CoreMatchers.is(true));
    }

    @Test
    public void shouldCreateInvalidPluginDescriptorBecausePluginXMLDoesNotConformToXSD() throws Exception {
        String pluginJarName = "invalid-descriptor-plugin.jar";
        copyPluginToThePluginDirectory(pluginDirectory, pluginJarName);
        File pluginJarFile = new File(pluginDirectory, pluginJarName);

        GoPluginDescriptor descriptor = goPluginDescriptorBuilder.build(pluginJarFile, true);

        GoPluginDescriptor expectedDescriptor = buildXMLSchemaErrorDescriptor(pluginJarName);
        assertThat(descriptor, CoreMatchers.is(expectedDescriptor));
        assertThat(descriptor.isInvalid(), CoreMatchers.is(true));
        assertThat(descriptor.isBundledPlugin(), CoreMatchers.is(true));
        assertThat(descriptor.getStatus().getMessages(), is(expectedDescriptor.getStatus().getMessages()));
    }

    @Test
    public void shouldCreatePluginDescriptorEvenIfPluginXMLIsNotFound() throws Exception {
        String pluginJarName = "descriptor-aware-test-plugin-with-no-plugin-xml.jar";
        copyPluginToThePluginDirectory(pluginDirectory, pluginJarName);
        File pluginJarFile = new File(pluginDirectory, pluginJarName);

        GoPluginDescriptor descriptor = goPluginDescriptorBuilder.build(pluginJarFile, false);

        assertThat(descriptor.isInvalid(), is(false));
        assertThat(descriptor.id(), is(pluginJarName));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionForInvalidPluginIfThePluginJarDoesNotExist() throws Exception {
        goPluginDescriptorBuilder.build(new File(pluginDirectory, "invalid"), true);
    }

    private void copyPluginToThePluginDirectory(File pluginDir, String destinationFilenameOfPlugin) throws IOException, URISyntaxException {
        URL resource = getClass().getClassLoader().getResource("defaultFiles/" + destinationFilenameOfPlugin);
        FileUtils.copyURLToFile(resource, new File(pluginDir, destinationFilenameOfPlugin));
    }

    private GoPluginDescriptor buildExpectedDescriptor(String name, String pluginJarFileLocation) {
        /*
            <?xml version="1.0" encoding="utf-8" ?>
            <go-plugin id="testplugin.descriptorValidator" version="1">
               <about>
                 <name>Plugin Descriptor Validator</name>
                 <version>1.0.1</version>
                 <target-go-version>12.4</target-go-version>
                 <description>Validates its own plugin descriptor</description>
                 <vendor>
                   <name>ThoughtWorks Go Team</name>
                   <url>www.thoughtworks.com</url>
                 </vendor>
                 <target-os>
                   <value>Linux</value>
                   <value>Windows</value>
                 </target-os>
               </about>
            </go-plugin>
        */
        return new GoPluginDescriptor(TESTPLUGIN_ID, "1",
                new GoPluginDescriptor.About("Plugin Descriptor Validator", "1.0.1", "12.4", "Validates its own plugin descriptor",
                        new GoPluginDescriptor.Vendor("ThoughtWorks Go Team", "www.thoughtworks.com"), Arrays.asList("Linux", "Windows", "Mac OS X")), pluginJarFileLocation,
                new File(bundleDirectory, name),
                true);
    }

    private GoPluginDescriptor buildXMLSchemaErrorDescriptor(String name) {
        /*
            <?xml version="1.0" encoding="utf-8" ?>
            <go-plugin id="testplugin.descriptorValidator" version="1">
               <about>
                 <name>Plugin Descriptor Validator</name>
                 <version>1.0.1</version>
                 <target-go-version>12.4</target-go-version>
                 <description>Validates its own plugin descriptor</description>
                 <vendor>
                   <name>ThoughtWorks Go Team</name>
                   <url>www.thoughtworks.com</url>
                 </vendor>
                 <target-os>
                   <value>Linux</value>
                   <value>Windows</value>
                 </target-os>
                 <target-os> // this tag is repeated - this is invalid
                   <value>Linux</value>
                   <value>Windows</value>
                 </target-os>
               </about>
            </go-plugin>
        */
        File pluginJarFile = new File(pluginDirectory, name);

        return GoPluginDescriptor.usingId(pluginJarFile.getName(), pluginJarFile.getAbsolutePath(), new File(bundleDirectory, name), true)
                .markAsInvalid(Arrays.asList(String.format("Plugin with ID (%s) is not valid: %s.", pluginJarFile.getName(),
                        "XML Schema validation of Plugin Descriptor(plugin.xml) failed. Cause: cvc-complex-type.2.4.d: Invalid content was found starting with element 'target-os'. No child element is expected at this point")                       ),
                        null);
    }
}
