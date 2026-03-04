package com.hypixel.hytale.server.npc.corecomponents.entity.filters;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterHeightDifference;
import com.hypixel.hytale.server.npc.role.Role;
import javax.annotation.Nonnull;

public class EntityFilterHeightDifference extends EntityFilterBase {
   public static final int COST = 200;
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   protected static final ComponentType<EntityStore, ModelComponent> MODEL_COMPONENT_TYPE = ModelComponent.getComponentType();
   protected static final ComponentType<EntityStore, BoundingBox> BOUNDING_BOX_COMPONENT_TYPE = BoundingBox.getComponentType();
   protected final double minHeightDifference;
   protected final double maxHeightDifference;
   protected final boolean useEyePosition;

   public EntityFilterHeightDifference(@Nonnull BuilderEntityFilterHeightDifference builder, @Nonnull BuilderSupport support) {
      double[] heightDifference = builder.getHeightDifference(support);
      this.minHeightDifference = heightDifference[0];
      this.maxHeightDifference = heightDifference[1];
      this.useEyePosition = builder.isUseEyePosition(support);
   }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      BoundingBox targetBoundingBoxComponent = store.getComponent(targetRef, BOUNDING_BOX_COMPONENT_TYPE);
      if (targetBoundingBoxComponent == null) {
         return false;
      } else {
         TransformComponent targetTransformComponent = store.getComponent(targetRef, TRANSFORM_COMPONENT_TYPE);

         assert targetTransformComponent != null;

         ModelComponent targetModelComponent = store.getComponent(targetRef, MODEL_COMPONENT_TYPE);
         float targetEyeHeight = targetModelComponent != null ? targetModelComponent.getModel().getEyeHeight() : 0.0F;
         Vector3d targetPosition = targetTransformComponent.getPosition();
         double targetY = targetPosition.y;
         Box box = targetBoundingBoxComponent.getBoundingBox();
         double minY = targetY + box.min.y;
         double maxY = targetY + box.max.y;
         TransformComponent transformComponent = store.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

         assert transformComponent != null;

         double posY = transformComponent.getPosition().y;
         if (this.useEyePosition) {
            posY += targetEyeHeight;
         }

         return minY - posY < this.maxHeightDifference && maxY - posY > this.minHeightDifference;
      }
   }

   @Override
   public int cost() {
      return 200;
   }
}
