package com.hypixel.hytale.server.core.modules.entity.repulsion;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class Repulsion implements Component<EntityStore> {
   public static final BuilderCodec<Repulsion> CODEC = BuilderCodec.builder(Repulsion.class, Repulsion::new)
      .append(
         new KeyedCodec<>("RepulsionConfigIndex", Codec.INTEGER),
         (hitboxCollision, integer) -> hitboxCollision.repulsionConfigIndex = integer,
         hitboxCollision -> hitboxCollision.repulsionConfigIndex
      )
      .add()
      .build();
   protected AssetExtraInfo.Data data;
   private int repulsionConfigIndex;
   private boolean isNetworkOutdated = true;

   public static ComponentType<EntityStore, Repulsion> getComponentType() {
      return EntityModule.get().getRepulsionComponentType();
   }

   public Repulsion(@Nonnull RepulsionConfig repulsionConfig) {
      this.repulsionConfigIndex = RepulsionConfig.getAssetMap().getIndexOrDefault(repulsionConfig.getId(), -1);
   }

   protected Repulsion() {
   }

   public int getRepulsionConfigIndex() {
      return this.repulsionConfigIndex;
   }

   public void setRepulsionConfigIndex(int repulsionConfigIndex) {
      this.repulsionConfigIndex = repulsionConfigIndex;
   }

   public boolean consumeNetworkOutdated() {
      boolean temp = this.isNetworkOutdated;
      this.isNetworkOutdated = false;
      return temp;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      Repulsion component = new Repulsion();
      component.repulsionConfigIndex = this.repulsionConfigIndex;
      return component;
   }
}
