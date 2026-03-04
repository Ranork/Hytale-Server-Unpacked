package com.hypixel.hytale.builtin.adventure.farming.config.stages.spread;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.worldlocationcondition.WorldLocationCondition;
import javax.annotation.Nonnull;

public abstract class SpreadGrowthBehaviour {
   @Nonnull
   public static final CodecMapCodec<SpreadGrowthBehaviour> CODEC = new CodecMapCodec<>("Type");
   @Nonnull
   public static final BuilderCodec<SpreadGrowthBehaviour> BASE_CODEC = BuilderCodec.abstractBuilder(SpreadGrowthBehaviour.class)
      .append(
         new KeyedCodec<>("LocationConditions", new ArrayCodec<>(WorldLocationCondition.CODEC, WorldLocationCondition[]::new)),
         (spreadGrowthBehaviour, worldLocationConditions) -> spreadGrowthBehaviour.worldLocationConditions = worldLocationConditions,
         spreadGrowthBehaviour -> spreadGrowthBehaviour.worldLocationConditions
      )
      .documentation("Defines the possible location conditions a position has to fulfill to be considered as valid.")
      .add()
      .build();
   protected WorldLocationCondition[] worldLocationConditions;

   public abstract void execute(
      @Nonnull ComponentAccessor<ChunkStore> var1, @Nonnull Ref<ChunkStore> var2, @Nonnull Ref<ChunkStore> var3, int var4, int var5, int var6, float var7
   );

   protected boolean validatePosition(@Nonnull World world, int worldX, int worldY, int worldZ) {
      if (this.worldLocationConditions == null) {
         return true;
      } else {
         for (int i = 0; i < this.worldLocationConditions.length; i++) {
            if (!this.worldLocationConditions[i].test(world, worldX, worldY, worldZ)) {
               return false;
            }
         }

         return true;
      }
   }
}
