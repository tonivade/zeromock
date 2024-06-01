/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.core.Function1;
import com.github.tonivade.purefun.Kind;

public interface PostFilterK<F extends Kind<F, ?>> extends Function1<HttpResponse, Kind<F, HttpResponse>> { }
