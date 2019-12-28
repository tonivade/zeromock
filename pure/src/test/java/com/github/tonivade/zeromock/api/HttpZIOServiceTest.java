/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.Nothing.nothing;
import static com.github.tonivade.zeromock.api.Bytes.asBytes;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.api.Responses.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.effect.Task;
import com.github.tonivade.purefun.effect.ZIO;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.purefun.type.Option;

public class HttpZIOServiceTest {

  @Test
  public void ping() {
    HttpZIOService<Nothing> service = new HttpZIOService<Nothing>("test")
        .when(get("/ping")).then(request -> ZIO.pure(ok("pong")));
    
    Option<ZIO<Nothing, Nothing, HttpResponse>> execute = service.execute(Requests.get("/ping"));
    
    assertEquals(Either.right(ok("pong")), execute.get().provide(nothing()));
  }
  
  @Test
  public void echo() {
    HttpZIOService<Nothing> service = new HttpZIOService<Nothing>("test")
        .when(get("/echo"))
        .then(request -> Task.task(request::body).fold(Responses::error, Responses::ok).<Nothing, Nothing>toZIO());
    
    Option<ZIO<Nothing, Nothing, HttpResponse>> execute = service.execute(Requests.get("/echo").withBody(asBytes("hello")));
    
    assertEquals(Either.right(ok("hello")), execute.get().provide(nothing()));
  }
}
