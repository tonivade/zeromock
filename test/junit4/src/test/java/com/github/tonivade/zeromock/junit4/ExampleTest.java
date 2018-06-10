package com.github.tonivade.zeromock.junit4;

import static com.github.tonivade.zeromock.api.Bytes.asString;
import static com.github.tonivade.zeromock.api.Handlers.ok;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.server.HttpClient.connectTo;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpStatus;
import com.github.tonivade.zeromock.api.Requests;

public class ExampleTest {
  @Rule
  public MockHttpServerRule server = new MockHttpServerRule(8080);
  
  @Test
  public void ping() {
    server.when(get("/ping")).then(ok("pong"));
    
    HttpResponse response = connectTo("http://localhost:8080").request(Requests.get("/ping"));
    
    assertEquals(HttpStatus.OK, response.status());
    assertEquals("pong", asString(response.body()));
  }
}
