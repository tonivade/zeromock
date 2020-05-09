/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.type.Option;

public class HttpServiceTest {

  @Test
  public void initialState() {
    HttpService service = new HttpService("service");
    
    assertAll(() -> assertEquals("service", service.name()),
              () -> assertEquals(Option.none(), service.execute(Requests.get("/ping"))));
  }

  @Test
  public void whenThen() {
    HttpService service = new HttpService("service").when(Matchers.get("/ping")).then(Handlers.ok("pong"));
    
    assertEquals(Option.some(Responses.ok("pong")), service.execute(Requests.get("/ping")));
  }

  @Test
  public void exec() {
    HttpService service = new HttpService("service").exec(Handlers.ok("pong"));
    
    assertEquals(Option.some(Responses.ok("pong")), service.execute(Requests.get("/ping")));
  }
  
  @Test
  public void mount() {
    HttpService service1 = new HttpService("service1").when(Matchers.get("/ping")).then(Handlers.ok("pong"));
    HttpService service2 = new HttpService("service2").mount("/path", service1);
    
    assertAll(() -> assertEquals(Option.some(Responses.ok("pong")), service2.execute(Requests.get("/path/ping"))),
              () -> assertEquals(Option.none(), service2.execute(Requests.get("/path/notfound"))),
              () -> assertEquals(Option.none(), service2.execute(Requests.get("/ping"))));
  }
  
  @Test
  public void combine() {
    HttpService service1 = new HttpService("service1").when(Matchers.get("/ping")).then(Handlers.ok("pong"));
    HttpService service2 = new HttpService("service2");
    
    assertEquals(Option.some(Responses.ok("pong")), service1.combine(service2).execute(Requests.get("/ping")));
  }
}
