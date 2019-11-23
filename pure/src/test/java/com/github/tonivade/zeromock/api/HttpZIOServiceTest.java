/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.concurrent.Promise;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;
import com.github.tonivade.purefun.zio.Task;
import com.github.tonivade.purefun.zio.ZIO;
import org.junit.jupiter.api.Test;

import static com.github.tonivade.zeromock.api.Bytes.asBytes;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.api.Responses.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpZIOServiceTest {

  @Test
  public void ping() {
    HttpZIOService<Nothing> service = new HttpZIOService<>("test", Nothing::nothing)
        .when(get("/ping")).then(request -> ZIO.pure(ok("pong")));
    
    Option<Promise<HttpResponse>> execute = service.execute(Requests.get("/ping"));
    
    assertEquals(Try.success(ok("pong")), execute.get().get());
  }
  
  @Test
  public void echo() {
    HttpZIOService<Nothing> service = new HttpZIOService<>("test", Nothing::nothing)
        .when(get("/echo")).then(request -> Task.from(request::body).fold(Responses::error, Responses::ok).toZIO());
    
    Option<Promise<HttpResponse>> execute = service.execute(Requests.get("/echo").withBody(asBytes("hello")));
    
    assertEquals(Try.success(ok("hello")), execute.get().get());
  }
}
