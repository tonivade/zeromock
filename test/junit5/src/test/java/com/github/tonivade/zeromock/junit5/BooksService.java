/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

import java.util.Objects;
import java.util.stream.Stream;

import com.github.tonivade.zeromock.core.Handler2;
import com.github.tonivade.zeromock.core.Option;
import com.github.tonivade.zeromock.core.OptionHandler;
import com.github.tonivade.zeromock.core.StreamHandler;
import com.github.tonivade.zeromock.core.Try;
import com.github.tonivade.zeromock.core.TryHandler;

public class BooksService {

  public <T> StreamHandler<T, Book> findAll() {
    return ignore -> Stream.of(new Book(1, "title"));
  }

  public OptionHandler<Integer, Book> find() {
    return id -> Option.some(new Book(id, "title"));
  }

  public TryHandler<String, Book> create() {
    return title -> Try.success(new Book(1, title));
  }

  public Handler2<Integer, String, Try<Book>> update() {
    return (id, title) -> Try.success(new Book(id, title));
  }

  public TryHandler<Integer, Void> delete() {
    return id -> Try.success(null);
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

    @Override
    public int hashCode() {
      return Objects.hash(id, title);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Book other = (Book) obj;
      return Objects.equals(this.id, other.id) 
          && Objects.equals(this.title, other.title);
    }
  }
}
