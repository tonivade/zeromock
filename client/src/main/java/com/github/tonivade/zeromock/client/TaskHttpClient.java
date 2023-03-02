/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

import static com.github.tonivade.purefun.effect.TaskOf.toTask;
import static com.github.tonivade.purefun.typeclasses.Instances.async;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Type;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.effect.Task;
import com.github.tonivade.purefun.effect.Task_;
import com.github.tonivade.purejson.PureJson;
import com.github.tonivade.zeromock.api.Bytes;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;

public class TaskHttpClient implements HttpClientOf<Task_> {

  private final HttpClientK<Task_> client;

  private TaskHttpClient(HttpClientK<Task_> client) {
    this.client = requireNonNull(client);
  }

  public static TaskHttpClient connectTo(String baseUrl) {
    return new TaskHttpClient(new HttpClientK<>(baseUrl, async(Task_.class)));
  }

  public Task<HttpResponse> request(HttpRequest request) {
    return client.request(request).fix(toTask());
  }
  
  public static <T> Function1<HttpResponse, Task<T>> parse(Class<T> type) {
    return parse((Type) type);
  }

  public static <T> Function1<HttpResponse, Task<T>> parse(Type type) {
    return response -> Task.fromTry(new PureJson<T>(type).fromJson(Bytes.asString(response.body())))
        .flatMap(Task::fromOption);
  }
}
