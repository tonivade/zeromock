/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

import static com.github.tonivade.zeromock.api.Bytes.empty;
import static com.github.tonivade.zeromock.api.Deserializers.jsonToObject;
import static com.github.tonivade.zeromock.api.Matchers.body;
import static com.github.tonivade.zeromock.api.Matchers.delete;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.api.Matchers.post;
import static com.github.tonivade.zeromock.api.Matchers.put;
import static com.github.tonivade.zeromock.client.HttpClient.connectTo;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Type;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.tonivade.zeromock.api.Bytes;
import com.github.tonivade.zeromock.api.Deserializers;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpService;
import com.github.tonivade.zeromock.api.HttpStatus;
import com.github.tonivade.zeromock.api.Requests;
import com.github.tonivade.zeromock.junit5.BooksService.Book;
import com.github.tonivade.zeromock.server.MockHttpServer;
import com.google.gson.reflect.TypeToken;

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
  public void findsBooks(MockHttpServer server) {
    server.mount("/store", booksService);

    HttpResponse response = connectTo("http://localhost:8080/store").request(Requests.get("/books"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(singletonList(new Book(1, "title")), asBooks(response.body())));
  }

  @Test
  public void findsBook(MockHttpServer server) {
    server.mount("/store", booksService);

    HttpResponse response = connectTo("http://localhost:8080/store").request(Requests.get("/books/1"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(new Book(1, "title"), asBook(response.body())));
  }

  @Test
  public void createsBook(MockHttpServer server) {
    server.mount("/store", booksService);

    HttpResponse response =
        connectTo("http://localhost:8080/store").request(Requests.post("/books").withBody("create"));

    assertAll(() -> assertEquals(HttpStatus.CREATED, response.status()),
              () -> assertEquals(new Book(1, "create"), asBook(response.body())),
              () -> server.verify(post("/store/books").and(body("create"))));
  }

  @Test
  public void deletesBook(MockHttpServer server) {
    server.mount("/store", booksService);

    HttpResponse response =
        connectTo("http://localhost:8080/store").request(Requests.delete("/books/1"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(empty(), response.body()));
  }

  @Test
  public void updatesBook(MockHttpServer server) {
    server.mount("/store", booksService);

    HttpResponse response =
        connectTo("http://localhost:8080/store").request(Requests.put("/books/1").withBody("update"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(new Book(1, "update"), asBook(response.body())));
  }

  private Book asBook(Bytes body) {
    return jsonToObject(Book.class).apply(body);
  }

  private List<Book> asBooks(Bytes body) {
    return Deserializers.<List<Book>>jsonTo(listOfBooks()).apply(body);
  }

  private Type listOfBooks() {
    return new TypeToken<List<Book>>(){}.getType();
  }
}
