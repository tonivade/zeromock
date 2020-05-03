/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import com.github.tonivade.purefun.effect.Task;
import com.github.tonivade.purefun.instances.TaskInstances;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;

import static java.util.Objects.requireNonNull;

public class TaskHttpClient {

  private final HttpClientK<Task.µ> client;

  public TaskHttpClient(HttpClientK<Task.µ> client) {
    this.client = requireNonNull(client);
  }

  public static TaskHttpClient connectTo(String baseUrl) {
    return new TaskHttpClient(new HttpClientK<>(baseUrl, TaskInstances.monadDefer()));
  }

  public Task<HttpResponse> request(HttpRequest request) {
    return client.request(request).fix1(Task::narrowK);
  }
}
