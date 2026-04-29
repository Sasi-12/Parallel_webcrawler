package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final Object delegate;
  private final ProfilingState state;
  private final ZonedDateTime startTime;

  ProfilingMethodInterceptor(
          Clock clock,
          Object delegate,
          ProfilingState state,
          ZonedDateTime startTime) {

    this.clock = Objects.requireNonNull(clock);
    this.delegate = Objects.requireNonNull(delegate);
    this.state = Objects.requireNonNull(state);
    this.startTime = Objects.requireNonNull(startTime);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    if (method.getDeclaringClass() == Object.class &&
            method.getName().equals("equals")) {
      return method.invoke(delegate, args);
    }

    boolean profiled = method.isAnnotationPresent(Profiled.class);
    Instant start = null;

    if (profiled) {
      start = clock.instant();
    }

    try {
      Object result = method.invoke(delegate, args);
      return result;

    } catch (InvocationTargetException e) {
      throw e.getCause();

    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);

    } finally {
      if (profiled) {
        Duration duration = Duration.between(start, clock.instant());
        state.record(delegate.getClass(), method, duration);
      }
    }
  }
}