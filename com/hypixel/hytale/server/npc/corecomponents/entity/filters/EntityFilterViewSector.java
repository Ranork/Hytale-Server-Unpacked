package com.hypixel.hytale.server.npc.corecomponents.entity.filters;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterViewSector;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import javax.annotation.Nonnull;

public class EntityFilterViewSector extends EntityFilterBase {
   public static final int COST = 300;
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   protected final float viewCone;

   public EntityFilterViewSector(@Nonnull BuilderEntityFilterViewSector builder, @Nonnull BuilderSupport support) {
      this.viewCone = builder.getViewSectorRadians(support);
   }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      TransformComponent transformComponent = store.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      TransformComponent targetTransformComponent = store.getComponent(targetRef, TRANSFORM_COMPONENT_TYPE);

      assert targetTransformComponent != null;

      Vector3d targetPosition = targetTransformComponent.getPosition();
      return NPCPhysicsMath.inViewSector(
         position.getX(), position.getZ(), headRotationComponent.getRotation().getYaw(), this.viewCone, targetPosition.getX(), targetPosition.getZ()
      );
   }

   @Override
   public int cost() {
      return 300;
   }

   public float getViewAngle() {
      return this.viewCone;
   }
}
