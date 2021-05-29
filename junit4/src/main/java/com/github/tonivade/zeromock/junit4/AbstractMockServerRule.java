/*
 * Copyright (c) 2018-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import static java.util.Objects.requireNonNull;

import org.junit.rules.ExternalResource;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpServiceK;
import com.github.tonivade.zeromock.api.Matchers;
import com.github.tonivade.zeromock.api.RequestHandlerK;
import com.github.tonivade.zeromock.client.HttpClient;
import com.github.tonivade.zeromock.server.MockHttpServerK;

public abstract class AbstractMockServerRule<F extends Witness> extends ExternalResource {

  private final MockHttpServerK<F> server;

  protected AbstractMockServerRule(MockHttpServerK<F> server) {
    this.server = requireNonNull(server);
  }

  @Override
  protected void before() {
    server.start();
  }

  @Override
  protected void after() {
    server.stop();
  }

  public HttpClient client() {
    return HttpClient.connectTo("http://localhost:" + server.getPort());
  }

  public AbstractMockServerRule<F> verify(Matcher1<HttpRequest> matcher) {
    server.verify(matcher);
    return this;
  }

  public AbstractMockServerRule<F> verifyNot(Matcher1<HttpRequest> matcher) {
    server.verifyNot(matcher);
    return this;
  }

  public AbstractMockServerRule<F> addMapping(Matcher1<HttpRequest> matcher, RequestHandlerK<F> handler) {
    server.when(matcher).then(handler);
    return this;
  }

  public HttpServiceK.ThenStep<F, AbstractMockServerRule<F>> when(Matcher1<HttpRequest> matcher) {
    return handler -> addMapping(matcher, handler);
  }

  public HttpServiceK.ThenStep<F, AbstractMockServerRule<F>> get(String path) {
    return when(Matchers.get(path));
  }

  public HttpServiceK.ThenStep<F, AbstractMockServerRule<F>> post(String path) {
    return when(Matchers.post(path));
  }

  public HttpServiceK.ThenStep<F, AbstractMockServerRule<F>> put(String path) {
    return when(Matchers.put(path));
  }

  public HttpServiceK.ThenStep<F, AbstractMockServerRule<F>> delete(String path) {
    return when(Matchers.delete(path));
  }

  public HttpServiceK.ThenStep<F, AbstractMockServerRule<F>> patch(String path) {
    return when(Matchers.patch(path));
  }

  public HttpServiceK.ThenStep<F, AbstractMockServerRule<F>> options(String path) {
    return when(Matchers.options(path));
  }

  public HttpServiceK.ThenStep<F, AbstractMockServerRule<F>> head(String path) {
    return when(Matchers.head(path));
  }

  public AbstractMockServerRule<F> mount(String path, HttpServiceK<F> service) {
    server.mount(path, service);
    return this;
  }
}
