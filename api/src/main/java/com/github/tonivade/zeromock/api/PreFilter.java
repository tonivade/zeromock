/*
 * Copyright (c) 2018-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.type.Either;

public interface PreFilter extends Function1<HttpRequest, Either<HttpResponse, HttpRequest>> {

  static PreFilter filter(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    return request -> matcher.match(request) ?
        Either.left(handler.apply(request)) : Either.right(request);
  }
  
  static PreFilter print(PrintStream output) {
    return print(new PrintWriter(new OutputStreamWriter(output, UTF_8), true));
  }

  static PreFilter print(PrintWriter output) {
    return request -> {
      output.println(new Date());
      output.println(request.method() + " " + request.path().toPath() + request.params().toQueryString());
      request.headers().forEach((name, value) -> output.println(name + ":" + value));
      output.println();
      output.println(Bytes.asString(request.body()));
      return Either.right(request);
    };
  }
}
