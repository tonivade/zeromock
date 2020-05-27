/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import org.junit.rules.ExternalResource;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpService;
import com.github.tonivade.zeromock.api.HttpService.MappingBuilder;
import com.github.tonivade.zeromock.api.RequestHandler;
import com.github.tonivade.zeromock.client.AsyncHttpClient;
import com.github.tonivade.zeromock.client.HttpClient;
import com.github.tonivade.zeromock.client.IOHttpClient;
import com.github.tonivade.zeromock.client.TaskHttpClient;
import com.github.tonivade.zeromock.client.UIOHttpClient;
import com.github.tonivade.zeromock.server.MockHttpServer;

public class MockHttpServerRule extends ExternalResource {

  private final MockHttpServer server;

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
    return HttpClient.connectTo(getBaseUrl());
  }

  public AsyncHttpClient asyncClient() {
    return AsyncHttpClient.connectTo(getBaseUrl());
  }

  public IOHttpClient ioClient() {
    return IOHttpClient.connectTo(getBaseUrl());
  }

  public UIOHttpClient uioClient() {
    return UIOHttpClient.connectTo(getBaseUrl());
  }

  public TaskHttpClient taskClient() {
    return TaskHttpClient.connectTo(getBaseUrl());
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

  public MappingBuilder<MockHttpServerRule> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::addMapping).when(matcher);
  }

  public MockHttpServerRule mount(String path, HttpService service) {
    server.mount(path, service);
    return this;
  }
}
