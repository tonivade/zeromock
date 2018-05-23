/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

import static com.github.tonivade.zeromock.api.Extractors.asInteger;
import static com.github.tonivade.zeromock.api.Extractors.asString;
import static com.github.tonivade.zeromock.api.Extractors.body;
import static com.github.tonivade.zeromock.api.Extractors.pathParam;
import static com.github.tonivade.zeromock.api.Headers.contentJson;
import static com.github.tonivade.zeromock.api.Serializers.empty;
import static com.github.tonivade.zeromock.api.Serializers.json;
import static java.util.stream.Collectors.toList;

import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.RequestHandler;
import com.github.tonivade.zeromock.api.Responses;
import com.github.tonivade.zeromock.core.Handler1;

public class BooksAPI {
  
  private final BooksService service;

  public BooksAPI(BooksService service) {
    this.service = service;
  }

  public RequestHandler findAll() {
    return service.findAll()
        .collect(toList())
        .andThen(json())
        .andThen(Responses::ok)
        .andThen(contentJson())::handle;
  }

  public RequestHandler update() {
    return service.update().compose(getBookId(), getBookTitle())
        .liftTry()
        .map(json())
        .map(Responses::ok)
        .orElse(Responses::error)
        .andThen(contentJson())::handle;
  }

  public RequestHandler find() {
    return getBookId()
        .andThen(service.find())
        .liftOption()
        .map(json())
        .map(Responses::ok)
        .orElse(Responses::noContent)
        .andThen(contentJson())::handle;
  }

  public RequestHandler create() {
    return getBookTitle()
        .andThen(service.create())
        .liftTry()
        .map(json())
        .map(Responses::created)
        .orElse(Responses::error)
        .andThen(contentJson())::handle;
  }

  public RequestHandler delete() {
    return getBookId().andThen(service.delete())
        .liftTry()
        .map(empty())
        .map(Responses::ok)
        .orElse(Responses::error)
        .andThen(contentJson())::handle;
  }

  private static Handler1<HttpRequest, Integer> getBookId() {
    return pathParam(1).andThen(asInteger());
  }

  private static Handler1<HttpRequest, String> getBookTitle() {
    return body().andThen(asString());
  }
}
