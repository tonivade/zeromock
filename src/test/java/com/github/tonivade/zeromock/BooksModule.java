/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static java.util.Arrays.asList;

import java.util.List;

public class BooksModule {

  public List<Book> findAllBooks() {
    return asList(new Book(1, "title"));
  }

  public Book findBook(Integer id) {
    return new Book(id, "title");
  }

  public Book createBook(String title) {
    return new Book(1, title);
  }

  public Object updateBook(Integer id, String title) {
    return new Book(id, title);
  }

  public Void deleteBook(Integer id) {
    return null;
  }
  
  public static class Book {
    final Integer id;
    final String title;

    public Book(Integer id, String title) {
      this.id = id;
      this.title = title;
    }
    
    @Override
    public String toString() {
      return "Book(id:" + id + ",title:" + title + ")";
    }
  }
}
