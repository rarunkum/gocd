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

import java.util.ArrayList;

import com.thoughtworks.go.helper.PipelineTemplateConfigMother;
import com.thoughtworks.go.helper.StageConfigMother;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class TemplatesConfigTest {

    @Test
    public void shouldRemoveATemplateByName() {
        PipelineTemplateConfig template2 = template("template2");
        TemplatesConfig templates = new TemplatesConfig(template("template1"), template2);

        templates.removeTemplateNamed(new CaseInsensitiveString("template1"));

        assertThat(templates.size(), is(1));
        assertThat(templates.get(0), is(template2));
    }

    @Test
    public void shouldIgnoreTryingToRemoveNonExistentTemplate() {
        TemplatesConfig templates = new TemplatesConfig(template("template1"), template("template2"));

        templates.removeTemplateNamed(new CaseInsensitiveString("sachin"));

        assertThat(templates.size(), is(2));
    }

    @Test
    public void shouldReturnTemplateByName() {
        PipelineTemplateConfig template1 = template("template1");
        TemplatesConfig templates = new TemplatesConfig(template1, template("template2"));
        assertThat(templates.templateByName(new CaseInsensitiveString("template1")), is(template1));
    }

    @Test
    public void shouldReturnNullIfTemplateIsNotFound() {
        PipelineTemplateConfig template1 = template("template1");
        TemplatesConfig templates = new TemplatesConfig(template1);
        assertThat(templates.templateByName(new CaseInsensitiveString("some_invalid_template")), is(nullValue()));
    }

    @Test
    public void shouldErrorOutIfTemplateNameIsAlreadyPresent() {
        PipelineTemplateConfig template = template("template1");
        TemplatesConfig templates = new TemplatesConfig(template);
        PipelineTemplateConfig duplicateTemplate = template("template1");
        templates.add(duplicateTemplate);

        templates.validate(null);

        assertThat(template.errors().isEmpty(), is(false));
        assertThat(duplicateTemplate.errors().isEmpty(), is(false));
        assertThat(template.errors().on(PipelineTemplateConfig.NAME), is(String.format("Template name '%s' is not unique", template.name())));
        assertThat(duplicateTemplate.errors().on(PipelineTemplateConfig.NAME), is(String.format("Template name '%s' is not unique", template.name())));
    }

    @Test
    public void shouldErrorOutIfTemplateNameIsAlreadyPresent_CaseInsensitiveMap() {
        PipelineTemplateConfig template = template("TEmplatE1");
        TemplatesConfig templates = new TemplatesConfig(template);
        PipelineTemplateConfig duplicateTemplate = template("template1");
        templates.add(duplicateTemplate);

        templates.validate(null);

        assertThat(template.errors().isEmpty(), is(false));
        assertThat(duplicateTemplate.errors().isEmpty(), is(false));
    }

    @Test
    public void shouldReturnTrueIfUserCanViewAndEditAtLeastOneTemplate() throws Exception {
        ArrayList<PipelineTemplateConfig> templateList = new ArrayList<PipelineTemplateConfig>();
        for (int i = 0; i < 100; i++) {
            templateList.add(PipelineTemplateConfigMother.createTemplate("template" + i));
        }
        CaseInsensitiveString templateAdmin = new CaseInsensitiveString("template-admin");
        templateList.add(PipelineTemplateConfigMother.createTemplate("template100", new Authorization(new AdminsConfig(new AdminUser(templateAdmin))), StageConfigMother.manualStage("stage-name")));
        TemplatesConfig templates = new TemplatesConfig(templateList.toArray(new PipelineTemplateConfig[0]));

        assertThat(templates.canViewAndEditTemplate(templateAdmin), is(true));
    }

    @Test
    public void shouldReturnFalseIfUserCannotViewAndEditAtLeastOneTemplate() throws Exception {
        ArrayList<PipelineTemplateConfig> templateList = new ArrayList<PipelineTemplateConfig>();
        for (int i = 0; i < 100; i++) {
            templateList.add(PipelineTemplateConfigMother.createTemplate("template" + i));
        }
        CaseInsensitiveString templateAdmin = new CaseInsensitiveString("template-admin");
        CaseInsensitiveString nonTemplateAdmin = new CaseInsensitiveString("some-random-user");
        templateList.add(PipelineTemplateConfigMother.createTemplate("template100", new Authorization(new AdminsConfig(new AdminUser(templateAdmin))), StageConfigMother.manualStage("stage-name")));
        TemplatesConfig templates = new TemplatesConfig(templateList.toArray(new PipelineTemplateConfig[0]));

        assertThat(templates.canViewAndEditTemplate(nonTemplateAdmin), is(false));
    }

    @Test
    public void shouldReturnTrueIfUserCanEditTemplate() {
        CaseInsensitiveString templateAdmin = new CaseInsensitiveString("template-admin");
        String templateName = "template1";
        PipelineTemplateConfig template = PipelineTemplateConfigMother.createTemplate(templateName, new Authorization(new AdminsConfig(new AdminUser(templateAdmin))),
                StageConfigMother.manualStage("stage-name"));
        TemplatesConfig templates = new TemplatesConfig(template);
        assertThat(templates.canUserEditTemplate(templateName, templateAdmin), is(true));
    }

    @Test
    public void shouldReturnFalseIfUserCannotEditTemplate() {
        CaseInsensitiveString templateAdmin = new CaseInsensitiveString("template-admin");
        CaseInsensitiveString templateAdminWhoDoesNotHavePermissionToThisTemplate = new CaseInsensitiveString("user");
        String templateName = "template1";
        PipelineTemplateConfig template = PipelineTemplateConfigMother.createTemplate(templateName, new Authorization(new AdminsConfig(new AdminUser(templateAdmin))),
                StageConfigMother.manualStage("stage-name"));
        TemplatesConfig templates = new TemplatesConfig(template);
        assertThat(templates.canUserEditTemplate(templateName, templateAdminWhoDoesNotHavePermissionToThisTemplate), is(false));
    }

    private PipelineTemplateConfig template(final String name) {
        return new PipelineTemplateConfig(new CaseInsensitiveString(name), StageConfigMother.stageConfig("stage1"));
    }
}
