package com.github.tonivade.zeromock;

import static java.util.Collections.emptyMap;

import java.util.function.Function;

import com.github.tonivade.zeromock.MockHttpServer.Request;
import com.github.tonivade.zeromock.MockHttpServer.Response;

public class Responses {
  public static Function<Request, Response> ok(String body) {
    return request -> new Response(200, body, emptyMap());
  }

  public static Function<Request, Response> notFound(String body) {
    return request -> new Response(404, body, emptyMap());
  }

  public static Function<Request, Response> error(String body) {
    return request -> new Response(500, body, emptyMap());
  }
  
  public static Function<Response, Response> contentType(String value) {
    return response -> response.withHeader("Content-Type", value);
  }
}
