/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.Bytes.asBytes;
import static com.github.tonivade.zeromock.api.Headers.contentJson;
import static com.github.tonivade.zeromock.api.Headers.contentPlain;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.api.Responses.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.type.Option;

public class HttpIOServiceTest {
  
  @Test
  public void ping() {
    HttpIOService service = new HttpIOService("test")
        .when(get("/ping")).then(request -> IO.pure(ok("pong")));
    
    IO<Option<HttpResponse>> execute = service.execute(Requests.get("/ping"));
    
    assertEquals(ok("pong"), execute.unsafeRunSync().get());
  }
  
  @Test
  public void echo() {
    HttpIOService service = new HttpIOService("test")
        .when(get("/echo")).then(request -> IO.task(() -> ok(request.body())))
        .postFilter(contentPlain());
    
    IO<Option<HttpResponse>> execute = service.execute(Requests.get("/echo").withBody(asBytes("hello")));
    
    assertEquals(ok("hello").withHeader("Content-type", "text/plain"), execute.unsafeRunSync().get());
  }
}
