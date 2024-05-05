/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.concurrent.Future;
import org.junit.jupiter.api.Test;

import static com.github.tonivade.purefun.type.Option.none;
import static com.github.tonivade.purefun.type.Option.some;
import static com.github.tonivade.zeromock.api.Handlers.forbidden;
import static com.github.tonivade.zeromock.api.Handlers.ok;
import static com.github.tonivade.zeromock.api.Headers.contentPlain;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.api.Matchers.put;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AsyncHttpServiceTest {

  @Test
  public void initialState() {
    AsyncHttpService service = new AsyncHttpService("service");
    
    assertAll(
        () -> assertEquals("service", service.name()),
        () -> assertEquals(none(), service.execute(Requests.get("/ping")).await().getOrElseThrow())
    );
  }

  @Test
  public void whenThen() {
    AsyncHttpService service = new AsyncHttpService("service")
        .when(get("/ping")).then(ok("pong").andThen(Future::success)::apply);
    
    assertEquals(some(Responses.ok("pong")), service.execute(Requests.get("/ping")).await().getOrElseThrow());
  }

  @Test
  public void exec() {
    AsyncHttpService service = new AsyncHttpService("service")
        .exec(ok("pong").andThen(Future::success)::apply);
    
    assertEquals(some(Responses.ok("pong")), service.execute(Requests.get("/ping")).await().getOrElseThrow());
  }
  
  @Test
  public void mount() {
    AsyncHttpService service1 = new AsyncHttpService("service")
        .when(get("/ping")).then(ok("pong").andThen(Future::success)::apply);
    AsyncHttpService service2 = new AsyncHttpService("service")
        .mount("/path", service1);
    
    assertAll(
        () -> assertEquals(some(Responses.ok("pong")), service2.execute(Requests.get("/path/ping")).await().getOrElseThrow()),
        () -> assertEquals(none(), service2.execute(Requests.get("/path/notfound")).await().getOrElseThrow()),
        () -> assertEquals(none(), service2.execute(Requests.get("/ping")).await().getOrElseThrow())
    );
  }

  @Test
  public void combine() {
    AsyncHttpService service1 = new AsyncHttpService("service1")
        .when(get("/ping")).then(ok("pong").andThen(Future::success)::apply);
    AsyncHttpService service2 = new AsyncHttpService("service2");

    assertEquals(some(Responses.ok("pong")), service1.combine(service2).execute(Requests.get("/ping")).await().getOrElseThrow());
  }

  @Test
  public void filters() {
    AsyncHttpService service1 = new AsyncHttpService("service1")
        .preFilter(PreFilter.filter(put(), forbidden()))
        .when(get("/ping")).then(ok("pong").andThen(Future::success)::apply)
        .postFilter(contentPlain());

    assertAll(
        () -> assertEquals(some(Responses.forbidden()), service1.execute(Requests.put("/ping")).await().getOrElseThrow()),
        () -> assertEquals(
            some(Responses.ok("pong").withHeader("Content-type", "text/plain")),
            service1.execute(Requests.get("/ping")).await().getOrElseThrow())
    );
  }
}
