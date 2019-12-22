package com.github.tonivade.zeromock.junit4;

import static com.github.tonivade.purefun.Nothing.nothing;
import static com.github.tonivade.zeromock.api.Bytes.asString;
import static com.github.tonivade.zeromock.api.Handlers.ok;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.server.HttpClient.connectTo;
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
  public MockHttpServerRule syncServer = new MockHttpServerRule(8081);
  @Rule
  public AsyncMockHttpServerRule asyncServer = new AsyncMockHttpServerRule(8082);
  @Rule
  public IOMockHttpServerRule ioServer = new IOMockHttpServerRule(8083);
  @Rule
  public UIOMockHttpServerRule uioServer = new UIOMockHttpServerRule(8084);
  @Rule
  public ZIOMockHttpServerRule<Nothing> zioServer = new ZIOMockHttpServerRule<Nothing>(nothing(), 8085);
  
  @Test
  public void ping() {
    server.when(get("/ping")).then(ok("pong"));
    
    HttpResponse response = connectTo("http://localhost:8080").request(Requests.get("/ping"));
    
    assertEquals(HttpStatus.OK, response.status());
    assertEquals("pong", asString(response.body()));
  }
  
  @Test
  public void pingSync() {
    syncServer.when(get("/ping")).then(ok("pong"));
    
    HttpResponse response = connectTo("http://localhost:8081").request(Requests.get("/ping"));
    
    assertEquals(HttpStatus.OK, response.status());
    assertEquals("pong", asString(response.body()));
  }
  
  @Test
  public void pingAsync() {
    asyncServer.when(get("/ping")).then(ok("pong").async());
    
    HttpResponse response = connectTo("http://localhost:8082").request(Requests.get("/ping"));
    
    assertEquals(HttpStatus.OK, response.status());
    assertEquals("pong", asString(response.body()));
  }
  
  @Test
  public void pingIO() {
    ioServer.when(get("/ping")).then(request -> IO.pure(Responses.ok("pong")));
    
    HttpResponse response = connectTo("http://localhost:8083").request(Requests.get("/ping"));
    
    assertEquals(HttpStatus.OK, response.status());
    assertEquals("pong", asString(response.body()));
  }
  
  @Test
  public void pingUIO() {
    uioServer.when(get("/ping")).then(request -> UIO.pure(Responses.ok("pong")));
    
    HttpResponse response = connectTo("http://localhost:8084").request(Requests.get("/ping"));
    
    assertEquals(HttpStatus.OK, response.status());
    assertEquals("pong", asString(response.body()));
  }
  
  @Test
  public void pingZIO() {
    zioServer.when(get("/ping")).then(request -> ZIO.pure(Responses.ok("pong")));
    
    HttpResponse response = connectTo("http://localhost:8085").request(Requests.get("/ping"));
    
    assertEquals(HttpStatus.OK, response.status());
    assertEquals("pong", asString(response.body()));
  }
}
