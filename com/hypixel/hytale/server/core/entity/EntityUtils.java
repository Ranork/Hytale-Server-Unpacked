package com.hypixel.hytale.server.core.entity;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityUtils {
   @Nonnull
   public static Holder<EntityStore> toHolder(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk) {
      Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
      Archetype<EntityStore> archetype = archetypeChunk.getArchetype();

      for (int i = archetype.getMinIndex(); i < archetype.length(); i++) {
         ComponentType componentType = archetype.get(i);
         if (componentType != null) {
            Component component = archetypeChunk.getComponent(index, componentType);
            if (component != null) {
               holder.addComponent(componentType, component);
            }
         }
      }

      return holder;
   }

   @Nullable
   private static <T extends Entity> ComponentType<EntityStore, T> findComponentType(@Nonnull Archetype<EntityStore> archetype) {
      return findComponentType(archetype, Entity.class);
   }

   @Nullable
   private static <C extends Component<EntityStore>, T extends C> ComponentType<EntityStore, T> findComponentType(
      @Nonnull Archetype<EntityStore> archetype, @Nonnull Class<C> entityClass
   ) {
      for (int i = archetype.getMinIndex(); i < archetype.length(); i++) {
         ComponentType<EntityStore, ? extends Component<EntityStore>> componentType = (ComponentType<EntityStore, ? extends Component<EntityStore>>)archetype.get(
            i
         );
         if (componentType != null && entityClass.isAssignableFrom(componentType.getTypeClass())) {
            return (ComponentType<EntityStore, T>)componentType;
         }
      }

      return null;
   }

   @Deprecated
   @Nullable
   public static Entity getEntity(@Nullable Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (ref != null && ref.isValid()) {
         ComponentType<EntityStore, Entity> componentType = findComponentType(componentAccessor.getArchetype(ref));
         return componentType == null ? null : componentAccessor.getComponent(ref, componentType);
      } else {
         return null;
      }
   }

   @Nullable
   @Deprecated
   public static Entity getEntity(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk) {
      ComponentType<EntityStore, Entity> componentType = findComponentType(archetypeChunk.getArchetype());
      return componentType == null ? null : archetypeChunk.getComponent(index, componentType);
   }

   @Nullable
   @Deprecated
   public static Entity getEntity(@Nonnull Holder<EntityStore> holder) {
      Archetype<EntityStore> archetype = holder.getArchetype();
      if (archetype == null) {
         return null;
      } else {
         ComponentType<EntityStore, Entity> componentType = findComponentType(archetype);
         return componentType == null ? null : holder.getComponent(componentType);
      }
   }

   @Deprecated
   public static boolean hasEntity(@Nonnull Archetype<EntityStore> archetype) {
      return findComponentType(archetype) != null;
   }

   @Deprecated
   public static boolean hasLivingEntity(@Nonnull Archetype<EntityStore> archetype) {
      return findComponentType(archetype, LivingEntity.class) != null;
   }

   @Nonnull
   @Deprecated
   public static PhysicsValues getPhysicsValues(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      PhysicsValues physicsValuesComponent = componentAccessor.getComponent(ref, PhysicsValues.getComponentType());
      if (physicsValuesComponent != null) {
         return physicsValuesComponent;
      } else {
         ModelComponent modelComponent = componentAccessor.getComponent(ref, ModelComponent.getComponentType());
         Model model = modelComponent != null ? modelComponent.getModel() : null;
         return model != null && model.getPhysicsValues() != null ? model.getPhysicsValues() : PhysicsValues.getDefault();
      }
   }
}
