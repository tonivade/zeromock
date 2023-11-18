/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import static com.github.tonivade.purefun.Precondition.checkNonNull;

import org.junit.rules.ExternalResource;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.annotation.Witness;
import com.github.tonivade.purefun.typeclasses.Monad;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpRouteBuilderK;
import com.github.tonivade.zeromock.api.HttpServiceK;
import com.github.tonivade.zeromock.api.RequestHandlerK;
import com.github.tonivade.zeromock.client.HttpClient;
import com.github.tonivade.zeromock.server.MockHttpServerK;

public abstract class AbstractMockServerRule<F extends Witness> extends ExternalResource implements HttpRouteBuilderK<F, AbstractMockServerRule<F>> {

  private final Monad<F> monad;
  private final MockHttpServerK<F> server;

  protected AbstractMockServerRule(Monad<F> monad, MockHttpServerK<F> server) {
    this.monad = checkNonNull(monad);
    this.server = checkNonNull(server);
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

  @Override
  public ThenStepK<F, AbstractMockServerRule<F>> when(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(monad, handler -> addMapping(matcher, handler));
  }

  public AbstractMockServerRule<F> mount(String path, HttpServiceK<F> service) {
    server.mount(path, service);
    return this;
  }
}
