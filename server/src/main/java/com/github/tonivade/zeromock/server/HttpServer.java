/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import java.util.List;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.zeromock.api.HttpRequest;

public interface HttpServer {

  HttpServer start();

  void stop();

  void reset();

  HttpServer verify(Matcher1<HttpRequest> matcher);

  List<HttpRequest> getUnmatched();
}
