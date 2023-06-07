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
import io.dapr.config.Properties;
import io.dapr.utils.Version;
import io.dapr.workflows.runtime.Workflow;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

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
  public WorkflowMetadata getInstanceMetadata(String instanceId, boolean getInputsAndOutputs) {
    OrchestrationMetadata metadata = this.innerClient.getInstanceMetadata(instanceId, getInputsAndOutputs);
    if (metadata == null) {
      return null;
    }
    return new WorkflowMetadata(metadata);
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
