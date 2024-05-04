/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

import static com.github.tonivade.purefun.core.Precondition.checkNonEmpty;
import static com.github.tonivade.purefun.core.Precondition.checkPositive;

import java.util.Objects;

import com.github.tonivade.purefun.core.Equal;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purejson.JsonCreator;
import com.github.tonivade.purejson.JsonProperty;

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

    @JsonCreator
    public Book(@JsonProperty("id") int id, @JsonProperty("title") String title) {
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
