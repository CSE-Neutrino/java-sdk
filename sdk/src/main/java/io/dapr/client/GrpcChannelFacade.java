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

package io.dapr.client;

import io.dapr.v1.DaprGrpc;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;

/**
 * Facade for common operations on gRPC channel.
 *
 * @see DaprGrpc
 * @see DaprClient
 */
class GrpcChannelFacade implements Closeable {

  /**
   * The GRPC managed channel to be used.
   */
  private final ManagedChannel channel;


  /**
   * Default access level constructor, in order to create an instance of this class use io.dapr.client.DaprClientBuilder
   *
   * @param channel A Managed GRPC channel
   * @see DaprClientBuilder
   */
  GrpcChannelFacade(ManagedChannel channel) {
    this.channel = channel;
  }

  @Override
  public void close() throws IOException {
    if (channel != null && !channel.isShutdown()) {
      channel.shutdown();
    }
  }

  public Mono<Void> waitForChannelReady(int timeoutInMilliseconds) {
    return Mono.fromRunnable(() -> {
      boolean isReady = false;
      long startTime = System.currentTimeMillis();
      while (!isReady && System.currentTimeMillis() - startTime < timeoutInMilliseconds) {
        isReady = this.channel.getState(true) == ConnectivityState.READY;
        if (!isReady) {
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Waiting for gRPC channel ready interrupted.", e);
          }
        }
      }

      if (!isReady) {
        throw new RuntimeException("Timeout waiting for gRPC channel to be ready.");
      }
    });
  }
}
