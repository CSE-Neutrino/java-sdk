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

import com.microsoft.durabletask.FailureDetails;

/**
 * Represents a workflow failure details.
 */
public class WorkflowFailureDetails {

    FailureDetails workflowFailureDetails;

    public WorkflowFailureDetails(FailureDetails failureDetails) {
        this.workflowFailureDetails = failureDetails;
    }

    /**
     * Gets the error type, which is the namespace-qualified exception type name.
     * 
     * @return the error type, which is the namespace-qualified exception type name
     */
    public String getErrorType() {
        return workflowFailureDetails.getErrorType();
    }

    /**
     * Gets the error message.
     * 
     * @return the error message
     */
    public String getErrorMessage() {
        return workflowFailureDetails.getErrorMessage();
    }

    /**
     * Gets the stack trace.
     * 
     * @return the stack trace
     */
    public String getStackTrace() {
        return workflowFailureDetails.getStackTrace();
    }

    public boolean isNonRetriable() {
        return workflowFailureDetails.isNonRetriable();
    }

    /**
     * Returns <c>true</c> if the failure was caused by the specified exception
     * type.
     * 
     * This method allows checking if a workflow task failed due to an exception of
     * a specific type by attempting to load the type specified in
     * <see cref="ErrorType"/>. If the exception type cannot be loaded for any
     * reason, this method will return <c>false</c>. Base types are supported.
     * 
     * @param exceptionClass The type of exception to test against
     * 
     * @return the inner exception details
     */
    public boolean isCausedBy(Class<? extends Exception> exceptionClass) {
        return workflowFailureDetails.isCausedBy(exceptionClass);
    }

    @Override
    public String toString() {
        return workflowFailureDetails.toString();
    }
}
