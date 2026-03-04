package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class FromWorldGen implements Component<EntityStore> {
   public static final BuilderCodec<FromWorldGen> CODEC = BuilderCodec.builder(FromWorldGen.class, FromWorldGen::new)
      .append(new KeyedCodec<>("WorldGenId", Codec.INTEGER), (fromWorldGen, i) -> fromWorldGen.worldGenId = i, fromWorldGen -> fromWorldGen.worldGenId)
      .add()
      .build();
   private int worldGenId;

   public static ComponentType<EntityStore, FromWorldGen> getComponentType() {
      return EntityModule.get().getFromWorldGenComponentType();
   }

   private FromWorldGen() {
   }

   public FromWorldGen(int worldGenId) {
      this.worldGenId = worldGenId;
   }

   public int getWorldGenId() {
      return this.worldGenId;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new FromWorldGen(this.worldGenId);
   }
}
