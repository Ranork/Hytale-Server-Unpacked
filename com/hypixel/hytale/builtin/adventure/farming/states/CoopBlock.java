package com.hypixel.hytale.builtin.adventure.farming.states;

import com.hypixel.hytale.builtin.adventure.farming.FarmingPlugin;
import com.hypixel.hytale.builtin.adventure.farming.FarmingUtil;
import com.hypixel.hytale.builtin.adventure.farming.component.CoopResidentComponent;
import com.hypixel.hytale.builtin.adventure.farming.config.FarmingCoopAsset;
import com.hypixel.hytale.builtin.tagset.TagSetPlugin;
import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.range.IntRange;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDrop;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.EmptyItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.metadata.CapturedNPCMetadata;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import com.hypixel.hytale.server.spawning.SpawnTestResult;
import com.hypixel.hytale.server.spawning.SpawningContext;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CoopBlock implements Component<ChunkStore> {
   @Nonnull
   public static final String STATE_PRODUCE = "Produce_Ready";
   @Nonnull
   public static final BuilderCodec<CoopBlock> CODEC = BuilderCodec.builder(CoopBlock.class, CoopBlock::new)
      .append(new KeyedCodec<>("FarmingCoopId", Codec.STRING, true), (coop, s) -> coop.coopAssetId = s, coop -> coop.coopAssetId)
      .add()
      .append(
         new KeyedCodec<>("Residents", new ArrayCodec<>(CoopBlock.CoopResident.CODEC, CoopBlock.CoopResident[]::new)),
         (coop, residents) -> coop.residents = new ArrayList<>(Arrays.asList(residents)),
         coop -> coop.residents.toArray(CoopBlock.CoopResident[]::new)
      )
      .add()
      .append(new KeyedCodec<>("Storage", ItemContainer.CODEC), (coop, storage) -> coop.itemContainer = storage, coop -> coop.itemContainer)
      .add()
      .build();
   protected String coopAssetId;
   @Nonnull
   protected List<CoopBlock.CoopResident> residents = new ObjectArrayList();
   @Nonnull
   protected ItemContainer itemContainer = EmptyItemContainer.INSTANCE;

   public static ComponentType<ChunkStore, CoopBlock> getComponentType() {
      return FarmingPlugin.get().getCoopBlockStateComponentType();
   }

   public CoopBlock() {
      List<ItemStack> remainder = new ObjectArrayList();
      this.itemContainer = ItemContainer.ensureContainerCapacity(this.itemContainer, (short)5, SimpleItemContainer::new, remainder);
   }

   @Nullable
   public FarmingCoopAsset getCoopAsset() {
      return FarmingCoopAsset.getAssetMap().getAsset(this.coopAssetId);
   }

   public CoopBlock(@Nonnull String farmingCoopId, @Nonnull List<CoopBlock.CoopResident> residents, @Nonnull ItemContainer itemContainer) {
      this.coopAssetId = farmingCoopId;
      this.residents.addAll(residents);
      this.itemContainer = itemContainer.clone();
      List<ItemStack> remainder = new ObjectArrayList();
      this.itemContainer = ItemContainer.ensureContainerCapacity(this.itemContainer, (short)5, SimpleItemContainer::new, remainder);
   }

   public boolean tryPutResident(@Nonnull CapturedNPCMetadata metadata, @Nonnull WorldTimeResource worldTimeResource) {
      FarmingCoopAsset coopAsset = this.getCoopAsset();
      if (coopAsset == null) {
         return false;
      } else if (this.residents.size() >= coopAsset.getMaxResidents()) {
         return false;
      } else if (!this.getCoopAcceptsNPC(metadata.getNpcNameKey())) {
         return false;
      } else {
         this.residents.add(new CoopBlock.CoopResident(metadata, null, worldTimeResource.getGameTime()));
         return true;
      }
   }

   public boolean tryPutWildResidentFromWild(
      @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> entityRef, @Nonnull WorldTimeResource worldTimeResource, @Nonnull Vector3i coopLocation
   ) {
      FarmingCoopAsset coopAsset = this.getCoopAsset();
      if (coopAsset == null) {
         return false;
      } else {
         NPCEntity npcComponent = store.getComponent(entityRef, NPCEntity.getComponentType());
         if (npcComponent == null) {
            return false;
         } else {
            CoopResidentComponent coopResidentComponent = store.getComponent(entityRef, CoopResidentComponent.getComponentType());
            if (coopResidentComponent != null) {
               return false;
            } else if (!this.getCoopAcceptsNPC(npcComponent.getRoleName())) {
               return false;
            } else if (this.residents.size() >= coopAsset.getMaxResidents()) {
               return false;
            } else {
               coopResidentComponent = store.ensureAndGetComponent(entityRef, CoopResidentComponent.getComponentType());
               coopResidentComponent.setCoopLocation(coopLocation);
               UUIDComponent uuidComponent = store.getComponent(entityRef, UUIDComponent.getComponentType());
               if (uuidComponent == null) {
                  return false;
               } else {
                  PersistentRef persistentRef = new PersistentRef();
                  persistentRef.setEntity(entityRef, uuidComponent.getUuid());
                  CapturedNPCMetadata metadata = FarmingUtil.generateCapturedNPCMetadata(store, entityRef, npcComponent.getRoleName());
                  CoopBlock.CoopResident residentRecord = new CoopBlock.CoopResident(metadata, persistentRef, worldTimeResource.getGameTime());
                  residentRecord.deployedToWorld = true;
                  this.residents.add(residentRecord);
                  return true;
               }
            }
         }
      }
   }

   public boolean getCoopAcceptsNPC(String npcNameKey) {
      TagSetPlugin.TagSetLookup tagSetPlugin = TagSetPlugin.get(NPCGroup.class);
      int roleIndex = NPCPlugin.get().getIndex(npcNameKey);
      FarmingCoopAsset coopAsset = this.getCoopAsset();
      if (coopAsset == null) {
         return false;
      } else {
         int[] acceptedNpcGroupIndexes = coopAsset.getAcceptedNpcGroupIndexes();
         if (acceptedNpcGroupIndexes == null) {
            return true;
         } else {
            for (int group : acceptedNpcGroupIndexes) {
               if (tagSetPlugin.tagInSet(group, roleIndex)) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public void generateProduceToInventory(@Nonnull WorldTimeResource worldTimeResource) {
      Instant currentTime = worldTimeResource.getGameTime();
      FarmingCoopAsset coopAsset = this.getCoopAsset();
      if (coopAsset != null) {
         Map<String, String> produceDropsMap = coopAsset.getProduceDrops();
         if (!produceDropsMap.isEmpty()) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            List<ItemStack> generatedItemDrops = new ArrayList<>();

            for (CoopBlock.CoopResident resident : this.residents) {
               Instant lastProduced = resident.getLastProduced();
               if (lastProduced == null) {
                  resident.setLastProduced(currentTime);
               } else {
                  CapturedNPCMetadata residentMeta = resident.getMetadata();
                  String npcName = residentMeta.getNpcNameKey();
                  String npcDropListName = produceDropsMap.get(npcName);
                  if (npcDropListName != null) {
                     ItemDropList dropListAsset = ItemDropList.getAssetMap().getAsset(npcDropListName);
                     if (dropListAsset != null) {
                        Duration harvestDiff = Duration.between(lastProduced, currentTime);
                        long hoursSinceLastHarvest = harvestDiff.toHours();
                        int produceCount = MathUtil.ceil((float)hoursSinceLastHarvest / WorldTimeResource.HOURS_PER_DAY);
                        List<ItemDrop> configuredItemDrops = new ArrayList<>();

                        for (int i = 0; i < produceCount; i++) {
                           dropListAsset.getContainer().populateDrops(configuredItemDrops, random::nextDouble, npcDropListName);

                           for (ItemDrop drop : configuredItemDrops) {
                              if (drop != null && drop.getItemId() != null) {
                                 int amount = drop.getRandomQuantity(random);
                                 if (amount > 0) {
                                    generatedItemDrops.add(new ItemStack(drop.getItemId(), amount, drop.getMetadata()));
                                 }
                              } else {
                                 ((HytaleLogger.Api)HytaleLogger.forEnclosingClass().atWarning())
                                    .log("Tried to create ItemDrop for non-existent item in drop list id '%s'", npcDropListName);
                              }
                           }

                           configuredItemDrops.clear();
                        }

                        resident.setLastProduced(currentTime);
                     }
                  }
               }
            }

            this.itemContainer.addItemStacks(generatedItemDrops);
         }
      }
   }

   public void gatherProduceFromContainer(@Nonnull ItemContainer playerInventory) {
      for (ItemStack item : this.itemContainer.removeAllItemStacks()) {
         playerInventory.addItemStack(item);
      }
   }

   public void ensureSpawnResidentsInWorld(
      @Nonnull World world, @Nonnull Store<EntityStore> store, @Nonnull Vector3d coopLocation, @Nonnull Vector3d spawnOffset
   ) {
      FarmingCoopAsset coopAsset = this.getCoopAsset();
      if (coopAsset != null) {
         float radiansPerSpawn = (float) (Math.PI * 2) / coopAsset.getMaxResidents();
         Vector3d spawnOffsetIteration = spawnOffset;
         SpawningContext spawningContext = new SpawningContext();

         for (CoopBlock.CoopResident resident : this.residents) {
            CapturedNPCMetadata residentMeta = resident.getMetadata();
            String npcNameKey = residentMeta.getNpcNameKey();
            int npcRoleIndex = NPCPlugin.get().getIndex(npcNameKey);
            boolean residentDeployed = resident.getDeployedToWorld();
            PersistentRef residentEntityId = resident.getPersistentRef();
            if (!residentDeployed && residentEntityId == null) {
               Vector3d residentSpawnLocation = new Vector3d().assign(coopLocation).add(spawnOffsetIteration);
               Builder<Role> roleBuilder = NPCPlugin.get().tryGetCachedValidRole(npcRoleIndex);
               if (roleBuilder != null) {
                  spawningContext.setSpawnable((ISpawnableWithModel)roleBuilder);
                  if (spawningContext.set(world, residentSpawnLocation.x, residentSpawnLocation.y, residentSpawnLocation.z)
                     && spawningContext.canSpawn() == SpawnTestResult.TEST_OK) {
                     Pair<Ref<EntityStore>, NPCEntity> npcPair = NPCPlugin.get()
                        .spawnEntity(store, npcRoleIndex, spawningContext.newPosition(), Vector3f.ZERO, null, null);
                     if (npcPair == null) {
                        resident.setPersistentRef(null);
                        resident.setDeployedToWorld(false);
                     } else {
                        Ref<EntityStore> npcRef = (Ref<EntityStore>)npcPair.first();
                        NPCEntity npcComponent = (NPCEntity)npcPair.second();
                        npcComponent.getLeashPoint().assign(coopLocation);
                        if (npcRef != null && npcRef.isValid()) {
                           UUIDComponent uuidComponent = store.getComponent(npcRef, UUIDComponent.getComponentType());
                           if (uuidComponent == null) {
                              resident.setPersistentRef(null);
                              resident.setDeployedToWorld(false);
                           } else {
                              CoopResidentComponent coopResidentComponent = new CoopResidentComponent();
                              coopResidentComponent.setCoopLocation(coopLocation.toVector3i());
                              store.addComponent(npcRef, CoopResidentComponent.getComponentType(), coopResidentComponent);
                              PersistentRef persistentRef = new PersistentRef();
                              persistentRef.setEntity(npcRef, uuidComponent.getUuid());
                              resident.setPersistentRef(persistentRef);
                              resident.setDeployedToWorld(true);
                              spawnOffsetIteration = spawnOffsetIteration.rotateY(radiansPerSpawn);
                           }
                        } else {
                           resident.setPersistentRef(null);
                           resident.setDeployedToWorld(false);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void ensureNoResidentsInWorld(@Nonnull Store<EntityStore> store) {
      FarmingCoopAsset coopAsset = this.getCoopAsset();
      if (coopAsset != null) {
         ObjectArrayList<CoopBlock.CoopResident> residentsToRemove = new ObjectArrayList();

         for (CoopBlock.CoopResident resident : this.residents) {
            boolean deployed = resident.getDeployedToWorld();
            PersistentRef entityUuid = resident.getPersistentRef();
            if (deployed || entityUuid != null) {
               Ref<EntityStore> entityRef = entityUuid.getEntity(store);
               if (entityRef == null) {
                  residentsToRemove.add(resident);
               } else {
                  CoopResidentComponent coopResidentComponent = store.getComponent(entityRef, CoopResidentComponent.getComponentType());
                  if (coopResidentComponent == null) {
                     residentsToRemove.add(resident);
                  } else {
                     DeathComponent deathComponent = store.getComponent(entityRef, DeathComponent.getComponentType());
                     if (deathComponent != null) {
                        residentsToRemove.add(resident);
                     } else if (!this.getCoopAcceptsNPC(resident.metadata.getNpcNameKey())) {
                        residentsToRemove.add(resident);
                     } else {
                        coopResidentComponent.setMarkedForDespawn(true);
                        resident.setPersistentRef(null);
                        resident.setDeployedToWorld(false);
                     }
                  }
               }
            }
         }

         ObjectListIterator var11 = residentsToRemove.iterator();

         while (var11.hasNext()) {
            CoopBlock.CoopResident residentx = (CoopBlock.CoopResident)var11.next();
            this.residents.remove(residentx);
         }
      }
   }

   public boolean shouldResidentsBeInCoop(@Nonnull WorldTimeResource worldTimeResource) {
      FarmingCoopAsset coopAsset = this.getCoopAsset();
      if (coopAsset == null) {
         return true;
      } else {
         IntRange roamTimeRange = coopAsset.getResidentRoamTime();
         if (roamTimeRange == null) {
            return true;
         } else {
            int gameHour = worldTimeResource.getCurrentHour();
            return !roamTimeRange.includes(gameHour);
         }
      }
   }

   @Nullable
   public Instant getNextScheduledTick(@Nonnull WorldTimeResource worldTimeResource) {
      Instant gameTime = worldTimeResource.getGameTime();
      LocalDateTime gameDateTime = worldTimeResource.getGameDateTime();
      int gameHour = worldTimeResource.getCurrentHour();
      int minutes = gameDateTime.getMinute();
      FarmingCoopAsset coopAsset = this.getCoopAsset();
      if (coopAsset == null) {
         return null;
      } else {
         IntRange roamTimeRange = coopAsset.getResidentRoamTime();
         if (roamTimeRange == null) {
            return null;
         } else {
            int nextScheduledHour = 0;
            int minTime = roamTimeRange.getInclusiveMin();
            int maxTime = roamTimeRange.getInclusiveMax();
            if (coopAsset.getResidentRoamTime().includes(gameHour)) {
               nextScheduledHour = coopAsset.getResidentRoamTime().getInclusiveMax() + 1 - gameHour;
            } else if (gameHour > maxTime) {
               nextScheduledHour = WorldTimeResource.HOURS_PER_DAY - gameHour + minTime;
            } else {
               nextScheduledHour = minTime - gameHour;
            }

            return gameTime.plus(nextScheduledHour * 60L - minutes, ChronoUnit.MINUTES);
         }
      }
   }

   public void handleResidentDespawn(@Nonnull UUID entityUuid) {
      CoopBlock.CoopResident removedResident = null;

      for (CoopBlock.CoopResident resident : this.residents) {
         if (resident.persistentRef != null && resident.persistentRef.getUuid() == entityUuid) {
            removedResident = resident;
            break;
         }
      }

      if (removedResident != null) {
         this.residents.remove(removedResident);
      }
   }

   public void handleBlockBroken(
      @Nonnull World world, @Nonnull WorldTimeResource worldTimeResource, @Nonnull Store<EntityStore> store, int blockX, int blockY, int blockZ
   ) {
      Vector3i location = new Vector3i(blockX, blockY, blockZ);
      world.execute(() -> this.ensureSpawnResidentsInWorld(world, store, location.toVector3d(), new Vector3d().assign(Vector3d.FORWARD)));
      this.generateProduceToInventory(worldTimeResource);
      Vector3d dropPosition = new Vector3d(blockX + 0.5F, blockY, blockZ + 0.5F);
      Holder<EntityStore>[] itemEntityHolders = ItemComponent.generateItemDrops(store, this.itemContainer.removeAllItemStacks(), dropPosition, Vector3f.ZERO);
      if (itemEntityHolders.length > 0) {
         world.execute(() -> store.addEntities(itemEntityHolders, AddReason.SPAWN));
      }

      world.execute(() -> {
         for (CoopBlock.CoopResident resident : this.residents) {
            PersistentRef persistentRef = resident.getPersistentRef();
            if (persistentRef != null) {
               Ref<EntityStore> ref = persistentRef.getEntity(store);
               if (ref == null) {
                  return;
               }

               store.tryRemoveComponent(ref, CoopResidentComponent.getComponentType());
            }
         }
      });
   }

   public boolean hasProduce() {
      return !this.itemContainer.isEmpty();
   }

   @Override
   public Component<ChunkStore> clone() {
      return new CoopBlock(this.coopAssetId, this.residents, this.itemContainer);
   }

   public static class CoopResident {
      @Nonnull
      public static final BuilderCodec<CoopBlock.CoopResident> CODEC = BuilderCodec.builder(CoopBlock.CoopResident.class, CoopBlock.CoopResident::new)
         .append(new KeyedCodec<>("Metadata", CapturedNPCMetadata.CODEC), (coop, meta) -> coop.metadata = meta, coop -> coop.metadata)
         .add()
         .append(
            new KeyedCodec<>("PersistentRef", PersistentRef.CODEC), (coop, persistentRef) -> coop.persistentRef = persistentRef, coop -> coop.persistentRef
         )
         .add()
         .append(
            new KeyedCodec<>("DeployedToWorld", Codec.BOOLEAN), (coop, deployedToWorld) -> coop.deployedToWorld = deployedToWorld, coop -> coop.deployedToWorld
         )
         .add()
         .append(new KeyedCodec<>("LastHarvested", Codec.INSTANT), (coop, instant) -> coop.lastProduced = instant, coop -> coop.lastProduced)
         .add()
         .build();
      protected CapturedNPCMetadata metadata;
      @Nullable
      protected PersistentRef persistentRef;
      protected boolean deployedToWorld;
      protected Instant lastProduced;

      public CoopResident() {
      }

      public CoopResident(CapturedNPCMetadata metadata, @Nullable PersistentRef persistentRef, @Nonnull Instant lastProduced) {
         this.metadata = metadata;
         this.persistentRef = persistentRef;
         this.lastProduced = lastProduced;
      }

      public CapturedNPCMetadata getMetadata() {
         return this.metadata;
      }

      @Nullable
      public PersistentRef getPersistentRef() {
         return this.persistentRef;
      }

      public void setPersistentRef(@Nullable PersistentRef persistentRef) {
         this.persistentRef = persistentRef;
      }

      public boolean getDeployedToWorld() {
         return this.deployedToWorld;
      }

      public void setDeployedToWorld(boolean deployedToWorld) {
         this.deployedToWorld = deployedToWorld;
      }

      public Instant getLastProduced() {
         return this.lastProduced;
      }

      public void setLastProduced(Instant lastProduced) {
         this.lastProduced = lastProduced;
      }
   }
}
