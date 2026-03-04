package com.hypixel.hytale.server.npc.corecomponents;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.EntityFilterViewSector;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponent;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponentCollection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class SensorWithEntityFilters extends SensorBase implements IAnnotatedComponentCollection {
   @Nonnull
   private final IEntityFilter[] filters;

   public SensorWithEntityFilters(@Nonnull BuilderSensorBase builderSensorBase, @Nonnull IEntityFilter[] filters) {
      super(builderSensorBase);
      this.filters = filters;
      IEntityFilter.prioritiseFilters(this.filters);
   }

   @Override
   public void registerWithSupport(Role role) {
      for (IEntityFilter filter : this.filters) {
         filter.registerWithSupport(role);
      }
   }

   @Override
   public void motionControllerChanged(
      @Nullable Ref<EntityStore> ref,
      @Nonnull NPCEntity npcComponent,
      MotionController motionController,
      @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
      for (IEntityFilter filter : this.filters) {
         filter.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
      }
   }

   @Override
   public void loaded(Role role) {
      for (IEntityFilter filter : this.filters) {
         filter.loaded(role);
      }
   }

   @Override
   public void spawned(Role role) {
      for (IEntityFilter filter : this.filters) {
         filter.spawned(role);
      }
   }

   @Override
   public void unloaded(Role role) {
      for (IEntityFilter filter : this.filters) {
         filter.unloaded(role);
      }
   }

   @Override
   public void removed(Role role) {
      for (IEntityFilter filter : this.filters) {
         filter.removed(role);
      }
   }

   @Override
   public void teleported(Role role, World from, World to) {
      for (IEntityFilter filter : this.filters) {
         filter.teleported(role, from, to);
      }
   }

   @Override
   public int componentCount() {
      return this.filters.length;
   }

   @Override
   public IAnnotatedComponent getComponent(int index) {
      return this.filters[index];
   }

   @Override
   public void setContext(IAnnotatedComponent parent, int index) {
      super.setContext(parent, index);

      for (int i = 0; i < this.filters.length; i++) {
         this.filters[i].setContext(this, i);
      }
   }

   protected boolean matchesFilters(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      for (IEntityFilter filter : this.filters) {
         if (!filter.matchesEntity(ref, targetRef, role, store)) {
            return false;
         }
      }

      return true;
   }

   protected float findViewAngleFromFilters() {
      for (IEntityFilter filter : this.filters) {
         if (filter instanceof EntityFilterViewSector viewSector) {
            return viewSector.getViewAngle();
         }
      }

      return 0.0F;
   }
}
