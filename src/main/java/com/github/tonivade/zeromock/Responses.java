package com.github.tonivade.zeromock;

import java.util.function.Function;

import com.github.tonivade.zeromock.MockHttpServer.Request;
import com.github.tonivade.zeromock.MockHttpServer.Response;

public class Responses {
  public static Function<Request, Response> ok(String body) {
    return request -> new Response(200, body);
  }

  public static Function<Request, Response> notFound(String body) {
    return request -> new Response(404, body);
  }

  public static Function<Request, Response> error(String body) {
    return request -> new Response(500, body);
  }
}
