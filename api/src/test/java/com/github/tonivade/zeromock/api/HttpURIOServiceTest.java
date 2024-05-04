/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.Bytes.asBytes;
import static com.github.tonivade.zeromock.api.Headers.contentPlain;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.api.Responses.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.effect.Task;
import com.github.tonivade.purefun.effect.URIO;
import com.github.tonivade.purefun.type.Option;

public class HttpURIOServiceTest {

  @Test
  public void ping() {
    HttpURIOService<Void> service = new HttpURIOService<Void>("test")
        .when(get("/ping")).then(request -> URIO.pure(ok("pong")));

    URIO<Void, Option<HttpResponse>> execute = service.execute(Requests.get("/ping"));

    assertEquals(Option.some(ok("pong")), execute.unsafeRunSync(null));
  }

  @Test
  public void echo() {
    HttpURIOService<Void> service = new HttpURIOService<Void>("test")
        .when(get("/echo"))
        .then(request -> Task.task(request::body).fold(Responses::error, Responses::ok).toURIO())
        .postFilter(contentPlain());

    URIO<Void, Option<HttpResponse>> execute = service.execute(Requests.get("/echo").withBody(asBytes("hello")));

    assertEquals(ok("hello").withHeader("Content-type", "text/plain"), execute.unsafeRunSync(null).getOrElseThrow());
  }
}
