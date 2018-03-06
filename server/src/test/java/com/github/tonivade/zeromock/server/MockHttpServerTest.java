/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.zeromock.core.Bytes.asString;
import static com.github.tonivade.zeromock.core.Handlers.badRequest;
import static com.github.tonivade.zeromock.core.Handlers.noContent;
import static com.github.tonivade.zeromock.core.Handlers.ok;
import static com.github.tonivade.zeromock.core.Headers.contentJson;
import static com.github.tonivade.zeromock.core.Headers.contentXml;
import static com.github.tonivade.zeromock.core.Predicates.acceptsJson;
import static com.github.tonivade.zeromock.core.Predicates.acceptsXml;
import static com.github.tonivade.zeromock.core.Predicates.get;
import static com.github.tonivade.zeromock.core.Predicates.param;
import static com.github.tonivade.zeromock.core.Predicates.path;
import static com.github.tonivade.zeromock.core.Serializers.json;
import static com.github.tonivade.zeromock.core.Serializers.plain;
import static com.github.tonivade.zeromock.core.Serializers.xml;
import static com.github.tonivade.zeromock.server.HttpClient.connectTo;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tonivade.zeromock.core.Combinators;
import com.github.tonivade.zeromock.core.Deserializers;
import com.github.tonivade.zeromock.core.HttpRequest;
import com.github.tonivade.zeromock.core.HttpResponse;
import com.github.tonivade.zeromock.core.HttpService;
import com.github.tonivade.zeromock.core.HttpStatus;
import com.github.tonivade.zeromock.core.Requests;

public class MockHttpServerTest {

  private static final String BASE_URL = "http://localhost:8080/path";

  private HttpService service1 = new HttpService("hello")
      .when(get().and(path("/hello")).and(param("name")), ok(plain().compose(this::helloWorld)))
      .when(get().and(path("/hello")).and(param("name").negate()), badRequest("missing parameter name"));

  private HttpService service2 = new HttpService("test")
      .when(get().and(path("/test")).and(acceptsXml()), 
            ok(asFunction(this::sayHello).andThen(xml())).andThen(contentXml()))
      .when(get().and(path("/test")).and(acceptsJson()), 
            ok(asFunction(this::sayHello).andThen(json())).andThen(contentJson()))
      .when(get().and(path("/empty")), noContent());
  
  private HttpService service3 = new HttpService("other").when(get("/ping")).then(ok("pong"));
  
  private static MockHttpServer server = MockHttpServer.listenAt(8080);
  
  @Test
  public void hello() {
    server.mount("/path", service1.combine(service2));
    
    HttpResponse response = connectTo(BASE_URL).request(Requests.get("/hello").withParam("name", "World"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals("Hello World!", asString(response.body())));
  }
  
  @Test
  public void helloMissingParam() {
    server.mount("/path", service1.combine(service2));
    
    HttpResponse response = connectTo(BASE_URL).request(Requests.get("/hello"));

    assertEquals(HttpStatus.BAD_REQUEST, response.status());
  }

  @Test
  public void jsonTest() {
    server.mount("/path", service1.combine(service2));
    
    HttpResponse response = connectTo(BASE_URL).request(Requests.get("/test").withHeader("Accept", "application/json"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(sayHello(), Deserializers.json(Say.class).apply(response.body())),
              () -> assertEquals(asList("application/json"), response.headers().get("Content-type")));
  }

  @Test
  public void xmlTest() {
    server.mount("/path", service1.combine(service2));
    
    HttpResponse response = connectTo(BASE_URL).request(Requests.get("/test").withHeader("Accept", "text/xml"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(sayHello(), Deserializers.xml(Say.class).apply(response.body())),
              () -> assertEquals(asList("text/xml"), response.headers().get("Content-type")));
  }

  @Test
  public void noContentTest() {
    server.mount("/path", service1.combine(service2));
    
    HttpResponse response = connectTo(BASE_URL).request(Requests.get("/empty"));

    assertAll(() -> assertEquals(HttpStatus.NO_CONTENT, response.status()),
              () -> assertEquals("", asString(response.body())));
  }

  @Test
  public void ping() {
    server.mount("/path", service3);
    
    HttpResponse response = connectTo(BASE_URL).request(Requests.get("/ping"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals("pong", asString(response.body())));
  }
  
  @BeforeEach
  public void beforeEach() {
    server.reset();
  }
  
  @BeforeAll
  public static void beforeAll() {
    server.start();
  }
  
  @AfterAll
  public static void afterAll() {
    server.stop();
  }
  
  private String helloWorld(HttpRequest request) {
    return String.format("Hello %s!", request.param("name"));
  }

  private Say sayHello() {
    return new Say("hello");
  }

  private static <T> Function<HttpRequest, T> asFunction(Supplier<T> supplier) {
    return Combinators.<HttpRequest, T>force(supplier);
  }
}
