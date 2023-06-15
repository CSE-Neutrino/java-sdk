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
import com.microsoft.durabletask.OrchestrationMetadata;
import com.microsoft.durabletask.OrchestrationRuntimeStatus;
import com.microsoft.durabletask.OrchestrationStatusQuery;
import com.microsoft.durabletask.OrchestrationStatusQueryResult;

import io.dapr.workflows.runtime.Workflow;
import io.dapr.workflows.runtime.WorkflowContext;
import io.grpc.ManagedChannel;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DaprWorkflowClientTest {
  private static Constructor<DaprWorkflowClient> constructor;
  private DaprWorkflowClient client;
  private DurableTaskClient mockInnerClient;
  private ManagedChannel mockGrpcChannel;

  public static class TestWorkflow extends Workflow {
    @Override
    public void run(WorkflowContext ctx) {
    }
  }

  @BeforeClass
  public static void beforeAll() {
    constructor =
        Constructor.class.cast(Arrays.stream(DaprWorkflowClient.class.getDeclaredConstructors())
            .filter(c -> c.getParameters().length == 2).map(c -> {
              c.setAccessible(true);
              return c;
            }).findFirst().get());
  }

  @Before
  public void setUp() throws Exception {
    mockInnerClient = mock(DurableTaskClient.class);
    mockGrpcChannel = mock(ManagedChannel.class);
    when(mockGrpcChannel.shutdown()).thenReturn(mockGrpcChannel);

    client = constructor.newInstance(mockInnerClient, mockGrpcChannel);
  }

  @Test
  public void EmptyConstructor() {
    assertDoesNotThrow(DaprWorkflowClient::new);
  }

  @Test
  public void scheduleNewWorkflowWithArgName() {
    String expectedName = TestWorkflow.class.getCanonicalName();

    client.scheduleNewWorkflow(TestWorkflow.class);

    verify(mockInnerClient, times(1)).scheduleNewOrchestrationInstance(expectedName);
  }

  @Test
  public void scheduleNewWorkflowWithArgsNameInput() {
    String expectedName = TestWorkflow.class.getCanonicalName();
    Object expectedInput = new Object();

    client.scheduleNewWorkflow(TestWorkflow.class, expectedInput);

    verify(mockInnerClient, times(1))
        .scheduleNewOrchestrationInstance(expectedName, expectedInput);
  }

  @Test
  public void scheduleNewWorkflowWithArgsNameInputInstance() {
    String expectedName = TestWorkflow.class.getCanonicalName();
    Object expectedInput = new Object();
    String expectedInstanceId = "myTestInstance123";

    client.scheduleNewWorkflow(TestWorkflow.class, expectedInput, expectedInstanceId);

    verify(mockInnerClient, times(1))
        .scheduleNewOrchestrationInstance(expectedName, expectedInput, expectedInstanceId);
  }

  @Test
  public void terminateWorkflow() {
    String expectedArgument = "TestWorkflowInstanceId";

    client.terminateWorkflow(expectedArgument, null);
    verify(mockInnerClient, times(1)).terminate(expectedArgument, null);
  }

  @Test
  public void getInstanceState() {

    // Arrange
    String instanceId = "TestWorkflowInstanceId";

    OrchestrationMetadata expectedMetadata = mock(OrchestrationMetadata.class);
    when(expectedMetadata.getInstanceId()).thenReturn(instanceId);
    when(expectedMetadata.getName()).thenReturn("WorkflowName");
    when(expectedMetadata.getRuntimeStatus()).thenReturn(OrchestrationRuntimeStatus.RUNNING);
    when(mockInnerClient.getInstanceMetadata(instanceId, true)).thenReturn(expectedMetadata);

    // Act
    WorkflowState metadata = client.getInstanceState(instanceId, true);

    // Assert
    verify(mockInnerClient, times(1)).getInstanceMetadata(instanceId, true);
    assertNotEquals(metadata, null);
    assertEquals(metadata.getInstanceId(), expectedMetadata.getInstanceId());
    assertEquals(metadata.getName(), expectedMetadata.getName());
    assertEquals(metadata.isRunning(), expectedMetadata.isRunning());
    assertEquals(metadata.isCompleted(), expectedMetadata.isCompleted());
  }

  @Test
  public void waitForInstanceStart() throws TimeoutException {

    // Arrange
    String instanceId = "TestWorkflowInstanceId";
    Duration timeout = Duration.ofSeconds(10);

    OrchestrationMetadata expectedMetadata = mock(OrchestrationMetadata.class);
    when(expectedMetadata.getInstanceId()).thenReturn(instanceId);
    when(mockInnerClient.waitForInstanceStart(instanceId, timeout, true)).thenReturn(expectedMetadata);

    // Act
    WorkflowState result = client.waitForInstanceStart(instanceId, timeout, true);

    // Assert
    verify(mockInnerClient, times(1)).waitForInstanceStart(instanceId, timeout, true);
    assertNotEquals(result, null);
    assertEquals(result.getInstanceId(), expectedMetadata.getInstanceId());
  }

  @Test
  public void waitForInstanceCompletion() throws TimeoutException {

    // Arrange
    String instanceId = "TestWorkflowInstanceId";
    Duration timeout = Duration.ofSeconds(10);

    OrchestrationMetadata expectedMetadata = mock(OrchestrationMetadata.class);
    when(expectedMetadata.getInstanceId()).thenReturn(instanceId);
    when(mockInnerClient.waitForInstanceCompletion(instanceId, timeout, true)).thenReturn(expectedMetadata);

    // Act
    WorkflowState result = client.waitForInstanceCompletion(instanceId, timeout, true);

    // Assert
    verify(mockInnerClient, times(1)).waitForInstanceCompletion(instanceId, timeout, true);
    assertNotEquals(result, null);
    assertEquals(result.getInstanceId(), expectedMetadata.getInstanceId());
  }

  @Test
  public void raiseEvent(){
    String expectedInstanceId="TestWorkflowInstanceId";
    String expectedEventName="TestEventName";
    Object expectedEventPayload=new Object();
    client.raiseEvent(expectedInstanceId,expectedEventName,expectedEventPayload);
    verify(mockInnerClient,times(1)).raiseEvent(expectedInstanceId,
            expectedEventName,expectedEventPayload);
  }

  @Test
  public void purgeInstance(){
    String expectedArgument="TestWorkflowInstanceId";
    client.purgeInstance(expectedArgument);
    verify(mockInnerClient,times(1)).purgeInstance(expectedArgument);
  }

  @Test
  public void createTaskHub(){
    boolean expectedArgument=true;
    client.createTaskHub(expectedArgument);
    verify(mockInnerClient,times(1)).createTaskHub(expectedArgument);
  }

  @Test
  public void deleteTaskHub(){
    client.deleteTaskHub();
    verify(mockInnerClient,times(1)).deleteTaskHub();
  }

  @Test
  public void raiseEvent(){
    String expectedInstanceId="TestWorkflowInstanceId";
    String expectedEventName="TestEventName";
    Object expectedEventPayload=new Object();
    client.raiseEvent(expectedInstanceId,expectedEventName,expectedEventPayload);
    verify(mockInnerClient,times(1)).raiseEvent(expectedInstanceId,
            expectedEventName,expectedEventPayload);
  }

  @Test
  public void purgeInstance(){
    String expectedArgument="TestWorkflowInstanceId";
    client.purgeInstance(expectedArgument);
    verify(mockInnerClient,times(1)).purgeInstance(expectedArgument);
  }

  @Test
  public void createTaskHub(){
    boolean expectedArgument=true;
    client.createTaskHub(expectedArgument);
    verify(mockInnerClient,times(1)).createTaskHub(expectedArgument);
  }

  @Test
  public void deleteTaskHub(){
    client.deleteTaskHub();
    verify(mockInnerClient,times(1)).deleteTaskHub();
  }

  @Test
  public void queryInstances() {

    // Arrange
    OrchestrationMetadata orchestrationMetadata1 = mock(OrchestrationMetadata.class);
    when(orchestrationMetadata1.getInstanceId()).thenReturn("TestWorkflowInstanceId1");
    when(orchestrationMetadata1.getRuntimeStatus()).thenReturn(OrchestrationRuntimeStatus.RUNNING);

    OrchestrationMetadata orchestrationMetadata2 = mock(OrchestrationMetadata.class);
    when(orchestrationMetadata2.getInstanceId()).thenReturn("TestWorkflowInstanceId2");
    when(orchestrationMetadata2.getRuntimeStatus()).thenReturn(OrchestrationRuntimeStatus.COMPLETED);

    List<OrchestrationMetadata> orchestrationMetadataList 
        = Arrays.asList(orchestrationMetadata1, orchestrationMetadata2);

    OrchestrationStatusQueryResult orchestrationStatusQueryResult = mock(OrchestrationStatusQueryResult.class);
    when(orchestrationStatusQueryResult.getOrchestrationState()).thenReturn(orchestrationMetadataList);
    when(orchestrationStatusQueryResult.getContinuationToken()).thenReturn("TestContinuationToken");

    when(mockInnerClient.queryInstances(any(OrchestrationStatusQuery.class)))
        .thenReturn(orchestrationStatusQueryResult);
      
    // Act
    WorkflowStatusQuery statusQuery = new WorkflowStatusQuery();
    WorkflowStatusQueryResult result = client.queryInstances(statusQuery);

    // Assert
    verify(mockInnerClient, times(1)).queryInstances(any(OrchestrationStatusQuery.class));
    assertNotEquals(result, null);

    List<WorkflowState> workflowStates = result.getWorkflowStates();
    assertEquals(workflowStates.size(), 2);
    assertEquals(workflowStates.stream().anyMatch(item -> item.getInstanceId() == orchestrationMetadata1.getInstanceId()
                                          && item.getRuntimeStatus() == WorkflowRuntimeStatus.RUNNING
                                          ), true);

    assertEquals(workflowStates.stream().anyMatch(item -> item.getInstanceId() == orchestrationMetadata2.getInstanceId()
                                          && item.getRuntimeStatus() == WorkflowRuntimeStatus.COMPLETED
                                          ), true);
  }

  @Test
  public void close() throws InterruptedException {
    client.close();
    verify(mockInnerClient, times(1)).close();
    verify(mockGrpcChannel, times(1)).shutdown();
  }
}
