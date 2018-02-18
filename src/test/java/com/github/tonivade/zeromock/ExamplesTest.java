/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.Bytes.asString;
import static com.github.tonivade.zeromock.Extractors.pathParam;
import static com.github.tonivade.zeromock.Extractors.queryParam;
import static com.github.tonivade.zeromock.Handlers.ok;
import static com.github.tonivade.zeromock.HttpClient.connectTo;
import static com.github.tonivade.zeromock.Predicates.get;
import static com.github.tonivade.zeromock.Predicates.param;
import static com.github.tonivade.zeromock.Serializers.json;
import static com.github.tonivade.zeromock.Serializers.plain;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockHttpServerExtension.class)
public class ExamplesTest {

  private static final String BASE_URL = "http://localhost:8080";

  @Test
  public void ping(MockHttpServer server) {
    server.when(get("/ping"), ok("pong"));
    
    HttpResponse response = connectTo(BASE_URL).request(Requests.get("/ping"));
    
    assertEquals("pong", asString(response.body()));
  }

  @Test
  public void echoQueryParam(MockHttpServer server) {
    server.when(get("/echo").and(param("say")), ok(queryParam("say").andThen(plain())));
    
    HttpResponse response = connectTo(BASE_URL)
        .request(Requests.get("/echo").withParam("say", "Hello World!"));
    
    assertEquals("Hello World!", asString(response.body()));
  }
  
  @Test
  public void echoPathParam(MockHttpServer server) {
    server.when(get("/echo/:message"), ok(pathParam(1).andThen(plain())));
    
    HttpResponse response = connectTo(BASE_URL).request(Requests.get("/echo/saysomething"));
    
    assertEquals("saysomething", asString(response.body()));
  }
  
  @Test
  public void pojoSerialization(MockHttpServer server) {
    server.when(get("/echo").and(param("say")), ok(queryParam("say").andThen(Say::new).andThen(json())));
    
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
    return Deserializers.<Say>json(Say.class).apply(body);
  }

  private static final class Say {
    private final String message;

    public Say(String message) {
      this.message = message;
    }
    
    @Override
    public int hashCode() {
      return Objects.hash(message);
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj == null)
        return false;
      if (this == obj)
        return true;
      if (getClass() != obj.getClass())
        return false;
      return Objects.equals(((Say) obj).message, this.message);
    }
    
    @Override
    public String toString() {
      return "Say(message=" + message + ")";
    }
  }
}
