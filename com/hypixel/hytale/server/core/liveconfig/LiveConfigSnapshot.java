package com.hypixel.hytale.server.core.liveconfig;

import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class LiveConfigSnapshot {
   public static final LiveConfigSnapshot EMPTY = new LiveConfigSnapshot();
   @Nullable
   private final String version;
   @Nullable
   private final String etag;
   @Nonnull
   private final Map<String, LiveConfigSnapshot.ResolvedFlag> flags;

   private LiveConfigSnapshot() {
      this.version = null;
      this.etag = null;
      this.flags = Collections.emptyMap();
   }

   public LiveConfigSnapshot(@Nonnull String version, @Nullable String etag, @Nonnull Map<String, LiveConfigSnapshot.ResolvedFlag> flags) {
      this.version = version;
      this.etag = etag;
      this.flags = Collections.unmodifiableMap(flags);
   }

   public boolean getBoolFlag(@Nonnull String name, boolean defaultValue) {
      LiveConfigSnapshot.ResolvedFlag flag = this.flags.get(name);
      return flag != null && flag.value() instanceof Boolean b ? b : defaultValue;
   }

   public int getIntFlag(@Nonnull String name, int defaultValue) {
      LiveConfigSnapshot.ResolvedFlag flag = this.flags.get(name);
      return flag != null && flag.value() instanceof Integer i ? i : defaultValue;
   }

   @Nonnull
   public String getStringFlag(@Nonnull String name, @Nonnull String defaultValue) {
      LiveConfigSnapshot.ResolvedFlag flag = this.flags.get(name);
      return flag != null && flag.value() instanceof String s ? s : defaultValue;
   }

   public boolean hasFlag(@Nonnull String name) {
      return this.flags.containsKey(name);
   }

   @Nullable
   public String getVersion() {
      return this.version;
   }

   @Nullable
   public String getEtag() {
      return this.etag;
   }

   public int getFlagCount() {
      return this.flags.size();
   }

   @Nonnull
   public Map<String, LiveConfigSnapshot.ResolvedFlag> getAllFlags() {
      return this.flags;
   }

   public record ResolvedFlag(@Nonnull String type, @Nullable Object value) {
   }
}
