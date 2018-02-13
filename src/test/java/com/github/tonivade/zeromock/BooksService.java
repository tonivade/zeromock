/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static java.util.Arrays.asList;

import java.util.List;

public class BooksService {

  public List<Book> findAll() {
    return asList(new Book(1, "title"));
  }

  public Book find(Integer id) {
    return new Book(id, "title");
  }

  public Book create(String title) {
    return new Book(1, title);
  }

  public Book update(Integer id, String title) {
    return new Book(id, title);
  }

  public void delete(Integer id) {
  }
  
  public static class Book {
    private final Integer id;
    private final String title;

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
