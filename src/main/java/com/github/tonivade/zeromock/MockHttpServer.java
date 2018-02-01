package com.github.tonivade.zeromock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Collections;
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
                       exchange.getRequestURI().toString(), 
                       readBody(exchange.getRequestBody()),
                       exchange.getRequestHeaders());
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
    exchange.sendResponseHeaders(response.statusCode, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.getResponseBody().close();
  }

  private String readBody(InputStream body) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    while (true) {
      int read = body.read(buffer);
      if (read > 0) {
        out.write(buffer, 0, read);
      } else break;
    }
    return new String(out.toByteArray(), Charset.forName("UTF-8"));
  }
  
  public static final class Request {
    final String method;
    final String url;
    final String body;
    final Map<String, List<String>> headers;

    public Request(String method, String url, String body, Map<String, List<String>> headers) {
      this.method = method;
      this.url = url;
      this.body = body;
      this.headers = Collections.unmodifiableMap(headers);
    }
  }
  
  public static final class Response {
    final int statusCode;
    final String body;
    
    public Response(int statusCode, String body) {
      this.statusCode = statusCode;
      this.body = body;
    }
    
    public byte[] getBytes() {
      return body.getBytes();
    }
  }
  
  public static MockHttpServer listenAt(int port) {
    return new MockHttpServer(port);
  }
}
