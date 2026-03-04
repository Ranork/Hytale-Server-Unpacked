package com.hypixel.hytale.server.core.modules.entity.hitboxcollision;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class HitboxCollision implements Component<EntityStore> {
   public static final BuilderCodec<HitboxCollision> CODEC = BuilderCodec.builder(HitboxCollision.class, HitboxCollision::new)
      .append(
         new KeyedCodec<>("HitboxCollisionConfigIndex", Codec.INTEGER),
         (hitboxCollision, integer) -> hitboxCollision.hitboxCollisionConfigIndex = integer,
         hitboxCollision -> hitboxCollision.hitboxCollisionConfigIndex
      )
      .add()
      .build();
   private int hitboxCollisionConfigIndex;
   private boolean isNetworkOutdated = true;

   public static ComponentType<EntityStore, HitboxCollision> getComponentType() {
      return EntityModule.get().getHitboxCollisionComponentType();
   }

   public HitboxCollision(@Nonnull HitboxCollisionConfig hitboxCollisionConfig) {
      this.hitboxCollisionConfigIndex = HitboxCollisionConfig.getAssetMap().getIndexOrDefault(hitboxCollisionConfig.getId(), -1);
   }

   protected HitboxCollision() {
   }

   public int getHitboxCollisionConfigIndex() {
      return this.hitboxCollisionConfigIndex;
   }

   public void setHitboxCollisionConfigIndex(int hitboxCollisionConfigIndex) {
      this.hitboxCollisionConfigIndex = hitboxCollisionConfigIndex;
   }

   public boolean consumeNetworkOutdated() {
      boolean temp = this.isNetworkOutdated;
      this.isNetworkOutdated = false;
      return temp;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      HitboxCollision component = new HitboxCollision();
      component.hitboxCollisionConfigIndex = this.hitboxCollisionConfigIndex;
      return component;
   }
}
