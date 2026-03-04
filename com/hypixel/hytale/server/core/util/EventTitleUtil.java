package com.hypixel.hytale.server.core.util;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.HideEventTitle;
import com.hypixel.hytale.protocol.packets.interface_.ShowEventTitle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EventTitleUtil {
   public static final String DEFAULT_ZONE = "Void";
   public static final float DEFAULT_DURATION = 4.0F;
   public static final float DEFAULT_FADE_DURATION = 1.5F;

   public static void showEventTitleToUniverse(
      @Nonnull Message primaryTitle, @Nonnull Message secondaryTitle, boolean isMajor, String icon, float duration, float fadeInDuration, float fadeOutDuration
   ) {
      for (World world : Universe.get().getWorlds().values()) {
         world.execute(() -> {
            Store<EntityStore> store = world.getEntityStore().getStore();
            showEventTitleToWorld(primaryTitle, secondaryTitle, isMajor, icon, duration, fadeInDuration, fadeOutDuration, store);
         });
      }
   }

   public static void showEventTitleToWorld(
      @Nonnull Message primaryTitle,
      @Nonnull Message secondaryTitle,
      boolean isMajor,
      String icon,
      float duration,
      float fadeInDuration,
      float fadeOutDuration,
      @Nonnull Store<EntityStore> store
   ) {
      World world = store.getExternalData().getWorld();

      for (PlayerRef playerRef : world.getPlayerRefs()) {
         showEventTitleToPlayer(playerRef, primaryTitle, secondaryTitle, isMajor, icon, duration, fadeInDuration, fadeOutDuration);
      }
   }

   public static void hideEventTitleFromWorld(float fadeOutDuration, @Nonnull Store<EntityStore> store) {
      World world = store.getExternalData().getWorld();

      for (PlayerRef playerRef : world.getPlayerRefs()) {
         hideEventTitleFromPlayer(playerRef, fadeOutDuration);
      }
   }

   public static void showEventTitleToPlayer(
      @Nonnull PlayerRef playerRefComponent,
      @Nonnull Message primaryTitle,
      @Nonnull Message secondaryTitle,
      boolean isMajor,
      @Nullable String icon,
      float duration,
      float fadeInDuration,
      float fadeOutDuration
   ) {
      playerRefComponent.getPacketHandler()
         .writeNoCache(
            new ShowEventTitle(
               fadeInDuration, fadeOutDuration, duration, icon, isMajor, primaryTitle.getFormattedMessage(), secondaryTitle.getFormattedMessage()
            )
         );
   }

   public static void showEventTitleToPlayer(
      @Nonnull PlayerRef playerRefComponent, @Nonnull Message primaryTitle, @Nonnull Message secondaryTitle, boolean isMajor
   ) {
      showEventTitleToPlayer(playerRefComponent, primaryTitle, secondaryTitle, isMajor, null, 4.0F, 1.5F, 1.5F);
   }

   public static void hideEventTitleFromPlayer(@Nonnull PlayerRef playerRefComponent, float fadeOutDuration) {
      playerRefComponent.getPacketHandler().writeNoCache(new HideEventTitle(fadeOutDuration));
   }
}
