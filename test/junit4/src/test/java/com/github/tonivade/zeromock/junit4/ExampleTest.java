package com.github.tonivade.zeromock.junit4;

import static com.github.tonivade.zeromock.core.Bytes.asString;
import static com.github.tonivade.zeromock.core.Handlers.ok;
import static com.github.tonivade.zeromock.core.Mappings.get;
import static com.github.tonivade.zeromock.server.HttpClient.connectTo;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import com.github.tonivade.zeromock.core.HttpResponse;
import com.github.tonivade.zeromock.core.HttpStatus;
import com.github.tonivade.zeromock.core.Requests;

public class ExampleTest {
  @Rule
  public MockHttpServerRule server = new MockHttpServerRule(8080);
  
  @Test
  public void ping() {
    server.when(get("/ping").then(ok("pong")));
    
    HttpResponse response = connectTo("http://localhost:8080").request(Requests.get("/ping"));
    
    assertEquals(HttpStatus.OK, response.status());
    assertEquals(asString(response.body()), "pong");
  }
}
