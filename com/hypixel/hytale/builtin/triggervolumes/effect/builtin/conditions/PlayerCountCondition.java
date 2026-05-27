package com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashSet;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class PlayerCountCondition extends TriggerCondition {
   @Nonnull
   public static final BuilderCodec<PlayerCountCondition> CODEC = BuilderCodec.builder(PlayerCountCondition.class, PlayerCountCondition::new, BASE_CODEC)
      .append(
         new KeyedCodec<>("Comparison", new EnumCodec<>(PlayerCountCondition.Comparison.class), false),
         (condition, comparison) -> condition.comparison = comparison,
         condition -> condition.comparison
      )
      .add()
      .append(new KeyedCodec<>("Count", Codec.INTEGER, false), (condition, count) -> condition.count = count, condition -> condition.count)
      .add()
      .build();
   @Nonnull
   private PlayerCountCondition.Comparison comparison = PlayerCountCondition.Comparison.AT_LEAST;
   private int count = 1;

   @Nonnull
   public static PlayerCountCondition create(@Nonnull TriggerEventType eventType, @Nonnull PlayerCountCondition.Comparison comparison, int count) {
      PlayerCountCondition condition = new PlayerCountCondition();
      condition.setEventType(eventType);
      condition.comparison = comparison;
      condition.count = count;
      return condition;
   }

   @Override
   public boolean test(@Nonnull TriggerContext context) {
      return this.matches(this.countPlayers(context));
   }

   public int countPlayers(@Nonnull TriggerContext context) {
      Store<EntityStore> store = context.getStore();
      HashSet<UUID> players = new HashSet<>();

      for (VolumeEntry volume : context.getSpatialVolumes()) {
         for (Entry<UUID, Ref<EntityStore>> entry : volume.getTrackedEntities().entrySet()) {
            if (!players.contains(entry.getKey()) && isPlayer(store, entry.getValue())) {
               players.add(entry.getKey());
            }
         }
      }

      return players.size();
   }

   public boolean matches(int playerCount) {
      return switch (this.comparison) {
         case AT_LEAST -> playerCount >= this.count;
         case AT_MOST -> playerCount <= this.count;
         case EXACTLY -> playerCount == this.count;
         case MORE_THAN -> playerCount > this.count;
         case LESS_THAN -> playerCount < this.count;
      };
   }

   private static boolean isPlayer(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref) {
      return !ref.isValid() ? false : store.getComponent(ref, Player.getComponentType()) != null;
   }

   public static enum Comparison {
      AT_LEAST,
      AT_MOST,
      EXACTLY,
      MORE_THAN,
      LESS_THAN;
   }
}
