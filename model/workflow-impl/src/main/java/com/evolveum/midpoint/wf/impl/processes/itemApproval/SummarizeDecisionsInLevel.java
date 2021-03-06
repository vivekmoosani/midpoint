/*
 * Copyright (c) 2010-2013 Evolveum
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

package com.evolveum.midpoint.wf.impl.processes.itemApproval;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.wf.impl.processes.common.CommonProcessVariableNames;
import com.evolveum.midpoint.wf.impl.processes.common.SpringApplicationContextHolder;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ApprovalLevelOutcomeType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LevelEvaluationStrategyType;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.commons.lang.Validate;

import java.util.List;

/**
 * @author  mederly
 */
public class SummarizeDecisionsInLevel implements JavaDelegate {

    private static final Trace LOGGER = TraceManager.getTrace(SummarizeDecisionsInLevel.class);

    public void execute(DelegateExecution execution) {

        List<Decision> decisionList = (List<Decision>) execution.getVariable(ProcessVariableNames.DECISIONS_IN_LEVEL);
        Validate.notNull(decisionList, ProcessVariableNames.DECISIONS_IN_LEVEL + " is null");
        ApprovalLevelImpl level = (ApprovalLevelImpl) execution.getVariable(ProcessVariableNames.LEVEL);
        Validate.notNull(level, "level is null");
        level.setPrismContext(SpringApplicationContextHolder.getPrismContext());

        LOGGER.trace("****************************************** Summarizing decisions in level {} (level evaluation strategy = {}): ", level.getName(), level.getEvaluationStrategy());

		boolean approved;
		ApprovalLevelOutcomeType predeterminedOutcome = (ApprovalLevelOutcomeType) execution.getVariable(ProcessVariableNames.PREDETERMINED_LEVEL_OUTCOME);
		if (predeterminedOutcome == null) {
			boolean allApproved = true;
			for (Decision decision : decisionList) {
				LOGGER.trace(" - {}", decision);
				allApproved &= decision.isApproved();
			}

			if (level.getEvaluationStrategy() == null
					|| level.getEvaluationStrategy() == LevelEvaluationStrategyType.ALL_MUST_AGREE) {
				approved = allApproved;
			} else if (level.getEvaluationStrategy() == LevelEvaluationStrategyType.FIRST_DECIDES) {
				if (!decisionList.isEmpty()) {
					approved = decisionList.get(0).isApproved();
				} else {
					// shouldn't occur in 3.6+ (predetermined outcome should be set instead)
					approved = false;
				}
			} else {
				throw new IllegalStateException("Unknown level evaluation strategy: " + level.getEvaluationStrategy());
			}
		} else {
			approved = predeterminedOutcome == ApprovalLevelOutcomeType.APPROVE;
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Approval process instance {} (id {}), level {}: result of this level: {}",
					execution.getVariable(CommonProcessVariableNames.VARIABLE_PROCESS_INSTANCE_NAME),
					execution.getProcessInstanceId(),
					level.getDebugName(), approved);
		}
        execution.setVariable(ProcessVariableNames.LOOP_LEVELS_STOP, !approved);
    }
}
