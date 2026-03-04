package com.hypixel.hytale.server.core.modules.entity.damage;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.dependency.SystemGroupDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.asset.type.gameplay.BrokenPenalties;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.meta.MetaKey;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.DamageEntityInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.DamageCalculator;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DamageCalculatorSystems {
   @Nonnull
   public static MetaKey<DamageCalculatorSystems.DamageSequence> DAMAGE_SEQUENCE = Damage.META_REGISTRY.registerMetaObject();

   @Nonnull
   public static Damage[] queueDamageCalculator(
      @Nonnull World world,
      @Nonnull Object2FloatMap<DamageCause> relativeDamage,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull Damage.Source source,
      @Nullable ItemStack itemInHand
   ) {
      Damage[] results = new Damage[relativeDamage.size()];
      int offset = 0;
      ObjectIterator var8 = relativeDamage.keySet().iterator();

      while (var8.hasNext()) {
         DamageCause damageCause = (DamageCause)var8.next();
         float damageAmount = relativeDamage.getFloat(damageCause);
         if (damageCause == DamageCause.PHYSICAL && itemInHand != null && itemInHand.isBroken()) {
            BrokenPenalties brokenPenalties = world.getGameplayConfig().getItemDurabilityConfig().getBrokenPenalties();
            damageAmount *= (float)(1.0 - brokenPenalties.getWeapon(0.0));
         }

         Damage damage = new Damage(source, damageCause, damageAmount);
         damage.getMetaStore().putMetaObject(Damage.CAN_BE_PREDICTED, Boolean.TRUE);
         results[offset++] = damage;
      }

      return results;
   }

   public static class DamageSequence {
      @Nonnull
      private final DamageCalculatorSystems.Sequence sequence;
      @Nonnull
      private final DamageCalculator damageCalculator;
      @Nullable
      private DamageEntityInteraction.EntityStatOnHit[] entityStatOnHit;

      public DamageSequence(@Nonnull DamageCalculatorSystems.Sequence sequence, @Nonnull DamageCalculator damageCalculator) {
         this.sequence = sequence;
         this.damageCalculator = damageCalculator;
      }

      public int getSequentialHits() {
         return this.sequence.hits;
      }

      public void addSequentialHit() {
         this.sequence.hits++;
      }

      @Nonnull
      public DamageCalculator getDamageCalculator() {
         return this.damageCalculator;
      }

      @Nullable
      public DamageEntityInteraction.EntityStatOnHit[] getEntityStatOnHit() {
         return this.entityStatOnHit;
      }

      public void setEntityStatOnHit(@Nullable DamageEntityInteraction.EntityStatOnHit[] entityStatOnHit) {
         this.entityStatOnHit = entityStatOnHit;
      }
   }

   public static class Sequence {
      @Nonnull
      public static final BuilderCodec<DamageCalculatorSystems.Sequence> CODEC = BuilderCodec.builder(
            DamageCalculatorSystems.Sequence.class, DamageCalculatorSystems.Sequence::new
         )
         .append(new KeyedCodec<>("Hits", Codec.INTEGER), (entityEffect, x) -> entityEffect.hits = x, entityEffect -> entityEffect.hits)
         .add()
         .build();
      private int hits;

      public Sequence() {
      }

      public Sequence(int hits) {
         this.hits = hits;
      }

      public int getHits() {
         return this.hits;
      }

      @Nonnull
      @Override
      public String toString() {
         return "Sequence{hits=" + this.hits + "}";
      }
   }

   public static class SequenceModifier extends DamageEventSystem {
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies = Set.of(
         new SystemGroupDependency<>(Order.AFTER, DamageModule.get().getGatherDamageGroup()),
         new SystemGroupDependency<>(Order.AFTER, DamageModule.get().getFilterDamageGroup()),
         new SystemDependency<EntityStore, DamageSystems.ApplyDamage>(Order.BEFORE, DamageSystems.ApplyDamage.class)
      );

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return Archetype.empty();
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull Damage damage
      ) {
         if (!(damage.getAmount() <= 0.0F)) {
            DamageCalculatorSystems.DamageSequence damageSequence = damage.getIfPresentMetaObject(DamageCalculatorSystems.DAMAGE_SEQUENCE);
            if (damageSequence != null) {
               DamageCalculator damageCalculator = damageSequence.getDamageCalculator();
               if (damageSequence.getSequentialHits() > 0) {
                  float sequentialModifier = Math.max(
                     1.0F - damageCalculator.getSequentialModifierStep() * damageSequence.getSequentialHits(), damageCalculator.getSequentialModifierMinimum()
                  );
                  damage.setAmount(damage.getAmount() * sequentialModifier);
               }

               damageSequence.addSequentialHit();
               DamageEntityInteraction.EntityStatOnHit[] entityStatsOnHit = damageSequence.getEntityStatOnHit();
               if (entityStatsOnHit != null && damage.getSource() instanceof Damage.EntitySource entitySource) {
                  Ref<EntityStore> sourceRef = entitySource.getRef();
                  EntityStatMap sourceEntityStatMapComponent = commandBuffer.getComponent(sourceRef, EntityStatMap.getComponentType());
                  if (sourceEntityStatMapComponent == null) {
                     return;
                  }

                  for (DamageEntityInteraction.EntityStatOnHit statOnHit : entityStatsOnHit) {
                     statOnHit.processEntityStatsOnHit(damageSequence.getSequentialHits(), sourceEntityStatMapComponent);
                  }
               }
            }
         }
      }
   }
}
