package com.hypixel.hytale.builtin.ambience.components;

import com.hypixel.hytale.builtin.ambience.AmbiencePlugin;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.packets.world.UpdateEnvironmentMusic;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmbienceTracker implements Component<EntityStore> {
   @Nonnull
   private final UpdateEnvironmentMusic musicPacket = new UpdateEnvironmentMusic(0);
   private int forcedMusicIndex;

   public static ComponentType<EntityStore, AmbienceTracker> getComponentType() {
      return AmbiencePlugin.get().getAmbienceTrackerComponentType();
   }

   public void setForcedMusicIndex(int forcedMusicIndex) {
      this.forcedMusicIndex = forcedMusicIndex;
   }

   public int getForcedMusicIndex() {
      return this.forcedMusicIndex;
   }

   @Nonnull
   public UpdateEnvironmentMusic getMusicPacket() {
      return this.musicPacket;
   }

   @Nullable
   @Override
   public Component<EntityStore> clone() {
      AmbienceTracker clone = new AmbienceTracker();
      clone.forcedMusicIndex = this.forcedMusicIndex;
      return clone;
   }
}
