package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  private boolean hasProfiledMethod(Class<?> klass) {
    return Arrays.stream(klass.getDeclaredMethods())
            .anyMatch(method -> method.isAnnotationPresent(Profiled.class));
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);
    Objects.requireNonNull(delegate);

    if (!hasProfiledMethod(klass)) {
      throw new IllegalArgumentException(
              klass.getName() + " doesn't have any @Profiled methods.");
    }

    ProfilingMethodInterceptor interceptor =
            new ProfilingMethodInterceptor(clock, delegate, state, startTime);

    Object proxy = Proxy.newProxyInstance(
            klass.getClassLoader(),
            new Class<?>[]{klass},
            interceptor
    );

    return klass.cast(proxy);
  }

  @Override
  public void writeData(Path path) {
    Objects.requireNonNull(path);

    try (Writer writer = Files.newBufferedWriter(
            path,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
    )) {
      writeData(writer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());

    state.write(writer);

    writer.write(System.lineSeparator());
  }
}