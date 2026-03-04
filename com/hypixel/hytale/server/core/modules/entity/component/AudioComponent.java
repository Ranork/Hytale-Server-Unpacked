package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import javax.annotation.Nonnull;

public class AudioComponent implements Component<EntityStore> {
   private IntList soundEventIds = new IntArrayList();
   private boolean isNetworkOutdated = true;

   public static ComponentType<EntityStore, AudioComponent> getComponentType() {
      return EntityModule.get().getAudioComponentType();
   }

   public AudioComponent() {
   }

   public AudioComponent(IntList soundEventIds) {
      this.soundEventIds = soundEventIds;
   }

   public int[] getSoundEventIds() {
      return this.soundEventIds.toIntArray();
   }

   public void addSound(int soundIndex) {
      this.soundEventIds.add(soundIndex);
      this.isNetworkOutdated = true;
   }

   public boolean consumeNetworkOutdated() {
      boolean temp = this.isNetworkOutdated;
      this.isNetworkOutdated = false;
      return temp;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new AudioComponent(new IntArrayList(this.soundEventIds));
   }
}
