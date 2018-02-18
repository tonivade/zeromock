/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.Bytes.asString;
import static com.github.tonivade.zeromock.Extractors.queryParam;
import static com.github.tonivade.zeromock.Handlers.ok;
import static com.github.tonivade.zeromock.HttpClient.connectTo;
import static com.github.tonivade.zeromock.Predicates.get;
import static com.github.tonivade.zeromock.Predicates.param;
import static com.github.tonivade.zeromock.Serializers.plain;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockHttpServerExtension.class)
public class ExamplesTest {

  private static final String BASE_URL = "http://localhost:8080";

  @Test
  void ping(MockHttpServer server) {
    server.when(get("/ping"), ok("pong"));
    
    HttpResponse response = connectTo(BASE_URL).request(Requests.get("/ping"));
    
    assertEquals("pong", asString(response.body()));
  }

  @Test
  void echo(MockHttpServer server) {
    server.when(get("/echo").and(param("say")), ok(queryParam("say").andThen(plain())));
    
    HttpResponse response = connectTo(BASE_URL)
        .request(Requests.get("/echo").withParam("say", "Hello World!"));
    
    assertEquals("Hello World!", asString(response.body()));
  }
  
  @Test
  void unmatched(MockHttpServer server) {
    HttpResponse response = connectTo(BASE_URL).request(Requests.get("/ping"));
    
    assertEquals(HttpStatus.NOT_FOUND, response.status());
  }
}
