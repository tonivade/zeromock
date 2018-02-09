/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.MockHttpServer.listenAt;
import static com.github.tonivade.zeromock.Predicates.body;
import static com.github.tonivade.zeromock.Predicates.delete;
import static com.github.tonivade.zeromock.Predicates.get;
import static com.github.tonivade.zeromock.Predicates.path;
import static com.github.tonivade.zeromock.Predicates.post;
import static com.github.tonivade.zeromock.Predicates.put;
import static com.github.tonivade.zeromock.Requests.delete;
import static com.github.tonivade.zeromock.Requests.get;
import static com.github.tonivade.zeromock.Requests.post;
import static com.github.tonivade.zeromock.Requests.put;
import static com.github.tonivade.zeromock.Handlers.created;
import static com.github.tonivade.zeromock.Handlers.ok;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BooksModuleTest {
  
  private BooksModule module = new BooksModule();
  
  private HttpService booksService = new HttpService("books")
      .when(post()  .and(path("/books")),     created(module::createBook))
      .when(get()   .and(path("/books")),     ok(module::findAllBooks))
      .when(get()   .and(path("/books/:id")), ok(module::findBook))
      .when(delete().and(path("/books/:id")), ok(module::deleteBook))
      .when(put()   .and(path("/books/:id")), ok(module::updateBook));
  
  private MockHttpServer server = listenAt(8080).mount("/store", booksService);
  
  @Test
  public void findBooks() {
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    HttpResponse response = client.request(get("/books"));
    
    assertAll(() -> assertEquals(HttpStatus.OK, response.statusCode),
              () -> assertEquals("find all books", response.body));
  }
  
  @Test
  public void findBook() {
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    HttpResponse response = client.request(get("/books/1"));
    
    assertAll(() -> assertEquals(HttpStatus.OK, response.statusCode),
              () -> assertEquals("find one book 1", response.body));
  }
  
  @Test
  public void bookCreated() {
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    HttpResponse response = client.request(post("/books").withBody("create"));
    
    assertAll(() -> assertEquals(HttpStatus.CREATED, response.statusCode),
              () -> assertEquals("book created", response.body),
              () -> server.verify(post().and(path("/store/books")).and(body("create"))));
  }
  
  @Test
  public void bookDeleted() {
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    HttpResponse response = client.request(delete("/books/1"));
    
    assertAll(() -> assertEquals(HttpStatus.OK, response.statusCode),
              () -> assertEquals("book deleted 1", response.body));
  }
  
  @Test
  public void bookUpdated() {
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    HttpResponse response = client.request(put("/books/1").withBody("update"));
    
    assertAll(() -> assertEquals(HttpStatus.OK, response.statusCode),
              () -> assertEquals("book updated 1", response.body));
  }

  @BeforeEach
  public void setUp() {
    server.start();
  }

  @AfterEach
  public void tearDown() {
    server.stop();
  }
}
