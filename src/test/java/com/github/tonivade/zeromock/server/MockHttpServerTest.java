/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.zeromock.core.Bytes.asString;
import static com.github.tonivade.zeromock.core.Handlers.badRequest;
import static com.github.tonivade.zeromock.core.Handlers.contentJson;
import static com.github.tonivade.zeromock.core.Handlers.contentXml;
import static com.github.tonivade.zeromock.core.Handlers.force;
import static com.github.tonivade.zeromock.core.Handlers.noContent;
import static com.github.tonivade.zeromock.core.Handlers.ok;
import static com.github.tonivade.zeromock.core.Predicates.acceptsJson;
import static com.github.tonivade.zeromock.core.Predicates.acceptsXml;
import static com.github.tonivade.zeromock.core.Predicates.get;
import static com.github.tonivade.zeromock.core.Predicates.param;
import static com.github.tonivade.zeromock.core.Predicates.path;
import static com.github.tonivade.zeromock.core.Serializers.json;
import static com.github.tonivade.zeromock.core.Serializers.plain;
import static com.github.tonivade.zeromock.core.Serializers.xml;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.tonivade.zeromock.Say;
import com.github.tonivade.zeromock.core.Deserializers;
import com.github.tonivade.zeromock.core.HttpRequest;
import com.github.tonivade.zeromock.core.HttpResponse;
import com.github.tonivade.zeromock.core.HttpService;
import com.github.tonivade.zeromock.core.HttpStatus;
import com.github.tonivade.zeromock.core.Requests;
import com.github.tonivade.zeromock.junit5.MockHttpServerExtension;
import com.github.tonivade.zeromock.server.HttpClient;
import com.github.tonivade.zeromock.server.MockHttpServer;

@ExtendWith(MockHttpServerExtension.class)
public class MockHttpServerTest {

  private HttpService service1 = new HttpService("hello")
      .when(get().and(path("/hello")).and(param("name")), ok(plain().compose(this::helloWorld)))
      .when(get().and(path("/hello")).and(param("name").negate()), badRequest("missing parameter name"));

  private HttpService service2 = new HttpService("test")
      .when(get().and(path("/test")).and(acceptsXml()), 
            ok(force(this::sayHello).andThen(xml())).andThen(contentXml()))
      .when(get().and(path("/test")).and(acceptsJson()), 
            ok(force(this::sayHello).andThen(json())).andThen(contentJson()))
      .when(get().and(path("/empty")), noContent());
  
  @Test
  public void hello(MockHttpServer server) {
    server.mount("/path", service1.combine(service2));
    
    HttpClient client = new HttpClient("http://localhost:8080/path");
    
    HttpResponse response = client.request(Requests.get("/hello").withParam("name", "World"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals("Hello World!", asString(response.body())));
  }
  
  @Test
  public void helloMissingParam(MockHttpServer server) {
    server.mount("/path", service1.combine(service2));
    
    HttpClient client = new HttpClient("http://localhost:8080/path");
    
    HttpResponse response = client.request(Requests.get("/hello"));

    assertEquals(HttpStatus.BAD_REQUEST, response.status());
  }

  @Test
  public void jsonTest(MockHttpServer server) {
    server.mount("/path", service1.combine(service2));
    
    HttpClient client = new HttpClient("http://localhost:8080/path");
    
    HttpResponse response = client.request(Requests.get("/test").withHeader("Accept", "application/json"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(sayHello(), Deserializers.json(Say.class).apply(response.body())),
              () -> assertEquals(asList("application/json"), response.headers().get("Content-type")));
  }

  @Test
  public void xmlTest(MockHttpServer server) {
    server.mount("/path", service1.combine(service2));
    
    HttpClient client = new HttpClient("http://localhost:8080/path");
    
    HttpResponse response = client.request(Requests.get("/test").withHeader("Accept", "text/xml"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(sayHello(), Deserializers.xml(Say.class).apply(response.body())),
              () -> assertEquals(asList("text/xml"), response.headers().get("Content-type")));
  }

  @Test
  public void noContentTest(MockHttpServer server) {
    server.mount("/path", service1.combine(service2));
    
    HttpClient client = new HttpClient("http://localhost:8080/path");
    
    HttpResponse response = client.request(Requests.get("/empty"));

    assertAll(() -> assertEquals(HttpStatus.NO_CONTENT, response.status()),
              () -> assertEquals("", asString(response.body())));
  }
  
  private String helloWorld(HttpRequest request) {
    return String.format("Hello %s!", request.param("name"));
  }

  private Say sayHello() {
    return new Say("hello");
  }
}
