/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import static java.util.Objects.requireNonNull;
import org.junit.rules.ExternalResource;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpServiceK;
import com.github.tonivade.zeromock.api.HttpServiceK.MappingBuilderK;
import com.github.tonivade.zeromock.api.RequestHandlerK;
import com.github.tonivade.zeromock.server.MockHttpServerK;

public abstract class AbstractMockServerRule<F extends Witness> extends ExternalResource {

  private final MockHttpServerK<F> server;

  public AbstractMockServerRule(MockHttpServerK<F> server) {
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

  public AbstractMockServerRule<F> verify(Matcher1<HttpRequest> matcher) {
    server.verify(matcher);
    return this;
  }

  public AbstractMockServerRule<F> addMapping(Matcher1<HttpRequest> matcher, RequestHandlerK<F> handler) {
    server.when(matcher).then(handler);
    return this;
  }

  public MappingBuilderK<F, AbstractMockServerRule<F>> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilderK<>(this::addMapping).when(matcher);
  }

  public AbstractMockServerRule<F> mount(String path, HttpServiceK<F> service) {
    server.mount(path, service);
    return this;
  }
}
