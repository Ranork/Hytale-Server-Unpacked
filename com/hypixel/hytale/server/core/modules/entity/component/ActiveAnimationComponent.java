package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActiveAnimationComponent implements Component<EntityStore> {
   private final String[] activeAnimations = new String[AnimationSlot.VALUES.length];
   private boolean isNetworkOutdated = false;

   public static ComponentType<EntityStore, ActiveAnimationComponent> getComponentType() {
      return EntityModule.get().getActiveAnimationComponentType();
   }

   public ActiveAnimationComponent() {
   }

   public ActiveAnimationComponent(String[] activeAnimations) {
      System.arraycopy(activeAnimations, 0, this.activeAnimations, 0, activeAnimations.length);
   }

   public String[] getActiveAnimations() {
      return this.activeAnimations;
   }

   public void setPlayingAnimation(AnimationSlot slot, @Nullable String animation) {
      if (this.activeAnimations[slot.ordinal()] == null || !this.activeAnimations[slot.ordinal()].equals(animation)) {
         this.activeAnimations[slot.ordinal()] = animation;
      }
   }

   public boolean consumeNetworkOutdated() {
      boolean temp = this.isNetworkOutdated;
      this.isNetworkOutdated = false;
      return temp;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new ActiveAnimationComponent(this.activeAnimations);
   }
}
