/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
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
import static com.github.tonivade.zeromock.server.UIOMockHttpServer.listenAt;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.data.ImmutableSet;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.zeromock.api.Deserializers;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpStatus;
import com.github.tonivade.zeromock.api.HttpUIOService;
import com.github.tonivade.zeromock.api.Requests;
import com.github.tonivade.zeromock.api.Responses;
import com.github.tonivade.zeromock.api.UIORequestHandler;

public class UIOMockHttpServerTest {

  private static final String BASE_URL = "http://localhost:%s/path";

  private final HttpUIOService service1 = new HttpUIOService("hello")
      .when(get().and(path("/hello")).and(param("name")))
        .then(request -> UIO.task(() -> helloWorld(request)).map(Responses::ok))
      .when(get().and(path("/hello")).and(param("name").negate()))
        .then(request -> UIO.pure(badRequest("missing parameter name")));

  private final HttpUIOService service2 = new HttpUIOService("test")
      .when(get().and(path("/test")).and(acceptsXml()))
        .then(request -> UIO.task(this::sayHello).flatMap(objectToXml().andThen(UIO::fromTry)).map(Responses::ok).map(contentXml()))
      .when(get().and(path("/test")).and(acceptsJson()))
        .then(request -> UIO.task(this::sayHello).flatMap(objectToJson(Say.class).andThen(UIO::fromTry)).map(Responses::ok).map(contentJson()))
      .when(get().and(path("/empty")))
        .then(request -> UIO.pure(noContent()));

  private final HttpUIOService service3 = new HttpUIOService("other").when(get("/ping")).then(request -> UIO.pure(ok("pong")));

  private static UIOMockHttpServer server = listenAt(0);

  @Test
  public void hello() {
    server.mount("/path", service1.combine(service2));

    HttpResponse response = connectTo(baseUrl()).request(Requests.get("/hello").withParam("name", "World"));

    assertAll(
        () -> assertEquals(HttpStatus.OK, response.status()),
        () -> assertEquals("Hello World!", asString(response.body()))
    );
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

    assertAll(
        () -> assertEquals(HttpStatus.OK, response.status()),
        () -> assertEquals(success(some(sayHello())), Deserializers.jsonToObject(Say.class).apply(response.body())),
        () -> assertEquals(ImmutableSet.of("application/json"), response.headers().get("Content-type"))
    );
  }

  @Test
  public void xmlTest() {
    server.mount("/path", service1.combine(service2));

    HttpResponse response = connectTo(baseUrl()).request(Requests.get("/test").withHeader("Accept", "text/xml"));

    assertAll(
        () -> assertEquals(HttpStatus.OK, response.status()),
        () -> assertEquals(success(sayHello()), Deserializers.xmlToObject(Say.class).apply(response.body())),
        () -> assertEquals(ImmutableSet.of("text/xml"), response.headers().get("Content-type"))
    );
  }

  @Test
  public void noContentTest() {
    server.mount("/path", service1.combine(service2));

    HttpResponse response = connectTo(baseUrl()).request(Requests.get("/empty"));

    assertAll(
        () -> assertEquals(HttpStatus.NO_CONTENT, response.status()),
        () -> assertEquals("", asString(response.body()))
    );
  }

  @Test
  public void ping() {
    server.mount("/path", service3.postFilter(contentJson()));

    HttpResponse response = connectTo(baseUrl()).request(Requests.get("/ping"));

    assertAll(
        () -> assertEquals(HttpStatus.OK, response.status()),
        () -> assertEquals("pong", asString(response.body())),
        () -> assertTrue(response.headers().contains("Content-type"))
    );
  }

  @Test
  public void exec() {
    UIORequestHandler echo = request -> UIO.pure(ok(request.body()));
    UIOMockHttpServer server = listenAt(0).exec(echo).start();

    HttpResponse response = connectTo("http://localhost:" + server.getPort()).request(Requests.post("/").withBody("echo"));

    assertAll(
        () -> assertEquals(HttpStatus.OK, response.status()),
        () -> assertEquals("echo", asString(response.body()))
    );

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
