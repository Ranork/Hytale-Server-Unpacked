package com.hypixel.hytale.server.core.modules.interaction;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockInteractionUtils {
   public static boolean isNaturalAction(@Nullable Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (ref == null) {
         return true;
      } else {
         Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
         return playerComponent != null ? playerComponent.getGameMode() == GameMode.Adventure : true;
      }
   }
}
