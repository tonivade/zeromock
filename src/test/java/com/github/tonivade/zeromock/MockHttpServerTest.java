package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.Predicates.get;
import static com.github.tonivade.zeromock.MockHttpServer.listenAt;
import static com.github.tonivade.zeromock.Predicates.acceptsJson;
import static com.github.tonivade.zeromock.Predicates.path;
import static com.github.tonivade.zeromock.Predicates.acceptsXml;
import static com.github.tonivade.zeromock.Responses.contentType;
import static com.github.tonivade.zeromock.Responses.ok;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MockHttpServerTest {

  public MockHttpServer server = listenAt(8080)
      .when(get().and(path("/hello")), ok("Hello World!"))
      .when(get().and(path("/test")).and(acceptsXml()), ok("<body/>").andThen(contentType("text/xml")))
      .when(get().and(path("/test")).and(acceptsJson()), ok("{ \"json\": true }").andThen(contentType("application/json")));

  @Before
  public void setUp() {
    server.start();
  }

  @After
  public void tearDown() {
    server.stop();
  }

  @Test
  public void hello() throws IOException {
    URL url = new URL("http://localhost:8080/hello");
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");

    assertEquals(200, con.getResponseCode());
  }

  @Test
  public void testJson() throws IOException {
    URL url = new URL("http://localhost:8080/test");
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("Accept", "application/json");

    assertEquals(200, con.getResponseCode());
    assertEquals("{ \"json\": true }", read(con.getInputStream()));
    assertEquals("application/json", con.getHeaderField("Content-Type"));
  }

  @Test
  public void testXml() throws IOException {
    URL url = new URL("http://localhost:8080/test");
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("Accept", "text/xml");

    assertEquals(200, con.getResponseCode());
    assertEquals("<body/>", read(con.getInputStream()));
    assertEquals("text/xml", con.getHeaderField("Content-Type"));
  }

  private String read(InputStream input) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    while (true) {
      int read = input.read(buffer);
      if (read > 0) {
        out.write(buffer, 0, read);
      } else break;
    }
    return new String(out.toByteArray(), Charset.forName("UTF-8"));
  }
}
