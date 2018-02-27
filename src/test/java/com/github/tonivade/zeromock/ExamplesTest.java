/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.core.Bytes.asString;
import static com.github.tonivade.zeromock.core.Extractors.pathParam;
import static com.github.tonivade.zeromock.core.Extractors.queryParam;
import static com.github.tonivade.zeromock.core.Handlers.ok;
import static com.github.tonivade.zeromock.core.Mappings.get;
import static com.github.tonivade.zeromock.core.Predicates.param;
import static com.github.tonivade.zeromock.core.Serializers.json;
import static com.github.tonivade.zeromock.core.Serializers.plain;
import static com.github.tonivade.zeromock.server.HttpClient.connectTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.tonivade.zeromock.core.Bytes;
import com.github.tonivade.zeromock.core.Deserializers;
import com.github.tonivade.zeromock.core.HttpResponse;
import com.github.tonivade.zeromock.core.HttpStatus;
import com.github.tonivade.zeromock.core.Requests;
import com.github.tonivade.zeromock.junit5.ListenAt;
import com.github.tonivade.zeromock.junit5.MockHttpServerExtension;
import com.github.tonivade.zeromock.server.MockHttpServer;

@ListenAt(8081)
@ExtendWith(MockHttpServerExtension.class)
public class ExamplesTest {

  private static final String BASE_URL = "http://localhost:8081";

  @Test
  public void ping(MockHttpServer server) {
    server.when(get("/ping").then(ok("pong")));
    
    HttpResponse response = connectTo(BASE_URL).request(Requests.get("/ping"));
    
    assertEquals("pong", asString(response.body()));
  }

  @Test
  public void echoQueryParam(MockHttpServer server) {
    server.when(get("/echo").and(param("say")).then(ok(queryParam("say").andThen(plain()))));
    
    HttpResponse response = connectTo(BASE_URL)
        .request(Requests.get("/echo").withParam("say", "Hello World!"));
    
    assertEquals("Hello World!", asString(response.body()));
  }
  
  @Test
  public void echoPathParam(MockHttpServer server) {
    server.when(get("/echo/:message").then(ok(pathParam(1).andThen(plain()))));
    
    HttpResponse response = connectTo(BASE_URL).request(Requests.get("/echo/saysomething"));
    
    assertEquals("saysomething", asString(response.body()));
  }
  
  @Test
  public void pojoSerialization(MockHttpServer server) {
    server.when(get("/echo").and(param("say")).then(ok(queryParam("say").andThen(Say::new).andThen(json()))));
    
    HttpResponse response = connectTo(BASE_URL)
        .request(Requests.get("/echo").withParam("say", "Hello World!"));
    
    assertEquals(new Say("Hello World!"), asObject(response.body()));
  }
  
  @Test
  public void unmatched(MockHttpServer server) {
    HttpResponse response = connectTo(BASE_URL).request(Requests.get("/ping"));
    
    assertEquals(HttpStatus.NOT_FOUND, response.status());
  }
  
  private Say asObject(Bytes body) {
    return Deserializers.json(Say.class).apply(body);
  }
}
