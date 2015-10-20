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

package com.thoughtworks.go.agent.bootstrapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.googlecode.junit.ext.checkers.OSChecker;
import com.thoughtworks.go.agent.common.util.Downloader;
import com.thoughtworks.go.agent.testhelper.FakeBootstrapperServer;
import com.thoughtworks.go.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

@RunWith(FakeBootstrapperServer.class)
public class AgentBootstrapperFunctionalTest {

    public static final OSChecker OS_CHECKER = new OSChecker(OSChecker.WINDOWS);

    @Before
    public void setUp() throws IOException {
        new File(".agent-bootstrapper.running").delete();
        FileUtils.copyFile(new File("testdata", Downloader.AGENT_LAUNCHER), new File(Downloader.AGENT_LAUNCHER));
    }

    @After
    public void tearDown() {
        FileUtils.deleteQuietly(new File(Downloader.AGENT_LAUNCHER));
    }

    @Test
    public void shouldCheckout_Bundled_agentLauncher() throws IOException {
        try {
            new File(Downloader.AGENT_LAUNCHER).delete();
            new AgentBootstrapper().validate();
            assertEquals("agent launcher from default files", FileUtil.readToEnd(new File(Downloader.AGENT_LAUNCHER)));
        } finally {
            new File(Downloader.AGENT_LAUNCHER).delete();
        }
    }

    @Test
    public void shouldLoadAndBootstrapJarUsingAgentBootstrapCode_specifiedInAgentManifestFile() {
        if (!OS_CHECKER.satisfy()) {
            PrintStream err = System.err;
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                System.setErr(new PrintStream(os));
                File agentJar = new File("agent.jar");
                agentJar.delete();
                new AgentBootstrapper(){
                    @Override void jvmExit(int returnValue) {
                    }
                }.go(false, "localhost", 9090);
                agentJar.delete();
                assertThat(new String(os.toByteArray()), is("Hello World Fellas!"));
            } finally {
                System.setErr(err);
            }
        }
    }

    @Test
    public void shouldDownloadJarIfItDoesNotExist() throws Exception {
        if (!OS_CHECKER.satisfy()) {
            File agentJar = new File("agent.jar");
            agentJar.delete();
            new AgentBootstrapper(){
                    @Override void jvmExit(int returnValue) {
                    }
                }.go(false, "localhost", 9090);
            assertTrue("No agent downloaded", agentJar.exists());
            agentJar.delete();
        }
    }

    @Test
    public void shouldDownloadJarIfTheCurrentOneIsWrong() throws Exception {
        if (!OS_CHECKER.satisfy()) {
            File agentJar = new File("agent.jar");
            agentJar.delete();
            createRandomFile(agentJar);
            long original = agentJar.length();
            new AgentBootstrapper(){
                    @Override void jvmExit(int returnValue) {
                    }
                }.go(false, "localhost", 9090);
            assertThat(agentJar.length(), not(original));
            agentJar.delete();
        }
    }

    @Ignore @Test
    public void shouldDieNicelyIfWrongUrl() {
        new File("agent.jar").delete();
        try {
            new AgentBootstrapper(){
                    @Override void jvmExit(int returnValue) {
                    }
                }.go(false, "IShouldNotResolveAtAll", 9090);
            fail("Shouldn't work if wrong URL provided");
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("Please check the URL"));
        }
    }

    private void createRandomFile(File agentJar) {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(agentJar);
            IOUtils.write("some rubbish", output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(output);
        }
    }
}
