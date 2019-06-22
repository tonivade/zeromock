/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import java.util.concurrent.Executor;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.type.Id;

@FunctionalInterface
public interface RequestHandler extends Function1<HttpRequest, HttpResponse> {

  default AsyncRequestHandler async() {
    return async(Future.DEFAULT_EXECUTOR);
  }

  default AsyncRequestHandler async(Executor executor) {
    return request -> Future.run(executor, () -> apply(request));
  }

  default SyncRequestHandler sync() {
    return request -> Id.of(apply(request));
  }

  default RequestHandler preHandle(Function1<HttpRequest, HttpRequest> before) {
    return compose(before)::apply;
  }

  default RequestHandler postHandle(Function1<HttpResponse, HttpResponse> after) {
    return andThen(after)::apply;
  }
}
