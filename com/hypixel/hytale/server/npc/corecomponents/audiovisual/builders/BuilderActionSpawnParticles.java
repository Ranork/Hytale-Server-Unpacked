package com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ParticleSystemExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.ActionSpawnParticles;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import javax.annotation.Nonnull;

public class BuilderActionSpawnParticles extends BuilderActionBase {
   protected final AssetHolder particleSystem = new AssetHolder();
   protected final DoubleHolder range = new DoubleHolder();
   protected final NumberArrayHolder offset = new NumberArrayHolder();
   protected final StringHolder targetNodeName = new StringHolder();
   protected final BooleanHolder isDetachedFromModel = new BooleanHolder();

   @Nonnull
   public ActionSpawnParticles build(@Nonnull BuilderSupport builderSupport) {
      return new ActionSpawnParticles(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Spawn particle system visible within a given range with an offset relative to npc heading";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.WorkInProgress;
   }

   @Nonnull
   public BuilderActionSpawnParticles readConfig(@Nonnull JsonElement data) {
      this.requireAsset(
         data, "ParticleSystem", this.particleSystem, ParticleSystemExistsValidator.required(), BuilderDescriptorState.Stable, "Particle system to spawn", null
      );
      this.getDouble(data, "Range", this.range, 75.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum visibility range", null);
      this.getVector3d(data, "Offset", this.offset, null, null, BuilderDescriptorState.Stable, "Offset relative to footpoint in view direction of NPC", null);
      this.getString(data, "TargetNodeName", this.targetNodeName, null, null, BuilderDescriptorState.Stable, "Target node name to position particles at", null);
      this.getBoolean(
         data, "IsDetachedFromModel", this.isDetachedFromModel, false, BuilderDescriptorState.Stable, "Whether to attach particles to the model", null
      );
      return this;
   }

   public String getParticleSystem(@Nonnull BuilderSupport support) {
      return this.particleSystem.get(support.getExecutionContext());
   }

   public double getRange(BuilderSupport support) {
      return this.range.get(support.getExecutionContext());
   }

   public Vector3d getOffset(BuilderSupport support) {
      return createVector3d(this.offset.get(support.getExecutionContext()), Vector3d.ZERO::clone);
   }

   public String getTargetNodeName(BuilderSupport support) {
      return this.targetNodeName.get(support.getExecutionContext());
   }

   public boolean isDetachedFromModel(BuilderSupport support) {
      return this.isDetachedFromModel.get(support.getExecutionContext());
   }
}
