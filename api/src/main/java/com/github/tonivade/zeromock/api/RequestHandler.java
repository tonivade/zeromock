/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.zeromock.core.Handler1;

@FunctionalInterface
public interface RequestHandler extends Handler1<HttpRequest, HttpResponse> {
  
  default RequestHandler preHandle(Handler1<HttpRequest, HttpRequest> before) {
    return compose(before)::handle;
  }
  
  default RequestHandler postHandle(Handler1<HttpResponse, HttpResponse> after) {
    return andThen(after)::handle;
  }
}
