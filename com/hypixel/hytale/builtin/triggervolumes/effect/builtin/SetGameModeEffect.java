package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class SetGameModeEffect extends TriggerEffect {
   @Nonnull
   public static final BuilderCodec<SetGameModeEffect> CODEC = BuilderCodec.builder(SetGameModeEffect.class, SetGameModeEffect::new, BASE_CODEC)
      .append(new KeyedCodec<>("GameMode", new EnumCodec<>(GameMode.class), false), (e, v) -> e.gameMode = v, e -> e.gameMode)
      .add()
      .build();
   @Nonnull
   private GameMode gameMode = GameMode.Adventure;

   @Override
   public void execute(@Nonnull TriggerContext context) {
      Store<EntityStore> store = context.getStore();
      Ref<EntityStore> entityRef = context.getEntityRef();
      Player player = store.getComponent(entityRef, Player.getComponentType());
      if (player != null && store.getComponent(entityRef, PlayerRef.getComponentType()) != null) {
         if (store.getComponent(entityRef, MovementManager.getComponentType()) != null) {
            if (this.gameMode != null && player.getGameMode() != this.gameMode) {
               Player.setGameMode(entityRef, this.gameMode, store);
            }
         }
      }
   }
}
