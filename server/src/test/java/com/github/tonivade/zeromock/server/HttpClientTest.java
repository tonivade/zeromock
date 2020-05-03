/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.effect.Task;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.effect.ZIO;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.zeromock.api.Bytes;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpStatus;
import com.github.tonivade.zeromock.api.Requests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.github.tonivade.purefun.Nothing.nothing;
import static com.github.tonivade.zeromock.api.Handlers.ok;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.server.MockHttpServer.listenAt;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpClientTest {

  public static final String BASE_URL = "http://localhost:8080";

  private static final MockHttpServer server =
      listenAt(8080).when(get("/ping")).then(ok("pong"));

  private final HttpRequest ping = Requests.get("/ping");

  @BeforeAll
  public static void beforeAll() {
    server.start();
  }

  @Test
  void test() {
    HttpResponse response = HttpClient.connectTo(BASE_URL).request(ping);

    assertResponse(response);
  }

  @Test
  void async() {
    Future<HttpResponse> response = AsyncHttpClient.connectTo(BASE_URL).request(ping);

    assertResponse(response.get());
  }

  @Test
  void io() {
    IO<HttpResponse> response = IOHttpClient.connectTo(BASE_URL).request(ping);

    assertResponse(response.unsafeRunSync());
  }

  @Test
  void uio() {
    UIO<HttpResponse> response = UIOHttpClient.connectTo(BASE_URL).request(ping);

    assertResponse(response.unsafeRunSync());
  }

  @Test
  void task() {
    Task<HttpResponse> response = TaskHttpClient.connectTo(BASE_URL).request(ping);

    assertResponse(response.safeRunSync().get());
  }

  @Test
  void zio() {
    ZIO<Nothing, Throwable, HttpResponse> response = ZIOHttpClient.<Nothing>connectTo(BASE_URL).request(ping);

    assertResponse(response.provide(nothing()).get());
  }

  @AfterAll
  public static void afterAll() {
    server.stop();
  }

  private void assertResponse(HttpResponse response) {
    assertEquals(HttpStatus.OK, response.status());
    assertEquals(Bytes.asBytes("pong"), response.body());
  }
}