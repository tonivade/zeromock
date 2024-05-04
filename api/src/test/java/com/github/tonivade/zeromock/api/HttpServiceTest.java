/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

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

public class HttpServiceTest {

  @Test
  public void initialState() {
    HttpService service = new HttpService("service");
    
    assertAll(
        () -> assertEquals("service", service.name()),
        () -> assertEquals(none(), service.execute(Requests.get("/ping")))
    );
  }

  @Test
  public void whenThen() {
    HttpService service = new HttpService("service")
        .when(get("/ping")).then(ok("pong"));
    
    assertEquals(some(Responses.ok("pong")), service.execute(Requests.get("/ping")));
  }

  @Test
  public void exec() {
    HttpService service = new HttpService("service")
        .exec(ok("pong"));
    
    assertEquals(some(Responses.ok("pong")), service.execute(Requests.get("/ping")));
  }
  
  @Test
  public void mount() {
    HttpService service1 = new HttpService("service1")
        .when(get("/ping")).then(ok("pong"));
    HttpService service2 = new HttpService("service2")
        .mount("/path", service1);
    
    assertAll(
        () -> assertEquals(some(Responses.ok("pong")), service2.execute(Requests.get("/path/ping"))),
        () -> assertEquals(none(), service2.execute(Requests.get("/path/notfound"))),
        () -> assertEquals(none(), service2.execute(Requests.get("/ping")))
    );
  }

  @Test
  public void combine() {
    HttpService service1 = new HttpService("service1").when(get("/ping")).then(ok("pong"));
    HttpService service2 = new HttpService("service2");

    assertEquals(some(Responses.ok("pong")), service1.combine(service2).execute(Requests.get("/ping")));
  }

  @Test
  public void filters() {
    HttpService service1 = new HttpService("service1")
        .preFilter(put()).then(forbidden())
        .when(get("/ping")).then(ok("pong"))
        .postFilter(contentPlain());

    assertAll(
        () -> assertEquals(some(Responses.forbidden()), service1.execute(Requests.put("/ping"))),
        () -> assertEquals(
            some(Responses.ok("pong").withHeader("Content-type", "text/plain")),
            service1.execute(Requests.get("/ping")))
    );
  }
}
