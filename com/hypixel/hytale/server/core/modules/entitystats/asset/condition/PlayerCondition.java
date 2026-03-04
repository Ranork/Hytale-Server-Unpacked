package com.hypixel.hytale.server.core.modules.entitystats.asset.condition;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerCondition extends Condition {
   @Nonnull
   public static final BuilderCodec<PlayerCondition> CODEC = BuilderCodec.builder(PlayerCondition.class, PlayerCondition::new, Condition.BASE_CODEC)
      .append(
         new KeyedCodec<>("GameMode", new EnumCodec<>(GameMode.class)),
         (condition, gameMode) -> condition.gameModeToCheck = gameMode,
         condition -> condition.gameModeToCheck
      )
      .documentation("The game mode to check for. If null, the condition always passes.")
      .add()
      .build();
   @Nullable
   private GameMode gameModeToCheck;

   protected PlayerCondition() {
   }

   @Override
   public boolean eval0(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime) {
      if (this.gameModeToCheck == null) {
         return true;
      } else {
         Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
         return playerComponent == null ? false : playerComponent.getGameMode() == this.gameModeToCheck;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "PlayerCondition{} " + super.toString();
   }
}
