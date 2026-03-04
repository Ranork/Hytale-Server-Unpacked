package com.hypixel.hytale.server.npc.corecomponents.entity.filters;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterSpotsMe;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import com.hypixel.hytale.server.npc.util.ViewTest;
import javax.annotation.Nonnull;

public class EntityFilterSpotsMe extends EntityFilterBase {
   public static final int COST = 400;
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   protected final float viewAngle;
   protected final boolean testLineOfSight;
   protected final ViewTest viewTest;
   protected final Vector3d view = new Vector3d();

   public EntityFilterSpotsMe(@Nonnull BuilderEntityFilterSpotsMe builder) {
      this.viewAngle = builder.getViewAngle();
      this.testLineOfSight = builder.testLineOfSight();
      this.viewTest = builder.getViewTest();
   }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      return this.inViewTest(targetRef, ref, store) && (!this.testLineOfSight || role.getPositionCache().hasInverseLineOfSight(ref, targetRef, store));
   }

   @Override
   public int cost() {
      return 400;
   }

   protected boolean inViewTest(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Store<EntityStore> store) {
      return switch (this.viewTest) {
         case VIEW_CONE -> this.inViewCone(ref, targetRef, store);
         case VIEW_SECTOR -> this.inViewSector(ref, targetRef, store);
         default -> false;
      };
   }

   protected boolean inViewSector(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Store<EntityStore> store) {
      TransformComponent transformComponent = store.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      TransformComponent targetTransformComponent = store.getComponent(targetRef, TRANSFORM_COMPONENT_TYPE);

      assert targetTransformComponent != null;

      Vector3d targetPosition = targetTransformComponent.getPosition();
      HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      return NPCPhysicsMath.inViewSector(
         position.getX(), position.getZ(), headRotationComponent.getRotation().getYaw(), this.viewAngle, targetPosition.getX(), targetPosition.getZ()
      );
   }

   protected boolean inViewCone(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Store<EntityStore> store) {
      TransformComponent transformComponent = store.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

      assert transformComponent != null;

      HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      TransformComponent targetTransformComponent = store.getComponent(targetRef, TRANSFORM_COMPONENT_TYPE);

      assert targetTransformComponent != null;

      NPCPhysicsMath.getViewDirection(headRotationComponent.getRotation(), this.view);
      this.view.normalize();
      return NPCPhysicsMath.isInViewCone(
         transformComponent.getPosition(), this.view, TrigMathUtil.cos(this.viewAngle / 2.0F), targetTransformComponent.getPosition()
      );
   }
}
