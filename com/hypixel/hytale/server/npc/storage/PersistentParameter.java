package com.hypixel.hytale.server.npc.storage;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class PersistentParameter<Type> {
   protected PersistentParameter() {
   }

   public void set(@Nonnull Ref<EntityStore> ownerRef, @Nullable Type value, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.set0(value);
      TransformComponent transformComponent = componentAccessor.getComponent(ownerRef, TransformComponent.getComponentType());
      if (transformComponent != null) {
         transformComponent.markChunkDirty(componentAccessor);
      }
   }

   protected abstract void set0(@Nullable Type var1);
}
