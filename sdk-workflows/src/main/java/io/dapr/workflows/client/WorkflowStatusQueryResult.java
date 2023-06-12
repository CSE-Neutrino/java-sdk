/*
 * Copyright 2023 The Dapr Authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
limitations under the License.
*/

package io.dapr.workflows.client;

import com.microsoft.durabletask.DurableTaskClient;
import com.microsoft.durabletask.OrchestrationStatusQueryResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class representing the results of a filtered orchestration metadata query.
 *
 * <p>Orchestration metadata can be queried with filters using the
 * {@link DurableTaskClient#queryInstances} method.
 */
public class WorkflowStatusQueryResult {

  OrchestrationStatusQueryResult orchestrationStatusQueryResult;
  private List<WorkflowState> workflowStates;

  /**
   * Class constructor.
   *
   * @param orchestrationStatusQueryResult orchestration Status Query Result
   */
  public WorkflowStatusQueryResult(OrchestrationStatusQueryResult orchestrationStatusQueryResult) {
    this.orchestrationStatusQueryResult = orchestrationStatusQueryResult;

    if (orchestrationStatusQueryResult != null) {
      workflowStates = orchestrationStatusQueryResult.getOrchestrationState()
                              .stream()
                              .map(x -> new WorkflowState(x))
                              .collect(Collectors.toList());
    }
  }

  /**
   * Gets the list of orchestration metadata records that matched the query.
   *
   * @return the list of orchestration metadata records that matched the query.
   */
  public List<WorkflowState> getWorkflowStates() {
    return this.workflowStates;
  }

  /**
   * Gets the continuation token to use with the next query or {@code null} if no
   * more metadata records are found.
   *
   * <p>Note that a non-null value does not always mean that there are more metadata
   * records that can be returned by a query.
   *
   * @return the continuation token to use with the next query or {@code null} if
   *         no more metadata records are found.
   */
  public String getContinuationToken() {
    return this.orchestrationStatusQueryResult.getContinuationToken();
  }
}
