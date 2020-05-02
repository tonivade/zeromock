/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

import static com.github.tonivade.zeromock.api.Extractors.asInteger;
import static com.github.tonivade.zeromock.api.Extractors.asString;
import static com.github.tonivade.zeromock.api.Extractors.body;
import static com.github.tonivade.zeromock.api.Extractors.pathParam;
import static com.github.tonivade.zeromock.api.Headers.contentJson;
import static com.github.tonivade.zeromock.api.Serializers.empty;
import static com.github.tonivade.zeromock.api.Serializers.objectToJson;

import com.github.tonivade.purefun.Consumer1;
import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Function2;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.type.Try;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.RequestHandler;
import com.github.tonivade.zeromock.api.Responses;

public class BooksAPI {
  
  private final BooksService service;

  public BooksAPI(BooksService service) {
    this.service = service;
  }

  public RequestHandler findAll() {
    return Producer.of(service::findAll)
        .asFunction()
        .andThen(ImmutableList::toList)
        .andThen(objectToJson())
        .andThen(Responses::ok)
        .andThen(contentJson())::apply;
  }

  public RequestHandler update() {
    return Function2.of(service::update)
        .compose(getBookId(), getBookTitle())
        .liftTry()
        .andThen(map(objectToJson()))
        .andThen(map(Responses::ok))
        .andThen(getOrElse(Responses::error))
        .andThen(contentJson())::apply;
  }

  public RequestHandler find() {
    return Function1.of(service::find)
        .compose(getBookId())
        .andThen(x -> x.map(objectToJson()))
        .andThen(x -> x.map(Responses::ok))
        .andThen(x -> x.getOrElse(Responses::noContent))
        .andThen(contentJson())::apply;
  }

  public RequestHandler create() {
    return getBookTitle()
        .andThen(service::create)
        .liftTry()
        .andThen(map(objectToJson()))
        .andThen(map(Responses::created))
        .andThen(getOrElse(Responses::error))
        .andThen(contentJson())::apply;
  }

  public RequestHandler delete() {
    return getBookId()
        .andThen(Consumer1.of(service::delete).asFunction())
        .liftTry()
        .andThen(map(empty()))
        .andThen(map(Responses::ok))
        .andThen(getOrElse(Responses::error))
        .andThen(contentJson())::apply;
  }

  private static Function1<HttpRequest, Integer> getBookId() {
    return pathParam(1).andThen(asInteger());
  }

  private static Function1<HttpRequest, String> getBookTitle() {
    return body().andThen(asString());
  }

  private <T> Function1<Try<T>, T> getOrElse(Producer<T> response) {
    return x -> x.getOrElse(response);
  }

  private <T, R> Function1<Try<T>, Try<R>> map(Function1<T, R> mapper) {
    return x -> x.map(mapper);
  }
}
