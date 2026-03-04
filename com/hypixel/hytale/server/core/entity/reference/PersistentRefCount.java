package com.hypixel.hytale.server.core.entity.reference;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PersistentRefCount implements Component<EntityStore> {
   public static final BuilderCodec<PersistentRefCount> CODEC = BuilderCodec.builder(PersistentRefCount.class, PersistentRefCount::new)
      .append(new KeyedCodec<>("Count", Codec.INTEGER), (instance, value) -> instance.refCount = value, instance -> instance.refCount)
      .add()
      .build();
   private int refCount;

   public static ComponentType<EntityStore, PersistentRefCount> getComponentType() {
      return EntityModule.get().getPersistentRefCountComponentType();
   }

   public int get() {
      return this.refCount;
   }

   public void increment() {
      if (this.refCount >= Integer.MAX_VALUE) {
         this.refCount = 0;
      } else {
         this.refCount++;
      }
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      PersistentRefCount ref = new PersistentRefCount();
      ref.refCount = this.refCount;
      return ref;
   }
}
