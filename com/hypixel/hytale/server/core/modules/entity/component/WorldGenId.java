package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class WorldGenId implements Component<EntityStore> {
   public static final int NON_WORLD_GEN_ID = 0;
   public static final BuilderCodec<WorldGenId> CODEC = BuilderCodec.builder(WorldGenId.class, WorldGenId::new)
      .append(new KeyedCodec<>("WorldGenId", Codec.INTEGER), (worldGenId, i) -> worldGenId.worldGenId = i, worldGenId -> worldGenId.worldGenId)
      .add()
      .build();
   private int worldGenId;

   public static ComponentType<EntityStore, WorldGenId> getComponentType() {
      return EntityModule.get().getWorldGenIdComponentType();
   }

   public WorldGenId(int worldGenId) {
      this.worldGenId = worldGenId;
   }

   private WorldGenId() {
   }

   public int getWorldGenId() {
      return this.worldGenId;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new WorldGenId(this.worldGenId);
   }
}
