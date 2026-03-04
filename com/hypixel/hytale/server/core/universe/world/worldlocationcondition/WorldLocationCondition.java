package com.hypixel.hytale.server.core.universe.world.worldlocationcondition;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.server.core.universe.world.World;
import javax.annotation.Nonnull;

public abstract class WorldLocationCondition {
   @Nonnull
   public static final CodecMapCodec<WorldLocationCondition> CODEC = new CodecMapCodec<>("Type");
   @Nonnull
   public static final BuilderCodec<WorldLocationCondition> BASE_CODEC = BuilderCodec.abstractBuilder(WorldLocationCondition.class).build();

   public abstract boolean test(@Nonnull World var1, int var2, int var3, int var4);

   @Override
   public abstract boolean equals(Object var1);

   @Override
   public abstract int hashCode();

   @Nonnull
   @Override
   public String toString() {
      return "WorldLocationCondition{}";
   }
}
