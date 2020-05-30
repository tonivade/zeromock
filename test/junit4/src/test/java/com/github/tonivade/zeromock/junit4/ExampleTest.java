/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import static com.github.tonivade.purefun.Nothing.nothing;
import static com.github.tonivade.zeromock.api.Bytes.asString;
import static com.github.tonivade.zeromock.api.Handlers.ok;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.effect.ZIO;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpStatus;
import com.github.tonivade.zeromock.api.Requests;
import com.github.tonivade.zeromock.api.Responses;

public class ExampleTest {
  @Rule
  public MockHttpServerRule server = new MockHttpServerRule();
  @Rule
  public AsyncMockHttpServerRule asyncServer = new AsyncMockHttpServerRule();
  @Rule
  public IOMockHttpServerRule ioServer = new IOMockHttpServerRule();
  @Rule
  public UIOMockHttpServerRule uioServer = new UIOMockHttpServerRule();
  @Rule
  public ZIOMockHttpServerRule<Nothing> zioServer = new ZIOMockHttpServerRule<>(nothing());

  @Test
  public void pingSync() {
    server.when(get("/ping")).then(ok("pong"));

    HttpResponse response = server.client().request(ping());

    assertPong(response);
  }

  @Test
  public void pingAsync() {
    asyncServer.when(get("/ping")).then(ok("pong").liftFuture()::apply);

    HttpResponse response = asyncServer.client().request(ping());

    assertPong(response);
  }

  @Test
  public void pingIO() {
    ioServer.when(get("/ping")).then(request -> IO.pure(Responses.ok("pong")));

    HttpResponse response = ioServer.client().request(ping());

    assertPong(response);
  }

  @Test
  public void pingUIO() {
    uioServer.when(get("/ping")).then(request -> UIO.pure(Responses.ok("pong")));

    HttpResponse response = uioServer.client().request(ping());

    assertPong(response);
  }

  @Test
  public void pingZIO() {
    zioServer.when(get("/ping")).then(request -> ZIO.pure(Responses.ok("pong")));

    HttpResponse response = zioServer.client().request(ping());

    assertPong(response);
  }

  private HttpRequest ping() {
    return Requests.get("/ping");
  }

  private void assertPong(HttpResponse response) {
    assertEquals(HttpStatus.OK, response.status());
    assertEquals("pong", asString(response.body()));
  }
}
