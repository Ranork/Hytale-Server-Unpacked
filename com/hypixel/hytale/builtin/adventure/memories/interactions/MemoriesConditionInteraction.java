package com.hypixel.hytale.builtin.adventure.memories.interactions;

import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.Int2ObjectMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.CollectorTag;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.StringTag;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.Label;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.OperationsBuilder;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MemoriesConditionInteraction extends Interaction {
   @Nonnull
   public static final BuilderCodec<MemoriesConditionInteraction> CODEC = BuilderCodec.builder(
         MemoriesConditionInteraction.class, MemoriesConditionInteraction::new, ABSTRACT_CODEC
      )
      .appendInherited(
         new KeyedCodec<>("Next", new Int2ObjectMapCodec<>(Interaction.CHILD_ASSET_CODEC, Int2ObjectOpenHashMap::new)),
         (o, v) -> o.next = v,
         o -> o.next,
         (o, p) -> o.next = p.next
      )
      .documentation("The interaction to run if the player's memories level matches the key.")
      .addValidator(Validators.nonNull())
      .add()
      .<String>appendInherited(new KeyedCodec<>("Failed", Interaction.CHILD_ASSET_CODEC), (o, v) -> o.failed = v, o -> o.failed, (o, p) -> o.failed = p.failed)
      .documentation("The interaction to run if the player's memories level does not match any key.")
      .add()
      .afterDecode(o -> {
         o.levelToLabel.defaultReturnValue(-1);
         o.sortedKeys = o.next.keySet().toIntArray();
         Arrays.sort(o.sortedKeys);
         o.levelToLabel.clear();

         for (int i = 0; i < o.sortedKeys.length; i++) {
            o.levelToLabel.put(o.sortedKeys[i], i);
         }
      })
      .build();
   @Nonnull
   private static final StringTag TAG_FAILED = StringTag.of("Failed");
   @Nonnull
   private Int2ObjectMap<String> next = Int2ObjectMaps.emptyMap();
   private transient int[] sortedKeys;
   @Nonnull
   private final Int2IntOpenHashMap levelToLabel = new Int2IntOpenHashMap();
   @Nullable
   private String failed;

   @Override
   protected void tick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      World world = commandBuffer.getExternalData().getWorld();
      int memoriesLevel = MemoriesPlugin.get().getMemoriesLevel(world.getGameplayConfig());
      context.getState().chainingIndex = memoriesLevel;
      int labelIndex = this.levelToLabel.get(memoriesLevel);
      if (labelIndex == -1) {
         labelIndex = this.sortedKeys.length;
         context.getState().state = InteractionState.Failed;
      } else {
         context.getState().state = InteractionState.Finished;
      }

      context.jump(context.getLabel(labelIndex));
   }

   @Override
   protected void simulateTick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      int memoriesLevel = context.getServerState().chainingIndex;
      int labelIndex = this.levelToLabel.get(memoriesLevel);
      if (labelIndex == -1) {
         labelIndex = this.sortedKeys.length;
         context.getState().state = InteractionState.Failed;
      } else {
         context.getState().state = InteractionState.Finished;
      }

      context.jump(context.getLabel(labelIndex));
   }

   @Override
   public void compile(@Nonnull OperationsBuilder builder) {
      Label end = builder.createUnresolvedLabel();
      Label[] labels = new Label[this.next.size() + 1];

      for (int i = 0; i < labels.length; i++) {
         labels[i] = builder.createUnresolvedLabel();
      }

      builder.addOperation(this, labels);
      builder.jump(end);

      for (int i = 0; i < this.sortedKeys.length; i++) {
         int key = this.sortedKeys[i];
         builder.resolveLabel(labels[i]);
         Interaction interaction = Interaction.getInteractionOrUnknown((String)this.next.get(key));
         interaction.compile(builder);
         builder.jump(end);
      }

      int failedIndex = this.sortedKeys.length;
      builder.resolveLabel(labels[failedIndex]);
      if (this.failed != null) {
         Interaction interaction = Interaction.getInteractionOrUnknown(this.failed);
         interaction.compile(builder);
      }

      builder.resolveLabel(end);
   }

   @Nonnull
   @Override
   protected com.hypixel.hytale.protocol.Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.MemoriesConditionInteraction();
   }

   @Override
   protected void configurePacket(@Nonnull com.hypixel.hytale.protocol.Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.MemoriesConditionInteraction p = (com.hypixel.hytale.protocol.MemoriesConditionInteraction)packet;
      p.memoriesNext = new Int2IntOpenHashMap(this.next.size());
      ObjectIterator var3 = this.next.int2ObjectEntrySet().iterator();

      while (var3.hasNext()) {
         Entry<String> e = (Entry<String>)var3.next();
         p.memoriesNext.put(e.getIntKey(), Interaction.getInteractionIdOrUnknown((String)e.getValue()));
      }

      p.failed = Interaction.getInteractionIdOrUnknown(this.failed);
   }

   @Override
   public boolean walk(@Nonnull Collector collector, @Nonnull InteractionContext context) {
      ObjectIterator var3 = this.next.int2ObjectEntrySet().iterator();

      while (var3.hasNext()) {
         Entry<String> entry = (Entry<String>)var3.next();
         if (InteractionManager.walkInteraction(collector, context, new MemoriesConditionInteraction.MemoriesTag(entry.getIntKey()), (String)entry.getValue())) {
            return true;
         }
      }

      return this.failed != null ? InteractionManager.walkInteraction(collector, context, TAG_FAILED, this.failed) : false;
   }

   @Override
   public boolean needsRemoteSync() {
      return false;
   }

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Server;
   }

   private record MemoriesTag(int memoryLevel) implements CollectorTag {
   }
}
