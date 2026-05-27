package com.hypixel.hytale.server.core.telemetry;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.annotation.Nonnull;

final class TelemetryJsonSerializer {
   private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

   private TelemetryJsonSerializer() {
   }

   @Nonnull
   static String serialize(@Nonnull Object obj) {
      return GSON.toJson(obj);
   }
}
