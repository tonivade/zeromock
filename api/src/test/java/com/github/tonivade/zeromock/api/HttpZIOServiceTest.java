/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.Nothing.nothing;
import static com.github.tonivade.zeromock.api.Bytes.asBytes;
import static com.github.tonivade.zeromock.api.Headers.contentPlain;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.api.Responses.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.effect.Task;
import com.github.tonivade.purefun.effect.ZIO;
import com.github.tonivade.purefun.type.Option;

public class HttpZIOServiceTest {

  @Test
  public void ping() {
    HttpZIOService<Nothing> service = new HttpZIOService<Nothing>("test")
        .when(get("/ping")).then(request -> ZIO.pure(ok("pong")));
    
    ZIO<Nothing, Nothing, Option<HttpResponse>> execute = service.execute(Requests.get("/ping"));
    
    assertEquals(Option.some(ok("pong")), execute.provide(nothing()).get());
  }
  
  @Test
  public void echo() {
    HttpZIOService<Nothing> service = new HttpZIOService<Nothing>("test")
        .when(get("/echo"))
        .then(request -> Task.task(request::body).fold(Responses::error, Responses::ok).toZIO())
        .postFilter(contentPlain());

    ZIO<Nothing, Nothing, Option<HttpResponse>> execute = service.execute(Requests.get("/echo").withBody(asBytes("hello")));

    assertEquals(ok("hello").withHeader("Content-type", "text/plain"), execute.provide(nothing()).get().get());
  }
}
