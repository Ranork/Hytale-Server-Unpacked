package com.hypixel.hytale.server.npc.role.support;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.Instruction;
import com.hypixel.hytale.server.npc.role.builders.BuilderRole;
import com.hypixel.hytale.server.npc.util.IComponentExecutionControl;
import com.hypixel.hytale.server.npc.util.expression.StdLib;
import com.hypixel.hytale.server.npc.util.expression.StdScope;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntitySupport {
   protected final NPCEntity parent;
   @Nullable
   protected final String[] displayNames;
   @Nullable
   protected String nominatedDisplayName;
   protected StdScope sensorScope;
   @Nullable
   protected Instruction nextBodyMotionStep;
   @Nullable
   protected Instruction nextHeadMotionStep;
   protected final List<IComponentExecutionControl> delayingComponents = new ObjectArrayList();
   @Nullable
   protected List<String> targetPlayerActiveTasks;

   public EntitySupport(NPCEntity parent, @Nonnull BuilderRole builder) {
      this.parent = parent;
      this.displayNames = builder.getDisplayNames();
   }

   public StdScope getSensorScope() {
      return this.sensorScope;
   }

   @Nullable
   public Instruction getNextBodyMotionStep() {
      return this.nextBodyMotionStep;
   }

   public boolean setNextBodyMotionStep(Instruction step) {
      if (this.nextBodyMotionStep != null) {
         return false;
      } else {
         this.nextBodyMotionStep = step;
         return true;
      }
   }

   public void clearNextBodyMotionStep() {
      this.nextBodyMotionStep = null;
   }

   @Nullable
   public Instruction getNextHeadMotionStep() {
      return this.nextHeadMotionStep;
   }

   public boolean setNextHeadMotionStep(Instruction step) {
      if (this.nextHeadMotionStep != null) {
         return false;
      } else {
         this.nextHeadMotionStep = step;
         return true;
      }
   }

   public void clearNextHeadMotionStep() {
      this.nextHeadMotionStep = null;
   }

   public void postRoleBuilt(@Nonnull BuilderSupport builderSupport) {
      this.sensorScope = builderSupport.getSensorScope();
   }

   public void tick(float dt) {
      int i = 0;

      while (i < this.delayingComponents.size()) {
         IComponentExecutionControl component = this.delayingComponents.get(i);
         if (component.processDelay(dt)) {
            this.delayingComponents.remove(i);
         } else {
            i++;
         }
      }
   }

   public void handleNominatedDisplayName(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.nominatedDisplayName != null) {
         setDisplayName(ref, this.nominatedDisplayName, componentAccessor);
      }

      this.nominatedDisplayName = null;
   }

   public void nominateDisplayName(@Nonnull String displayName) {
      this.nominatedDisplayName = displayName;
   }

   public void pickRandomDisplayName(@Nonnull Holder<EntityStore> holder, boolean override) {
      if (this.displayNames != null && this.displayNames.length != 0) {
         setDisplayName(holder, this.displayNames[MathUtil.randomInt(0, this.displayNames.length)], override);
      }
   }

   public static void setDisplayName(@Nonnull Holder<EntityStore> holder, @Nonnull String displayName) {
      setDisplayName(holder, displayName, true);
   }

   public static void setDisplayName(@Nonnull Holder<EntityStore> holder, @Nullable String displayName, boolean override) {
      DisplayNameComponent displayNameComponent = holder.getComponent(DisplayNameComponent.getComponentType());
      if (displayNameComponent != null) {
         Message displayNameMessage = displayNameComponent.getDisplayName();
         if (displayNameMessage != null && !displayNameMessage.getAnsiMessage().isEmpty() && !override) {
            return;
         }
      }

      holder.putComponent(DisplayNameComponent.getComponentType(), new DisplayNameComponent(Message.raw(displayName != null ? displayName : "")));
      if (displayName != null) {
         Nameplate nameplateComponent = holder.ensureAndGetComponent(Nameplate.getComponentType());
         nameplateComponent.setText(displayName);
      } else {
         holder.removeComponent(Nameplate.getComponentType());
      }
   }

   public void pickRandomDisplayName(@Nonnull Ref<EntityStore> ref, boolean override, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      setRandomDisplayName(ref, this.displayNames, override, componentAccessor);
   }

   public static void setRandomDisplayName(
      @Nonnull Ref<EntityStore> ref, @Nullable String[] names, boolean override, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (names != null && names.length != 0) {
         setDisplayName(ref, names[MathUtil.randomInt(0, names.length)], override, componentAccessor);
      }
   }

   public static void setDisplayName(@Nonnull Ref<EntityStore> ref, @Nonnull String displayName, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      setDisplayName(ref, displayName, true, componentAccessor);
   }

   public static void setDisplayName(
      @Nonnull Ref<EntityStore> ref, @Nullable String displayName, boolean override, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (ref.isValid()) {
         DisplayNameComponent displayNameComponent = componentAccessor.getComponent(ref, DisplayNameComponent.getComponentType());
         if (displayNameComponent != null) {
            Message displayNameMessage = displayNameComponent.getDisplayName();
            if (displayNameMessage != null && !displayNameMessage.getAnsiMessage().isEmpty() && !override) {
               return;
            }
         }

         componentAccessor.putComponent(
            ref, DisplayNameComponent.getComponentType(), new DisplayNameComponent(Message.raw(displayName != null ? displayName : ""))
         );
         if (displayName != null) {
            Nameplate nameplateComponent = componentAccessor.ensureAndGetComponent(ref, Nameplate.getComponentType());
            nameplateComponent.setText(displayName);
         } else {
            componentAccessor.removeComponent(ref, Nameplate.getComponentType());
         }
      }
   }

   public void addTargetPlayerActiveTask(@Nonnull String task) {
      if (this.targetPlayerActiveTasks == null) {
         this.targetPlayerActiveTasks = new ObjectArrayList();
      }

      this.targetPlayerActiveTasks.add(task);
   }

   public void clearTargetPlayerActiveTasks() {
      if (this.targetPlayerActiveTasks != null) {
         this.targetPlayerActiveTasks.clear();
      }
   }

   @Nullable
   public List<String> getTargetPlayerActiveTasks() {
      return this.targetPlayerActiveTasks;
   }

   public void registerDelay(@Nonnull IComponentExecutionControl component) {
      this.delayingComponents.add(component);
   }

   @Nonnull
   public static StdScope createScope(@Nonnull NPCEntity entity) {
      StdScope scope = new StdScope(StdLib.getInstance());
      scope.addSupplier("blocked", () -> entity.getRole().getActiveMotionController().isObstructed());
      scope.addSupplier("health", () -> {
         EntityStatValue healthStat = EntityStatsModule.get(entity).get(DefaultEntityStatTypes.getHealth());
         return Objects.requireNonNull(healthStat).asPercentage();
      });
      return scope;
   }
}
