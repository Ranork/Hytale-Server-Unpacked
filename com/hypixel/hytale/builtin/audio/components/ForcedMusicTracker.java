package com.hypixel.hytale.builtin.audio.components;

import com.hypixel.hytale.builtin.audio.AudioPlugin;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.packets.world.UpdateForcedMusic;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ForcedMusicTracker implements Component<EntityStore> {
   @Nonnull
   private final UpdateForcedMusic musicPacket = new UpdateForcedMusic();
   private int currentContainerIndex;
   private int lastSentContainerIndex;

   public static ComponentType<EntityStore, ForcedMusicTracker> getComponentType() {
      return AudioPlugin.get().getForcedMusicTrackerComponentType();
   }

   public void setCurrentContainerIndex(int index) {
      this.currentContainerIndex = index;
   }

   public int getCurrentContainerIndex() {
      return this.currentContainerIndex;
   }

   public void setLastSentContainerIndex(int index) {
      this.lastSentContainerIndex = index;
   }

   public int getLastSentContainerIndex() {
      return this.lastSentContainerIndex;
   }

   @Nonnull
   public UpdateForcedMusic getMusicPacket() {
      return this.musicPacket;
   }

   @Nullable
   @Override
   public Component<EntityStore> clone() {
      ForcedMusicTracker clone = new ForcedMusicTracker();
      clone.currentContainerIndex = this.currentContainerIndex;
      clone.lastSentContainerIndex = this.lastSentContainerIndex;
      return clone;
   }
}
