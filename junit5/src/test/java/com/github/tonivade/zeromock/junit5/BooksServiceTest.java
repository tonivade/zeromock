/*
 * Copyright (c) 2018-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

import static com.github.tonivade.purefun.type.Option.some;
import static com.github.tonivade.purefun.type.Try.success;
import static com.github.tonivade.zeromock.api.Bytes.empty;
import static com.github.tonivade.zeromock.api.Deserializers.jsonToObject;
import static com.github.tonivade.zeromock.api.Matchers.body;
import static com.github.tonivade.zeromock.api.Matchers.delete;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.api.Matchers.post;
import static com.github.tonivade.zeromock.api.Matchers.put;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Type;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;
import com.github.tonivade.purejson.TypeToken;
import com.github.tonivade.zeromock.api.Bytes;
import com.github.tonivade.zeromock.api.Deserializers;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpService;
import com.github.tonivade.zeromock.api.HttpStatus;
import com.github.tonivade.zeromock.api.Requests;
import com.github.tonivade.zeromock.client.HttpClient;
import com.github.tonivade.zeromock.junit5.BooksService.Book;
import com.github.tonivade.zeromock.server.MockHttpServer;

@ExtendWith(MockHttpServerExtension.class)
public class BooksServiceTest {

  private final BooksAPI books = new BooksAPI(new BooksService());

  private final HttpService booksService = new HttpService("books")
      .when(get("/books")).then(books.findAll())
      .when(get("/books/:id")).then(books.find())
      .when(post("/books")).then(books.create())
      .when(delete("/books/:id")).then(books.delete())
      .when(put("/books/:id")).then(books.update());

  @Test
  public void findsBooks(MockHttpServer server, HttpClient client) {
    server.mount("/store", booksService);

    HttpResponse response = client.request(Requests.get("/store/books"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(success(some(singletonList(new Book(1, "title")))), asBooks(response.body())));
  }

  @Test
  public void findsBook(MockHttpServer server, HttpClient client) {
    server.mount("/store", booksService);

    HttpResponse response = client.request(Requests.get("/store/books/1"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(success(some(new Book(1, "title"))), asBook(response.body())));
  }

  @Test
  public void createsBook(MockHttpServer server, HttpClient client) {
    server.mount("/store", booksService);

    HttpResponse response = client.request(Requests.post("/store/books").withBody("create"));

    assertAll(() -> assertEquals(HttpStatus.CREATED, response.status()),
              () -> assertEquals(success(some(new Book(1, "create"))), asBook(response.body())),
              () -> server.verify(post("/store/books").and(body("create"))));
  }

  @Test
  public void deletesBook(MockHttpServer server, HttpClient client) {
    server.mount("/store", booksService);

    HttpResponse response = client.request(Requests.delete("/store/books/1"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(empty(), response.body()));
  }

  @Test
  public void updatesBook(MockHttpServer server, HttpClient client) {
    server.mount("/store", booksService);

    HttpResponse response = client.request(Requests.put("/store/books/1").withBody("update"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(success(some(new Book(1, "update"))), asBook(response.body())));
  }

  private Try<Option<Book>> asBook(Bytes body) {
    return jsonToObject(Book.class).apply(body);
  }

  private Try<Option<List<Book>>> asBooks(Bytes body) {
    return Deserializers.<List<Book>>jsonTo(listOfBooks()).apply(body);
  }

  private Type listOfBooks() {
    return new TypeToken<List<Book>>(){}.getType();
  }
}
