/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

public class BooksModule {

  public Object findAllBooks(HttpRequest request) {
    return "find all books";
  }

  public Object findBook(HttpRequest request) {
    return "find one book " + request.path.getAt(1);
  }

  public Object createBook(HttpRequest request) {
    return "book created";
  }

  public Object updateBook(HttpRequest request) {
    return "book updated " + request.path.getAt(1);
  }

  public Object deleteBook(HttpRequest request) {
    return "book deleted " + request.path.getAt(1);
  }
}
