/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.MockHttpServer.listenAt;
import static com.github.tonivade.zeromock.Predicates.acceptsJson;
import static com.github.tonivade.zeromock.Predicates.acceptsXml;
import static com.github.tonivade.zeromock.Predicates.get;
import static com.github.tonivade.zeromock.Predicates.param;
import static com.github.tonivade.zeromock.Predicates.path;
import static com.github.tonivade.zeromock.Responses.badRequest;
import static com.github.tonivade.zeromock.Responses.contentJson;
import static com.github.tonivade.zeromock.Responses.contentXml;
import static com.github.tonivade.zeromock.Responses.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MockHttpServerTest {

  private Resource resource = new Resource("test")
      .when(get().and(path("/hello")).and(param("name")), ok("Hello %s!"))
      .when(get().and(path("/hello")).and(param("name").negate()), badRequest("missing parameter name"))
      .when(get().and(path("/test")).and(acceptsXml()), ok("<body/>").andThen(contentXml()))
      .when(get().and(path("/test")).and(acceptsJson()), contentJson().compose(ok("{ }")));
  
  private MockHttpServer server = listenAt(8080).mount("/path", resource);

  @Test
  public void hello() throws IOException {
    HttpClient client = new HttpClient("http://localhost:8080/path");
    
    Response response = client.request(Requests.get("/hello").withParam("name", "World"));

    assertEquals(200, response.statusCode);
    assertEquals("Hello World!", response.body);
  }
  
  @Test
  public void helloMissingParam() throws IOException {
    HttpClient client = new HttpClient("http://localhost:8080/path");
    
    Response response = client.request(Requests.get("/hello"));

    assertEquals(400, response.statusCode);
  }

  @Test
  public void json() throws IOException {
    HttpClient client = new HttpClient("http://localhost:8080/path");
    
    Response response = client.request(Requests.get("/test").withHeader("Accept", "application/json"));

    assertEquals(200, response.statusCode);
    assertEquals("{ }", response.body);
    assertEquals(List.of("application/json"), response.headers.get("Content-type"));
  }

  @Test
  public void xml() throws IOException {
    HttpClient client = new HttpClient("http://localhost:8080/path");
    
    Response response = client.request(Requests.get("/test").withHeader("Accept", "text/xml"));

    assertEquals(200, response.statusCode);
    assertEquals("<body/>", response.body);
    assertEquals(List.of("text/xml"), response.headers.get("Content-type"));
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
