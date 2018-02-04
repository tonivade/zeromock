/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.MockHttpServer.listenAt;
import static com.github.tonivade.zeromock.Predicates.delete;
import static com.github.tonivade.zeromock.Predicates.get;
import static com.github.tonivade.zeromock.Predicates.param;
import static com.github.tonivade.zeromock.Predicates.path;
import static com.github.tonivade.zeromock.Predicates.post;
import static com.github.tonivade.zeromock.Predicates.put;
import static com.github.tonivade.zeromock.Requests.delete;
import static com.github.tonivade.zeromock.Requests.get;
import static com.github.tonivade.zeromock.Requests.post;
import static com.github.tonivade.zeromock.Requests.put;
import static com.github.tonivade.zeromock.Responses.created;
import static com.github.tonivade.zeromock.Responses.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BooksModuleTest {
  
  private BooksModule module = new BooksModule();
  
  private Resource resource = new Resource("books")
      .when(post().and(path("/books")), created(module::createBook))
      .when(get().and(path("/books")).and(param("id").negate()), ok(module::findAllBooks))
      .when(get().and(path("/books")).and(param("id")), ok(module::findBook))
      .when(delete().and(path("/books")).and(param("id")), ok(module::deleteBook))
      .when(put().and(path("/books")).and(param("id")), ok(module::updateBook));
  
  private MockHttpServer server = listenAt(8080).mount("/store", resource);
  
  @Test
  public void findBooks() throws IOException {
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    Response response = client.request(get("/books"));
    
    assertEquals(200, response.statusCode);
    assertEquals("find all books", response.body);
  }
  
  @Test
  public void findBook() throws IOException {
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    Response response = client.request(get("/books").withParam("id", "1"));
    
    assertEquals(200, response.statusCode);
    assertEquals("find one book 1", response.body);
  }
  
  @Test
  public void bookCreated() throws IOException {
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    Response response = client.request(post("/books"));
    
    assertEquals(201, response.statusCode);
    assertEquals("book created", response.body);
  }
  
  @Test
  public void bookDeleted() throws IOException {
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    Response response = client.request(delete("/books").withParam("id", "1"));
    
    assertEquals(200, response.statusCode);
    assertEquals("book deleted 1", response.body);
  }
  
  @Test
  public void bookUpdated() throws IOException {
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    Response response = client.request(put("/books").withParam("id", "1"));
    
    assertEquals(200, response.statusCode);
    assertEquals("book updated 1", response.body);
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
