package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PositionDataComponent implements Component<EntityStore> {
   private int insideBlockTypeId = 0;
   private int standingOnBlockTypeId = 0;

   public static ComponentType<EntityStore, PositionDataComponent> getComponentType() {
      return EntityModule.get().getPositionDataComponentType();
   }

   public PositionDataComponent() {
   }

   public PositionDataComponent(int insideBlockTypeId, int standingOnBlockTypeId) {
      this.insideBlockTypeId = insideBlockTypeId;
      this.standingOnBlockTypeId = standingOnBlockTypeId;
   }

   public int getInsideBlockTypeId() {
      return this.insideBlockTypeId;
   }

   public void setInsideBlockTypeId(int insideBlockTypeId) {
      this.insideBlockTypeId = insideBlockTypeId;
   }

   public int getStandingOnBlockTypeId() {
      return this.standingOnBlockTypeId;
   }

   public void setStandingOnBlockTypeId(int standingOnBlockTypeId) {
      this.standingOnBlockTypeId = standingOnBlockTypeId;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new PositionDataComponent(this.insideBlockTypeId, this.standingOnBlockTypeId);
   }
}
