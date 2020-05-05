/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Operator1;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.type.Id;

import static com.github.tonivade.purefun.Function1.identity;

@FunctionalInterface
public interface RequestHandler extends Function1<HttpRequest, HttpResponse> {

  default AsyncRequestHandler async() {
    return request -> Future.async(() -> apply(request));
  }

  default SyncRequestHandler sync() {
    return request -> Id.of(apply(request));
  }

  default RequestHandler preHandle(PreFilter before) {
    return request -> before.apply(request).fold(identity(), this::apply);
  }

  default RequestHandler postHandle(PostFilter after) {
    return andThen(after)::apply;
  }
}
