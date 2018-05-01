/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server.admin;

import static com.github.tonivade.zeromock.core.Handler1.adapt;
import static com.github.tonivade.zeromock.core.Handlers.ok;
import static com.github.tonivade.zeromock.core.Headers.contentJson;
import static com.github.tonivade.zeromock.core.Serializers.json;

import com.github.tonivade.zeromock.core.Handler1;
import com.github.tonivade.zeromock.core.HttpRequest;
import com.github.tonivade.zeromock.core.RequestHandler;

public class AdminAPI {
  private final AdminService service;
  
  public AdminAPI(AdminService service) {
    this.service = service;
  }
  
  public RequestHandler unmatched() {
    return okJson(adapt(service::getUnmatched));
  }
  
  public RequestHandler matched() {
    return okJson(adapt(service::getMatched));
  }
  
  private static <T> RequestHandler okJson(Handler1<HttpRequest, T> handler) {
    return ok(handler.andThen(json())).postHandle(contentJson());
  }
}
