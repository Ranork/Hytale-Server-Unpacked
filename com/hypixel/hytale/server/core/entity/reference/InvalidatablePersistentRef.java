package com.hypixel.hytale.server.core.entity.reference;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class InvalidatablePersistentRef extends PersistentRef {
   public static final BuilderCodec<InvalidatablePersistentRef> CODEC = BuilderCodec.builder(
         InvalidatablePersistentRef.class, InvalidatablePersistentRef::new, PersistentRef.CODEC
      )
      .append(new KeyedCodec<>("RefCount", Codec.INTEGER), (instance, value) -> instance.refCount = value, instance -> instance.refCount)
      .add()
      .build();
   protected int refCount;

   @Override
   public void setEntity(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      super.setEntity(ref, componentAccessor);
      PersistentRefCount refCount = componentAccessor.getComponent(ref, PersistentRefCount.getComponentType());
      if (refCount == null) {
         refCount = new PersistentRefCount();
         componentAccessor.addComponent(ref, PersistentRefCount.getComponentType(), refCount);
      }

      this.refCount = refCount.get();
   }

   @Override
   public void clear() {
      super.clear();
      this.refCount = -1;
   }

   public void setRefCount(int refCount) {
      this.refCount = refCount;
   }

   public int getRefCount() {
      return this.refCount;
   }

   @Override
   protected boolean validateEntityReference(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      PersistentRefCount refCount = componentAccessor.getComponent(ref, PersistentRefCount.getComponentType());
      return super.validateEntityReference(ref, componentAccessor) && refCount != null && refCount.get() == this.refCount;
   }
}
