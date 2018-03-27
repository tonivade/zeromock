/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

import static com.github.tonivade.zeromock.core.Handler2.adapt;
import static com.github.tonivade.zeromock.core.Extractors.asInteger;
import static com.github.tonivade.zeromock.core.Extractors.asString;
import static com.github.tonivade.zeromock.core.Extractors.body;
import static com.github.tonivade.zeromock.core.Extractors.pathParam;
import static com.github.tonivade.zeromock.core.Handler1.adapt;
import static com.github.tonivade.zeromock.core.Handlers.created;
import static com.github.tonivade.zeromock.core.Handlers.ok;
import static com.github.tonivade.zeromock.core.Headers.contentJson;
import static com.github.tonivade.zeromock.core.Serializers.empty;
import static com.github.tonivade.zeromock.core.Serializers.json;
import static java.util.stream.Collectors.toList;

import com.github.tonivade.zeromock.core.Handler1;
import com.github.tonivade.zeromock.core.OptionalHandler;
import com.github.tonivade.zeromock.core.RequestHandler;
import com.github.tonivade.zeromock.core.HttpRequest;
import com.github.tonivade.zeromock.core.Responses;
import com.github.tonivade.zeromock.core.StreamHandler;

public class BooksAPI {
  
  private final BooksService service;

  public BooksAPI(BooksService service) {
    this.service = service;
  }

  public RequestHandler findAll() {
    return okJson(adapt(service::findAll)::handle);
  }

  public RequestHandler update() {
    return okJson(adapt(service::update).compose(getBookId(), getBookTitle()));
  }

  public RequestHandler find() {
    return okOrNoContentJson(getBookId().andThen(service::find)::handle);
  }

  public RequestHandler create() {
    return createdJson(getBookTitle().andThen(service::create));
  }

  public RequestHandler delete() {
    return okEmpty(getBookId().andThen(adapt(service::delete)));
  }

  private static Handler1<HttpRequest, Integer> getBookId() {
    return pathParam(1).andThen(asInteger());
  }

  private static Handler1<HttpRequest, String> getBookTitle() {
    return body().andThen(asString());
  }
  
  private static <T> RequestHandler okJson(Handler1<HttpRequest, T> handler) {
    return ok(handler.andThen(json())).postHandle(contentJson());
  }
  
  private static <T> RequestHandler okJson(StreamHandler<HttpRequest, T> handler) {
    return ok(handler.collect(toList()).andThen(json())).postHandle(contentJson());
  }
  
  private static <T> RequestHandler okOrNoContentJson(OptionalHandler<HttpRequest, T> handler) {
    return okOrNoContent(handler).postHandle(contentJson());
  }

  private static <T> RequestHandler okOrNoContent(OptionalHandler<HttpRequest, T> handler) {
    return handler.map(json()).map(Responses::ok).orElse(Responses::noContent)::handle;
  }
  
  private static <T> RequestHandler okEmpty(Handler1<HttpRequest, T> handler) {
    return ok(handler.andThen(empty())).postHandle(contentJson());
  }
  
  private static <T> RequestHandler createdJson(Handler1<HttpRequest, T> handler) {
    return created(handler.andThen(json())).postHandle(contentJson());
  }
}
