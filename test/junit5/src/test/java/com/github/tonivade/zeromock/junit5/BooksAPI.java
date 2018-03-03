/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

import static com.github.tonivade.zeromock.core.Combinators.force;
import static com.github.tonivade.zeromock.core.Combinators.join;
import static com.github.tonivade.zeromock.core.Combinators.split;
import static com.github.tonivade.zeromock.core.Extractors.asInteger;
import static com.github.tonivade.zeromock.core.Extractors.asString;
import static com.github.tonivade.zeromock.core.Extractors.body;
import static com.github.tonivade.zeromock.core.Extractors.pathParam;
import static com.github.tonivade.zeromock.core.Handlers.contentJson;
import static com.github.tonivade.zeromock.core.Handlers.created;
import static com.github.tonivade.zeromock.core.Handlers.ok;
import static com.github.tonivade.zeromock.core.Serializers.json;

import java.util.function.Function;

import com.github.tonivade.zeromock.core.HttpRequest;
import com.github.tonivade.zeromock.core.HttpResponse;

public class BooksAPI {
  
  private final BooksService service;

  public BooksAPI(BooksService service) {
    this.service = service;
  }

  public Function<HttpRequest, HttpResponse> findAll() {
    return okJson(force(service::findAll));
  }

  public Function<HttpRequest, HttpResponse> update() {
    return okJson(join(getBookId(), getBookTitle()).andThen(split(service::update)));
  }

  public Function<HttpRequest, HttpResponse> find() {
    return okJson(getBookId().andThen(service::find));
  }

  public Function<HttpRequest, HttpResponse> create() {
    return createdJson(getBookTitle().andThen(service::create));
  }

  public Function<HttpRequest, HttpResponse> delete() {
    return okJson(getBookId().andThen(force(service::delete)));
  }

  private static Function<HttpRequest, Integer> getBookId() {
    return pathParam(1).andThen(asInteger());
  }

  private static Function<HttpRequest, String> getBookTitle() {
    return body().andThen(asString());
  }
  
  private static <T> Function<HttpRequest, HttpResponse> okJson(Function<HttpRequest, T> handler) {
    return ok(handler.andThen(json())).andThen(contentJson());
  }
  
  private static <T> Function<HttpRequest, HttpResponse> createdJson(Function<HttpRequest, T> handler) {
    return created(handler.andThen(json())).andThen(contentJson());
  }
}
