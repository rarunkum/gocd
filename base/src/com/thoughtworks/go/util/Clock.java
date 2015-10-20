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

package com.thoughtworks.go.util;

import java.util.Date;

import com.thoughtworks.go.utils.Timeout;
import org.joda.time.DateTime;

public interface Clock {
    Date currentTime();

    DateTime currentDateTime();

    long currentTimeMillis();

    void sleepForSeconds(long seconds) throws InterruptedException;

    void sleepForMillis(long download_sleep_millis) throws InterruptedException;

    DateTime timeoutTime(Timeout timeout);

    DateTime timeoutTime(long milliSeconds);
}
