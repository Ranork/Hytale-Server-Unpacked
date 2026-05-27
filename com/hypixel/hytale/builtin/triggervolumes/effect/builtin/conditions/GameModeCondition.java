package com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import javax.annotation.Nonnull;

public class GameModeCondition extends TriggerCondition {
   @Nonnull
   public static final BuilderCodec<GameModeCondition> CODEC = BuilderCodec.builder(GameModeCondition.class, GameModeCondition::new, BASE_CODEC)
      .append(new KeyedCodec<>("GameMode", new EnumCodec<>(GameMode.class), false), (c, v) -> c.gameMode = v, c -> c.gameMode)
      .add()
      .build();
   @Nonnull
   private GameMode gameMode = GameMode.Adventure;

   @Nonnull
   public static GameModeCondition create(@Nonnull TriggerEventType eventType, @Nonnull GameMode gameMode) {
      GameModeCondition condition = new GameModeCondition();
      condition.setEventType(eventType);
      condition.gameMode = gameMode;
      return condition;
   }

   @Override
   public boolean test(@Nonnull TriggerContext context) {
      Player player = context.getStore().getComponent(context.getEntityRef(), Player.getComponentType());
      return player != null && player.getGameMode() == this.gameMode;
   }
}
