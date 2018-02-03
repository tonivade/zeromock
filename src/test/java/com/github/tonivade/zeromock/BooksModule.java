/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import java.util.Map;

public class BooksModule {

  public String findAllBooks(Map<String, String> params) {
    return "find all books";
  }

  public String findBook(Map<String, String> params) {
    return "find one book " + params.get("id");
  }

  public String createBook(Map<String, String> params) {
    return "book created";
  }

  public String updateBook(Map<String, String> params) {
    return "book updated " + params.get("id");
  }

  public String deleteBook(Map<String, String> params) {
    return "book deleted " + params.get("id");
  }
}
