/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.Handlers.badRequest;
import static com.github.tonivade.zeromock.Handlers.contentJson;
import static com.github.tonivade.zeromock.Handlers.contentXml;
import static com.github.tonivade.zeromock.Handlers.ok;
import static com.github.tonivade.zeromock.MockHttpServer.listenAt;
import static com.github.tonivade.zeromock.Predicates.acceptsJson;
import static com.github.tonivade.zeromock.Predicates.acceptsXml;
import static com.github.tonivade.zeromock.Predicates.get;
import static com.github.tonivade.zeromock.Predicates.param;
import static com.github.tonivade.zeromock.Predicates.path;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

public class MockHttpServerTest {

  private HttpService service1 = new HttpService("hello")
      .when(get().and(path("/hello")).and(param("name")), ok(this::helloWorld))
      .when(get().and(path("/hello")).and(param("name").negate()), badRequest("missing parameter name"));

  private HttpService service2 = new HttpService("test")
      .when(get().and(path("/test")).and(acceptsXml()), ok("<body/>").andThen(contentXml()))
      .when(get().and(path("/test")).and(acceptsJson()), contentJson().compose(ok(new JsonObject())));
  
  private MockHttpServer server = listenAt(8080).mount("/path", service1.combine(service2));

  @Test
  public void hello() {
    HttpClient client = new HttpClient("http://localhost:8080/path");
    
    HttpResponse response = client.request(Requests.get("/hello").withParam("name", "World"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals("Hello World!", response.body()));
  }
  
  @Test
  public void helloMissingParam() {
    HttpClient client = new HttpClient("http://localhost:8080/path");
    
    HttpResponse response = client.request(Requests.get("/hello"));

    assertEquals(HttpStatus.BAD_REQUEST, response.status());
  }

  @Test
  public void json() {
    HttpClient client = new HttpClient("http://localhost:8080/path");
    
    HttpResponse response = client.request(Requests.get("/test").withHeader("Accept", "application/json"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(new JsonObject(), response.body()),
              () -> assertEquals(asList("application/json"), response.headers().get("Content-type")));
  }

  @Test
  public void xml() {
    HttpClient client = new HttpClient("http://localhost:8080/path");
    
    HttpResponse response = client.request(Requests.get("/test").withHeader("Accept", "text/xml"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals("<body/>", response.body()),
              () -> assertEquals(asList("text/xml"), response.headers().get("Content-type")));
  }

  @BeforeEach
  public void setUp() {
    server.start();
  }

  @AfterEach
  public void tearDown() {
    server.stop();
  }
  
  private Object helloWorld(HttpRequest request) {
    return String.format("Hello %s!", request.param("name"));
  }
}
