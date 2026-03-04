package com.hypixel.hytale.server.npc.corecomponents.combat;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.combat.builders.BuilderSensorDamage;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.CombatSupport;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import com.hypixel.hytale.server.npc.sensorinfo.EntityPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.DamageData;
import javax.annotation.Nonnull;

public class SensorDamage extends SensorBase {
   protected final boolean combatDamage;
   protected final boolean friendlyDamage;
   protected final boolean drowningDamage;
   protected final boolean environmentDamage;
   protected final boolean otherDamage;
   protected final int targetSlot;
   protected final EntityPositionProvider positionProvider = new EntityPositionProvider();

   public SensorDamage(@Nonnull BuilderSensorDamage builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.combatDamage = builder.isCombatDamage();
      this.friendlyDamage = builder.isFriendlyDamage();
      this.drowningDamage = builder.isDrowningDamage();
      this.environmentDamage = builder.isEnvironmentDamage();
      this.otherDamage = builder.isOtherDamage();
      this.targetSlot = builder.getTargetSlot(support);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         this.positionProvider.clear();
         return false;
      } else {
         NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());

         assert npcComponent != null;

         CombatSupport combatSupport = role.getCombatSupport();
         DamageData damageData = npcComponent.getDamageData();
         if (this.combatDamage) {
            Ref<EntityStore> attackerRef = damageData.getMostDamagingAttacker();
            if (attackerRef == null) {
               attackerRef = damageData.getAnyAttacker();
            }

            attackerRef = this.positionProvider.setTarget(attackerRef, store);
            if (attackerRef != null) {
               if (this.friendlyDamage) {
                  int[] damageGroups = combatSupport.getDisableDamageGroups();
                  if (!WorldSupport.isGroupMember(npcComponent.getRoleIndex(), attackerRef, damageGroups, store)) {
                     return false;
                  }
               }

               if (this.targetSlot >= 0) {
                  role.getMarkedEntitySupport().setMarkedEntity(this.targetSlot, attackerRef);
               }

               return true;
            }

            if (damageData.hasSufferedDamage(DamageCause.PHYSICAL) || damageData.hasSufferedDamage(DamageCause.PROJECTILE)) {
               this.positionProvider.clear();
               return true;
            }
         }

         this.positionProvider.clear();
         return this.drowningDamage && damageData.hasSufferedDamage(DamageCause.DROWNING)
            || this.environmentDamage && damageData.hasSufferedDamage(DamageCause.ENVIRONMENT)
            || this.otherDamage
               && (
                  damageData.hasSufferedDamage(DamageCause.FALL)
                     || damageData.hasSufferedDamage(DamageCause.OUT_OF_WORLD)
                     || damageData.hasSufferedDamage(DamageCause.SUFFOCATION)
                     || damageData.hasSufferedDamage(DamageCause.COMMAND)
               );
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.positionProvider;
   }
}
