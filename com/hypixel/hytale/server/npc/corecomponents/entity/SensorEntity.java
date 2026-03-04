package com.hypixel.hytale.server.npc.corecomponents.entity;

import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderSensorEntity;
import javax.annotation.Nonnull;

public class SensorEntity extends SensorEntityBase {
   protected final boolean getPlayers;
   protected final boolean getNPCs;
   protected final boolean excludeOwnType;

   public SensorEntity(@Nonnull BuilderSensorEntity builder, @Nonnull BuilderSupport builderSupport) {
      super(builder, builder.getPrioritiser(builderSupport), builderSupport);
      this.getPlayers = builder.isGetPlayers(builderSupport);
      this.getNPCs = builder.isGetNPCs(builderSupport);
      this.excludeOwnType = builder.isExcludeOwnType(builderSupport);
      this.initialisePrioritiser();
   }

   @Override
   public boolean isGetPlayers() {
      return this.getPlayers;
   }

   @Override
   public boolean isGetNPCs() {
      return this.getNPCs;
   }

   @Override
   public boolean isExcludingOwnType() {
      return this.excludeOwnType;
   }
}
