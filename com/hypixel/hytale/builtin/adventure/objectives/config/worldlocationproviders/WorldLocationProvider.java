package com.hypixel.hytale.builtin.adventure.objectives.config.worldlocationproviders;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class WorldLocationProvider {
   @Nonnull
   public static final CodecMapCodec<WorldLocationProvider> CODEC = new CodecMapCodec<>("Type");
   @Nonnull
   public static final BuilderCodec<WorldLocationProvider> BASE_CODEC = BuilderCodec.abstractBuilder(WorldLocationProvider.class).build();

   @Nullable
   public abstract Vector3i runCondition(@Nonnull World var1, @Nonnull Vector3i var2);

   @Override
   public abstract boolean equals(Object var1);

   @Override
   public abstract int hashCode();

   @Nonnull
   @Override
   public String toString() {
      return "WorldLocationProvider{}";
   }

   static {
      CODEC.register("LookBlocksBelow", LookBlocksBelowProvider.class, LookBlocksBelowProvider.CODEC);
      CODEC.register("LocationRadius", LocationRadiusProvider.class, LocationRadiusProvider.CODEC);
      CODEC.register("TagBlockHeight", CheckTagWorldHeightRadiusProvider.class, CheckTagWorldHeightRadiusProvider.CODEC);
   }
}
