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

import com.microsoft.durabletask.OrchestrationMetadata;
import com.microsoft.durabletask.OrchestrationRuntimeStatus;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.time.Instant;

public class WorkflowMetadataTest {

  private OrchestrationMetadata mockOrchestrationMetadata;
  private WorkflowMetadata workflowMetadata;

  @Before
  public void setUp() throws Exception {
    mockOrchestrationMetadata = mock(OrchestrationMetadata.class);
    workflowMetadata = new WorkflowMetadata(mockOrchestrationMetadata);
  }

  @Test
  public void getInstanceId() {
    // Arrange
    when(mockOrchestrationMetadata.getInstanceId()).thenReturn("instanceId");

    // Act
    
    // Assert
    assertEquals(workflowMetadata.getInstanceId(), mockOrchestrationMetadata.getInstanceId());
  }

  @Test
  public void getName() {
    // Arrange
    when(mockOrchestrationMetadata.getName()).thenReturn("WorkflowName");
    
    // Act
    // Assert
    assertEquals(workflowMetadata.getName(), mockOrchestrationMetadata.getName());
  }


  @Test
    public void getCreatedAt() {
      // Arrange
      when(mockOrchestrationMetadata.getCreatedAt()).thenReturn(Instant.now());

      // Act
      // Assert
      assertEquals(workflowMetadata.getCreatedAt(), mockOrchestrationMetadata.getCreatedAt());
    }

    @Test
    public void getLastUpdatedAt() {
      // Arrange
      when(mockOrchestrationMetadata.getLastUpdatedAt()).thenReturn(Instant.now());
      
      // Act
      // Assert
      assertEquals(workflowMetadata.getLastUpdatedAt(), mockOrchestrationMetadata.getLastUpdatedAt());
    }

    @Test
    public void getFailureDetails() {
      // Arrange
      // Act
      // Assert
      assertEquals(workflowMetadata.getFailureDetails(), mockOrchestrationMetadata.getFailureDetails());
    }

    @Test
    public void getRuntimeStatus() {
      // Arrange
      when(mockOrchestrationMetadata.getRuntimeStatus()).thenReturn(OrchestrationRuntimeStatus.RUNNING);

      // Act
      WorkflowRuntimeStatus result = workflowMetadata.getRuntimeStatus();
      
      // Assert
      assertEquals(result, WorkflowRuntimeStatus.RUNNING);
      verify(mockOrchestrationMetadata, times(1)).getRuntimeStatus();
    }

    @Test
    public void isRunning() {
      // Arrange
      when(mockOrchestrationMetadata.isRunning()).thenReturn(true);

      // Act
      // Assert
      assertEquals(workflowMetadata.isRunning(), mockOrchestrationMetadata.isRunning());
    }

    @Test
    public void isCompleted() {
      // Arrange
      when(mockOrchestrationMetadata.isCompleted()).thenReturn(false);

      // Act
      // Assert
      assertEquals(workflowMetadata.isCompleted(), mockOrchestrationMetadata.isCompleted());
    }

    @Test
    public void isCustomStatusFetched() {
      // Arrange
      when(mockOrchestrationMetadata.isCustomStatusFetched()).thenReturn(false);

      // Act
      // Assert
      assertEquals(workflowMetadata.isCustomStatusFetched(), mockOrchestrationMetadata.isCustomStatusFetched());
    }

    @Test
    public void isInstanceFound() {
      // Arrange
      when(mockOrchestrationMetadata.isInstanceFound()).thenReturn(true);

      // Act
      // Assert
      assertEquals(workflowMetadata.isInstanceFound(), mockOrchestrationMetadata.isInstanceFound());
    }

    @Test
    public void getSerializedInput() {
      // Arrange
      when(mockOrchestrationMetadata.getSerializedInput()).thenReturn("{input: \"test\"}");

      // Act
      // Assert
      assertEquals(workflowMetadata.getSerializedInput(), mockOrchestrationMetadata.getSerializedInput());
    }

    @Test
    public void getSerializedOutput() {
      // Arrange
      when(mockOrchestrationMetadata.getSerializedOutput()).thenReturn("{output: \"test\"}");

      // Act
      // Assert
      assertEquals(workflowMetadata.getSerializedOutput(), mockOrchestrationMetadata.getSerializedOutput());
    }

    @Test
    public void readCustomStatusAs() {
      // Arrange
      when(mockOrchestrationMetadata.readCustomStatusAs(String.class)).thenReturn("customStatus");

      // Act
      // Assert
      assertEquals(workflowMetadata.readCustomStatusAs(String.class), mockOrchestrationMetadata.readCustomStatusAs(String.class));
      assertEquals(true, true);
    }

    @Test
    public void readInputAs() {
      // Arrange
      when(mockOrchestrationMetadata.readInputAs(String.class)).thenReturn("input");

      // Act
      // Assert
      assertEquals(workflowMetadata.readInputAs(String.class), mockOrchestrationMetadata.readInputAs(String.class));
      assertEquals(true, true);
    }

    @Test
    public void readOutputAs() {
      // Arrange
      when(mockOrchestrationMetadata.readOutputAs(String.class)).thenReturn("output");

      // Act
      // Assert
      assertEquals(workflowMetadata.readOutputAs(String.class), mockOrchestrationMetadata.readOutputAs(String.class));
    }

    @Test
    public void testToString() {
      // Arrange
      when(mockOrchestrationMetadata.toString()).thenReturn("toString");

      // Act
      // Assert
      assertEquals(workflowMetadata.toString(), mockOrchestrationMetadata.toString());
    }
}
