package com.hypixel.hytale.server.npc.corecomponents.entity.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.corecomponents.entity.SensorEntity;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorEntity extends BuilderSensorEntityBase {
   protected final BooleanHolder getPlayers = new BooleanHolder();
   protected final BooleanHolder getNPCs = new BooleanHolder();
   protected final BooleanHolder excludeOwnType = new BooleanHolder();

   @Nonnull
   public SensorEntity build(@Nonnull BuilderSupport builderSupport) {
      return new SensorEntity(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Test if entity matching specific attributes and filters is in range";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Return true if entity matching specific attributes and filters is in range. Target is entity.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      super.readConfig(data);
      this.getBoolean(data, "GetPlayers", this.getPlayers, false, BuilderDescriptorState.Stable, "Test players", null);
      this.getBoolean(data, "GetNPCs", this.getNPCs, true, BuilderDescriptorState.Stable, "Test mobs/NPCs", null);
      this.getBoolean(data, "ExcludeOwnType", this.excludeOwnType, true, BuilderDescriptorState.Stable, "Exclude NPCs of same type as current NPC", null);
      this.validateAny(this.getPlayers, this.getNPCs);
      return this;
   }

   public boolean isGetPlayers(@Nonnull BuilderSupport support) {
      return this.getPlayers.get(support.getExecutionContext());
   }

   public boolean isGetNPCs(@Nonnull BuilderSupport support) {
      return this.getNPCs.get(support.getExecutionContext());
   }

   public boolean isExcludeOwnType(@Nonnull BuilderSupport support) {
      return this.excludeOwnType.get(support.getExecutionContext());
   }
}
