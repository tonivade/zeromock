/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import static java.util.Objects.requireNonNull;

import org.junit.rules.ExternalResource;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpServiceK;
import com.github.tonivade.zeromock.api.HttpServiceK.MappingBuilderK;
import com.github.tonivade.zeromock.api.RequestHandlerK;
import com.github.tonivade.zeromock.server.MockHttpServerK;

public abstract class AbstractMockServerRule<F extends Kind, H extends RequestHandlerK<F>> extends ExternalResource {

  private final MockHttpServerK<F> server;

  public AbstractMockServerRule(MockHttpServerK<F> server) {
    this.server = requireNonNull(server);
  }

  @Override
  protected void before() throws Throwable {
    server.start();
  }

  @Override
  protected void after() {
    server.stop();
  }

  public AbstractMockServerRule<F, H> verify(Matcher1<HttpRequest> matcher) {
    server.verify(matcher);
    return this;
  }

  public AbstractMockServerRule<F, H> add(Matcher1<HttpRequest> matcher, H handler) {
    server.add(matcher, handler);
    return this;
  }

  public MappingBuilderK<F, H, AbstractMockServerRule<F, H>> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilderK<>(this::add).when(matcher);
  }

  public AbstractMockServerRule<F, H> mount(String path, HttpServiceK<F> service) {
    server.mount(path, service);
    return this;
  }
}
