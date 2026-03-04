package com.hypixel.hytale.server.core.asset.type.attitude;

import com.hypixel.hytale.codec.codecs.EnumCodec;
import java.util.function.Supplier;

public enum Attitude implements Supplier<String> {
   IGNORE("is ignoring the target"),
   HOSTILE("is hostile towards the target"),
   NEUTRAL("is neutral towards the target"),
   FRIENDLY("is friendly towards the target"),
   REVERED("reveres the target");

   public static final EnumCodec<Attitude> CODEC = new EnumCodec<>(Attitude.class);
   public static final Attitude[] VALUES = values();
   private final String description;

   private Attitude(String description) {
      this.description = description;
   }

   public String get() {
      return this.description;
   }
}
