package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.IOUtils.readAll;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableMap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class MockHttpServer {
  private final HttpServer server;

  private Map<String, Request> requests = new HashMap<>();
  private Map<Predicate<Request>, Function<Request, Response>> mappings = new HashMap<>();
  
  private MockHttpServer(int port) {
    try {
      server = HttpServer.create(new InetSocketAddress(port), 0);
      server.createContext("/", this::handle);
    } catch (IOException e) {
      throw new UncheckedIOException("unable to start server at port " + port, e);
    }
  }
  
  public MockHttpServer when(Predicate<Request> matcher, Function<Request, Response> handler) {
    mappings.put(matcher, handler);
    return this;
  }

  public void start() {
    server.start();
  }

  public void stop() {
    server.stop(0);
  }

  public Request getRequest(String url) {
    return requests.get(url);
  }

  private void handle(HttpExchange exchange) throws IOException {
    Request request = createRequest(exchange);
    Function<Request, Response> handler = findHandler(request);
    processResponse(exchange, handler.apply(request));
  }

  private Request createRequest(HttpExchange exchange) throws IOException {
    return new Request(exchange.getRequestMethod(),
                       exchange.getRequestURI().getPath(), 
                       readAll(exchange.getRequestBody()),
                       exchange.getRequestHeaders(),
                       queryToMap(exchange.getRequestURI().getQuery()));
  }
  
  private Map<String, String> queryToMap(String query) {
    Map<String, String> result = new HashMap<>();
    if (query != null) {
      for (String param : query.split("&")) {
        String[] pair = param.split("=");
        if (pair.length > 1) {
          result.put(pair[0], pair[1]);
        } else {
          result.put(pair[0], "");
        }
      }
    }
    return result;
  }

  private Function<Request, Response> findHandler(Request request) {
    return mappings.entrySet().stream()
        .filter(entry -> entry.getKey().test(request))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElse(Responses.notFound("not found"));
  }

  private void processResponse(HttpExchange exchange, Response response) throws IOException {
    byte[] bytes = response.getBytes();
    response.headers.forEach((key, values) -> values.forEach(value -> exchange.getResponseHeaders().add(key, value)));
    exchange.sendResponseHeaders(response.statusCode, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.getResponseBody().close();
  }
  
  public static final class Request {
    final String method;
    final String url;
    final String body;
    final Map<String, List<String>> headers;
    final Map<String, String> params;

    public Request(String method, String url, String body, Map<String, List<String>> headers, Map<String, String> params) {
      this.method = method;
      this.url = url;
      this.body = body;
      this.headers = unmodifiableMap(headers);
      this.params = unmodifiableMap(params);
    }
  }
  
  public static final class Response {
    final int statusCode;
    final String body;
    final Map<String, List<String>> headers;
    
    public Response(int statusCode, String body, Map<String, List<String>> headers) {
      this.statusCode = statusCode;
      this.body = body;
      this.headers = unmodifiableMap(headers);
    }
    
    public byte[] getBytes() {
      return body.getBytes();
    }

    public Response withHeader(String string, String value) {
      Map<String, List<String>> newHeaders = new HashMap<>(headers);
      newHeaders.merge(string, singletonList(value), (oldValue, newValue) -> {
        List<String> newList = new ArrayList<>(oldValue);
        newList.addAll(newValue);
        return newList;
      });
      return new Response(statusCode, body, newHeaders);
    }
  }
  
  public static MockHttpServer listenAt(int port) {
    return new MockHttpServer(port);
  }
}
