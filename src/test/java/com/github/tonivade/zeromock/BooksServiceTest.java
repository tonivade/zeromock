/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.Handlers.createdJson;
import static com.github.tonivade.zeromock.Handlers.okJson;
import static com.github.tonivade.zeromock.MockHttpServer.listenAt;
import static com.github.tonivade.zeromock.Predicates.body;
import static com.github.tonivade.zeromock.Predicates.delete;
import static com.github.tonivade.zeromock.Predicates.get;
import static com.github.tonivade.zeromock.Predicates.path;
import static com.github.tonivade.zeromock.Predicates.post;
import static com.github.tonivade.zeromock.Predicates.put;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tonivade.zeromock.BooksService.Book;
import com.google.gson.reflect.TypeToken;

public class BooksServiceTest {
  
  private BooksAPI books = new BooksAPI(new BooksService());
  
  private HttpService booksService = new HttpService("books")
      .when(get("/books"), okJson(books.findAll()))
      .when(get("/books/:id"), okJson(books.find()))
      .when(post("/books"), createdJson(books.create()))
      .when(delete("/books/:id"), okJson(books.delete()))
      .when(put("/books/:id"), okJson(books.update()));
  
  private MockHttpServer server = listenAt(8080).mount("/store", booksService);
  
  @Test
  public void findsBooks() {
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    HttpResponse response = client.request(Requests.get("/books"));
    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(asList(new Book(1, "title")), asBooks(response.body())));
  }

  @Test
  public void findsBook() {
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    HttpResponse response = client.request(Requests.get("/books/1"));
    
    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(new Book(1, "title"), asBook(response.body())));
  }
  
  @Test
  public void createsBook() {
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    HttpResponse response = client.request(Requests.post("/books").withBody("create"));
    
    assertAll(() -> assertEquals(HttpStatus.CREATED, response.status()),
              () -> assertEquals(new Book(1, "create"), asBook(response.body())),
              () -> server.verify(post().and(path("/store/books")).and(body("create"))));
  }
  
  @Test
  public void deletesBook() {
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    HttpResponse response = client.request(Requests.delete("/books/1"));
    
    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(null, asBook(response.body())));
  }
  
  @Test
  public void updatesBook() {
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    HttpResponse response = client.request(Requests.put("/books/1").withBody("update"));
    
    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(new Book(1, "update"), asBook(response.body())));
  }

  @BeforeEach
  public void setUp() {
    server.start();
  }

  @AfterEach
  public void tearDown() {
    server.stop();
  }

  private Book asBook(ByteBuffer body) {
    return Deserializers.<Book>json(Book.class).apply(body);
  }

  private List<Book> asBooks(ByteBuffer body) {
    return Deserializers.<List<Book>>json(listOfBooks()).apply(body);
  }
  
  private Type listOfBooks() {
    return new TypeToken<List<Book>>(){}.getType();
  }
}
