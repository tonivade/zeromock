/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.Extractors.asInteger;
import static com.github.tonivade.zeromock.Extractors.body;
import static com.github.tonivade.zeromock.Extractors.pathParam;
import static com.github.tonivade.zeromock.Handlers.force;
import static com.github.tonivade.zeromock.Handlers.join;
import static com.github.tonivade.zeromock.Handlers.split;

import java.util.function.Function;
import java.util.function.Supplier;

import com.github.tonivade.zeromock.BooksService.Book;

public class BooksAPI {
  
  private final BooksService service;

  public BooksAPI(BooksService service) {
    this.service = service;
  }

  public Supplier<Object> findAll() {
    return service::findAll;
  }

  public Function<HttpRequest, Book> update() {
    return join(getBookId(), getBookTitle()).andThen(split(service::update));
  }

  public Function<HttpRequest, Book> find() {
    return getBookId().andThen(service::find);
  }

  public Function<HttpRequest, Book> create() {
    return getBookTitle().andThen(service::create);
  }

  public Function<HttpRequest, Void> delete() {
    return getBookId().andThen(force(service::delete));
  }

  private static Function<HttpRequest, Integer> getBookId() {
    return pathParam(1).andThen(asInteger());
  }

  private static Function<HttpRequest, String> getBookTitle() {
    return body().andThen(Bytes::asString);
  }
}
