package com.hypixel.hytale.server.npc.corecomponents.entity;

import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderSensorPlayer;
import javax.annotation.Nonnull;

public class SensorPlayer extends SensorEntityBase {
   public SensorPlayer(@Nonnull BuilderSensorPlayer builder, @Nonnull BuilderSupport builderSupport) {
      super(builder, builder.getPrioritiser(builderSupport), builderSupport);
      this.initialisePrioritiser();
   }

   @Override
   public boolean isGetPlayers() {
      return true;
   }

   @Override
   public boolean isGetNPCs() {
      return false;
   }
}
