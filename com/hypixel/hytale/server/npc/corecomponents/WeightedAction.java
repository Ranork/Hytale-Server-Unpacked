package com.hypixel.hytale.server.npc.corecomponents;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderWeightedAction;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.ComponentInfo;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WeightedAction extends AnnotatedComponentBase implements Action {
   @Nullable
   private final Action action;
   private final double weight;

   public WeightedAction(@Nonnull BuilderWeightedAction builder, @Nonnull BuilderSupport support) {
      this.action = builder.getAction(support);
      this.weight = builder.getWeight(support);
   }

   public double getWeight() {
      return this.weight;
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return this.action.canExecute(ref, role, sensorInfo, dt, store);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return this.action.execute(ref, role, sensorInfo, dt, store);
   }

   @Override
   public void activate(Role role, InfoProvider infoProvider) {
      this.action.activate(role, infoProvider);
   }

   @Override
   public void deactivate(Role role, InfoProvider infoProvider) {
      this.action.deactivate(role, infoProvider);
   }

   @Override
   public boolean isActivated() {
      return this.action.isActivated();
   }

   @Override
   public void getInfo(Role role, ComponentInfo holder) {
      this.action.getInfo(role, holder);
   }

   @Override
   public boolean processDelay(float dt) {
      return this.action.processDelay(dt);
   }

   @Override
   public void clearOnce() {
      this.action.clearOnce();
   }

   @Override
   public void setOnce() {
      this.action.setOnce();
   }

   @Override
   public boolean isTriggered() {
      return this.action.isTriggered();
   }

   @Override
   public void registerWithSupport(Role role) {
      this.action.registerWithSupport(role);
   }

   @Override
   public void motionControllerChanged(
      @Nullable Ref<EntityStore> ref,
      @Nonnull NPCEntity npcComponent,
      MotionController motionController,
      @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.action.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
   }

   @Override
   public void loaded(Role role) {
      this.action.loaded(role);
   }

   @Override
   public void spawned(Role role) {
      this.action.spawned(role);
   }

   @Override
   public void unloaded(Role role) {
      this.action.unloaded(role);
   }

   @Override
   public void removed(Role role) {
      this.action.removed(role);
   }

   @Override
   public void teleported(Role role, World from, World to) {
      this.action.teleported(role, from, to);
   }
}
