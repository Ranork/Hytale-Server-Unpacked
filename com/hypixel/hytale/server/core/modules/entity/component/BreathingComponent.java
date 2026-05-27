package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BreathingComponent implements Component<EntityStore> {
   @Nullable
   private BlockMaterial lastBreathingMaterial;
   private int lastFluidId = -1;
   private boolean suffocating;

   public static ComponentType<EntityStore, BreathingComponent> getComponentType() {
      return EntityModule.get().getBreathingComponentType();
   }

   @Nullable
   public BlockMaterial getLastBreathingMaterial() {
      return this.lastBreathingMaterial;
   }

   public void setLastBreathingMaterial(@Nonnull BlockMaterial lastBreathingMaterial) {
      this.lastBreathingMaterial = lastBreathingMaterial;
   }

   public int getLastFluidId() {
      return this.lastFluidId;
   }

   public void setLastFluidId(int lastFluidId) {
      this.lastFluidId = lastFluidId;
   }

   public boolean isSuffocating() {
      return this.suffocating;
   }

   public void setSuffocating(boolean suffocating) {
      this.suffocating = suffocating;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      BreathingComponent component = new BreathingComponent();
      component.lastBreathingMaterial = this.lastBreathingMaterial;
      component.lastFluidId = this.lastFluidId;
      component.suffocating = this.suffocating;
      return component;
   }
}
