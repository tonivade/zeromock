/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Operator1;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.type.Id;

@FunctionalInterface
public interface RequestHandler extends Function1<HttpRequest, HttpResponse> {

  default AsyncRequestHandler async() {
    return request -> Future.async(() -> apply(request));
  }

  default SyncRequestHandler sync() {
    return request -> Id.of(apply(request));
  }

  default RequestHandler preHandle(Operator1<HttpRequest> before) {
    return compose(before)::apply;
  }

  default RequestHandler postHandle(Operator1<HttpResponse> after) {
    return andThen(after)::apply;
  }
}
