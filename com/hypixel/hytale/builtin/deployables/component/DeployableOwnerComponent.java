package com.hypixel.hytale.builtin.deployables.component;

import com.hypixel.hytale.builtin.deployables.DeployablesPlugin;
import com.hypixel.hytale.builtin.deployables.config.DeployableConfig;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class DeployableOwnerComponent implements Component<EntityStore> {
   @Nonnull
   private final List<Pair<String, Ref<EntityStore>>> deployables = new ObjectArrayList();
   @Nonnull
   private final Object2IntMap<String> deployableCountPerId = new Object2IntOpenHashMap();
   @Nonnull
   private final List<Ref<EntityStore>> deployablesForDestruction = new ObjectArrayList();
   @Nonnull
   private final List<Pair<String, Ref<EntityStore>>> tempDestructionList = new ObjectArrayList();

   @Nonnull
   public static ComponentType<EntityStore, DeployableOwnerComponent> getComponentType() {
      return DeployablesPlugin.get().getDeployableOwnerComponentType();
   }

   private static int getMaxDeployablesForId(@Nonnull DeployableComponent comp) {
      return comp.getConfig().getMaxLiveCount();
   }

   private static int getMaxDeployablesGlobal(@Nonnull Store<EntityStore> store) {
      World world = store.getExternalData().getWorld();
      GameplayConfig gameplayConfig = world.getGameplayConfig();
      return gameplayConfig.getPlayerConfig().getMaxDeployableEntities();
   }

   public void tick(@Nonnull CommandBuffer<EntityStore> commandBuffer) {
      this.handleOverMaxDeployableDestruction(commandBuffer);
   }

   public void registerDeployable(
      @Nonnull Ref<EntityStore> owner,
      @Nonnull DeployableComponent deployableComp,
      @Nonnull String id,
      @Nonnull Ref<EntityStore> deployable,
      @Nonnull Store<EntityStore> store
   ) {
      this.deployables.add(Pair.of(id, deployable));
      this.incrementId(id);
      this.handlePerDeployableLimit(id, deployableComp);
      this.handleGlobalDeployableLimit(store, owner);
   }

   public void deRegisterDeployable(@Nonnull String id, @Nonnull Ref<EntityStore> deployable) {
      this.deployables.remove(Pair.of(id, deployable));
      this.decrementId(id);
   }

   private void incrementId(@Nonnull String id) {
      if (!this.deployableCountPerId.containsKey(id)) {
         this.deployableCountPerId.put(id, 1);
      } else {
         this.deployableCountPerId.put(id, this.deployableCountPerId.getInt(id) + 1);
      }
   }

   private void decrementId(@Nonnull String id) {
      if (!this.deployableCountPerId.containsKey(id)) {
         this.deployableCountPerId.put(id, 0);
      } else {
         this.deployableCountPerId.put(id, this.deployableCountPerId.getInt(id) - 1);
      }
   }

   private int getCurrentDeployablesById(@Nonnull String id) {
      return this.deployableCountPerId.getOrDefault(id, 0);
   }

   private void handlePerDeployableLimit(@Nonnull String id, @Nonnull DeployableComponent deployableComponent) {
      int limit = getMaxDeployablesForId(deployableComponent);
      int current = this.getCurrentDeployablesById(id);
      if (current > limit) {
         int diff = current - limit;
         this.tempDestructionList.clear();

         for (Pair<String, Ref<EntityStore>> deployablePair : this.deployables) {
            if (((String)deployablePair.key()).equals(id)) {
               this.deployablesForDestruction.add((Ref<EntityStore>)deployablePair.value());
               this.tempDestructionList.add(deployablePair);
               diff--;
            }

            if (diff <= 0) {
               break;
            }
         }

         this.deployables.removeAll(this.tempDestructionList);
         this.tempDestructionList.clear();
      }
   }

   private void handleGlobalDeployableLimit(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> owner) {
      int limit = 1;
      int current = 0;

      for (Pair<String, Ref<EntityStore>> deployablePair : this.deployables) {
         DeployableComponent deployableComponent = store.getComponent((Ref<EntityStore>)deployablePair.value(), DeployableComponent.getComponentType());

         assert deployableComponent != null;

         DeployableConfig deployableConfig = deployableComponent.getConfig();
         if (deployableConfig.getCountTowardsGlobalLimit()) {
            current++;
         }
      }

      if (current > 1) {
         int diff = current - 1;
         this.tempDestructionList.clear();

         for (Pair<String, Ref<EntityStore>> deployablePair : this.deployables) {
            Ref<EntityStore> deployableRef = (Ref<EntityStore>)deployablePair.value();
            DeployableComponent deployableComponentx = store.getComponent(deployableRef, DeployableComponent.getComponentType());

            assert deployableComponentx != null;

            DeployableConfig deployableConfig = deployableComponentx.getConfig();
            if (deployableConfig.getCountTowardsGlobalLimit()) {
               this.deployablesForDestruction.add(deployableRef);
               this.tempDestructionList.add(deployablePair);
               if (--diff <= 0) {
                  break;
               }
            }
         }

         this.deployables.removeAll(this.tempDestructionList);
         this.tempDestructionList.clear();
      }
   }

   private void handleOverMaxDeployableDestruction(@Nonnull CommandBuffer<EntityStore> commandBuffer) {
      if (!this.deployablesForDestruction.isEmpty()) {
         for (Ref<EntityStore> deployableEntityRef : this.deployablesForDestruction) {
            DeathComponent.tryAddComponent(commandBuffer, deployableEntityRef, new Damage(Damage.NULL_SOURCE, DamageCause.COMMAND, 0.0F));
         }

         this.deployablesForDestruction.clear();
      }
   }

   @Override
   public Component<EntityStore> clone() {
      return new KnockbackComponent();
   }
}
