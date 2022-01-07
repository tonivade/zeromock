/*
 * Copyright (c) 2018-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;

import com.github.tonivade.purefun.Operator1;

public interface PostFilter extends Operator1<HttpResponse> {
  
  static PostFilter print(PrintStream output) {
    return print(new PrintWriter(new OutputStreamWriter(output, UTF_8), true));
  }

  static PostFilter print(PrintWriter output) {
    return response -> {
      output.println(new Date());
      output.println(response.status());
      response.headers().forEach((name, value) -> output.println(name + ":" + value));
      output.println();
      output.println(Bytes.asString(response.body()));
      return response;
    };
  }
}
