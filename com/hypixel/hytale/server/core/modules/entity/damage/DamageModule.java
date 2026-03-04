package com.hypixel.hytale.server.core.modules.entity.damage;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemGroupDependency;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackSystems;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.damage.commands.DesyncDamageCommand;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entityui.EntityUIModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import javax.annotation.Nonnull;

public class DamageModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(DamageModule.class)
      .depends(EntityModule.class)
      .depends(EntityStatsModule.class)
      .depends(EntityUIModule.class)
      .build();
   private static DamageModule instance;
   private ComponentType<EntityStore, DeathComponent> deathComponentType;
   private ComponentType<EntityStore, DeferredCorpseRemoval> deferredCorpseRemovalComponentType;
   private SystemGroup<EntityStore> gatherDamageGroup;
   private SystemGroup<EntityStore> filterDamageGroup;
   private SystemGroup<EntityStore> inspectDamageGroup;

   public static DamageModule get() {
      return instance;
   }

   public DamageModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      this.deathComponentType = entityStoreRegistry.registerComponent(DeathComponent.class, "Death", DeathComponent.CODEC);
      this.deferredCorpseRemovalComponentType = entityStoreRegistry.registerComponent(DeferredCorpseRemoval.class, () -> {
         throw new UnsupportedOperationException("not supported");
      });
      this.gatherDamageGroup = entityStoreRegistry.registerSystemGroup();
      this.filterDamageGroup = entityStoreRegistry.registerSystemGroup();
      this.inspectDamageGroup = entityStoreRegistry.registerSystemGroup();
      entityStoreRegistry.registerSystem(new DamageModule.OrderGatherFilter());
      entityStoreRegistry.registerSystem(new DamageSystems.ApplyDamage());
      entityStoreRegistry.registerSystem(new DamageSystems.CanBreathe());
      entityStoreRegistry.registerSystem(new DamageSystems.OutOfWorldDamage());
      entityStoreRegistry.registerSystem(new DamageSystems.FallDamagePlayers());
      entityStoreRegistry.registerSystem(new DamageSystems.FallDamageNPCs());
      entityStoreRegistry.registerSystem(new DamageSystems.FilterPlayerWorldConfig());
      entityStoreRegistry.registerSystem(new DamageSystems.FilterNPCWorldConfig());
      entityStoreRegistry.registerSystem(new DamageSystems.FilterUnkillable());
      entityStoreRegistry.registerSystem(new DamageSystems.PlayerDamageFilterSystem());
      entityStoreRegistry.registerSystem(new DamageSystems.WieldingDamageReduction());
      entityStoreRegistry.registerSystem(new DamageSystems.WieldingKnockbackReduction());
      entityStoreRegistry.registerSystem(new DamageSystems.ArmorKnockbackReduction());
      entityStoreRegistry.registerSystem(new DamageSystems.ArmorDamageReduction());
      entityStoreRegistry.registerSystem(new DamageSystems.HackKnockbackValues());
      entityStoreRegistry.registerSystem(new DamageSystems.RecordLastCombat());
      entityStoreRegistry.registerSystem(new DamageSystems.ApplyParticles());
      entityStoreRegistry.registerSystem(new DamageSystems.ApplySoundEffects());
      entityStoreRegistry.registerSystem(new DamageSystems.HitAnimation());
      entityStoreRegistry.registerSystem(new DamageSystems.TrackLastDamage());
      entityStoreRegistry.registerSystem(new DamageSystems.DamageArmor());
      entityStoreRegistry.registerSystem(new DamageSystems.DamageStamina());
      entityStoreRegistry.registerSystem(new DamageSystems.DamageAttackerTool());
      entityStoreRegistry.registerSystem(new DamageSystems.PlayerHitIndicators());
      entityStoreRegistry.registerSystem(new DamageSystems.ReticleEvents());
      entityStoreRegistry.registerSystem(new DamageSystems.EntityUIEvents());
      entityStoreRegistry.registerSystem(new KnockbackSystems.ApplyKnockback());
      entityStoreRegistry.registerSystem(new KnockbackSystems.ApplyPlayerKnockback());
      entityStoreRegistry.registerSystem(new DeathSystems.ClearHealth());
      entityStoreRegistry.registerSystem(new DeathSystems.ClearInteractions());
      entityStoreRegistry.registerSystem(new DeathSystems.ClearEntityEffects());
      entityStoreRegistry.registerSystem(new DeathSystems.PlayerKilledPlayer());
      entityStoreRegistry.registerSystem(new DeathSystems.DropPlayerDeathItems());
      entityStoreRegistry.registerSystem(new DeathSystems.PlayerDropItemsConfig());
      entityStoreRegistry.registerSystem(new DeathSystems.RunDeathInteractions());
      entityStoreRegistry.registerSystem(new DeathSystems.KillFeed());
      entityStoreRegistry.registerSystem(new DeathSystems.PlayerDeathScreen());
      entityStoreRegistry.registerSystem(new DeathSystems.PlayerDeathMarker());
      entityStoreRegistry.registerSystem(new DeathSystems.CorpseRemoval());
      entityStoreRegistry.registerSystem(new DeathSystems.DeathAnimation());
      entityStoreRegistry.registerSystem(new DeathSystems.SpawnedDeathAnimation());
      entityStoreRegistry.registerSystem(new RespawnSystems.ResetStatsRespawnSystem());
      entityStoreRegistry.registerSystem(new RespawnSystems.ResetPlayerRespawnSystem());
      entityStoreRegistry.registerSystem(new RespawnSystems.ClearEntityEffectsRespawnSystem());
      entityStoreRegistry.registerSystem(new RespawnSystems.ClearInteractionsRespawnSystem());
      entityStoreRegistry.registerSystem(new RespawnSystems.CheckBrokenItemsRespawnSystem());
      entityStoreRegistry.registerSystem(new RespawnSystems.ClearRespawnUI());
      entityStoreRegistry.registerSystem(new DamageCalculatorSystems.SequenceModifier());
      this.getCommandRegistry().registerCommand(new DesyncDamageCommand());
   }

   public ComponentType<EntityStore, DeathComponent> getDeathComponentType() {
      return this.deathComponentType;
   }

   public ComponentType<EntityStore, DeferredCorpseRemoval> getDeferredCorpseRemovalComponentType() {
      return this.deferredCorpseRemovalComponentType;
   }

   public SystemGroup<EntityStore> getGatherDamageGroup() {
      return this.gatherDamageGroup;
   }

   public SystemGroup<EntityStore> getFilterDamageGroup() {
      return this.filterDamageGroup;
   }

   public SystemGroup<EntityStore> getInspectDamageGroup() {
      return this.inspectDamageGroup;
   }

   @Deprecated
   public static class OrderGatherFilter implements ISystem<EntityStore> {
      private final Set<Dependency<EntityStore>> dependencies = Set.of(
         new SystemGroupDependency<>(Order.AFTER, DamageModule.get().getGatherDamageGroup()),
         new SystemGroupDependency<>(Order.BEFORE, DamageModule.get().getFilterDamageGroup())
      );

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }
   }
}
