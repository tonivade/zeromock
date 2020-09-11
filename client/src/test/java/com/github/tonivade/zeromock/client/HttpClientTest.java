/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

import static com.github.tonivade.zeromock.api.Handlers.ok;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.client.HttpClientBuilder.asyncClient;
import static com.github.tonivade.zeromock.client.HttpClientBuilder.client;
import static com.github.tonivade.zeromock.client.HttpClientBuilder.ioClient;
import static com.github.tonivade.zeromock.client.HttpClientBuilder.taskClient;
import static com.github.tonivade.zeromock.client.HttpClientBuilder.uioClient;
import static com.github.tonivade.zeromock.server.MockHttpServer.listenAt;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.effect.Task;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.zeromock.api.Bytes;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpStatus;
import com.github.tonivade.zeromock.api.Requests;
import com.github.tonivade.zeromock.server.MockHttpServer;

class HttpClientTest {

  private static final MockHttpServer server = listenAt(0).when(get("/ping")).then(ok("pong"));

  private final HttpRequest ping = Requests.get("/ping");

  @BeforeAll
  public static void beforeAll() {
    server.start();
  }

  @Test
  void test() {
    HttpResponse response = client().connectTo(baseUrl()).request(ping);

    assertResponse(response);
  }

  @Test
  void async() {
    Future<HttpResponse> response = asyncClient().connectTo(baseUrl()).request(ping);

    assertResponse(response.get());
  }

  @Test
  void io() {
    IO<HttpResponse> response = ioClient().connectTo(baseUrl()).request(ping);

    assertResponse(response.unsafeRunSync());
  }

  @Test
  void uio() {
    UIO<HttpResponse> response = uioClient().connectTo(baseUrl()).request(ping);

    assertResponse(response.unsafeRunSync());
  }

  @Test
  void task() {
    Task<HttpResponse> response = taskClient().connectTo(baseUrl()).request(ping);

    assertResponse(response.safeRunSync().get());
  }

  @AfterAll
  public static void afterAll() {
    server.stop();
  }

  private void assertResponse(HttpResponse response) {
    assertEquals(HttpStatus.OK, response.status());
    assertEquals(Bytes.asBytes("pong"), response.body());
  }

  private String baseUrl() {
    return "http://localhost:" + server.getPort();
  }
}