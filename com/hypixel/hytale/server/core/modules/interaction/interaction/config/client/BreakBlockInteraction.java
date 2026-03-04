package com.hypixel.hytale.server.core.modules.interaction.interaction.config.client;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BreakBlockInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final BuilderCodec<BreakBlockInteraction> CODEC = BuilderCodec.builder(
         BreakBlockInteraction.class, BreakBlockInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Attempts to break the target block.")
      .<Boolean>appendInherited(
         new KeyedCodec<>("Harvest", Codec.BOOLEAN),
         (interaction, v) -> interaction.harvest = v,
         interaction -> interaction.harvest,
         (o, p) -> o.harvest = p.harvest
      )
      .documentation("Whether this should trigger as a harvest gather vs a break gather.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("Tool", Codec.STRING), (interaction, v) -> interaction.toolId = v, interaction -> interaction.toolId, (o, p) -> o.toolId = p.toolId
      )
      .documentation("Tool to break as.")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("MatchTool", Codec.BOOLEAN),
         (interaction, v) -> interaction.matchTool = v,
         interaction -> interaction.matchTool,
         (o, p) -> o.matchTool = p.matchTool
      )
      .documentation("Whether to require an match to `Tool` to work.")
      .add()
      .build();
   protected boolean harvest;
   @Nullable
   protected String toolId;
   protected boolean matchTool;

   @Override
   protected void tick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      super.tick0(firstRun, time, type, context, cooldownHandler);
      this.computeCurrentBlockSyncData(context);
   }

   @Override
   protected void interactWithBlock(
      @Nonnull World param1,
      @Nonnull CommandBuffer<EntityStore> param2,
      @Nonnull InteractionType param3,
      @Nonnull InteractionContext param4,
      @Nullable ItemStack param5,
      @Nonnull Vector3i param6,
      @Nonnull CooldownHandler param7
   ) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.IllegalStateException: Invalid switch case set: [[const(0)], [const(1)], [const(null), null]] for selector of type Lcom/hypixel/hytale/protocol/GameMode;
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
      // 000: aload 4
      // 002: invokevirtual com/hypixel/hytale/server/core/entity/InteractionContext.getEntity ()Lcom/hypixel/hytale/component/Ref;
      // 005: astore 8
      // 007: aload 2
      // 008: aload 8
      // 00a: invokestatic com/hypixel/hytale/server/core/entity/entities/Player.getComponentType ()Lcom/hypixel/hytale/component/ComponentType;
      // 00d: invokevirtual com/hypixel/hytale/component/CommandBuffer.getComponent (Lcom/hypixel/hytale/component/Ref;Lcom/hypixel/hytale/component/ComponentType;)Lcom/hypixel/hytale/component/Component;
      // 010: checkcast com/hypixel/hytale/server/core/entity/entities/Player
      // 013: astore 9
      // 015: aload 9
      // 017: ifnonnull 039
      // 01a: invokestatic com/hypixel/hytale/logger/HytaleLogger.getLogger ()Lcom/hypixel/hytale/logger/HytaleLogger;
      // 01d: getstatic java/util/logging/Level.INFO Ljava/util/logging/Level;
      // 020: invokevirtual com/hypixel/hytale/logger/HytaleLogger.at (Ljava/util/logging/Level;)Lcom/hypixel/hytale/logger/HytaleLogger$Api;
      // 023: bipush 5
      // 024: getstatic java/util/concurrent/TimeUnit.MINUTES Ljava/util/concurrent/TimeUnit;
      // 027: invokeinterface com/hypixel/hytale/logger/HytaleLogger$Api.atMostEvery (ILjava/util/concurrent/TimeUnit;)Lcom/google/common/flogger/LoggingApi; 3
      // 02c: checkcast com/hypixel/hytale/logger/HytaleLogger$Api
      // 02f: ldc "BreakBlockInteraction requires a Player but was used for: %s"
      // 031: aload 8
      // 033: invokeinterface com/hypixel/hytale/logger/HytaleLogger$Api.log (Ljava/lang/String;Ljava/lang/Object;)V 3
      // 038: return
      // 039: aload 1
      // 03a: invokevirtual com/hypixel/hytale/server/core/universe/world/World.getChunkStore ()Lcom/hypixel/hytale/server/core/universe/world/storage/ChunkStore;
      // 03d: astore 10
      // 03f: aload 10
      // 041: invokevirtual com/hypixel/hytale/server/core/universe/world/storage/ChunkStore.getStore ()Lcom/hypixel/hytale/component/Store;
      // 044: astore 11
      // 046: aload 6
      // 048: getfield com/hypixel/hytale/math/vector/Vector3i.x I
      // 04b: aload 6
      // 04d: getfield com/hypixel/hytale/math/vector/Vector3i.z I
      // 050: invokestatic com/hypixel/hytale/math/util/ChunkUtil.indexChunkFromBlock (II)J
      // 053: lstore 12
      // 055: aload 10
      // 057: lload 12
      // 059: invokevirtual com/hypixel/hytale/server/core/universe/world/storage/ChunkStore.getChunkReference (J)Lcom/hypixel/hytale/component/Ref;
      // 05c: astore 14
      // 05e: aload 14
      // 060: ifnull 06b
      // 063: aload 14
      // 065: invokevirtual com/hypixel/hytale/component/Ref.isValid ()Z
      // 068: ifne 06c
      // 06b: return
      // 06c: aload 11
      // 06e: aload 14
      // 070: invokestatic com/hypixel/hytale/server/core/universe/world/chunk/WorldChunk.getComponentType ()Lcom/hypixel/hytale/component/ComponentType;
      // 073: invokevirtual com/hypixel/hytale/component/Store.getComponent (Lcom/hypixel/hytale/component/Ref;Lcom/hypixel/hytale/component/ComponentType;)Lcom/hypixel/hytale/component/Component;
      // 076: checkcast com/hypixel/hytale/server/core/universe/world/chunk/WorldChunk
      // 079: astore 15
      // 07b: getstatic com/hypixel/hytale/server/core/modules/interaction/interaction/config/client/BreakBlockInteraction.$assertionsDisabled Z
      // 07e: ifne 08e
      // 081: aload 15
      // 083: ifnonnull 08e
      // 086: new java/lang/AssertionError
      // 089: dup
      // 08a: invokespecial java/lang/AssertionError.<init> ()V
      // 08d: athrow
      // 08e: aload 11
      // 090: aload 14
      // 092: invokestatic com/hypixel/hytale/server/core/universe/world/chunk/BlockChunk.getComponentType ()Lcom/hypixel/hytale/component/ComponentType;
      // 095: invokevirtual com/hypixel/hytale/component/Store.getComponent (Lcom/hypixel/hytale/component/Ref;Lcom/hypixel/hytale/component/ComponentType;)Lcom/hypixel/hytale/component/Component;
      // 098: checkcast com/hypixel/hytale/server/core/universe/world/chunk/BlockChunk
      // 09b: astore 16
      // 09d: getstatic com/hypixel/hytale/server/core/modules/interaction/interaction/config/client/BreakBlockInteraction.$assertionsDisabled Z
      // 0a0: ifne 0b0
      // 0a3: aload 16
      // 0a5: ifnonnull 0b0
      // 0a8: new java/lang/AssertionError
      // 0ab: dup
      // 0ac: invokespecial java/lang/AssertionError.<init> ()V
      // 0af: athrow
      // 0b0: aload 16
      // 0b2: aload 6
      // 0b4: invokevirtual com/hypixel/hytale/math/vector/Vector3i.getY ()I
      // 0b7: invokevirtual com/hypixel/hytale/server/core/universe/world/chunk/BlockChunk.getSectionAtBlockY (I)Lcom/hypixel/hytale/server/core/universe/world/chunk/section/BlockSection;
      // 0ba: astore 17
      // 0bc: aload 1
      // 0bd: invokevirtual com/hypixel/hytale/server/core/universe/world/World.getGameplayConfig ()Lcom/hypixel/hytale/server/core/asset/type/gameplay/GameplayConfig;
      // 0c0: astore 18
      // 0c2: aload 18
      // 0c4: invokevirtual com/hypixel/hytale/server/core/asset/type/gameplay/GameplayConfig.getWorldConfig ()Lcom/hypixel/hytale/server/core/asset/type/gameplay/WorldConfig;
      // 0c7: astore 19
      // 0c9: aload 0
      // 0ca: getfield com/hypixel/hytale/server/core/modules/interaction/interaction/config/client/BreakBlockInteraction.harvest Z
      // 0cd: ifeq 14b
      // 0d0: aload 6
      // 0d2: invokevirtual com/hypixel/hytale/math/vector/Vector3i.getX ()I
      // 0d5: istore 20
      // 0d7: aload 6
      // 0d9: invokevirtual com/hypixel/hytale/math/vector/Vector3i.getY ()I
      // 0dc: istore 21
      // 0de: aload 6
      // 0e0: invokevirtual com/hypixel/hytale/math/vector/Vector3i.getZ ()I
      // 0e3: istore 22
      // 0e5: aload 15
      // 0e7: iload 20
      // 0e9: iload 21
      // 0eb: iload 22
      // 0ed: invokevirtual com/hypixel/hytale/server/core/universe/world/chunk/WorldChunk.getBlockType (III)Lcom/hypixel/hytale/server/core/asset/type/blocktype/config/BlockType;
      // 0f0: astore 23
      // 0f2: aload 23
      // 0f4: ifnonnull 103
      // 0f7: aload 4
      // 0f9: invokevirtual com/hypixel/hytale/server/core/entity/InteractionContext.getState ()Lcom/hypixel/hytale/protocol/InteractionSyncData;
      // 0fc: getstatic com/hypixel/hytale/protocol/InteractionState.Failed Lcom/hypixel/hytale/protocol/InteractionState;
      // 0ff: putfield com/hypixel/hytale/protocol/InteractionSyncData.state Lcom/hypixel/hytale/protocol/InteractionState;
      // 102: return
      // 103: aload 19
      // 105: invokevirtual com/hypixel/hytale/server/core/asset/type/gameplay/WorldConfig.isBlockGatheringAllowed ()Z
      // 108: ifne 117
      // 10b: aload 4
      // 10d: invokevirtual com/hypixel/hytale/server/core/entity/InteractionContext.getState ()Lcom/hypixel/hytale/protocol/InteractionSyncData;
      // 110: getstatic com/hypixel/hytale/protocol/InteractionState.Failed Lcom/hypixel/hytale/protocol/InteractionState;
      // 113: putfield com/hypixel/hytale/protocol/InteractionSyncData.state Lcom/hypixel/hytale/protocol/InteractionState;
      // 116: return
      // 117: aload 23
      // 119: invokestatic com/hypixel/hytale/server/core/modules/interaction/BlockHarvestUtils.shouldPickupByInteraction (Lcom/hypixel/hytale/server/core/asset/type/blocktype/config/BlockType;)Z
      // 11c: ifne 12b
      // 11f: aload 4
      // 121: invokevirtual com/hypixel/hytale/server/core/entity/InteractionContext.getState ()Lcom/hypixel/hytale/protocol/InteractionSyncData;
      // 124: getstatic com/hypixel/hytale/protocol/InteractionState.Failed Lcom/hypixel/hytale/protocol/InteractionState;
      // 127: putfield com/hypixel/hytale/protocol/InteractionSyncData.state Lcom/hypixel/hytale/protocol/InteractionState;
      // 12a: return
      // 12b: aload 17
      // 12d: iload 20
      // 12f: iload 21
      // 131: iload 22
      // 133: invokevirtual com/hypixel/hytale/server/core/universe/world/chunk/section/BlockSection.getFiller (III)I
      // 136: istore 24
      // 138: aload 8
      // 13a: aload 6
      // 13c: aload 23
      // 13e: iload 24
      // 140: aload 14
      // 142: aload 2
      // 143: aload 11
      // 145: invokestatic com/hypixel/hytale/server/core/modules/interaction/BlockHarvestUtils.performPickupByInteraction (Lcom/hypixel/hytale/component/Ref;Lcom/hypixel/hytale/math/vector/Vector3i;Lcom/hypixel/hytale/server/core/asset/type/blocktype/config/BlockType;ILcom/hypixel/hytale/component/Ref;Lcom/hypixel/hytale/component/ComponentAccessor;Lcom/hypixel/hytale/component/ComponentAccessor;)V
      // 148: goto 1ca
      // 14b: aload 19
      // 14d: invokevirtual com/hypixel/hytale/server/core/asset/type/gameplay/WorldConfig.isBlockBreakingAllowed ()Z
      // 150: istore 20
      // 152: iload 20
      // 154: ifne 163
      // 157: aload 4
      // 159: invokevirtual com/hypixel/hytale/server/core/entity/InteractionContext.getState ()Lcom/hypixel/hytale/protocol/InteractionSyncData;
      // 15c: getstatic com/hypixel/hytale/protocol/InteractionState.Failed Lcom/hypixel/hytale/protocol/InteractionState;
      // 15f: putfield com/hypixel/hytale/protocol/InteractionSyncData.state Lcom/hypixel/hytale/protocol/InteractionState;
      // 162: return
      // 163: aload 9
      // 165: invokevirtual com/hypixel/hytale/server/core/entity/entities/Player.getGameMode ()Lcom/hypixel/hytale/protocol/GameMode;
      // 168: astore 21
      // 16a: bipush 0
      // 16b: istore 22
      // 16d: aload 21
      // 16f: iload 22
      // 171: invokedynamic typeSwitch (Ljava/lang/Object;I)I bsm=java/lang/runtime/SwitchBootstraps.typeSwitch (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite; args=[ null.invoke Ljava/lang/Enum$EnumDesc;, null.invoke Ljava/lang/Enum$EnumDesc; ]
      // 176: tableswitch 74 -1 1 74 26 57
      // 190: aload 9
      // 192: aload 8
      // 194: aload 6
      // 196: aload 5
      // 198: aconst_null
      // 199: aload 0
      // 19a: getfield com/hypixel/hytale/server/core/modules/interaction/interaction/config/client/BreakBlockInteraction.toolId Ljava/lang/String;
      // 19d: aload 0
      // 19e: getfield com/hypixel/hytale/server/core/modules/interaction/interaction/config/client/BreakBlockInteraction.matchTool Z
      // 1a1: fconst_1
      // 1a2: bipush 0
      // 1a3: aload 14
      // 1a5: aload 2
      // 1a6: aload 11
      // 1a8: invokestatic com/hypixel/hytale/server/core/modules/interaction/BlockHarvestUtils.performBlockDamage (Lcom/hypixel/hytale/server/core/entity/LivingEntity;Lcom/hypixel/hytale/component/Ref;Lcom/hypixel/hytale/math/vector/Vector3i;Lcom/hypixel/hytale/server/core/inventory/ItemStack;Lcom/hypixel/hytale/server/core/asset/type/item/config/ItemTool;Ljava/lang/String;ZFILcom/hypixel/hytale/component/Ref;Lcom/hypixel/hytale/component/ComponentAccessor;Lcom/hypixel/hytale/component/ComponentAccessor;)Z
      // 1ab: pop
      // 1ac: goto 1ca
      // 1af: aload 8
      // 1b1: aload 5
      // 1b3: aload 6
      // 1b5: aload 14
      // 1b7: aload 2
      // 1b8: aload 11
      // 1ba: invokestatic com/hypixel/hytale/server/core/modules/interaction/BlockHarvestUtils.performBlockBreak (Lcom/hypixel/hytale/component/Ref;Lcom/hypixel/hytale/server/core/inventory/ItemStack;Lcom/hypixel/hytale/math/vector/Vector3i;Lcom/hypixel/hytale/component/Ref;Lcom/hypixel/hytale/component/ComponentAccessor;Lcom/hypixel/hytale/component/ComponentAccessor;)V
      // 1bd: goto 1ca
      // 1c0: new java/lang/UnsupportedOperationException
      // 1c3: dup
      // 1c4: ldc "GameMode is not supported"
      // 1c6: invokespecial java/lang/UnsupportedOperationException.<init> (Ljava/lang/String;)V
      // 1c9: athrow
      // 1ca: return
   }

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
   }

   @Nonnull
   @Override
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.BreakBlockInteraction();
   }

   @Override
   protected void configurePacket(Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.BreakBlockInteraction p = (com.hypixel.hytale.protocol.BreakBlockInteraction)packet;
      p.harvest = this.harvest;
   }

   @Nonnull
   @Override
   public String toString() {
      return "BreakBlockInteraction{harvest=" + this.harvest + "} " + super.toString();
   }
}
