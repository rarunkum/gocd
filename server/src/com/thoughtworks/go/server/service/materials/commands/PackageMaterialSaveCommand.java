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

package com.thoughtworks.go.server.service.materials.commands;

import java.util.Map;

import com.thoughtworks.go.config.CaseInsensitiveString;
import com.thoughtworks.go.config.CruiseConfig;
import com.thoughtworks.go.config.Validatable;
import com.thoughtworks.go.config.materials.PackageMaterialConfig;
import com.thoughtworks.go.config.update.UpdateConfigFromUI;
import com.thoughtworks.go.domain.packagerepository.PackageDefinition;
import com.thoughtworks.go.i18n.LocalizedMessage;
import com.thoughtworks.go.server.domain.Username;
import com.thoughtworks.go.server.service.SecurityService;
import com.thoughtworks.go.server.service.materials.PackageDefinitionService;
import com.thoughtworks.go.server.service.result.LocalizedOperationResult;

public abstract class PackageMaterialSaveCommand implements UpdateConfigFromUI {
    protected PackageMaterialConfig packageMaterialConfig;
    protected final String pipeline;
    private SecurityService securityService;
    private final Username username;
    private PackageDefinitionCreator packageDefinitionCreator;

    public PackageMaterialSaveCommand(PackageDefinitionService packageDefinitionService, SecurityService securityService, String pipeline, PackageMaterialConfig packageMaterialConfig,
                                      Username username, Map params) {
        this.pipeline = pipeline;
        this.securityService = securityService;
        this.username = username;
        this.packageMaterialConfig = packageMaterialConfig;
        this.packageDefinitionCreator = new PackageDefinitionCreator(packageDefinitionService, params);
    }

    public void checkPermission(CruiseConfig cruiseConfig, LocalizedOperationResult result) {
        String groupName = cruiseConfig.getGroups().findGroupNameByPipeline(new CaseInsensitiveString(pipeline));
        if (!securityService.isUserAdminOfGroup(username.getUsername(), groupName)) {
            result.unauthorized(LocalizedMessage.string("UNAUTHORIZED_TO_ADMINISTER"), null);
        }
    }

    public Validatable node(CruiseConfig cruiseConfig) {
        return cruiseConfig;
    }

    public Validatable updatedNode(CruiseConfig cruiseConfig) {
        if (packageMaterialConfig.getPackageDefinition() != null) {
            return cruiseConfig.pipelineConfigByName(new CaseInsensitiveString(pipeline)).materialConfigs().get(packageMaterialConfig);
        }
        return packageMaterialConfig;
    }

    protected abstract void updateConfig(CruiseConfig cruiseConfig);

    public final void update(Validatable node) {
        updateConfig((CruiseConfig) node);
    }

    public Validatable subject(Validatable node) {
        return packageMaterialConfig;
    }

    public Validatable updatedSubject(Validatable updatedNode) {
        return updatedNode;
    }

    protected PackageDefinition createNewPackageDefinition(CruiseConfig cruiseConfig) {
        return packageDefinitionCreator.createNewPackageDefinition(cruiseConfig);
    }

    protected PackageDefinition getPackageDefinition(CruiseConfig cruiseConfig) {
        return packageDefinitionCreator.getPackageDefinition(cruiseConfig);
    }
}
