package com.hypixel.hytale.server.npc.decisionmaker.core.conditions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base.ScaledCurveCondition;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TargetStatAbsoluteCondition extends ScaledCurveCondition {
   public static final BuilderCodec<TargetStatAbsoluteCondition> CODEC = BuilderCodec.builder(
         TargetStatAbsoluteCondition.class, TargetStatAbsoluteCondition::new, ScaledCurveCondition.ABSTRACT_CODEC
      )
      .documentation("A scaled curve condition that returns a utility value based on the absolute value of one of the target's stats.")
      .<String>appendInherited(
         new KeyedCodec<>("Stat", Codec.STRING),
         (condition, s) -> condition.stat = s,
         condition -> condition.stat,
         (condition, parent) -> condition.stat = parent.stat
      )
      .addValidator(Validators.nonNull())
      .addValidator(EntityStatType.VALIDATOR_CACHE.getValidator())
      .documentation("The stat to check.")
      .add()
      .afterDecode(condition -> condition.statIndex = EntityStatType.getAssetMap().getIndex(condition.stat))
      .build();
   protected String stat;
   protected int statIndex;

   protected TargetStatAbsoluteCondition() {
   }

   @Override
   protected double getInput(
      int selfIndex,
      ArchetypeChunk<EntityStore> archetypeChunk,
      @Nullable Ref<EntityStore> target,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      EvaluationContext context
   ) {
      if (target != null && target.isValid()) {
         EntityStatMap entityStatsMapComponent = commandBuffer.getComponent(target, EntityStatsModule.get().getEntityStatMapComponentType());

         assert entityStatsMapComponent != null;

         return Objects.requireNonNull(entityStatsMapComponent.get(this.statIndex)).get();
      } else {
         return Double.MAX_VALUE;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "TargetStatAbsoluteCondition{stat='" + this.stat + "', statIndex=" + this.statIndex + "}" + super.toString();
   }
}
