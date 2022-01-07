/*
 * Copyright (c) 2018-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.purefun.type.Option.some;
import static com.github.tonivade.purefun.type.Try.success;
import static com.github.tonivade.zeromock.api.Bytes.asString;
import static com.github.tonivade.zeromock.api.Headers.contentJson;
import static com.github.tonivade.zeromock.api.Headers.contentXml;
import static com.github.tonivade.zeromock.api.Matchers.acceptsJson;
import static com.github.tonivade.zeromock.api.Matchers.acceptsXml;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.api.Matchers.param;
import static com.github.tonivade.zeromock.api.Matchers.path;
import static com.github.tonivade.zeromock.api.Responses.badRequest;
import static com.github.tonivade.zeromock.api.Responses.noContent;
import static com.github.tonivade.zeromock.api.Responses.ok;
import static com.github.tonivade.zeromock.api.Serializers.objectToJson;
import static com.github.tonivade.zeromock.api.Serializers.objectToXml;
import static com.github.tonivade.zeromock.client.HttpClient.connectTo;
import static com.github.tonivade.zeromock.server.IOMockHttpServer.listenAt;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.data.ImmutableSet;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.zeromock.api.Deserializers;
import com.github.tonivade.zeromock.api.HttpIOService;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpStatus;
import com.github.tonivade.zeromock.api.IORequestHandler;
import com.github.tonivade.zeromock.api.Requests;
import com.github.tonivade.zeromock.api.Responses;

public class IOMockHttpServerTest {

  private static final String BASE_URL = "http://localhost:%s/path";

  private HttpIOService service1 = new HttpIOService("hello")
      .when(get().and(path("/hello")).and(param("name")))
        .then(request -> IO.task(() -> helloWorld(request)).map(Responses::ok))
      .when(get().and(path("/hello")).and(param("name").negate()))
        .then(request -> IO.pure(badRequest("missing parameter name")));

  private HttpIOService service2 = new HttpIOService("test")
      .when(get().and(path("/test")).and(acceptsXml()))
        .then(request -> IO.task(this::sayHello).flatMap(objectToXml().andThen(IO::fromTry)).map(Responses::ok).map(contentXml()))
      .when(get().and(path("/test")).and(acceptsJson()))
        .then(request -> IO.task(this::sayHello).flatMap(objectToJson(Say.class).andThen(IO::fromTry)).map(Responses::ok).map(contentJson()))
      .when(get().and(path("/empty")))
        .then(request -> IO.pure(noContent()));

  private HttpIOService service3 = new HttpIOService("other").when(get("/ping")).then(request -> IO.pure(ok("pong")));

  private static IOMockHttpServer server = listenAt(0);

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
    IORequestHandler echo = request -> IO.pure(ok(request.body()));
    IOMockHttpServer server = listenAt(0).exec(echo).start();

    HttpResponse response = connectTo("http://localhost:" + server.getPort()).request(Requests.post("/").withBody("echo"));

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

  private String baseUrl() {
    return String.format(BASE_URL, server.getPort());
  }
}
