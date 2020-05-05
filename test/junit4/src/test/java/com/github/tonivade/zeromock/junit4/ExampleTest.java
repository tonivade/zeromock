/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import static com.github.tonivade.purefun.Nothing.nothing;
import static com.github.tonivade.zeromock.api.Bytes.asString;
import static com.github.tonivade.zeromock.api.Handlers.ok;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.client.HttpClient.connectTo;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.effect.ZIO;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpStatus;
import com.github.tonivade.zeromock.api.Requests;
import com.github.tonivade.zeromock.api.Responses;

public class ExampleTest {
  @Rule
  public MockHttpServerRule server = new MockHttpServerRule(8080);
  @Rule
  public SyncMockHttpServerRule syncServer = new SyncMockHttpServerRule(8081);
  @Rule
  public AsyncMockHttpServerRule asyncServer = new AsyncMockHttpServerRule(8082);
  @Rule
  public IOMockHttpServerRule ioServer = new IOMockHttpServerRule(8083);
  @Rule
  public UIOMockHttpServerRule uioServer = new UIOMockHttpServerRule(8084);
  @Rule
  public ZIOMockHttpServerRule<Nothing> zioServer = new ZIOMockHttpServerRule<>(nothing(), 8085);
  
  @Test
  public void ping() {
    server.when(get("/ping")).then(ok("pong"));
    
    HttpResponse response = ping("http://localhost:8080");
    
    assertPong(response);
  }
  
  @Test
  public void pingSync() {
    syncServer.when(get("/ping")).then(ok("pong").sync());
    
    HttpResponse response = ping("http://localhost:8081");
    
    assertPong(response);
  }
  
  @Test
  public void pingAsync() {
    asyncServer.when(get("/ping")).then(ok("pong").async());
    
    HttpResponse response = ping("http://localhost:8082");
    
    assertPong(response);
  }
  
  @Test
  public void pingIO() {
    ioServer.when(get("/ping")).then(request -> IO.pure(Responses.ok("pong")));
    
    HttpResponse response = ping("http://localhost:8083");
    
    assertPong(response);
  }
  
  @Test
  public void pingUIO() {
    uioServer.when(get("/ping")).then(request -> UIO.pure(Responses.ok("pong")));
    
    HttpResponse response = ping("http://localhost:8084");
    
    assertPong(response);
  }
  
  @Test
  public void pingZIO() {
    zioServer.when(get("/ping")).then(request -> ZIO.pure(Responses.ok("pong")));
    
    HttpResponse response = ping("http://localhost:8085");
    
    assertPong(response);
  }

  private HttpResponse ping(String baseUrl) {
    return connectTo(baseUrl).request(Requests.get("/ping"));
  }

  private void assertPong(HttpResponse response) {
    assertEquals(HttpStatus.OK, response.status());
    assertEquals("pong", asString(response.body()));
  }
}
