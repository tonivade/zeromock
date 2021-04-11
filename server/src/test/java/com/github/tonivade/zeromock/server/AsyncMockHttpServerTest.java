/*
 * Copyright (c) 2018-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.purefun.type.Option.some;
import static com.github.tonivade.purefun.type.Try.success;
import static com.github.tonivade.zeromock.api.Bytes.asString;
import static com.github.tonivade.zeromock.api.Handlers.badRequest;
import static com.github.tonivade.zeromock.api.Handlers.fromTry;
import static com.github.tonivade.zeromock.api.Handlers.noContent;
import static com.github.tonivade.zeromock.api.Handlers.ok;
import static com.github.tonivade.zeromock.api.Headers.contentJson;
import static com.github.tonivade.zeromock.api.Headers.contentXml;
import static com.github.tonivade.zeromock.api.Matchers.acceptsJson;
import static com.github.tonivade.zeromock.api.Matchers.acceptsXml;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.api.Matchers.param;
import static com.github.tonivade.zeromock.api.Matchers.path;
import static com.github.tonivade.zeromock.api.Serializers.objectToJson;
import static com.github.tonivade.zeromock.api.Serializers.objectToXml;
import static com.github.tonivade.zeromock.api.Serializers.plain;
import static com.github.tonivade.zeromock.client.HttpClient.connectTo;
import static com.github.tonivade.zeromock.server.AsyncMockHttpServer.listenAt;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.data.ImmutableSet;
import com.github.tonivade.zeromock.api.AsyncHttpService;
import com.github.tonivade.zeromock.api.Deserializers;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpStatus;
import com.github.tonivade.zeromock.api.RequestHandler;
import com.github.tonivade.zeromock.api.Requests;
import com.github.tonivade.zeromock.api.Responses;

public class AsyncMockHttpServerTest {

  private static final String BASE_URL = "http://localhost:%s/path";

  private final AsyncHttpService service1 = new AsyncHttpService("hello")
      .when(get().and(path("/hello")).and(param("name"))).then(ok(plain().compose(this::helloWorld)).liftFuture()::apply)
      .when(get().and(path("/hello")).and(param("name").negate())).then(badRequest("missing parameter name").liftFuture()::apply);

  private final AsyncHttpService service2 = new AsyncHttpService("test")
      .when(get().and(path("/test")).and(acceptsXml()))
            .then(fromTry(adapt(this::sayHello).andThen(objectToXml())).postHandle(contentXml()).liftFuture()::apply)
      .when(get().and(path("/test")).and(acceptsJson()))
            .then(fromTry(adapt(this::sayHello).andThen(objectToJson(Say.class))).postHandle(contentJson()).liftFuture()::apply)
      .when(get().and(path("/empty")))
            .then(noContent().liftFuture()::apply);

  private final AsyncHttpService service3 = new AsyncHttpService("other").when(get("/ping")).then(ok("pong").liftFuture()::apply);

  private static AsyncMockHttpServer server = listenAt(0);

  @Test
  public void hello() {
    server.mount("/path", service1.combine(service2));

    HttpResponse response = connectTo(baseUrl()).request(Requests.get("/hello").withParam("name", "World"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals("Hello World!", asString(response.body())));
  }

  @Test
  public void helloMissingParam() {
    server.mount("/path", service1.combine(service2));

    HttpResponse response = connectTo(baseUrl()).request(Requests.get("/hello"));

    assertEquals(HttpStatus.BAD_REQUEST, response.status());
  }

  @Test
  public void jsonTest() {
    server.mount("/path", service1.combine(service2));

    HttpResponse response = connectTo(baseUrl()).request(Requests.get("/test").withHeader("Accept", "application/json"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(success(some(sayHello())), Deserializers.jsonToObject(Say.class).apply(response.body())),
              () -> assertEquals(ImmutableSet.of("application/json"), response.headers().get("Content-type")));
  }

  @Test
  public void xmlTest() {
    server.mount("/path", service1.combine(service2));

    HttpResponse response = connectTo(baseUrl()).request(Requests.get("/test").withHeader("Accept", "text/xml"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals(success(sayHello()), Deserializers.xmlToObject(Say.class).apply(response.body())),
              () -> assertEquals(ImmutableSet.of("text/xml"), response.headers().get("Content-type")));
  }

  @Test
  public void noContentTest() {
    server.mount("/path", service1.combine(service2));

    HttpResponse response = connectTo(baseUrl()).request(Requests.get("/empty"));

    assertAll(() -> assertEquals(HttpStatus.NO_CONTENT, response.status()),
              () -> assertEquals("", asString(response.body())));
  }

  @Test
  public void ping() {
    server.mount("/path", service3);

    HttpResponse response = connectTo(baseUrl()).request(Requests.get("/ping"));

    assertAll(() -> assertEquals(HttpStatus.OK, response.status()),
              () -> assertEquals("pong", asString(response.body())));
  }

  @Test
  public void exec() {
    RequestHandler echo = request -> Responses.ok(request.body());
    AsyncMockHttpServer server = listenAt(0).exec(echo.liftFuture()::apply).start();

    HttpResponse response = connectTo("http://localhost:" + server.getPort()).request(Requests.get("/").withBody("echo"));

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

  private String baseUrl() {
    return String.format(BASE_URL, server.getPort());
  }
}
