/*
 * Copyright (c) 2018-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to specify the port where the mock HTTP server will listen.
 * This annotation should be applied to a test class.
 * The server will start before all tests and will listen on the specified port.
 *
 * @deprecated use {@link Zeromock} instead
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Deprecated(forRemoval = true)
public @interface ListenAt {

  /**
   * The port where the server will listen.
   *
   * @return the port number
   */
  int value();
}
