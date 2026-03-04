package com.hypixel.hytale.builtin.ambience.resources;

import com.hypixel.hytale.builtin.ambience.AmbiencePlugin;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.asset.type.ambiencefx.config.AmbienceFX;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nullable;

public class AmbienceResource implements Resource<EntityStore> {
   private int forcedMusicIndex;

   public static ResourceType<EntityStore, AmbienceResource> getResourceType() {
      return AmbiencePlugin.get().getAmbienceResourceType();
   }

   public void setForcedMusicAmbience(@Nullable String musicAmbienceId) {
      if (musicAmbienceId == null) {
         this.forcedMusicIndex = 0;
      } else {
         this.forcedMusicIndex = AmbienceFX.getAssetMap().getIndex(musicAmbienceId);
      }
   }

   public int getForcedMusicIndex() {
      return this.forcedMusicIndex;
   }

   @Override
   public Resource<EntityStore> clone() {
      AmbienceResource clone = new AmbienceResource();
      clone.forcedMusicIndex = this.forcedMusicIndex;
      return null;
   }
}
