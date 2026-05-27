package com.hypixel.hytale.builtin.buildertools.snapshot;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class EntityScaleSnapshot implements EntitySnapshot<EntityScaleSnapshot> {
   @Nonnull
   private Ref<EntityStore> ref;
   private final float scale;

   public EntityScaleSnapshot(@Nonnull Ref<EntityStore> ref, float scale) {
      this.ref = ref;
      this.scale = scale;
   }

   public EntityScaleSnapshot(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.ref = ref;
      EntityScaleComponent scaleComponent = componentAccessor.getComponent(ref, EntityScaleComponent.getComponentType());
      this.scale = scaleComponent != null ? scaleComponent.getScale() : 1.0F;
   }

   @Override
   public void updateEntityRef(@Nonnull Ref<EntityStore> oldRef, @Nonnull Ref<EntityStore> newRef) {
      if (this.ref == oldRef) {
         this.ref = newRef;
      }
   }

   @Nonnull
   public Ref<EntityStore> getRef() {
      return this.ref;
   }

   public EntityScaleSnapshot restoreEntity(@Nonnull PlayerRef playerRef, @Nonnull World world, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (!this.ref.isValid()) {
         return null;
      } else {
         EntityScaleComponent scaleComponent = componentAccessor.getComponent(this.ref, EntityScaleComponent.getComponentType());
         float previousScale = scaleComponent != null ? scaleComponent.getScale() : 1.0F;
         if (scaleComponent == null) {
            scaleComponent = new EntityScaleComponent(this.scale);
            componentAccessor.addComponent(this.ref, EntityScaleComponent.getComponentType(), scaleComponent);
         } else {
            scaleComponent.setScale(this.scale);
         }

         return new EntityScaleSnapshot(this.ref, previousScale);
      }
   }
}
