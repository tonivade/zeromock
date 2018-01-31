package com.github.tonivade.zeromock;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.IOException;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import com.github.tonivade.zeromock.MockHttpServer.Response;

public class MockHttpServerTest {
  public MockHttpServer server = new MockHttpServer(8080).when("/test", request -> new Response(200, "Hello World!"));

  @Before
  public void setUp() {
    server.start();
  }

  @After
  public void tearDown() {
    server.stop();
  }

  @Test
  public void test() throws IOException {
    URL url = new URL("http://localhost:8080/test");
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");

    assertEquals(200, con.getResponseCode());
  }
}
