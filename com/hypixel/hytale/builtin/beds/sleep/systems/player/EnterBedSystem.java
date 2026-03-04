package com.hypixel.hytale.builtin.beds.sleep.systems.player;

import com.hypixel.hytale.builtin.beds.sleep.systems.world.CanSleepInWorld;
import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.protocol.BlockMountType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.gameplay.sleep.SleepConfig;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnterBedSystem extends RefChangeSystem<EntityStore, MountedComponent> {
   @Nonnull
   private static final Message MESSAGE_SERVER_INTERACTIONS_SLEEP_GAME_TIME_PAUSED = Message.translation("server.interactions.sleep.gameTimePaused");
   @Nonnull
   private static final Message MESSAGE_SERVER_INTERACTIONS_SLEEP_NOT_WITHIN_HOURS = Message.translation("server.interactions.sleep.notWithinHours");
   @Nonnull
   private static final Message MESSAGE_SERVER_INTERACTIONS_SLEEP_DISABLED = Message.translation("server.interactions.sleep.disabled");
   @Nonnull
   private final ComponentType<EntityStore, MountedComponent> mountedComponentType;
   @Nonnull
   private final ComponentType<EntityStore, PlayerRef> playerRefComponentType;
   @Nonnull
   private final Query<EntityStore> query;

   public EnterBedSystem(
      @Nonnull ComponentType<EntityStore, MountedComponent> mountedComponentType, @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType
   ) {
      this.mountedComponentType = mountedComponentType;
      this.playerRefComponentType = playerRefComponentType;
      this.query = Query.and(mountedComponentType, playerRefComponentType);
   }

   @Nonnull
   @Override
   public ComponentType<EntityStore, MountedComponent> componentType() {
      return this.mountedComponentType;
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.query;
   }

   public void onComponentAdded(
      @Nonnull Ref<EntityStore> ref, @Nonnull MountedComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      check(ref, component, store, this.playerRefComponentType);
   }

   public void onComponentSet(
      @Nonnull Ref<EntityStore> ref,
      @Nullable MountedComponent oldComponent,
      @Nonnull MountedComponent newComponent,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      check(ref, newComponent, store, this.playerRefComponentType);
   }

   public void onComponentRemoved(
      @Nonnull Ref<EntityStore> ref, @Nonnull MountedComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
   }

   private static void check(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull MountedComponent component,
      @Nonnull Store<EntityStore> store,
      @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType
   ) {
      if (component.getBlockMountType() == BlockMountType.Bed) {
         onEnterBed(ref, store, playerRefComponentType);
      }
   }

   private static void onEnterBed(
      @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType
   ) {
      World world = store.getExternalData().getWorld();
      CanSleepInWorld.Result canSleepResult = CanSleepInWorld.check(world);
      if (canSleepResult.isNegative()) {
         PlayerRef playerRefComponent = store.getComponent(ref, playerRefComponentType);

         assert playerRefComponent != null;

         if (canSleepResult instanceof CanSleepInWorld.NotDuringSleepHoursRange(LocalDateTime msg, SleepConfig var14)) {
            LocalTime startTime = var14.getSleepStartTime();
            Duration untilSleep = var14.computeDurationUntilSleep(msg);
            Message msgx = Message.translation("server.interactions.sleep.sleepAtTheseHours")
               .param("timeValue", startTime.toString())
               .param("until", formatDuration(untilSleep));
            playerRefComponent.sendMessage(msgx.color("#F2D729"));
            SoundUtil.playSoundEvent2dToPlayer(playerRefComponent, var14.getSounds().getFailIndex(), SoundCategory.UI);
         } else {
            Message msg = getMessage(canSleepResult);
            playerRefComponent.sendMessage(msg);
         }
      }
   }

   @Nonnull
   private static Message getMessage(@Nonnull CanSleepInWorld.Result param0) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.IllegalStateException: Invalid switch case set: [[const(0)], [var0_1 instanceof ignored], [null]] for selector of type Lcom/hypixel/hytale/builtin/beds/sleep/systems/world/CanSleepInWorld$Result;
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.SwitchHeadExprent.checkExprTypeBounds(SwitchHeadExprent.java:66)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.checkTypeExpr(VarTypeProcessor.java:140)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.checkTypeExprent(VarTypeProcessor.java:126)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.lambda$processVarTypes$2(VarTypeProcessor.java:114)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.DirectGraph.iterateExprents(DirectGraph.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.processVarTypes(VarTypeProcessor.java:114)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.calculateVarTypes(VarTypeProcessor.java:44)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionsProcessor.setVarVersions(VarVersionsProcessor.java:68)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarProcessor.setVarVersions(VarProcessor.java:47)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:302)
      //
      // Bytecode:
      // 00: aload 0
      // 01: dup
      // 02: invokestatic java/util/Objects.requireNonNull (Ljava/lang/Object;)Ljava/lang/Object;
      // 05: pop
      // 06: astore 1
      // 07: bipush 0
      // 08: istore 2
      // 09: aload 1
      // 0a: iload 2
      // 0b: invokedynamic typeSwitch (Ljava/lang/Object;I)I bsm=java/lang/runtime/SwitchBootstraps.typeSwitch (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite; args=[ null.invoke Ljava/lang/Enum$EnumDesc;, com/hypixel/hytale/builtin/beds/sleep/systems/world/CanSleepInWorld$NotDuringSleepHoursRange ]
      // 10: lookupswitch 45 2 0 28 1 34
      // 2c: getstatic com/hypixel/hytale/builtin/beds/sleep/systems/player/EnterBedSystem.MESSAGE_SERVER_INTERACTIONS_SLEEP_GAME_TIME_PAUSED Lcom/hypixel/hytale/server/core/Message;
      // 2f: goto 40
      // 32: aload 1
      // 33: checkcast com/hypixel/hytale/builtin/beds/sleep/systems/world/CanSleepInWorld$NotDuringSleepHoursRange
      // 36: astore 3
      // 37: getstatic com/hypixel/hytale/builtin/beds/sleep/systems/player/EnterBedSystem.MESSAGE_SERVER_INTERACTIONS_SLEEP_NOT_WITHIN_HOURS Lcom/hypixel/hytale/server/core/Message;
      // 3a: goto 40
      // 3d: getstatic com/hypixel/hytale/builtin/beds/sleep/systems/player/EnterBedSystem.MESSAGE_SERVER_INTERACTIONS_SLEEP_DISABLED Lcom/hypixel/hytale/server/core/Message;
      // 40: areturn
   }

   @Nonnull
   private static Message formatDuration(@Nonnull Duration duration) {
      long totalMinutes = duration.toMinutes();
      long hours = totalMinutes / 60L;
      long minutes = totalMinutes % 60L;
      String msgKey = hours > 0L ? "server.interactions.sleep.durationHours" : "server.interactions.sleep.durationMins";
      return Message.translation(msgKey).param("hours", hours).param("mins", hours == 0L ? String.valueOf(minutes) : String.format("%02d", minutes));
   }
}
