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

package com.thoughtworks.go.domain.materials;

import java.io.File;

import com.thoughtworks.go.config.materials.SubprocessExecutionContext;
import com.thoughtworks.go.domain.MaterialRevision;
import com.thoughtworks.go.util.command.ProcessOutputStreamConsumer;

public class AbstractMaterialAgent implements MaterialAgent {
    private MaterialRevision revision;
    private ProcessOutputStreamConsumer consumer;
    private File workingDirectory;
    private SubprocessExecutionContext execCtx;

    public AbstractMaterialAgent(MaterialRevision revision,
                                 ProcessOutputStreamConsumer consumer,
                                 File workingDirectory, final SubprocessExecutionContext execCtx) {
        this.revision = revision;
        this.consumer = consumer;
        this.workingDirectory = workingDirectory;
        this.execCtx = execCtx;
    }

    public void prepare() {
        revision.updateTo(workingDirectory, consumer, this.execCtx);
    }
}
