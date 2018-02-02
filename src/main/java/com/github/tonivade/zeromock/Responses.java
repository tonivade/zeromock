package com.github.tonivade.zeromock;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;

import java.util.Map;
import java.util.function.Function;

import com.github.tonivade.zeromock.MockHttpServer.Request;
import com.github.tonivade.zeromock.MockHttpServer.Response;

public class Responses {

  public static Function<Request, Response> ok(String body) {
    return request -> new Response(200, format(body, collectParams(request)), emptyMap());
  }
  
  public static Function<Request, Response> crated(String body) {
    return request -> new Response(201, body, emptyMap());
  }
  
  public static Function<Request, Response> noContent() {
    return request -> new Response(204, null, emptyMap());
  }

  public static Function<Request, Response> badRequest(String body) {
    return request -> new Response(400, body, emptyMap());
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
  
  public static Function<Response, Response> contentJson() {
    return contentType("application/json");
  }
  
  public static Function<Response, Response> contentXml() {
    return contentType("text/xml");
  }

  private static Object[] collectParams(Request request) {
    return request.params.entrySet().stream().map(Map.Entry::getValue).toArray();
  }
}
