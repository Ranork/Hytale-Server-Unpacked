package com.hypixel.hytale.server.core.modules.interaction.interaction.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.PrioritySlot;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionConfiguration implements NetworkSerializable<com.hypixel.hytale.protocol.InteractionConfiguration> {
   public static final InteractionConfiguration DEFAULT = new InteractionConfiguration();
   public static final InteractionConfiguration DEFAULT_WEAPON = new InteractionConfiguration(false);
   private static final Object2FloatMap<GameMode> DEFAULT_USE_DISTANCE = new Object2FloatOpenHashMap<GameMode>() {
      {
         this.putIfAbsent(GameMode.Adventure, 5.0F);
         this.putIfAbsent(GameMode.Creative, 6.0F);
      }
   };
   public static final BuilderCodec<InteractionConfiguration> CODEC = BuilderCodec.builder(InteractionConfiguration.class, InteractionConfiguration::new)
      .appendInherited(
         new KeyedCodec<>("DisplayOutlines", Codec.BOOLEAN),
         (o, i) -> o.displayOutlines = i,
         o -> o.displayOutlines,
         (o, p) -> o.displayOutlines = p.displayOutlines
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("DebugOutlines", Codec.BOOLEAN), (o, i) -> o.debugOutlines = i, o -> o.debugOutlines, (o, p) -> o.debugOutlines = p.debugOutlines
      )
      .add()
      .<Map<GameMode, Float>>appendInherited(
         new KeyedCodec<>("UseDistance", new EnumMapCodec<>(GameMode.class, Codec.FLOAT, () -> new Object2FloatOpenHashMap(DEFAULT_USE_DISTANCE), false)),
         (o, i) -> o.useDistance = Object2FloatMaps.unmodifiable((Object2FloatOpenHashMap)i),
         o -> o.useDistance,
         (o, p) -> o.useDistance = p.useDistance
      )
      .addValidator(Validators.nonNull())
      .add()
      .appendInherited(new KeyedCodec<>("AllEntities", Codec.BOOLEAN), (o, i) -> o.allEntities = i, o -> o.allEntities, (o, p) -> o.allEntities = p.allEntities)
      .add()
      .<Map<InteractionType, InteractionPriority>>appendInherited(
         new KeyedCodec<>("Priorities", new EnumMapCodec<>(InteractionType.class, InteractionPriority.CODEC, Object2ObjectOpenHashMap::new, false)),
         (o, v) -> o.priorities = v,
         o -> o.priorities,
         (o, p) -> o.priorities = p.priorities
      )
      .documentation("Configures the priority values for given interaction types on this item when two or more items are equipped.")
      .add()
      .build();
   protected boolean displayOutlines = true;
   protected boolean debugOutlines;
   protected Object2FloatMap<GameMode> useDistance = DEFAULT_USE_DISTANCE;
   protected boolean allEntities;
   @Nullable
   protected Map<InteractionType, InteractionPriority> priorities;

   public InteractionConfiguration() {
   }

   public InteractionConfiguration(boolean displayOutlines) {
      this.displayOutlines = displayOutlines;
   }

   public int getPriorityFor(InteractionType interactionType, PrioritySlot slot) {
      if (this.priorities == null) {
         return 0;
      } else {
         InteractionPriority priority = this.priorities.get(interactionType);
         return priority == null ? 0 : priority.getPriority(slot);
      }
   }

   public float getUseDistance(GameMode mode) {
      return this.useDistance == null ? DEFAULT_USE_DISTANCE.getOrDefault(mode, 1.0F) : this.useDistance.getOrDefault(mode, 1.0F);
   }

   @Nonnull
   public com.hypixel.hytale.protocol.InteractionConfiguration toPacket() {
      com.hypixel.hytale.protocol.InteractionConfiguration packet = new com.hypixel.hytale.protocol.InteractionConfiguration();
      packet.displayOutlines = this.displayOutlines;
      packet.debugOutlines = this.debugOutlines;
      packet.useDistance = this.useDistance;
      packet.allEntities = this.allEntities;
      if (this.priorities != null && !this.priorities.isEmpty()) {
         Object2ObjectOpenHashMap<InteractionType, com.hypixel.hytale.protocol.InteractionPriority> packetPriorities = new Object2ObjectOpenHashMap();

         for (Entry<InteractionType, InteractionPriority> entry : this.priorities.entrySet()) {
            packetPriorities.put(entry.getKey(), entry.getValue().toPacket());
         }

         packet.priorities = packetPriorities;
      }

      return packet;
   }
}
