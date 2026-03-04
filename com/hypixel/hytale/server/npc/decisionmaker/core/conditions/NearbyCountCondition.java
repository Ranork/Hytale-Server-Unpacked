package com.hypixel.hytale.server.npc.decisionmaker.core.conditions;

import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderManager;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base.ScaledCurveCondition;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.PositionCache;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import javax.annotation.Nonnull;

public class NearbyCountCondition extends ScaledCurveCondition {
   public static final BuilderCodec<NearbyCountCondition> CODEC = BuilderCodec.builder(
         NearbyCountCondition.class, NearbyCountCondition::new, ScaledCurveCondition.ABSTRACT_CODEC
      )
      .documentation("A scaled curve condition that returns a utility value based on the number of NPCs nearby belonging to a specific **NPCGroup**.")
      .<Double>append(new KeyedCodec<>("Range", Codec.DOUBLE), (condition, d) -> condition.range = d, condition -> condition.range)
      .documentation("The range within which to count NPCs.")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .<String>append(new KeyedCodec<>("NPCGroup", Codec.STRING), (condition, s) -> condition.npcGroup = s, condition -> condition.npcGroup)
      .documentation("The NPCGroup to count NPCs from.")
      .addValidator(Validators.nonNull())
      .addValidator(NPCGroup.VALIDATOR_CACHE.getValidator())
      .add()
      .afterDecode(condition -> condition.npcGroupIndex = NPCGroup.getAssetMap().getIndex(condition.npcGroup))
      .build();
   protected double range;
   protected String npcGroup;
   protected int npcGroupIndex;
   protected boolean includePlayers;

   protected NearbyCountCondition() {
   }

   public double getRange() {
      return this.range;
   }

   public String getNpcGroup() {
      return this.npcGroup;
   }

   public int getNpcGroupIndex() {
      return this.npcGroupIndex;
   }

   @Override
   public void setupNPC(@Nonnull Role role) {
      PositionCache positionCache = role.getPositionCache();
      positionCache.requireEntityDistanceSorted(this.range);
      this.includePlayers = WorldSupport.hasTagInGroup(this.npcGroupIndex, BuilderManager.getPlayerGroupID());
      if (this.includePlayers) {
         positionCache.requirePlayerDistanceSorted(this.range);
      }
   }

   @Override
   protected double getInput(
      int selfIndex,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      Ref<EntityStore> target,
      CommandBuffer<EntityStore> commandBuffer,
      EvaluationContext context
   ) {
      NPCEntity selfNpcComponent = archetypeChunk.getComponent(selfIndex, NPCEntity.getComponentType());

      assert selfNpcComponent != null;

      PositionCache positionCache = selfNpcComponent.getRole().getPositionCache();
      return positionCache.countEntitiesInRange(
         0.0, this.range, this.includePlayers, NearbyCountCondition::filterNPC, selfNpcComponent.getRole(), this, commandBuffer
      );
   }

   protected static boolean filterNPC(
      @Nonnull Role role, Ref<EntityStore> ref, @Nonnull NearbyCountCondition _this, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return WorldSupport.isGroupMember(role.getRoleIndex(), ref, _this.npcGroupIndex, componentAccessor);
   }

   @Nonnull
   @Override
   public String toString() {
      return "NearbyCountCondition{range=" + this.range + ", npcGroup=" + this.npcGroup + ", npcGroupIndex=" + this.npcGroupIndex + "} " + super.toString();
   }
}
