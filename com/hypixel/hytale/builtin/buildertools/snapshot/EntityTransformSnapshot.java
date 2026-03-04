package com.hypixel.hytale.builtin.buildertools.snapshot;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class EntityTransformSnapshot implements EntitySnapshot<EntityTransformSnapshot> {
   @Nonnull
   private final Ref<EntityStore> ref;
   @Nonnull
   private final Transform transform;
   @Nonnull
   private final Vector3f headRotation;

   public EntityTransformSnapshot(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.ref = ref;
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      HeadRotation headRotationComponent = componentAccessor.getComponent(ref, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      this.transform = transformComponent.getTransform().clone();
      this.headRotation = headRotationComponent.getRotation().clone();
   }

   public EntityTransformSnapshot restoreEntity(@Nonnull Player player, @Nonnull World world, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (!this.ref.isValid()) {
         return null;
      } else {
         TransformComponent transformComponent = componentAccessor.getComponent(this.ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         transformComponent.setPosition(this.transform.getPosition());
         transformComponent.setRotation(this.transform.getRotation());
         HeadRotation headRotationComponent = componentAccessor.getComponent(this.ref, HeadRotation.getComponentType());
         if (headRotationComponent != null) {
            headRotationComponent.setRotation(this.headRotation);
         }

         return new EntityTransformSnapshot(this.ref, componentAccessor);
      }
   }
}
