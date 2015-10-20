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

import java.io.File;
import java.util.Date;

import com.thoughtworks.go.database.Database;
import com.thoughtworks.go.server.domain.ServerBackup;
import com.thoughtworks.go.server.persistence.ServerBackupRepository;
import com.thoughtworks.go.server.util.ServerVersion;
import com.thoughtworks.go.service.ConfigRepository;
import com.thoughtworks.go.util.SystemEnvironment;
import com.thoughtworks.go.util.TempFiles;
import com.thoughtworks.go.util.ThrowingFn;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BackupServiceTest {

    private SystemEnvironment systemEnvironment;
    private TempFiles tempFiles;
    private ServerVersion serverVersion;
    private ConfigRepository configRepo;
    private Database databaseStrategy;

    @Before
    public void setUp() throws Exception {
        systemEnvironment = mock(SystemEnvironment.class);
        tempFiles = new TempFiles();
        when(systemEnvironment.getConfigDir()).thenReturn(tempFiles.createUniqueFile("config_dir").getAbsolutePath());
        serverVersion = mock(ServerVersion.class);
        configRepo = mock(ConfigRepository.class);
        databaseStrategy = mock(Database.class);
        when(configRepo.doLocked(Matchers.<ThrowingFn<Object, Exception>>any())).thenCallRealMethod();
    }

    @Test
    public void shouldGetServerBackupLocation() {
        ArtifactsDirHolder artifactsDirHolder = mock(ArtifactsDirHolder.class);
        String location = "/var/go-server-backups";
        when(artifactsDirHolder.getBackupsDir()).thenReturn(new File(location));

        BackupService backupService = new BackupService(null, artifactsDirHolder, mock(GoConfigService.class), null, null, systemEnvironment, serverVersion, configRepo, databaseStrategy);
        backupService.initialize();
        assertThat(backupService.backupLocation(), is(new File(location).getAbsolutePath()));
    }

    @Test
    public void shouldReturnTheLatestBackupTime() {
        ServerBackupRepository repo = mock(ServerBackupRepository.class);
        Date serverBackupTime = new Date();
        when(repo.lastBackup()).thenReturn(new ServerBackup("file_path", serverBackupTime, "user"));
        BackupService backupService = new BackupService(null, null, mock(GoConfigService.class), null, repo, systemEnvironment, serverVersion, configRepo, databaseStrategy);
        backupService.initialize();

        Date date = backupService.lastBackupTime();
        assertThat(date, is(serverBackupTime));
    }

    @Test
    public void shouldReturnNullWhenTheLatestBackupTimeIsNotAvailable() {
        ServerBackupRepository repo = mock(ServerBackupRepository.class);
        when(repo.lastBackup()).thenReturn(null);
        BackupService backupService = new BackupService(null, null, mock(GoConfigService.class), null, repo, systemEnvironment, serverVersion, configRepo, databaseStrategy);
        backupService.initialize();

        assertThat(backupService.lastBackupTime(), is(nullValue()));
    }
    @Test
    public void shouldReturnTheUserThatTriggeredTheLastBackup() {
        ServerBackupRepository repo = mock(ServerBackupRepository.class);
        when(repo.lastBackup()).thenReturn(new ServerBackup("file_path", new Date(), "loser"));
        BackupService backupService = new BackupService(null, null, mock(GoConfigService.class), null, repo, systemEnvironment, serverVersion, configRepo, databaseStrategy);
        backupService.initialize();

        String username = backupService.lastBackupUser();
        assertThat(username, is("loser"));
    }

    @Test
    public void shouldReturnNullWhenTheLatestBackupUserIsNotAvailable() {
        ServerBackupRepository repo = mock(ServerBackupRepository.class);
        when(repo.lastBackup()).thenReturn(null);
        BackupService backupService = new BackupService(null, null, mock(GoConfigService.class), null, repo, systemEnvironment, serverVersion, configRepo, databaseStrategy);
        backupService.initialize();

        assertThat(backupService.lastBackupUser(), is(nullValue()));
    }

    @Test
    public void shouldReturnAvailableDiskSpaceOnArtifactsDirectory() {
        ArtifactsDirHolder artifactsDirHolder = mock(ArtifactsDirHolder.class);
        File artifactDirectory = mock(File.class);
        when(artifactsDirHolder.getArtifactsDir()).thenReturn(artifactDirectory);
        when(artifactDirectory.getUsableSpace()).thenReturn(42424242L);
        BackupService backupService = new BackupService(null, artifactsDirHolder, mock(GoConfigService.class), null, null, systemEnvironment, serverVersion, configRepo, databaseStrategy);
        backupService.initialize();

        assertThat(backupService.availableDiskSpace(), is("40 MB"));

    }
}
