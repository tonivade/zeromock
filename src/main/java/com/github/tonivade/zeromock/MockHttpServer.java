package com.github.tonivade.zeromock;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class MockHttpServer {
  private final HttpServer server;

  private Map<String, Request> requests = new HashMap<>();
  private Map<String, RequestHandler> mappings = new HashMap<>();
  
  private static final RequestHandler NOT_FOUND = request -> new Response(404, "not found");

  public MockHttpServer(int port) {
    try {
      server = HttpServer.create(new InetSocketAddress(port), 0);
      server.createContext("/", this::handle);
    } catch (IOException e) {
      throw new UncheckedIOException("unable to start server at port " + port, e);
    }
  }
  
  public MockHttpServer when(String request, RequestHandler handler) {
    mappings.put(request, handler);
    return this;
  }

  public void start() {
    server.start();
  }

  public void stop() {
    server.stop(0);
  }

  private void handle(HttpExchange exchange) throws IOException {
    Request request = new Request(exchange.getRequestURI().toString(), 
                                  readBody(exchange.getRequestBody()));
    Response response = mappings.getOrDefault(request.url, NOT_FOUND).execute(request);
    processResponse(exchange, response);
  }

  private Request getRequest(String url) {
    return requests.get(url);
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

  @FunctionalInterface
  public static interface RequestHandler {
    Response execute(Request request);
  }
  
  public static final class Request {
    final String url;
    final String body;

    public Request(String url, String body) {
      this.url = url;
      this.body = body;
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
}
