package com.hypixel.hytale.builtin.buildertools.snapshot;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityFreezeSnapshot implements EntitySnapshot<EntityFreezeSnapshot> {
   @Nonnull
   private Ref<EntityStore> ref;
   private final boolean wasFrozen;
   @Nullable
   private final EntityTransformSnapshot transformSnapshot;

   public EntityFreezeSnapshot(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.ref = ref;
      this.wasFrozen = componentAccessor.getArchetype(ref).contains(Frozen.getComponentType());
      this.transformSnapshot = new EntityTransformSnapshot(ref, componentAccessor);
   }

   @Override
   public void updateEntityRef(@Nonnull Ref<EntityStore> oldRef, @Nonnull Ref<EntityStore> newRef) {
      if (this.ref == oldRef) {
         this.ref = newRef;
      }

      if (this.transformSnapshot != null) {
         this.transformSnapshot.updateEntityRef(oldRef, newRef);
      }
   }

   public EntityFreezeSnapshot restoreEntity(@Nonnull PlayerRef playerRef, @Nonnull World world, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (!this.ref.isValid()) {
         return null;
      } else {
         EntityFreezeSnapshot inverse = new EntityFreezeSnapshot(this.ref, componentAccessor);
         if (this.wasFrozen) {
            if (!componentAccessor.getArchetype(this.ref).contains(Frozen.getComponentType())) {
               componentAccessor.addComponent(this.ref, Frozen.getComponentType(), Frozen.get());
               resetToIdleAnimation(this.ref, componentAccessor);
            }

            if (this.transformSnapshot != null) {
               this.transformSnapshot.restoreEntity(playerRef, world, componentAccessor);
            }
         } else {
            componentAccessor.tryRemoveComponent(this.ref, Frozen.getComponentType());
            AnimationUtils.stopAnimation(this.ref, AnimationSlot.Movement, componentAccessor);
         }

         return inverse;
      }
   }

   private static void resetToIdleAnimation(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      ModelComponent modelComponent = componentAccessor.getComponent(ref, ModelComponent.getComponentType());
      if (modelComponent != null && modelComponent.getModel() != null) {
         if (modelComponent.getModel().getAnimationSetMap().containsKey("Idle")) {
            AnimationUtils.playAnimation(ref, AnimationSlot.Movement, "Idle", componentAccessor);
            AnimationUtils.stopAnimation(ref, AnimationSlot.Status, componentAccessor);
            AnimationUtils.stopAnimation(ref, AnimationSlot.Action, componentAccessor);
         }
      }
   }
}
