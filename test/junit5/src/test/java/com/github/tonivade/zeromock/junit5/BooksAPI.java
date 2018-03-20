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

import com.github.tonivade.zeromock.core.Handler1;
import com.github.tonivade.zeromock.core.OptionalHandler;
import com.github.tonivade.zeromock.core.HttpRequest;
import com.github.tonivade.zeromock.core.HttpResponse;
import com.github.tonivade.zeromock.core.Responses;

public class BooksAPI {
  
  private final BooksService service;

  public BooksAPI(BooksService service) {
    this.service = service;
  }

  public Handler1<HttpRequest, HttpResponse> findAll() {
    return okJson(adapt(service::findAll));
  }

  public Handler1<HttpRequest, HttpResponse> update() {
    return okJson(adapt(service::update).compose(getBookId(), getBookTitle()));
  }

  public Handler1<HttpRequest, HttpResponse> find() {
    return okOrNoContentJson(getBookId().andThen(service::find)::handle);
  }

  public Handler1<HttpRequest, HttpResponse> create() {
    return createdJson(getBookTitle().andThen(service::create));
  }

  public Handler1<HttpRequest, HttpResponse> delete() {
    return okEmpty(getBookId().andThen(adapt(service::delete)));
  }

  private static Handler1<HttpRequest, Integer> getBookId() {
    return pathParam(1).andThen(asInteger());
  }

  private static Handler1<HttpRequest, String> getBookTitle() {
    return body().andThen(asString());
  }
  
  private static <T> Handler1<HttpRequest, HttpResponse> okJson(Handler1<HttpRequest, T> handler) {
    return ok(handler.andThen(json())).andThen(contentJson());
  }
  
  private static <T> Handler1<HttpRequest, HttpResponse> okOrNoContentJson(OptionalHandler<HttpRequest, T> handler) {
    return handler.map(json()).map(Responses::ok).orElse(Responses::noContent).andThen(contentJson());
  }
  
  private static <T> Handler1<HttpRequest, HttpResponse> okEmpty(Handler1<HttpRequest, T> handler) {
    return ok(handler.andThen(empty())).andThen(contentJson());
  }
  
  private static <T> Handler1<HttpRequest, HttpResponse> createdJson(Handler1<HttpRequest, T> handler) {
    return created(handler.andThen(json())).andThen(contentJson());
  }
}
