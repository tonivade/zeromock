/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.zeromock.api.Bytes.asString;
import static com.github.tonivade.zeromock.api.Handlers.badRequest;
import static com.github.tonivade.zeromock.api.Handlers.noContent;
import static com.github.tonivade.zeromock.api.Handlers.ok;
import static com.github.tonivade.zeromock.api.Headers.contentJson;
import static com.github.tonivade.zeromock.api.Headers.contentXml;
import static com.github.tonivade.zeromock.api.Matchers.acceptsJson;
import static com.github.tonivade.zeromock.api.Matchers.acceptsXml;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.api.Matchers.param;
import static com.github.tonivade.zeromock.api.Serializers.objectToJson;
import static com.github.tonivade.zeromock.api.Serializers.plain;
import static com.github.tonivade.zeromock.api.Serializers.objectToXml;
import static com.github.tonivade.zeromock.server.HttpClient.connectTo;
import static com.github.tonivade.zeromock.server.MockHttpServer.listenAt;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.data.ImmutableSet;
import com.github.tonivade.zeromock.api.Deserializers;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpService;
import com.github.tonivade.zeromock.api.HttpStatus;
import com.github.tonivade.zeromock.api.Requests;
import com.github.tonivade.zeromock.api.Responses;

public class MockHttpServerTest {

  private static final String BASE_URL = "http://localhost:8080/path";

  private final HttpService service1 = new HttpService("hello")
      .when(get("/hello").and(param("name"))).then(ok(plain().compose(this::helloWorld)))
      .when(get("/hello").and(param("name").negate())).then(badRequest("missing parameter name"));

  private final HttpService service2 = new HttpService("test")
      .when(get("/test").and(acceptsXml()))
            .then(ok(adapt(this::sayHello).andThen(objectToXml())).postHandle(contentXml()))
      .when(get("/test").and(acceptsJson()))
            .then(ok(adapt(this::sayHello).andThen(objectToJson())).postHandle(contentJson()))
      .when(get("/empty"))
            .then(noContent()::apply);

  private final HttpService service3 = new HttpService("other").when(get("/ping")).then(ok("pong"));

  private static final MockHttpServer server = listenAt(8080);

  @Test
  public void hello() {
    server.mount("/path", service1.combine(service2));

    HttpResponse response = connectTo(BASE_URL).request(Requests.get("/hello").withParam("name", "World"));

    assertAll(
        () -> assertEquals(HttpStatus.OK, response.status()),
        () -> assertEquals("Hello World!", asString(response.body())),
        () -> server.verify(get("/path/hello").and(param("name", "World"))),
        () -> server.verifyNot(get("/path/hello").and(param("name", "everyone")))
    );
  }

  @Test
  public void helloMissingParam() {
    server.mount("/path", service1.combine(service2));

    HttpResponse response = connectTo(BASE_URL).request(Requests.get("/hello"));

    assertAll(
        () -> assertEquals(HttpStatus.BAD_REQUEST, response.status()),
        () -> server.verifyNot(get("/hello").and(param("name")))
    );
  }

  @Test
  public void jsonTest() {
    server.mount("/path", service1.combine(service2));

    HttpResponse response = connectTo(BASE_URL).request(Requests.get("/test").withHeader("Accept", "application/json"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(sayHello(), Deserializers.jsonToObject(Say.class).apply(response.body())),
              () -> assertEquals(ImmutableSet.of("application/json"), response.headers().get("Content-type")));
  }

  @Test
  public void xmlTest() {
    server.mount("/path", service1.combine(service2));

    HttpResponse response = connectTo(BASE_URL).request(Requests.get("/test").withHeader("Accept", "text/xml"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(sayHello(), Deserializers.xmlToObject(Say.class).apply(response.body())),
              () -> assertEquals(ImmutableSet.of("text/xml"), response.headers().get("Content-type")));
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

  @Test
  public void exec() {
    MockHttpServer server = listenAt(8082).exec(request -> Responses.ok(request.body())).start();

    HttpResponse response = connectTo("http://localhost:8082").request(Requests.get("/").withBody("echo"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals("echo", asString(response.body())));

    server.stop();
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

  private static <T> Function1<HttpRequest, T> adapt(Producer<T> supplier) {
    return supplier.asFunction();
  }
}
