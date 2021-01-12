/*
 * Copyright (c) 2018-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Witness;

public interface PostFilterK<F extends Witness> extends Function1<HttpResponse, Kind<F, HttpResponse>> { }
