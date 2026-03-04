package com.hypixel.hytale.server.npc.corecomponents.utility;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionWithDelay;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderActionTimeout;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponent;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponentCollection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionTimeout extends ActionWithDelay implements IAnnotatedComponentCollection {
   protected final boolean delayAfter;
   @Nullable
   protected final Action action;

   public ActionTimeout(@Nonnull BuilderActionTimeout builderActionTimeout, @Nonnull BuilderSupport builderSupport) {
      super(builderActionTimeout, builderSupport);
      this.action = builderActionTimeout.getAction(builderSupport);
      this.delayAfter = builderActionTimeout.isDelayAfter();
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      if (super.canExecute(ref, role, sensorInfo, dt, store) && (this.action == null || this.action.canExecute(ref, role, sensorInfo, dt, store))) {
         if (!this.isDelaying() && this.isDelayPrepared()) {
            this.startDelay(role.getEntitySupport());
         }

         return !this.isDelaying();
      } else {
         return false;
      }
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      if (this.action != null) {
         this.action.execute(ref, role, sensorInfo, dt, store);
      }

      this.prepareDelay();
      return true;
   }

   @Override
   public void registerWithSupport(Role role) {
      if (this.action != null) {
         this.action.registerWithSupport(role);
      }

      if (this.delayAfter) {
         this.clearDelay();
      } else {
         this.prepareDelay();
      }
   }

   @Override
   public void motionControllerChanged(
      @Nullable Ref<EntityStore> ref,
      @Nonnull NPCEntity npcComponent,
      MotionController motionController,
      @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.action != null) {
         this.action.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
      }
   }

   @Override
   public void loaded(Role role) {
      if (this.action != null) {
         this.action.loaded(role);
      }
   }

   @Override
   public void spawned(Role role) {
      if (this.action != null) {
         this.action.spawned(role);
      }
   }

   @Override
   public void unloaded(Role role) {
      if (this.action != null) {
         this.action.unloaded(role);
      }
   }

   @Override
   public void removed(Role role) {
      if (this.action != null) {
         this.action.removed(role);
      }
   }

   @Override
   public void teleported(Role role, World from, World to) {
      if (this.action != null) {
         this.action.teleported(role, from, to);
      }
   }

   @Override
   public void clearOnce() {
      super.clearOnce();
      if (this.delayAfter) {
         this.clearDelay();
      } else {
         this.prepareDelay();
      }
   }

   @Override
   public int componentCount() {
      return this.action != null ? 1 : 0;
   }

   @Nullable
   @Override
   public IAnnotatedComponent getComponent(int index) {
      if (index >= this.componentCount()) {
         throw new IndexOutOfBoundsException();
      } else {
         return this.action;
      }
   }
}
