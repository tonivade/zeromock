# ZeroMock

Http Mock Server with (mostly) zero dependencies.

Right now the unique dependency is gson (json object serialization), and gson doesn't have any additional dependency.

[![Build Status](https://api.travis-ci.org/tonivade/zeromock.svg?branch=master)](https://travis-ci.org/tonivade/zeromock)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a3718bd59d674b8592065ac84abdf82c)](https://www.codacy.com/app/tonivade/zeromock?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=tonivade/zeromock&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/a3718bd59d674b8592065ac84abdf82c)](https://www.codacy.com/app/tonivade/zeromock?utm_source=github.com&utm_medium=referral&utm_content=tonivade/zeromock&utm_campaign=Badge_Coverage)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.tonivade/zeromock-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.tonivade/zeromock-core)

## Why?

Usually, the sorter answer should be 'because I can', but I want to explain a bit why I developed this library, and the principal reason is frustration.

  - Frustration 1: Existing alternatives (like Wiremock, and others) are, in my opinion, too complex and dificult to read. In my daily work if I have to read an existing test that needs many stubs to work, is too difficult to understand the purpose of the test.
  
  - Frustration 2: Why I need different libraries to implement a REST API and stubs for this API, in the end, you are doing the same, but instead to compute a result, you are returning a mock result. But finally, both are the same thing.
  
  - Frustration 3: I have another project, called [resp-server](https://github.com/tonivade/resp-server), is an implementation of REdis Serialization Protocol, and I thought that it would be nice to use this protocol to develop REST API applications. I tried to call spring-mvc controller using RESP protocol but the result was an ugly hack. I tried the same using spring-webflux, but the result was the same. So, why I cannot decouple HTTP protocol from the controller implementation?

So, these are the objetives of this library:
  - provide an independent/decoupled/reusable implementation of REST API controllers.
  - provide a fluent API to make easy to read and maintain applications/tests.
  - keep the library small with the minimum of dependencies.

## How?

First of all, I implemented the model, `HttpRequest` and `HttpResponse`, immutable objects, and then I implemented the functions to control the model transformations to obtain a response from a request. In the end the controller is a simple `Function<HttpRequest, HttpResponse>`.

Then, I created the server based in the `HttpServer` included inside the JRE. This server is under a `com.sun` package, so, it's not recomended to use, but for my test purposes it's more than enough.

Also I implemented a simple `HttpClient` using `HttpURLConnection`, initially only for test purposes.

And finally, I implemented a Junit5 extension for Junit integration.

## Examples

This is the simplest example, a ping application. It uses the Junit5 extension. The test receives an instance of the server. Then you can create the stubs you need.

```java
  @Test
  public void ping(MockHttpServer server) {
    server.when(Matchers.get("/ping")).then(Handlers.ok("pong"));
    
    HttpResponse response = HttpClient.connectTo(BASE_URL).request(Requests.get("/ping"));
    
    assertEquals("pong", Bytes.asString(response.body()));
  }
```

`MockHttpServer` is the principal class, it listen for requests, finds for a handler, and then executes it. If no handler is found, a `NOT_FOUND(404)` is returned.

The class `Matchers` contains predicates of type `Predicate<HttpRequest>`, with these predicates you can create the matcher function. These functions can be composes using the existing `Predicate` combinators like `and`, `or` and `negate`. In this case, it defines a predicate that matches a `GET` command with `/ping` path.

The class `Handlers` contains functions of type `Function<HttpRequest, HttpResponse>` to represent the response. Also you can combine different functions using `Function` combinators like `andThen` or `compose`. In this case, it defines a `OK(200)` response with `pong` content.

The class `Bytes` is the wrapper class I use for request and response bodies. It contains a `ByteBuffer` with the raw data. Also this class defines utillity methods to create and convert `Bytes`. In this case, it converts the response body to a `String`.

Another example of echo:

```java
  @Test
  public void echoQueryParam(MockHttpServer server) {
    server.when(Matchers.get("/echo").and(Matchers.param("say")))
          .then(Handlers.ok(Extractors.queryParam("say").andThen(Serializers.plain())));
    
    HttpResponse response = HttpClient.connectTo(BASE_URL)
        .request(Requests.get("/echo").withParam("say", "Hello World!"));
    
    assertEquals("Hello World!", Bytes.asString(response.body()));
  }
```

The class `Extractors` contains functions of type `Function<HttpRequest, T>` to extract data from an `HttpRequest`. In this case extracts the content of the query param `say`.

The class `Serializers` contains functions of type `Function<T, Bytes>` to convert results to `Bytes`. In this case, it uses a plain text serializer.

The same example using path params:

```java
  @Test
  public void echoPathParam(MockHttpServer server) {
    server.when(Matchers.get("/echo/:message")) 
          .then(Handlers.ok(Extractors.pathParam(1).andThen(Serializers.plain())));
    
    HttpResponse response = HttpClient.connectTo(BASE_URL).request(Requests.get("/echo/saysomething"));
    
    assertEquals("saysomething", Bytes.asString(response.body()));
  }
```

To declare a path param, you have to use the colon prefix `:` before the name of the param. To access the value you have to use the position of the parameter in the path.

And the final example, by now, is an implementation of the echo server using json serialization

```java
  @Test
  public void pojoSerialization(MockHttpServer server) {
    server.when(Matchers.get("/echo").and(Matchers.param("say"))) 
          .then(Handlers.ok(Extractors.queryParam("say").andThen(Say::new).andThen(Serializers.json())));
    
    HttpResponse response = HttpClient.connectTo(BASE_URL)
        .request(Requests.get("/echo").withParam("say", "Hello World!"));
    
    assertEquals(new Say("Hello World!"), asObject(response.body()));
  }
  
  private Say asObject(Bytes body) {
    return Deserializers.json(Say.class).apply(body);
  }
```

In this example, a class `Say` is created with the `say` param content, and finally it converts the pojo using json.

In the other side, the client can use `Deserializers` class in order to create a Say class again. This class contains functions of type `Function<Bytes, T>` that converts `Bytes` to arbitrary objects.

Of course, you can use static imports for a clearer code if you want, I have added the full names only for explanatory purposes. All examples taken from this [test class](https://github.com/tonivade/zeromock/blob/master/test/junit5/src/test/java/com/github/tonivade/zeromock/junit5/ExamplesTest.java)

## License

This project is released under MIT License
