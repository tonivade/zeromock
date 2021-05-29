/*
 * Copyright (c) 2018-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.Function1.identity;
import com.github.tonivade.purefun.Function1;

@FunctionalInterface
public interface RequestHandler extends Function1<HttpRequest, HttpResponse> {

  default RequestHandler preHandle(PreFilter before) {
    return request -> before.apply(request).fold(identity(), this::apply);
  }

  default RequestHandler postHandle(PostFilter after) {
    return andThen(after)::apply;
  }
  
  default AsyncRequestHandler async() {
    return liftFuture()::apply;
  }
}
