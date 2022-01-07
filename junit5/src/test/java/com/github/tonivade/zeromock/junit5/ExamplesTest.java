/*
 * Copyright (c) 2018-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

import static com.github.tonivade.zeromock.api.Bytes.asString;
import static com.github.tonivade.zeromock.api.Extractors.pathParam;
import static com.github.tonivade.zeromock.api.Extractors.queryParam;
import static com.github.tonivade.zeromock.api.Handlers.fromTry;
import static com.github.tonivade.zeromock.api.Handlers.ok;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.api.Matchers.param;
import static com.github.tonivade.zeromock.api.Serializers.objectToJson;
import static com.github.tonivade.zeromock.api.Serializers.plain;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.tonivade.zeromock.api.Bytes;
import com.github.tonivade.zeromock.api.Deserializers;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpStatus;
import com.github.tonivade.zeromock.api.Requests;
import com.github.tonivade.zeromock.client.AsyncHttpClient;
import com.github.tonivade.zeromock.client.HttpClient;
import com.github.tonivade.zeromock.client.IOHttpClient;
import com.github.tonivade.zeromock.client.UIOHttpClient;
import com.github.tonivade.zeromock.server.MockHttpServer;

@ExtendWith(MockHttpServerExtension.class)
public class ExamplesTest {

  @Test
  public void ping(MockHttpServer server, HttpClient client) {
    server.when(get("/ping")).then(ok("pong"));

    HttpResponse response = client.request(Requests.get("/ping"));

    assertEquals("pong", asString(response.body()));
  }

  @Test
  public void echoQueryParam(MockHttpServer server, AsyncHttpClient client) {
    server.when(get("/echo").and(param("say"))).then(ok(queryParam("say").andThen(plain())));

    HttpResponse response = client.request(Requests.get("/echo").withParam("say", "Hello World!")).await().get();

    assertEquals("Hello World!", asString(response.body()));
  }

  @Test
  public void echoPathParam(MockHttpServer server, IOHttpClient client) {
    server.when(get("/echo/:message")).then(ok(pathParam(1).andThen(plain())));

    HttpResponse response = client.request(Requests.get("/echo/saysomething")).unsafeRunSync();

    assertEquals("saysomething", asString(response.body()));
  }

  @Test
  public void pojoSerialization(MockHttpServer server, UIOHttpClient client) {
    server.when(get("/echo").and(param("say"))).then(fromTry(queryParam("say").andThen(Say::new).andThen(objectToJson(Say.class))));

    HttpResponse response = client.request(Requests.get("/echo").withParam("say", "Hello World!")).unsafeRunSync();

    assertEquals(new Say("Hello World!"), asObject(response.body()));
  }

  @Test
  public void unmatched(MockHttpServer server, HttpClient client) {
    HttpResponse response = client.request(Requests.get("/ping"));

    assertEquals(HttpStatus.NOT_FOUND, response.status());
  }

  private Say asObject(Bytes body) {
    return Deserializers.jsonToObject(Say.class).apply(body).getOrElseThrow().getOrElseNull();
  }
}
