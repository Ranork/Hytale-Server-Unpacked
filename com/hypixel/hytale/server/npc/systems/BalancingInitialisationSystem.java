package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsSystems;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import java.util.Set;
import javax.annotation.Nonnull;

public class BalancingInitialisationSystem extends HolderSystem<EntityStore> {
   @Nonnull
   private static final String NPC_MAX_MODIFIER = "NPC_Max";
   @Nonnull
   public static final String HEALTH_STAT_INDEX = "Health";
   @Nonnull
   private final ComponentType<EntityStore, NPCEntity> npcComponentType = NPCEntity.getComponentType();
   @Nonnull
   private final ComponentType<EntityStore, EntityStatMap> entityStatMapComponentType = EntityStatMap.getComponentType();
   @Nonnull
   private final Set<Dependency<EntityStore>> dependencies = Set.of(
      new SystemDependency<>(Order.AFTER, RoleBuilderSystem.class), new SystemDependency<>(Order.AFTER, EntityStatsSystems.Setup.class)
   );
   @Nonnull
   private final Query<EntityStore> query = Archetype.of(this.npcComponentType, this.entityStatMapComponentType);

   @Nonnull
   @Override
   public Set<Dependency<EntityStore>> getDependencies() {
      return this.dependencies;
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.query;
   }

   @Override
   public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
      NPCEntity npcComponent = holder.getComponent(this.npcComponentType);

      assert npcComponent != null;

      Role role = npcComponent.getRole();
      int initialMaxHealth = role.getInitialMaxHealth();
      EntityStatMap entityStatMapComponent = holder.getComponent(this.entityStatMapComponentType);

      assert entityStatMapComponent != null;

      int statIndex = EntityStatType.getAssetMap().getIndex("Health");
      EntityStatType asset = EntityStatType.getAssetMap().getAsset(statIndex);
      StaticModifier modifier = new StaticModifier(Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.ADDITIVE, initialMaxHealth - asset.getMax());
      entityStatMapComponent.putModifier(statIndex, "NPC_Max", modifier);
      entityStatMapComponent.maximizeStatValue(statIndex);
   }

   @Override
   public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
   }
}
