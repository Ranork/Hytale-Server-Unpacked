package com.hypixel.hytale.server.npc.instructions;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.function.consumer.QuadConsumer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.builders.BuilderInstruction;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.DebugSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.ComponentInfo;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponent;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponentCollection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Instruction implements RoleStateChange, IAnnotatedComponentCollection {
   public static final Instruction[] EMPTY_ARRAY = new Instruction[0];
   public static final HytaleLogger LOGGER = NPCPlugin.get().getLogger();
   protected IAnnotatedComponent parent;
   @Nullable
   protected final String name;
   @Nullable
   protected final String tag;
   protected final Sensor sensor;
   protected int index;
   protected final Instruction[] instructionList;
   @Nullable
   protected final BodyMotion bodyMotion;
   @Nullable
   protected final HeadMotion headMotion;
   @Nonnull
   protected final ActionList actions;
   protected final double weight;
   protected final boolean treeMode;
   protected final boolean invertTreeModeResult;
   protected boolean continueAfter;
   @Nullable
   protected Instruction parentTreeModeStep;

   private Instruction(Instruction[] instructionList, @Nonnull BuilderSupport support) {
      this.tag = null;
      this.name = "Root";
      this.sensor = Sensor.NULL;
      this.instructionList = instructionList;
      this.bodyMotion = null;
      this.headMotion = null;
      this.actions = ActionList.EMPTY_ACTION_LIST;
      this.continueAfter = false;
      this.treeMode = false;
      this.invertTreeModeResult = false;
      this.weight = 1.0;
      int index = support.getInstructionSlot(this.name);
      support.putInstruction(index, this);
   }

   public Instruction(@Nonnull BuilderInstruction builder, Sensor sensor, @Nullable Instruction[] instructionList, @Nonnull BuilderSupport support) {
      this.tag = builder.getTag();
      this.name = builder.getName();
      this.sensor = sensor;
      if (instructionList != null) {
         this.instructionList = instructionList;
         this.bodyMotion = null;
         this.headMotion = null;
         this.actions = ActionList.EMPTY_ACTION_LIST;
      } else {
         this.instructionList = EMPTY_ARRAY;
         this.bodyMotion = builder.getBodyMotion(support);
         this.headMotion = builder.getHeadMotion(support);
         this.actions = builder.getActionList(support);
      }

      this.continueAfter = builder.isContinueAfter();
      this.treeMode = builder.isTreeMode();
      this.invertTreeModeResult = builder.isInvertTreeModeResult(support);
      this.weight = builder.getChance(support);
      int index = support.getInstructionSlot(this.name);
      support.putInstruction(index, this);
   }

   public Sensor getSensor() {
      return this.sensor;
   }

   @Nullable
   public String getDebugTag() {
      return this.tag;
   }

   public double getWeight() {
      return this.weight;
   }

   public boolean isContinueAfter() {
      return this.continueAfter;
   }

   @Nullable
   public BodyMotion getBodyMotion() {
      return this.bodyMotion;
   }

   @Nullable
   public HeadMotion getHeadMotion() {
      return this.headMotion;
   }

   @Override
   public void registerWithSupport(Role role) {
      this.sensor.registerWithSupport(role);

      for (Instruction instruction : this.instructionList) {
         instruction.registerWithSupport(role);
      }

      if (this.bodyMotion != null) {
         this.bodyMotion.registerWithSupport(role);
      }

      if (this.headMotion != null) {
         this.headMotion.registerWithSupport(role);
      }

      this.actions.registerWithSupport(role);
   }

   @Override
   public void motionControllerChanged(
      @Nullable Ref<EntityStore> ref,
      @Nonnull NPCEntity npcComponent,
      MotionController motionController,
      @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.sensor.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
      this.forEachInstruction(
         (instruction, motionController1) -> instruction.motionControllerChanged(ref, npcComponent, motionController1, componentAccessor), motionController
      );
      if (this.bodyMotion != null) {
         this.bodyMotion.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
      }

      if (this.headMotion != null) {
         this.headMotion.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
      }

      this.actions.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
   }

   @Override
   public void loaded(Role role) {
      this.sensor.loaded(role);
      this.forEachInstruction(Instruction::loaded, role);
      if (this.bodyMotion != null) {
         this.bodyMotion.loaded(role);
      }

      if (this.headMotion != null) {
         this.headMotion.loaded(role);
      }

      this.actions.loaded(role);
   }

   @Override
   public void spawned(Role role) {
      this.sensor.spawned(role);
      this.forEachInstruction(Instruction::spawned, role);
      if (this.bodyMotion != null) {
         this.bodyMotion.spawned(role);
      }

      if (this.headMotion != null) {
         this.headMotion.spawned(role);
      }

      this.actions.spawned(role);
   }

   @Override
   public void unloaded(Role role) {
      this.sensor.unloaded(role);
      this.forEachInstruction(Instruction::unloaded, role);
      if (this.bodyMotion != null) {
         this.bodyMotion.unloaded(role);
      }

      if (this.headMotion != null) {
         this.headMotion.unloaded(role);
      }

      this.actions.unloaded(role);
   }

   @Override
   public void removed(Role role) {
      this.sensor.removed(role);
      this.forEachInstruction(Instruction::removed, role);
      if (this.bodyMotion != null) {
         this.bodyMotion.removed(role);
      }

      if (this.headMotion != null) {
         this.headMotion.removed(role);
      }

      this.actions.removed(role);
   }

   @Override
   public void teleported(Role role, World from, World to) {
      this.sensor.teleported(role, from, to);
      this.forEachInstruction(Instruction::teleported, role, from, to);
      if (this.bodyMotion != null) {
         this.bodyMotion.teleported(role, from, to);
      }

      if (this.headMotion != null) {
         this.headMotion.teleported(role, from, to);
      }

      this.actions.teleported(role, from, to);
   }

   @Override
   public int componentCount() {
      int count = 1;
      count += this.actions.actionCount();
      count += this.instructionList.length;
      if (this.bodyMotion != null) {
         count++;
      }

      if (this.headMotion != null) {
         count++;
      }

      return count;
   }

   @Override
   public IAnnotatedComponent getComponent(int index) {
      if (index < 1) {
         return this.sensor;
      } else if (index <= this.actions.actionCount()) {
         return this.actions.getComponent(index - 1);
      } else if (index < this.componentCount()) {
         if (this.bodyMotion != null) {
            return (IAnnotatedComponent)(this.headMotion != null && index == this.componentCount() - 1 ? this.headMotion : this.bodyMotion);
         } else {
            return (IAnnotatedComponent)(this.headMotion != null ? this.headMotion : this.instructionList[index - 1]);
         }
      } else {
         throw new IndexOutOfBoundsException();
      }
   }

   @Override
   public void getInfo(Role role, @Nonnull ComponentInfo holder) {
      if (this.name != null && !this.name.isEmpty()) {
         holder.addField("Name: " + this.name);
      }
   }

   @Override
   public IAnnotatedComponent getParent() {
      return this.parent;
   }

   @Override
   public int getIndex() {
      return this.index;
   }

   @Nonnull
   @Override
   public String getLabel() {
      String tag = this.getDebugTag();
      if (tag != null) {
         return tag;
      } else {
         return this.index >= 0 ? String.format("[%s]%s", this.index, this.getClass().getSimpleName()) : this.getClass().getSimpleName();
      }
   }

   @Override
   public void setContext(IAnnotatedComponent parent, int index) {
      this.parent = parent;
      this.index = index;
      this.sensor.setContext(this, -1);
      if (this.bodyMotion != null) {
         this.bodyMotion.setContext(this, -1);
      }

      if (this.headMotion != null) {
         this.headMotion.setContext(this, -1);
      }

      this.actions.setContext(this);

      for (int i = 0; i < this.instructionList.length; i++) {
         this.instructionList[i].setContext(this, i);
      }
   }

   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      DebugSupport debugSupport = role.getDebugSupport();
      boolean traceSensorFails = debugSupport.isTraceSensorFails();
      if (traceSensorFails) {
         debugSupport.setLastFailingSensor(this.sensor);
      }

      if (this.sensor.matches(ref, role, dt, store)) {
         if (!this.treeMode && !this.continueAfter && !this.invertTreeModeResult) {
            role.notifySensorMatch();
         }

         if (traceSensorFails) {
            debugSupport.setLastFailingSensor(null);
         }

         return true;
      } else {
         if (debugSupport.isTraceFail()) {
            UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

            assert uuidComponent != null;

            LOGGER.at(Level.INFO).log("Instruction Sensor FAIL uuid=%d, debug=%s", uuidComponent.getUuid(), this.getBreadCrumbs());
         }

         if (traceSensorFails) {
            LOGGER.at(Level.INFO).log("Sensor FAIL, sensor=%s", debugSupport.getLastFailingSensor().getBreadCrumbs());
            debugSupport.setLastFailingSensor(null);
         }

         return false;
      }
   }

   public void executeActions(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      if (this.actions.canExecute(ref, role, sensorInfo, dt, store)) {
         this.actions.execute(ref, role, sensorInfo, dt, store);
      }
   }

   public void execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (this.instructionList.length <= 0) {
         InfoProvider sensorInfo = this.sensor.getSensorInfo();
         if (this.headMotion != null && role.getEntitySupport().setNextHeadMotionStep(this)) {
            this.headMotion.preComputeSteering(ref, role, sensorInfo, store);
         }

         if (this.bodyMotion != null && role.getEntitySupport().setNextBodyMotionStep(this)) {
            this.bodyMotion.preComputeSteering(ref, role, sensorInfo, store);
         }

         this.executeActions(ref, role, sensorInfo, dt, store);
         if (this.headMotion == null && this.bodyMotion == null) {
            this.sensor.setOnce();
            this.sensor.done();
         }

         if (role.getDebugSupport().isTraceSuccess()) {
            UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

            assert uuidComponent != null;

            LOGGER.at(Level.INFO).log("Instruction SUCC uuid=%d, debug=%s", uuidComponent.getUuid(), this.getBreadCrumbs());
         }
      } else {
         for (Instruction instruction : this.instructionList) {
            if (instruction.matches(ref, role, dt, store)) {
               instruction.onMatched(role);
               instruction.execute(ref, role, dt, store);
               instruction.onCompleted(role);
               if (!instruction.isContinueAfter()) {
                  break;
               }
            }
         }

         this.sensor.setOnce();
         this.sensor.done();
      }
   }

   public void clearOnce() {
      this.sensor.clearOnce();
      this.forEachInstruction(Instruction::clearOnce);
      this.actions.clearOnce();
   }

   public void onEndMotion() {
      this.actions.onEndMotion();
   }

   public void onMatched(@Nonnull Role role) {
      if (this.treeMode || this.invertTreeModeResult) {
         this.parentTreeModeStep = role.swapTreeModeSteps(this);
         if (this.treeMode) {
            this.continueAfter = true;
         }
      }
   }

   public void onCompleted(@Nonnull Role role) {
      if (this.treeMode || this.invertTreeModeResult) {
         role.swapTreeModeSteps(this.parentTreeModeStep);
         if (this.parentTreeModeStep != null) {
            if (this.continueAfter == this.invertTreeModeResult) {
               this.parentTreeModeStep.notifyChildSensorMatch();
            }

            this.parentTreeModeStep = null;
         }
      }
   }

   public void notifyChildSensorMatch() {
      if (this.treeMode) {
         this.continueAfter = false;
      }
   }

   public void reset() {
      this.clearOnce();
   }

   protected void forEachInstruction(@Nonnull Consumer<Instruction> instructionConsumer) {
      for (Instruction instruction : this.instructionList) {
         instructionConsumer.accept(instruction);
      }
   }

   protected <T> void forEachInstruction(@Nonnull BiConsumer<Instruction, T> instructionConsumer, T obj) {
      for (Instruction instruction : this.instructionList) {
         instructionConsumer.accept(instruction, obj);
      }
   }

   protected <T, U, V> void forEachInstruction(@Nonnull QuadConsumer<Instruction, T, U, V> instructionConsumer, T t, U u, V v) {
      for (Instruction instruction : this.instructionList) {
         instructionConsumer.accept(instruction, t, u, v);
      }
   }

   @Nonnull
   public static Instruction createRootInstruction(Instruction[] instructions, @Nonnull BuilderSupport support) {
      return new Instruction(instructions, support);
   }
}
