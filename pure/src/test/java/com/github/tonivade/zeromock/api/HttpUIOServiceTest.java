/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.Bytes.asBytes;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.api.Responses.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.type.Option;

public class HttpUIOServiceTest {
  
  @Test
  public void ping() {
    HttpUIOService service = new HttpUIOService("test")
        .when(get("/ping")).then(request -> UIO.pure(ok("pong")));
    
    Option<UIO<HttpResponse>> execute = service.execute(Requests.get("/ping"));
    
    assertEquals(ok("pong"), execute.get().unsafeRunSync());
  }
  
  @Test
  public void echo() {
    HttpUIOService service = new HttpUIOService("test")
        .when(get("/echo")).then(request -> UIO.task(() -> ok(request.body())));
    
    Option<UIO<HttpResponse>> execute = service.execute(Requests.get("/echo").withBody(asBytes("hello")));
    
    assertEquals(ok("hello"), execute.get().unsafeRunSync());
  }
}