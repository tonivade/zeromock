/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

import static com.github.tonivade.purefun.Precondition.checkNonEmpty;
import static com.github.tonivade.purefun.Precondition.checkPositive;

import java.util.Objects;

import com.github.tonivade.purefun.Equal;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.type.Option;

public class BooksService {

  public ImmutableList<Book> findAll() {
    return ImmutableList.of(new Book(1, "title"));
  }

  public Option<Book> find(Integer id) {
    return Option.some(new Book(id, "title"));
  }

  public Book create(String title) {
    return new Book(1, title);
  }

  public Book update(Integer id, String title) {
    return new Book(id, title);
  }

  public void delete(Integer id) {
    // nothing to do
  }

  public static class Book {

    private final int id;
    private final String title;

    public Book(int id, String title) {
      this.id = checkPositive(id);
      this.title = checkNonEmpty(title);
    }

    @Override
    public String toString() {
      return "Book(id:" + id + ",title:" + title + ")";
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, title);
    }

    @Override
    public boolean equals(Object obj) {
      return Equal.<Book>of()
          .comparing(b -> b.id)
          .comparing(b -> b.title)
          .applyTo(this, obj);
    }
  }
}
