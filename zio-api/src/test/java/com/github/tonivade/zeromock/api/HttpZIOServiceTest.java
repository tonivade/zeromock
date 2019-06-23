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

import com.github.tonivade.purefun.concurrent.Promise;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;
import com.github.tonivade.purefun.zio.ZIO;

public class HttpZIOServiceTest {
  
  @Test
  public void ping() {
    HttpZIOService<Environment> service = new HttpZIOService<>("test", Environment::create)
        .when(get("/ping")).then(ZIO.pure(ok("pong")));
    
    Option<Promise<HttpResponse>> execute = service.execute(Requests.get("/ping"));
    
    assertEquals(Try.success(ok("pong")), execute.get().get());
  }
  
  @Test
  public void echo() {
    HttpZIOService<Environment> service = new HttpZIOService<>("test", Environment::create)
        .when(get("/echo")).then(HasHttpRequest.<Environment>body().fold(Responses::error, Responses::ok));
    
    Option<Promise<HttpResponse>> execute = service.execute(Requests.get("/echo").withBody(asBytes("hello")));
    
    assertEquals(Try.success(ok("hello")), execute.get().get());
  }

}

interface Environment extends HasHttpRequest {
  
  static Environment create(HttpRequest request) {
    return new Environment() {
      
      @Override
      public Service request() {
        return HasHttpRequest.use(request);
      }
    };
  }
}