/*
 * Copyright (c) 2018-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import org.junit.rules.ExternalResource;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpRouteBuilder;
import com.github.tonivade.zeromock.api.HttpService;
import com.github.tonivade.zeromock.api.RequestHandler;
import com.github.tonivade.zeromock.client.AsyncHttpClient;
import com.github.tonivade.zeromock.client.HttpClient;
import com.github.tonivade.zeromock.client.HttpClientBuilder;
import com.github.tonivade.zeromock.client.IOHttpClient;
import com.github.tonivade.zeromock.client.TaskHttpClient;
import com.github.tonivade.zeromock.client.UIOHttpClient;
import com.github.tonivade.zeromock.server.MockHttpServer;

public class MockHttpServerRule extends ExternalResource implements HttpRouteBuilder<MockHttpServerRule> {

  private final MockHttpServer server;

  public MockHttpServerRule() {
     this(0);
  }

  public MockHttpServerRule(int port) {
    server = MockHttpServer.listenAt(port);
  }

  @Override
  protected void before() {
    server.start();
  }

  @Override
  protected void after() {
    server.stop();
  }

  public String getBaseUrl() {
    return "http://localhost:" + server.getPort();
  }

  public HttpClient client() {
    return HttpClientBuilder.client().connectTo(getBaseUrl());
  }

  public AsyncHttpClient asyncClient() {
    return HttpClientBuilder.asyncClient().connectTo(getBaseUrl());
  }

  public IOHttpClient ioClient() {
    return HttpClientBuilder.ioClient().connectTo(getBaseUrl());
  }

  public UIOHttpClient uioClient() {
    return HttpClientBuilder.uioClient().connectTo(getBaseUrl());
  }

  public TaskHttpClient taskClient() {
    return HttpClientBuilder.taskClient().connectTo(getBaseUrl());
  }

  public MockHttpServerRule verify(Matcher1<HttpRequest> matcher) {
    server.verify(matcher);
    return this;
  }

  public MockHttpServerRule verifyNot(Matcher1<HttpRequest> matcher) {
    server.verifyNot(matcher);
    return this;
  }

  public MockHttpServerRule addMapping(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    server.when(matcher).then(handler);
    return this;
  }

  public ThenStep<MockHttpServerRule> when(Matcher1<HttpRequest> matcher) {
    return handler -> addMapping(matcher, handler);
  }

  public MockHttpServerRule mount(String path, HttpService service) {
    server.mount(path, service);
    return this;
  }
}
