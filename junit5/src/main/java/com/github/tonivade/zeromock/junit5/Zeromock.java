/*
 * Copyright (c) 2018-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import com.github.tonivade.zeromock.server.HttpServer;
import com.github.tonivade.zeromock.server.MockHttpServer;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Annotation to specify the port and type of server for testing.
 * This annotation is used with JUnit 5 extensions to set up a mock HTTP server
 * that listens on a specified port.
 *
 * <p>By default, it uses {@link MockHttpServer} and listens on port 0,
 * which means the server will choose a random available port.
 *
 * <p>To use this annotation, apply it to a test class and specify the desired port
 * and server type if needed.
 *
 * <p>This is a meta-annotation that extends the {@link MockHttpServerExtension},
 *
 * <p>Example usage:
 * <pre>
 * {@code @Zeromock(port = 8080, type = CustomHttpServer.class)}
 * public class MyTest {
 *   // Test methods here
 * }
 * </pre>
 *
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@ExtendWith(MockHttpServerExtension.class)
public @interface Zeromock {

  /**
   * The port where the server will listen.
   * If the value is 0, the server will choose a random port.
   *
   * @return the port number
   */
  int port() default 0;

  /**
   * The type of the server to be used.
   * By default, it uses {@link MockHttpServer}.
   *
   * @return the server type
   */
  Class<? extends HttpServer> type() default MockHttpServer.class;

}
