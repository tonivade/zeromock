/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.IOUtils.readAll;
import static com.github.tonivade.zeromock.MockHttpServer.listenAt;
import static com.github.tonivade.zeromock.Predicates.acceptsJson;
import static com.github.tonivade.zeromock.Predicates.acceptsXml;
import static com.github.tonivade.zeromock.Predicates.get;
import static com.github.tonivade.zeromock.Predicates.param;
import static com.github.tonivade.zeromock.Predicates.path;
import static com.github.tonivade.zeromock.Responses.badRequest;
import static com.github.tonivade.zeromock.Responses.contentJson;
import static com.github.tonivade.zeromock.Responses.contentXml;
import static com.github.tonivade.zeromock.Responses.ok;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MockHttpServerTest {

  public MockHttpServer server = listenAt(8080)
      .when(get().and(path("/hello")).and(param("name")), ok("Hello %s!"))
      .when(get().and(path("/bye")).and(param("name").negate()), badRequest("missing parameter name"))
      .when(get().and(path("/test")).and(acceptsXml()), ok("<body/>").andThen(contentXml()))
      .when(get().and(path("/test")).and(acceptsJson()), contentJson().compose(ok("{ }")));

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
    URL url = new URL("http://localhost:8080/hello?name=World");
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");

    assertEquals(200, con.getResponseCode());
    assertEquals("Hello World!", readAll(con.getInputStream()));
  }
  
  @Test
  public void bye() throws IOException {
    URL url = new URL("http://localhost:8080/bye");
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");

    assertEquals(400, con.getResponseCode());
  }

  @Test
  public void json() throws IOException {
    URL url = new URL("http://localhost:8080/test");
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("Accept", "application/json");

    assertEquals(200, con.getResponseCode());
    assertEquals("{ }", readAll(con.getInputStream()));
    assertEquals("application/json", con.getHeaderField("Content-Type"));
  }

  @Test
  public void xml() throws IOException {
    URL url = new URL("http://localhost:8080/test");
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("Accept", "text/xml");

    assertEquals(200, con.getResponseCode());
    assertEquals("<body/>", readAll(con.getInputStream()));
    assertEquals("text/xml", con.getHeaderField("Content-Type"));
  }
}
