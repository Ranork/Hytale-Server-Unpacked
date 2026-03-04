package com.hypixel.hytale.server.core.modules.physics.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.modules.entity.AllLegacyEntityTypesQuery;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.system.ModelSystems;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import javax.annotation.Nonnull;

public class PhysicsValuesAddSystem extends HolderSystem<EntityStore> {
   private final ComponentType<EntityStore, PhysicsValues> physicsValuesComponentType;
   @Nonnull
   private final Query<EntityStore> query;
   private final Set<Dependency<EntityStore>> dependencies = Set.of(
      new SystemDependency<>(Order.AFTER, ModelSystems.SetRenderedModel.class), new SystemDependency<>(Order.AFTER, ModelSystems.PlayerConnect.class)
   );

   public PhysicsValuesAddSystem(ComponentType<EntityStore, PhysicsValues> physicsValuesComponentType) {
      this.physicsValuesComponentType = physicsValuesComponentType;
      this.query = Query.and(AllLegacyEntityTypesQuery.INSTANCE, Query.not(physicsValuesComponentType));
   }

   @Nonnull
   @Override
   public Set<Dependency<EntityStore>> getDependencies() {
      return this.dependencies;
   }

   @Override
   public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
      PhysicsValues physicsValues = holder.ensureAndGetComponent(this.physicsValuesComponentType);
      ModelComponent modelComponent = holder.getComponent(ModelComponent.getComponentType());
      if (modelComponent != null) {
         physicsValues.replaceValues(modelComponent.getModel().getPhysicsValues());
      }
   }

   @Override
   public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.query;
   }
}
