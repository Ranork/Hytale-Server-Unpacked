package com.hypixel.hytale.server.npc.role.support;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.builders.BuilderRole;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CombatSupport {
   public static final String ATTACK_TAG = "Attack";
   public static final int ATTACK_TAG_INDEX = AssetRegistry.getOrCreateTagIndex("Attack");
   public static final String AIMING_REFERENCE_TAG = "AimingReference";
   public static final int AIMING_REFERENCE_TAG_INDEX = AssetRegistry.getOrCreateTagIndex("AimingReference");
   public static final String MELEE_TAG = "Attack=Melee";
   public static final int MELEE_TAG_INDEX = AssetRegistry.getOrCreateTagIndex("Attack=Melee");
   public static final String RANGED_TAG = "Attack=Ranged";
   public static final int RANGED_TAG_INDEX = AssetRegistry.getOrCreateTagIndex("Attack=Ranged");
   public static final String BLOCK_TAG = "Attack=Block";
   public static final int BLOCK_TAG_INDEX = AssetRegistry.getOrCreateTagIndex("Attack=Block");
   protected final NPCEntity parent;
   protected final boolean disableDamageFlock;
   protected final int[] disableDamageGroups;
   @Nullable
   protected InteractionChain activeAttack;
   protected boolean dealFriendlyDamage;
   protected double attackPause;
   protected final List<String> attackOverrides = new ObjectArrayList();
   protected int attackOverrideIndex = -1;

   public CombatSupport(NPCEntity parent, @Nonnull BuilderRole builder, @Nonnull BuilderSupport support) {
      this.parent = parent;
      this.disableDamageFlock = builder.isDisableDamageFlock();
      this.disableDamageGroups = builder.getDisableDamageGroups(support);
   }

   public boolean isDealingFriendlyDamage() {
      return this.dealFriendlyDamage;
   }

   public int[] getDisableDamageGroups() {
      return this.disableDamageGroups;
   }

   public boolean isExecutingAttack() {
      return this.attackPause > 0.0 || this.activeAttack != null;
   }

   public void tick(double dt) {
      if (this.attackPause > 0.0) {
         this.attackPause -= dt;
      }

      if (this.activeAttack != null && this.activeAttack.getServerState() != InteractionState.NotFinished) {
         this.activeAttack = null;
      }
   }

   public boolean getCanCauseDamage(@Nonnull Ref<EntityStore> attackerRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.disableDamageFlock) {
         Ref<EntityStore> flockReference = FlockPlugin.getFlockReference(this.parent.getReference(), componentAccessor.getExternalData().getStore());
         if (flockReference != null) {
            EntityGroup entityGroupComponent = componentAccessor.getComponent(flockReference, EntityGroup.getComponentType());
            if (entityGroupComponent != null && entityGroupComponent.isMember(attackerRef)) {
               return false;
            }
         }
      }

      boolean friendlyDamage = false;
      NPCEntity npcComponent = componentAccessor.getComponent(attackerRef, NPCEntity.getComponentType());
      if (npcComponent != null) {
         friendlyDamage = npcComponent.getRole().getCombatSupport().isDealingFriendlyDamage();
      }

      return friendlyDamage || !WorldSupport.isGroupMember(this.parent.getRoleIndex(), attackerRef, this.disableDamageGroups, componentAccessor);
   }

   public void setExecutingAttack(InteractionChain chain, boolean damageFriendlies, double attackPause) {
      this.activeAttack = chain;
      this.dealFriendlyDamage = damageFriendlies;
      this.attackPause = attackPause;
   }

   public void addAttackOverride(String attackSequence) {
      this.attackOverrides.add(attackSequence);
      this.attackOverrideIndex = 0;
   }

   public void clearAttackOverrides() {
      this.attackOverrides.clear();
      this.attackOverrideIndex = -1;
   }

   @Nullable
   public String getNextAttackOverride() {
      if (this.attackOverrideIndex == -1) {
         return null;
      } else {
         int index = this.attackOverrideIndex;
         this.attackOverrideIndex = this.attackOverrideIndex < this.attackOverrides.size() - 1 ? this.attackOverrideIndex + 1 : 0;
         return this.attackOverrides.get(index);
      }
   }
}
