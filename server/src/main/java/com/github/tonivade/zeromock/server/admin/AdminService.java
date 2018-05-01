/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server.admin;

import static java.util.Collections.unmodifiableList;

import java.util.LinkedList;
import java.util.List;

import com.github.tonivade.zeromock.core.HttpRequest;

public class AdminService {

  private final List<HttpRequest> matched = new LinkedList<>();
  private final List<HttpRequest> unmatched = new LinkedList<>();

  public void addMatched(HttpRequest request) {
    matched.add(request);
  }
  
  public List<HttpRequest> getMatched() {
    return unmodifiableList(matched);
  }

  public void addUnmatched(HttpRequest request) {
    unmatched.add(request);
  }
  
  public List<HttpRequest> getUnmatched() {
    return unmodifiableList(unmatched);
  }

  public void clear() {
    matched.clear();
    unmatched.clear();
  }
}
