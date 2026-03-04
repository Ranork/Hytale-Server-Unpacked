package com.hypixel.hytale.builtin.adventure.reputation;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.adventure.reputation.assets.ReputationGroup;
import com.hypixel.hytale.builtin.adventure.reputation.assets.ReputationRank;
import com.hypixel.hytale.builtin.adventure.reputation.choices.ReputationRequirement;
import com.hypixel.hytale.builtin.adventure.reputation.command.ReputationCommand;
import com.hypixel.hytale.builtin.adventure.reputation.store.ReputationDataResource;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceRequirement;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReputationPlugin extends JavaPlugin {
   private static ReputationPlugin instance;
   private ComponentType<EntityStore, ReputationGroupComponent> reputationGroupComponentType;
   private ResourceType<EntityStore, ReputationDataResource> reputationDataResourceType;
   private List<ReputationRank> reputationRanks;
   private int maxReputationValue = Integer.MIN_VALUE;
   private int minReputationValue = Integer.MAX_VALUE;
   public static final int NO_REPUTATION_GROUP = Integer.MIN_VALUE;

   public static ReputationPlugin get() {
      return instance;
   }

   public ReputationPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   public ComponentType<EntityStore, ReputationGroupComponent> getReputationGroupComponentType() {
      return this.reputationGroupComponentType;
   }

   @Override
   protected void setup() {
      instance = this;
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(ReputationRank.class, new DefaultAssetMap())
                     .setPath("NPC/Reputation/Ranks"))
                  .setCodec(ReputationRank.CODEC))
               .setKeyFunction(ReputationRank::getId))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(ReputationGroup.class, new DefaultAssetMap())
                     .setPath("NPC/Reputation/Groups"))
                  .setCodec(ReputationGroup.CODEC))
               .setKeyFunction(ReputationGroup::getId))
            .build()
      );
      this.getCommandRegistry().registerCommand(new ReputationCommand());
      ChoiceRequirement.CODEC.register("Reputation", ReputationRequirement.class, ReputationRequirement.CODEC);
      this.reputationDataResourceType = this.getEntityStoreRegistry()
         .registerResource(ReputationDataResource.class, "ReputationData", ReputationDataResource.CODEC);
      this.reputationGroupComponentType = this.getEntityStoreRegistry().registerComponent(ReputationGroupComponent.class, () -> {
         throw new UnsupportedOperationException("Not implemented!");
      });
      GameplayConfig.PLUGIN_CODEC.register(ReputationGameplayConfig.class, "Reputation", ReputationGameplayConfig.CODEC);
   }

   @Override
   protected void start() {
      this.reputationRanks = new ObjectArrayList(ReputationRank.getAssetMap().getAssetMap().values());
      if (this.reputationRanks.size() > 1) {
         this.reputationRanks.sort(Comparator.comparingInt(ReputationRank::getMinValue));
         int previousMaxValue = this.reputationRanks.getFirst().getMaxValue();

         for (int i = 1; i < this.reputationRanks.size(); i++) {
            ReputationRank reputationRank = this.reputationRanks.get(i);
            if (previousMaxValue < reputationRank.getMinValue()) {
               this.getLogger()
                  .at(Level.WARNING)
                  .log(
                     "There is a gap between the values of the ReputationRank %s and %s, please review the assets.",
                     reputationRank.getId(),
                     this.reputationRanks.get(i - 1).getId()
                  );
            }

            if (previousMaxValue > reputationRank.getMinValue()) {
               this.getLogger()
                  .at(Level.WARNING)
                  .log(
                     "Min value of rank %s is already contained in rank %s, please review the asset.",
                     reputationRank.getId(),
                     this.reputationRanks.get(i - 1).getId()
                  );
            }

            previousMaxValue = reputationRank.getMaxValue();
         }

         this.minReputationValue = this.reputationRanks.getFirst().getMinValue();
         this.maxReputationValue = this.reputationRanks.getLast().getMaxValue();
      }
   }

   public int changeReputation(@Nonnull Player player, @Nonnull Ref<EntityStore> npcRef, int value, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      ReputationGroupComponent reputationGroupComponent = componentAccessor.getComponent(npcRef, this.reputationGroupComponentType);
      return reputationGroupComponent == null
         ? Integer.MIN_VALUE
         : this.changeReputation(player, reputationGroupComponent.getReputationGroupId(), value, componentAccessor);
   }

   public int changeReputation(@Nonnull Player player, @Nonnull String reputationGroupId, int value, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      World world = componentAccessor.getExternalData().getWorld();
      ReputationGameplayConfig reputationGameplayConfig = ReputationGameplayConfig.getOrDefault(world.getGameplayConfig());
      if (reputationGameplayConfig.getReputationStorageType() == ReputationGameplayConfig.ReputationStorageType.PerPlayer) {
         ReputationGroup reputationGroup = ReputationGroup.getAssetMap().getAsset(reputationGroupId);
         if (reputationGroup == null) {
            return Integer.MIN_VALUE;
         } else {
            PlayerConfigData playerConfigData = player.getPlayerConfigData();
            Object2IntOpenHashMap<String> reputationData = new Object2IntOpenHashMap(playerConfigData.getReputationData());
            int newReputationValue = this.computeReputation(reputationData, reputationGroup, value);
            playerConfigData.setReputationData(reputationData);
            return newReputationValue;
         }
      } else {
         return this.changeReputation(world, reputationGroupId, value);
      }
   }

   public int changeReputation(@Nonnull World world, @Nonnull String reputationGroupId, int value) {
      ReputationGameplayConfig reputationGameplayConfig = ReputationGameplayConfig.getOrDefault(world.getGameplayConfig());
      if (reputationGameplayConfig.getReputationStorageType() != ReputationGameplayConfig.ReputationStorageType.PerWorld) {
         return -1;
      } else {
         ReputationGroup reputationGroup = ReputationGroup.getAssetMap().getAsset(reputationGroupId);
         if (reputationGroup == null) {
            return Integer.MIN_VALUE;
         } else {
            ReputationDataResource reputationDataResource = world.getEntityStore().getStore().getResource(this.reputationDataResourceType);
            return this.computeReputation(reputationDataResource.getReputationStats(), reputationGroup, value);
         }
      }
   }

   private int computeReputation(@Nonnull Object2IntMap<String> reputationData, @Nonnull ReputationGroup reputationGroup, int value) {
      return (Integer)reputationData.compute(reputationGroup.getId(), (k, oldValue) -> {
         int newValue = oldValue == null ? reputationGroup.getInitialReputationValue() + value : oldValue + value;
         return MathUtil.clamp(newValue, this.minReputationValue, this.maxReputationValue - 1);
      });
   }

   public int getReputationValue(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> playerEntityRef, @Nonnull Ref<EntityStore> npcEntityRef) {
      ReputationGroupComponent reputationGroupComponent = store.getComponent(npcEntityRef, this.reputationGroupComponentType);
      return reputationGroupComponent == null
         ? Integer.MIN_VALUE
         : this.getReputationValue(store, playerEntityRef, reputationGroupComponent.getReputationGroupId());
   }

   public int getReputationValue(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> playerEntityRef, @Nonnull String reputationGroupId) {
      World world = store.getExternalData().getWorld();
      Player playerComponent = store.getComponent(playerEntityRef, Player.getComponentType());
      if (playerComponent == null) {
         return Integer.MIN_VALUE;
      } else {
         ReputationGameplayConfig reputationGameplayConfig = ReputationGameplayConfig.getOrDefault(world.getGameplayConfig());
         if (reputationGameplayConfig.getReputationStorageType() == ReputationGameplayConfig.ReputationStorageType.PerPlayer) {
            ReputationGroup reputationGroup = ReputationGroup.getAssetMap().getAsset(reputationGroupId);
            if (reputationGroup != null) {
               Object2IntMap<String> reputationData = playerComponent.getPlayerConfigData().getReputationData();
               return this.getReputationValueForGroup(reputationData, reputationGroup);
            } else {
               return Integer.MIN_VALUE;
            }
         } else {
            return this.getReputationValue(store, reputationGroupId);
         }
      }
   }

   public int getReputationValue(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> npcRef) {
      String reputationGroupId = store.getComponent(npcRef, this.reputationGroupComponentType).getReputationGroupId();
      return this.getReputationValue(store, reputationGroupId);
   }

   public int getReputationValue(@Nonnull Store<EntityStore> store, @Nonnull String reputationGroupId) {
      World world = store.getExternalData().getWorld();
      ReputationGameplayConfig reputationGameplayConfig = ReputationGameplayConfig.getOrDefault(world.getGameplayConfig());
      if (reputationGameplayConfig.getReputationStorageType() != ReputationGameplayConfig.ReputationStorageType.PerWorld) {
         return Integer.MIN_VALUE;
      } else {
         ReputationGroup reputationGroup = ReputationGroup.getAssetMap().getAsset(reputationGroupId);
         if (reputationGroup == null) {
            return Integer.MIN_VALUE;
         } else {
            ReputationDataResource reputationDataResource = world.getEntityStore().getStore().getResource(this.reputationDataResourceType);
            Object2IntMap<String> reputationData = reputationDataResource.getReputationStats();
            return this.getReputationValueForGroup(reputationData, reputationGroup);
         }
      }
   }

   private int getReputationValueForGroup(@Nonnull Object2IntMap<String> reputationData, @Nonnull ReputationGroup reputationGroup) {
      return reputationData.getOrDefault(reputationGroup.getId(), reputationGroup.getInitialReputationValue());
   }

   @Nullable
   public ReputationRank getReputationRank(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> npcRef) {
      ReputationGroupComponent reputationGroupComponent = store.getComponent(npcRef, this.reputationGroupComponentType);
      if (reputationGroupComponent == null) {
         return null;
      } else {
         String reputationGroupId = reputationGroupComponent.getReputationGroupId();
         return this.getReputationRank(store, ref, reputationGroupId);
      }
   }

   @Nullable
   public ReputationRank getReputationRank(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull String reputationGroupId) {
      int value = this.getReputationValue(store, ref, reputationGroupId);
      return this.getReputationRankFromValue(value);
   }

   @Nullable
   public ReputationRank getReputationRankFromValue(int value) {
      if (value == Integer.MIN_VALUE) {
         return null;
      } else {
         for (int i = 0; i < this.reputationRanks.size(); i++) {
            if (this.reputationRanks.get(i).containsValue(value)) {
               return this.reputationRanks.get(i);
            }
         }

         return null;
      }
   }

   @Nullable
   public ReputationRank getReputationRank(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> npcRef) {
      World world = store.getExternalData().getWorld();
      ReputationGameplayConfig reputationGameplayConfig = ReputationGameplayConfig.getOrDefault(world.getGameplayConfig());
      if (reputationGameplayConfig.getReputationStorageType() != ReputationGameplayConfig.ReputationStorageType.PerWorld) {
         return null;
      } else {
         int value = this.getReputationValue(store, npcRef);
         return this.getReputationRankFromValue(value);
      }
   }

   @Nullable
   public Attitude getAttitude(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> npc) {
      ReputationRank reputationRank = this.getReputationRank(store, ref, npc);
      return reputationRank != null ? reputationRank.getAttitude() : null;
   }

   @Nullable
   public Attitude getAttitude(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> npcRef) {
      ReputationRank reputationRank = this.getReputationRank(store, npcRef);
      return reputationRank != null ? reputationRank.getAttitude() : null;
   }
}
