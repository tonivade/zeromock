/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Function1;

@FunctionalInterface
public interface RequestHandler extends Function1<HttpRequest, HttpResponse> {
  
  default RequestHandler preHandle(Function1<HttpRequest, HttpRequest> before) {
    return compose(before)::apply;
  }
  
  default RequestHandler postHandle(Function1<HttpResponse, HttpResponse> after) {
    return andThen(after)::apply;
  }
}
