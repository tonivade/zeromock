/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

import static com.github.tonivade.zeromock.core.Deserializers.json;
import static com.github.tonivade.zeromock.core.Predicates.body;
import static com.github.tonivade.zeromock.core.Predicates.delete;
import static com.github.tonivade.zeromock.core.Predicates.get;
import static com.github.tonivade.zeromock.core.Predicates.post;
import static com.github.tonivade.zeromock.core.Predicates.put;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Type;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.tonivade.zeromock.core.Bytes;
import com.github.tonivade.zeromock.core.Deserializers;
import com.github.tonivade.zeromock.core.HttpResponse;
import com.github.tonivade.zeromock.core.HttpService;
import com.github.tonivade.zeromock.core.HttpStatus;
import com.github.tonivade.zeromock.core.Requests;
import com.github.tonivade.zeromock.junit5.BooksService.Book;
import com.github.tonivade.zeromock.server.HttpClient;
import com.github.tonivade.zeromock.server.MockHttpServer;
import com.google.gson.reflect.TypeToken;

@ExtendWith(MockHttpServerExtension.class)
public class BooksServiceTest {
  
  private BooksAPI books = new BooksAPI(new BooksService());
  
  private HttpService booksService = new HttpService("books")
      .when(get("/books")).then(books.findAll())
      .when(get("/books/:id")).then(books.find())
      .when(post("/books")).then(books.create())
      .when(delete("/books/:id")).then(books.delete())
      .when(put("/books/:id")).then(books.update());
  
  @Test
  public void findsBooks(MockHttpServer server) {
    server.mount("/store", booksService);
    
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    HttpResponse response = client.request(Requests.get("/books"));
    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(asList(new Book(1, "title")), asBooks(response.body())));
  }

  @Test
  public void findsBook(MockHttpServer server) {
    server.mount("/store", booksService);
    
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    HttpResponse response = client.request(Requests.get("/books/1"));
    
    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(new Book(1, "title"), asBook(response.body())));
  }
  
  @Test
  public void createsBook(MockHttpServer server) {
    server.mount("/store", booksService);
    
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    HttpResponse response = client.request(Requests.post("/books").withBody("create"));
    
    assertAll(() -> assertEquals(HttpStatus.CREATED, response.status()),
              () -> assertEquals(new Book(1, "create"), asBook(response.body())),
              () -> server.verify(post("/store/books").and(body("create"))));
  }
  
  @Test
  public void deletesBook(MockHttpServer server) {
    server.mount("/store", booksService);
    
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    HttpResponse response = client.request(Requests.delete("/books/1"));
    
    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(null, asBook(response.body())));
  }
  
  @Test
  public void updatesBook(MockHttpServer server) {
    server.mount("/store", booksService);
    
    HttpClient client = new HttpClient("http://localhost:8080/store");
    
    HttpResponse response = client.request(Requests.put("/books/1").withBody("update"));
    
    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(new Book(1, "update"), asBook(response.body())));
  }

  private Book asBook(Bytes body) {
    return json(Book.class).apply(body);
  }

  private List<Book> asBooks(Bytes body) {
    return Deserializers.<List<Book>>json(listOfBooks()).apply(body);
  }
  
  private Type listOfBooks() {
    return new TypeToken<List<Book>>(){}.getType();
  }
}
