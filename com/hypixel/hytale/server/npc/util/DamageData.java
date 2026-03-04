package com.hypixel.hytale.server.npc.util;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DamageData {
   private final Map<Ref<EntityStore>, Vector3d> kills = new HashMap<>();
   private final Object2DoubleMap<Ref<EntityStore>> damageInflicted = new Object2DoubleOpenHashMap();
   private final Object2DoubleMap<Ref<EntityStore>> damageSuffered = new Object2DoubleOpenHashMap();
   private final Object2DoubleMap<DamageCause> damageByCause = new Object2DoubleOpenHashMap();
   private double maxDamageSuffered;
   private double maxDamageInflicted;
   @Nullable
   private Ref<EntityStore> mostPersistentAttacker;
   @Nullable
   private Ref<EntityStore> mostDamagedVictim;

   public DamageData() {
      this.reset();
   }

   public void reset() {
      this.kills.clear();
      this.damageInflicted.clear();
      this.damageSuffered.clear();
      this.damageByCause.clear();
      this.maxDamageInflicted = 0.0;
      this.maxDamageSuffered = 0.0;
      this.mostPersistentAttacker = null;
      this.mostDamagedVictim = null;
   }

   public void onInflictedDamage(Ref<EntityStore> target, double amount) {
      double d = this.damageInflicted.mergeDouble(target, amount, Double::sum);
      if (d > this.maxDamageInflicted) {
         this.maxDamageInflicted = d;
         this.mostDamagedVictim = target;
      }
   }

   public void onSufferedDamage(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Damage damage) {
      this.damageByCause.mergeDouble(damage.getCause(), damage.getAmount(), Double::sum);
      if (damage.getSource() instanceof Damage.EntitySource) {
         Ref<EntityStore> ref = ((Damage.EntitySource)damage.getSource()).getRef();
         if (ref.isValid()) {
            Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
            if (playerComponent != null && playerComponent.getGameMode() == GameMode.Creative) {
               PlayerSettings playerSettingsComponent = commandBuffer.getComponent(ref, PlayerSettings.getComponentType());
               if (playerSettingsComponent == null || !playerSettingsComponent.creativeSettings().allowNPCDetection()) {
                  return;
               }
            }

            double damageByEntity = this.damageSuffered.mergeDouble(ref, damage.getAmount(), Double::sum);
            if (damageByEntity > this.maxDamageSuffered) {
               this.maxDamageSuffered = damageByEntity;
               this.mostPersistentAttacker = ref;
            }
         }
      }
   }

   public void onKill(@Nonnull Ref<EntityStore> victim, @Nonnull Vector3d position) {
      this.kills.put(victim, position);
   }

   public boolean haveKill() {
      return !this.kills.isEmpty();
   }

   public boolean haveKilled(Ref<EntityStore> entity) {
      return this.kills.containsKey(entity);
   }

   @Nullable
   public Ref<EntityStore> getAnyKilled() {
      if (this.kills.isEmpty()) {
         return null;
      } else {
         for (Ref<EntityStore> kill : this.kills.keySet()) {
            if (kill.isValid()) {
               return kill;
            }
         }

         return null;
      }
   }

   public Vector3d getKillPosition(Ref<EntityStore> entity) {
      return this.kills.get(entity);
   }

   public double getMaxDamageInflicted() {
      return this.maxDamageInflicted;
   }

   public double getMaxDamageSuffered() {
      return this.maxDamageSuffered;
   }

   public double getDamage(DamageCause cause) {
      return this.damageByCause.getDouble(cause);
   }

   public boolean hasSufferedDamage(DamageCause cause) {
      return this.damageByCause.containsKey(cause);
   }

   @Nullable
   public Ref<EntityStore> getMostDamagedVictim() {
      return this.mostDamagedVictim != null && this.mostDamagedVictim.isValid() ? this.mostDamagedVictim : null;
   }

   @Nullable
   public Ref<EntityStore> getMostDamagingAttacker() {
      return this.mostPersistentAttacker != null && this.mostPersistentAttacker.isValid() ? this.mostPersistentAttacker : null;
   }

   @Nullable
   public Ref<EntityStore> getAnyAttacker() {
      if (this.damageSuffered.isEmpty()) {
         return null;
      } else {
         ObjectIterator var1 = this.damageSuffered.keySet().iterator();

         while (var1.hasNext()) {
            Ref<EntityStore> attacker = (Ref<EntityStore>)var1.next();
            if (attacker.isValid()) {
               return attacker;
            }
         }

         return null;
      }
   }

   @Nonnull
   public DamageData clone() {
      DamageData damageData = new DamageData();
      damageData.kills.putAll(this.kills);
      damageData.damageInflicted.putAll(this.damageInflicted);
      damageData.damageSuffered.putAll(this.damageSuffered);
      damageData.damageByCause.putAll(this.damageByCause);
      damageData.maxDamageSuffered = this.maxDamageSuffered;
      damageData.maxDamageInflicted = this.maxDamageInflicted;
      damageData.mostPersistentAttacker = this.mostPersistentAttacker;
      damageData.mostDamagedVictim = this.mostDamagedVictim;
      return damageData;
   }

   @Nonnull
   @Override
   public String toString() {
      return "DamageData{kills="
         + this.kills
         + ", damageInflicted="
         + this.damageInflicted
         + ", damageSuffered="
         + this.damageSuffered
         + ", damageByCause="
         + this.damageByCause
         + ", maxDamageSuffered="
         + this.maxDamageSuffered
         + ", maxDamageInflicted="
         + this.maxDamageInflicted
         + ", mostPersistentAttacker="
         + this.mostPersistentAttacker
         + ", mostDamagedVictim="
         + this.mostDamagedVictim
         + "}";
   }
}
