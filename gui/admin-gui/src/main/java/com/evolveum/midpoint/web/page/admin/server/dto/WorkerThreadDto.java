/*
 * Copyright (c) 2010-2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.web.page.admin.server.dto;

import com.evolveum.midpoint.web.page.admin.server.dto.TaskDto;
import com.evolveum.midpoint.web.page.admin.server.dto.TaskDtoExecutionStatus;

import java.io.Serializable;

/**
 * @author Pavol Mederly
 */
public class WorkerThreadDto implements Serializable {

    public static final String F_NAME = "name";
    public static final String F_EXECUTION_STATUS = "executionStatus";
    public static final String F_PROGRESS = "progress";

    private final TaskDto subtaskDto;

    public WorkerThreadDto(TaskDto subtaskDto) {
        this.subtaskDto = subtaskDto;
    }

    public String getName() {
        return subtaskDto != null ? subtaskDto.getName() : null;
    }

    public Long getProgress() {
        return subtaskDto != null ? subtaskDto.getProgress() : null;
    }

    public TaskDtoExecutionStatus getExecutionStatus() {
        return subtaskDto != null ? subtaskDto.getExecution() : null;
    }
}
