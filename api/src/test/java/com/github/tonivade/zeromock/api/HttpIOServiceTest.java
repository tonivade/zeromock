/*
 * Copyright (c) 2018-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.Bytes.asBytes;
import static com.github.tonivade.zeromock.api.Headers.contentPlain;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.api.Responses.ok;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.type.Option;
import java.time.Duration;

public class HttpIOServiceTest {

  @Test
  public void ping() {
    HttpIOService service = new HttpIOService("test")
        .when(get("/ping")).then(request -> IO.pure(ok("pong")));

    IO<Option<HttpResponse>> execute = service.execute(Requests.get("/ping"));

    assertEquals(ok("pong"), execute.unsafeRunSync().getOrElseThrow());
  }

  @Test
  public void echo() {
    HttpIOService service = new HttpIOService("test")
        .when(get("/echo")).then(request -> IO.task(() -> ok(request.body())))
        .postFilter(contentPlain());

    IO<Option<HttpResponse>> execute = service.execute(Requests.get("/echo").withBody(asBytes("hello")));

    assertEquals(ok("hello").withHeader("Content-type", "text/plain"), execute.unsafeRunSync().getOrElseThrow());
  }

  @Test
  public void withDelay() {
    var delay = Duration.ofSeconds(5);
    HttpIOService service = new HttpIOService("test")
        .when(get("/ping")).then(request -> IO.delay(delay, () -> ok("pong")));

    var start = System.nanoTime();
    var response = service.execute(Requests.get("/ping")).unsafeRunSync();
    var duration = Duration.ofNanos(System.nanoTime() - start);

    assertEquals(ok("pong"), response.getOrElseThrow());
    assertTrue(duration.compareTo(delay) > 0);
  }
}
