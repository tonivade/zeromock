/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.zeromock.api.HttpRequest;

public interface HttpServer {

  HttpServer start();
  void stop();

  void reset();

  int getPort();
  String getPath();

  HttpServer verify(Matcher1<HttpRequest> matcher);
  HttpServer verifyNot(Matcher1<HttpRequest> matcher);

  Sequence<HttpRequest> getUnmatched();
}
