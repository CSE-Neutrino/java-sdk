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
import com.microsoft.durabletask.DurableTaskGrpcClientBuilder;
import com.microsoft.durabletask.OrchestrationMetadata;
import com.microsoft.durabletask.OrchestrationStatusQuery;
import com.microsoft.durabletask.OrchestrationStatusQueryResult;
import io.dapr.config.Properties;
import io.dapr.utils.Version;
import io.dapr.workflows.runtime.Workflow;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Defines client operations for managing Dapr Workflow instances.
 */
public class DaprWorkflowClient implements AutoCloseable {

  private final DurableTaskClient innerClient;
  private final ManagedChannel grpcChannel;

  /**
   * Public constructor for DaprWorkflowClient. This layer constructs the GRPC Channel.
   */
  public DaprWorkflowClient() {
    this(createGrpcChannel());
  }

  /**
   * Private Constructor that passes a created DurableTaskClient and the new GRPC channel.
   *
   * @param grpcChannel ManagedChannel for GRPC channel.
   */
  private DaprWorkflowClient(ManagedChannel grpcChannel) {
    this(createDurableTaskClient(grpcChannel), grpcChannel);
  }

  /**
   * Private Constructor for DaprWorkflowClient.
   *
   * @param innerClient DurableTaskGrpcClient with GRPC Channel set up.
   * @param grpcChannel ManagedChannel for instance variable setting.
   */
  private DaprWorkflowClient(DurableTaskClient innerClient, ManagedChannel grpcChannel) {
    this.innerClient = innerClient;
    this.grpcChannel = grpcChannel;
  }

  /**
   * Static method to create the DurableTaskClient.
   *
   * @param grpcChannel ManagedChannel for GRPC.
   * @return a new instance of a DurableTaskClient with a GRPC channel.
   */
  private static DurableTaskClient createDurableTaskClient(ManagedChannel grpcChannel) {
    return new DurableTaskGrpcClientBuilder()
        .grpcChannel(grpcChannel)
        .build();
  }

  /**
   * Static method to create the GRPC Channel for the DurableTaskClient.
   *
   * @return a Managed GRPC channel.
   * @throws IllegalStateException if the GRPC port is invalid.
   */
  private static ManagedChannel createGrpcChannel() throws IllegalStateException {
    int port = Properties.GRPC_PORT.get();
    if (port <= 0) {
      throw new IllegalStateException(String.format("Invalid port, %s. Must greater than 0", port));
    }

    return ManagedChannelBuilder.forAddress(Properties.SIDECAR_IP.get(), port)
        .usePlaintext()
        .userAgent(Version.getSdkVersion())
        .build();
  }

  /**
   * Schedules a new workflow using DurableTask client.
   *
   * @param <T> any Workflow type
   * @param clazz Class extending Workflow to start an instance of.
   * @return the randomly-generated instance ID for new Workflow instance.
   */
  public <T extends Workflow> String scheduleNewWorkflow(Class<T> clazz) {
    return this.innerClient.scheduleNewOrchestrationInstance(clazz.getCanonicalName());
  }

  /**
   * Schedules a new workflow using DurableTask client.
   *
   * @param <T> any Workflow type
   * @param clazz Class extending Workflow to start an instance of.
   * @param input the input to pass to the scheduled orchestration instance. Must be serializable.
   * @return the randomly-generated instance ID for new Workflow instance.
   */
  public <T extends Workflow> String scheduleNewWorkflow(Class<T> clazz, Object input) {
    return this.innerClient.scheduleNewOrchestrationInstance(clazz.getCanonicalName(), input);
  }

  /**
   * Schedules a new workflow using DurableTask client.
   *
   * @param <T> any Workflow type
   * @param clazz Class extending Workflow to start an instance of.
   * @param input the input to pass to the scheduled orchestration instance. Must be serializable.
   * @param instanceId the unique ID of the orchestration instance to schedule
   * @return the <code>instanceId</code> parameter value.
   */
  public <T extends Workflow> String scheduleNewWorkflow(Class<T> clazz, Object input, String instanceId) {
    return this.innerClient.scheduleNewOrchestrationInstance(clazz.getCanonicalName(), input, instanceId);
  }

  /**
   * Terminates the workflow associated with the provided instance id.
   *
   * @param workflowInstanceId Workflow instance id to terminate.
   * @param output the optional output to set for the terminated orchestration instance.
   */
  public void terminateWorkflow(String workflowInstanceId, @Nullable Object output) {
    this.innerClient.terminate(workflowInstanceId, output);
  }

  /**
   * Fetches workflow instance metadata from the configured durable store.
   *
   * @param instanceId the unique ID of the workflow instance to fetch
   * @param getInputsAndOutputs <code>true</code> to fetch the workflow instance's
     inputs, outputs, and custom status, or <code>false</code> to omit them
   * @return a metadata record that describes the workflow instance and its
     execution status, or a default instance if no such instance is found.
   */
  @Nullable
  public WorkflowState getInstanceState(String instanceId, boolean getInputsAndOutputs) {
    OrchestrationMetadata metadata = this.innerClient.getInstanceMetadata(instanceId, getInputsAndOutputs);
    if (metadata == null) {
      return null;
    }
    return new WorkflowState(metadata);
  }

  /**
   * Waits for an workflow to start running and returns an
   * {@link WorkflowState} object that contains metadata about the started 
   * instance and optionally its input, output, and custom status payloads.
   *
   * <p>A "started" workflow instance is any instance not in the Pending state.
   *
   * <p>If a workflow instance is already running when this method is called,
   * the method will return immediately.
   *
   * @param instanceId the unique ID of the workflow instance to wait for
   * @param timeout the amount of time to wait for the workflow instance to start
   * @param getInputsAndOutputs true to fetch the workflow instance's
   *                            inputs, outputs, and custom status, or false to omit them
   * @throws TimeoutException when the workflow instance is not started within the specified amount of time
   * @return the workflow instance metadata or null if no such instance is found
   */
  @Nullable
  public WorkflowState waitForInstanceStart(String instanceId, Duration timeout, boolean getInputsAndOutputs)
      throws TimeoutException {

    OrchestrationMetadata metadata = this.innerClient.waitForInstanceStart(instanceId, timeout, getInputsAndOutputs);
    return metadata == null ? null : new WorkflowState(metadata);
  }

  /**
   * Waits for an workflow to complete and returns an {@link WorkflowState} object that contains
   * metadata about the completed instance.
   *
   * <p>A "completed" workflow instance is any instance in one of the terminal states. For example, the
   * Completed, Failed, or Terminated states.
   *
   * <p>Workflows are long-running and could take hours, days, or months before completing.
   * Workflows can also be eternal, in which case they'll never complete unless terminated.
   * In such cases, this call may block indefinitely, so care must be taken to ensure appropriate timeouts are used.
   * If an workflow instance is already complete when this method is called, the method will return immediately.
   *
   * @param instanceId the unique ID of the workflow instance to wait for
   * @param timeout the amount of time to wait for the workflow instance to complete
   * @param getInputsAndOutputs true to fetch the workflow instance's inputs, outputs, and custom
   *                            status, or false to omit them
   * @throws TimeoutException when the workflow instance is not completed within the specified amount of time
   * @return the workflow instance metadata or null if no such instance is found
   */
  @Nullable
  public WorkflowState waitForInstanceCompletion(String instanceId, Duration timeout,
      boolean getInputsAndOutputs) throws TimeoutException {

    OrchestrationMetadata metadata = 
        this.innerClient.waitForInstanceCompletion(instanceId, timeout, getInputsAndOutputs);
    return metadata == null ? null : new WorkflowState(metadata);
  }

  /**
   * Fetches orchestration instance metadata from the configured durable store using a status query filter.
   *
   * @param query filter criteria that determines which orchestrations to fetch data for.
   * @return the result of the query operation, including instance metadata and possibly a continuation token
   */
  public WorkflowStatusQueryResult queryInstances(WorkflowStatusQuery query) {

    OrchestrationStatusQuery orchestrationStatusQuery = new OrchestrationStatusQuery()
        .setContinuationToken(query.getContinuationToken())
        .setCreatedTimeFrom(query.getCreatedTimeFrom())
        .setCreatedTimeTo(query.getCreatedTimeTo())
        .setInstanceIdPrefix(query.getInstanceIdPrefix())
        .setMaxInstanceCount(query.getMaxInstanceCount())
        .setRuntimeStatusList(WorkflowRuntimeStatus.toOrchestrationRuntimeStatus(query.getRuntimeStatusList()))
        .setTaskHubNames(query.getTaskHubNames())
        .setFetchInputsAndOutputs(query.isFetchInputsAndOutputs());

    OrchestrationStatusQueryResult queryResult = this.innerClient.queryInstances(orchestrationStatusQuery);
    return queryResult == null ? null : new WorkflowStatusQueryResult(queryResult);
  }

  /**
   * Closes the inner DurableTask client and shutdown the GRPC channel.
   */
  public void close() throws InterruptedException {
    if (this.innerClient != null) {
      this.innerClient.close();
    }
    if (this.grpcChannel != null && !this.grpcChannel.isShutdown()) {
      this.grpcChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
