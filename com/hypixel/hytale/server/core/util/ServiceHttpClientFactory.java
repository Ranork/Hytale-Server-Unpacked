package com.hypixel.hytale.server.core.util;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.time.Duration;
import java.util.Objects;
import javax.annotation.Nonnull;

public final class ServiceHttpClientFactory {
   private ServiceHttpClientFactory() {
   }

   @Nonnull
   public static Builder newBuilder(@Nonnull Duration connectTimeout) {
      Objects.requireNonNull(connectTimeout, "connectTimeout");
      return HttpClient.newBuilder().connectTimeout(connectTimeout);
   }

   @Nonnull
   public static HttpClient create(@Nonnull Duration connectTimeout) {
      return newBuilder(connectTimeout).build();
   }
}
