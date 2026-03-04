package com.hypixel.hytale.server.core.modules.interaction;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockSoundEvent;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocksound.config.BlockSoundSet;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockBreakingDropType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockGathering;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.HarvestingDropType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.PhysicsDropType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.SoftBlockDropType;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.type.gameplay.BrokenPenalties;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.asset.type.gameplay.GatheringEffectsConfig;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTool;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemToolSpec;
import com.hypixel.hytale.server.core.blocktype.component.BlockPhysics;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealth;
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealthChunk;
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealthModule;
import com.hypixel.hytale.server.core.modules.blockset.BlockSetModule;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.ConnectedBlocksUtil;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockHarvestUtils {
   @Nullable
   public static ItemToolSpec getSpecPowerDamageBlock(@Nullable Item item, @Nullable BlockType blockType, @Nullable ItemTool tool) {
      if (blockType == null) {
         return null;
      } else {
         BlockGathering gathering = blockType.getGathering();
         if (gathering == null) {
            return null;
         } else {
            BlockBreakingDropType breaking = gathering.getBreaking();
            if (breaking == null) {
               return null;
            } else {
               String gatherType = breaking.getGatherType();
               if (gatherType == null) {
                  return null;
               } else if (item == null || item.getWeapon() == null && item.getBuilderToolData() == null) {
                  int requiredQuality = breaking.getQuality();
                  if (tool == null) {
                     ItemToolSpec defaultSpec = ItemToolSpec.getAssetMap().getAsset(gatherType);
                     return defaultSpec != null && defaultSpec.getQuality() < requiredQuality ? null : defaultSpec;
                  } else {
                     if (tool.getSpecs() != null) {
                        for (ItemToolSpec spec : tool.getSpecs()) {
                           if (Objects.equals(spec.getGatherType(), gatherType)) {
                              if (spec.getQuality() < requiredQuality) {
                                 return null;
                              }

                              return spec;
                           }
                        }
                     }

                     return null;
                  }
               } else {
                  return null;
               }
            }
         }
      }
   }

   public static double calculateDurabilityUse(@Nonnull Item item, @Nullable BlockType blockType) {
      if (blockType == null) {
         return 0.0;
      } else if (blockType.getGathering().isSoft()) {
         return 0.0;
      } else if (item.getTool() == null) {
         return 0.0;
      } else {
         ItemTool itemTool = item.getTool();
         ItemTool.DurabilityLossBlockTypes[] durabilityLossBlockTypes = itemTool.getDurabilityLossBlockTypes();
         if (durabilityLossBlockTypes == null) {
            return item.getDurabilityLossOnHit();
         } else {
            String hitBlockTypeId = blockType.getId();
            int hitBlockTypeIndex = BlockType.getAssetMap().getIndex(hitBlockTypeId);
            if (hitBlockTypeIndex == Integer.MIN_VALUE) {
               throw new IllegalArgumentException("Unknown key! " + hitBlockTypeId);
            } else {
               BlockSetModule blockSetModule = BlockSetModule.getInstance();

               for (ItemTool.DurabilityLossBlockTypes durabilityLossBlockType : durabilityLossBlockTypes) {
                  int[] blockTypeIndexes = durabilityLossBlockType.getBlockTypeIndexes();
                  if (blockTypeIndexes != null) {
                     for (int blockTypeIndex : blockTypeIndexes) {
                        if (blockTypeIndex == hitBlockTypeIndex) {
                           return durabilityLossBlockType.getDurabilityLossOnHit();
                        }
                     }
                  }

                  int[] blockSetIndexes = durabilityLossBlockType.getBlockSetIndexes();
                  if (blockSetIndexes != null) {
                     for (int blockSetIndex : blockSetIndexes) {
                        if (blockSetModule.blockInSet(blockSetIndex, hitBlockTypeId)) {
                           return durabilityLossBlockType.getDurabilityLossOnHit();
                        }
                     }
                  }
               }

               return item.getDurabilityLossOnHit();
            }
         }
      }
   }

   public static boolean performBlockDamage(
      @Nonnull Vector3i targetBlock,
      @Nullable ItemStack itemStack,
      @Nullable ItemTool tool,
      float damageScale,
      int setBlockSettings,
      @Nonnull Ref<ChunkStore> chunkReference,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull ComponentAccessor<ChunkStore> chunkStore
   ) {
      return performBlockDamage(null, null, targetBlock, itemStack, tool, null, false, damageScale, setBlockSettings, chunkReference, commandBuffer, chunkStore);
   }

   public static boolean performBlockDamage(
      @Nullable LivingEntity entity,
      @Nullable Ref<EntityStore> ref,
      @Nonnull Vector3i targetBlockPos,
      @Nullable ItemStack itemStack,
      @Nullable ItemTool tool,
      @Nullable String toolId,
      boolean matchTool,
      float damageScale,
      int setBlockSettings,
      @Nonnull Ref<ChunkStore> chunkReference,
      @Nonnull ComponentAccessor<EntityStore> entityStore,
      @Nonnull ComponentAccessor<ChunkStore> chunkStore
   ) {
      World world = entityStore.getExternalData().getWorld();
      GameplayConfig gameplayConfig = world.getGameplayConfig();
      WorldChunk worldChunkComponent = chunkStore.getComponent(chunkReference, WorldChunk.getComponentType());
      if (worldChunkComponent == null) {
         return false;
      } else {
         BlockChunk blockChunkComponent = chunkStore.getComponent(chunkReference, BlockChunk.getComponentType());

         assert blockChunkComponent != null;

         BlockSection targetSection = blockChunkComponent.getSectionAtBlockY(targetBlockPos.y);
         int targetRotationIndex = targetSection.getRotationIndex(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z);
         boolean brokeBlock = false;
         int environmentId = blockChunkComponent.getEnvironment(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z);
         Environment environmentAsset = Environment.getAssetMap().getAsset(environmentId);
         SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = entityStore.getResource(EntityModule.get().getPlayerSpatialResourceType());
         if (environmentAsset != null && !environmentAsset.isBlockModificationAllowed()) {
            targetSection.invalidateBlock(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z);
            return false;
         } else {
            BlockType targetBlockType = worldChunkComponent.getBlockType(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z);
            if (targetBlockType == null) {
               return false;
            } else {
               BlockGathering blockGathering = targetBlockType.getGathering();
               if (blockGathering == null) {
                  return false;
               } else if (matchTool && !blockGathering.getToolData().containsKey(toolId)) {
                  return false;
               } else {
                  Vector3d targetBlockCenterPos = new Vector3d();
                  targetBlockType.getBlockCenter(targetRotationIndex, targetBlockCenterPos);
                  targetBlockCenterPos.add(targetBlockPos);
                  Vector3i originBlock = new Vector3i(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z);
                  if (!targetBlockType.isUnknown()) {
                     int filler = targetSection.getFiller(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z);
                     int fillerX = FillerBlockUtil.unpackX(filler);
                     int fillerY = FillerBlockUtil.unpackY(filler);
                     int fillerZ = FillerBlockUtil.unpackZ(filler);
                     if (fillerX != 0 || fillerY != 0 || fillerZ != 0) {
                        originBlock = originBlock.clone().subtract(fillerX, fillerY, fillerZ);
                        String oldBlockTypeKey = targetBlockType.getId();
                        targetBlockType = world.getBlockType(originBlock.getX(), originBlock.getY(), originBlock.getZ());
                        if (targetBlockType == null) {
                           return false;
                        }

                        if (!oldBlockTypeKey.equals(targetBlockType.getId())) {
                           worldChunkComponent.breakBlock(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z);
                           return true;
                        }

                        blockGathering = targetBlockType.getGathering();
                        if (blockGathering == null) {
                           return false;
                        }
                     }
                  }

                  Item heldItem = itemStack != null ? itemStack.getItem() : null;
                  if (tool == null && heldItem != null) {
                     tool = heldItem.getTool();
                  }

                  ItemToolSpec itemToolSpec = getSpecPowerDamageBlock(heldItem, targetBlockType, tool);
                  float specPower = itemToolSpec != null ? itemToolSpec.getPower() : 0.0F;
                  boolean canApplyItemStackPenalties = entity != null && entity.canApplyItemStackPenalties(ref, entityStore);
                  if (specPower != 0.0F && heldItem != null && heldItem.getTool() != null && itemStack.isBroken() && canApplyItemStackPenalties) {
                     BrokenPenalties brokenPenalties = gameplayConfig.getItemDurabilityConfig().getBrokenPenalties();
                     specPower *= 1.0F - (float)brokenPenalties.getTool(0.0);
                  }

                  int dropQuantity = 1;
                  String itemId;
                  String dropListId;
                  float damage;
                  if (specPower != 0.0F) {
                     BlockBreakingDropType breaking = blockGathering.getBreaking();
                     damage = specPower;
                     dropQuantity = breaking.getQuantity();
                     itemId = breaking.getItemId();
                     dropListId = breaking.getDropListId();
                  } else {
                     if (!blockGathering.isSoft()) {
                        if (heldItem != null && heldItem.getWeapon() == null) {
                           if (ref != null && entity != null) {
                              GatheringEffectsConfig unbreakableBlockConfig = gameplayConfig.getGatheringConfig().getUnbreakableBlockConfig();
                              if ((setBlockSettings & 4) == 0) {
                                 String particleSystemId = unbreakableBlockConfig.getParticleSystemId();
                                 if (particleSystemId != null) {
                                    ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
                                    playerSpatialResource.getSpatialStructure().collect(targetBlockCenterPos, 75.0, results);
                                    ParticleUtil.spawnParticleEffect(particleSystemId, targetBlockCenterPos, results, entityStore);
                                 }
                              }

                              if ((setBlockSettings & 1024) == 0) {
                                 int soundEventIndex = unbreakableBlockConfig.getSoundEventIndex();
                                 if (soundEventIndex != 0) {
                                    SoundUtil.playSoundEvent3d(ref, soundEventIndex, targetBlockCenterPos, entityStore);
                                 }

                                 if (heldItem.getTool() != null) {
                                    int hitSoundEventLayerIndex = heldItem.getTool().getIncorrectMaterialSoundLayerIndex();
                                    if (hitSoundEventLayerIndex != 0) {
                                       SoundUtil.playSoundEvent3d(ref, hitSoundEventLayerIndex, targetBlockCenterPos, entityStore);
                                    }
                                 }
                              }
                           }

                           return false;
                        }

                        return false;
                     }

                     SoftBlockDropType soft = blockGathering.getSoft();
                     if (!soft.isWeaponBreakable() && heldItem != null && heldItem.getWeapon() != null) {
                        return false;
                     }

                     damage = 1.0F;
                     itemId = soft.getItemId();
                     dropListId = soft.getDropListId();
                     damageScale = 1.0F;
                  }

                  damage *= damageScale;
                  ChunkColumn chunkColumnComponent = chunkStore.getComponent(chunkReference, ChunkColumn.getComponentType());
                  Ref<ChunkStore> chunkSectionRef = chunkColumnComponent != null
                     ? chunkColumnComponent.getSection(ChunkUtil.chunkCoordinate(targetBlockPos.y))
                     : null;
                  if (targetBlockType.getGathering().shouldUseDefaultDropWhenPlaced()) {
                     BlockPhysics decoBlocks = chunkSectionRef != null ? chunkStore.getComponent(chunkSectionRef, BlockPhysics.getComponentType()) : null;
                     boolean isDeco = decoBlocks != null && decoBlocks.isDeco(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z);
                     if (isDeco) {
                        itemId = null;
                        dropListId = null;
                     }
                  }

                  TimeResource timeResource = entityStore.getResource(TimeResource.getResourceType());
                  BlockHealthChunk blockHealthComponent = chunkStore.getComponent(chunkReference, BlockHealthModule.get().getBlockHealthChunkComponentType());

                  assert blockHealthComponent != null;

                  float current = blockHealthComponent.getBlockHealth(originBlock);
                  DamageBlockEvent event = new DamageBlockEvent(itemStack, originBlock, targetBlockType, current, damage);
                  if (ref != null) {
                     entityStore.invoke(ref, event);
                  } else {
                     entityStore.invoke(event);
                  }

                  if (event.isCancelled()) {
                     targetSection.invalidateBlock(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z);
                     return false;
                  } else {
                     damage = event.getDamage();
                     targetBlockType = event.getBlockType();
                     targetBlockPos = event.getTargetBlock();
                     targetSection = blockChunkComponent.getSectionAtBlockY(targetBlockPos.y);
                     targetRotationIndex = targetSection.getRotationIndex(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z);
                     targetBlockType.getBlockCenter(targetRotationIndex, targetBlockCenterPos);
                     targetBlockCenterPos.add(targetBlockPos);
                     BlockHealth blockDamage = blockHealthComponent.damageBlock(timeResource.getNow(), world, targetBlockPos, damage);
                     if (blockHealthComponent.isBlockFragile(targetBlockPos) || blockDamage.isDestroyed()) {
                        BlockGathering.BlockToolData requiredTool = blockGathering.getToolData().get(toolId);
                        boolean toolsMatch = requiredTool != null;
                        if (!toolsMatch) {
                           performBlockBreak(
                              world,
                              targetBlockPos,
                              targetBlockType,
                              itemStack,
                              dropQuantity,
                              itemId,
                              dropListId,
                              setBlockSettings,
                              ref,
                              chunkReference,
                              entityStore,
                              chunkStore
                           );
                           brokeBlock = true;
                        } else {
                           String toolStateId = requiredTool.getStateId();
                           BlockType newBlockType = toolStateId != null ? targetBlockType.getBlockForState(toolStateId) : null;
                           boolean shouldChangeState = newBlockType != null && !targetBlockType.getId().equals(newBlockType.getId());
                           if (shouldChangeState) {
                              blockDamage.setHealth(1.0F);
                              worldChunkComponent.setBlock(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z, newBlockType);
                              if ((setBlockSettings & 1024) == 0) {
                                 BlockSoundSet soundSet = BlockSoundSet.getAssetMap().getAsset(targetBlockType.getBlockSoundSetIndex());
                                 if (soundSet != null) {
                                    int soundEventIndexx = soundSet.getSoundEventIndices().getOrDefault(BlockSoundEvent.Break, 0);
                                    if (soundEventIndexx != 0) {
                                       SoundUtil.playSoundEvent3d(soundEventIndexx, SoundCategory.SFX, targetBlockCenterPos, entityStore);
                                    }
                                 }
                              }

                              if ((setBlockSettings & 2048) == 0) {
                                 List<ItemStack> itemStacks = getDrops(targetBlockType, 1, requiredTool.getItemId(), requiredTool.getDropListId());
                                 Vector3d dropPosition = new Vector3d(targetBlockPos.x + 0.5, targetBlockPos.y, targetBlockPos.z + 0.5);
                                 Holder<EntityStore>[] itemEntityHolders = ItemComponent.generateItemDrops(entityStore, itemStacks, dropPosition, Vector3f.ZERO);
                                 entityStore.addEntities(itemEntityHolders, AddReason.SPAWN);
                              }
                           } else {
                              performBlockBreak(
                                 world,
                                 targetBlockPos,
                                 targetBlockType,
                                 itemStack,
                                 dropQuantity,
                                 itemId,
                                 dropListId,
                                 setBlockSettings | 2048,
                                 ref,
                                 chunkReference,
                                 entityStore,
                                 chunkStore
                              );
                              brokeBlock = true;
                              if ((setBlockSettings & 2048) == 0) {
                                 List<ItemStack> toolDrops = getDrops(targetBlockType, 1, requiredTool.getItemId(), requiredTool.getDropListId());
                                 if (!toolDrops.isEmpty()) {
                                    Vector3d dropPosition = new Vector3d(targetBlockPos.x + 0.5, targetBlockPos.y, targetBlockPos.z + 0.5);
                                    Holder<EntityStore>[] itemEntityHolders = ItemComponent.generateItemDrops(
                                       entityStore, toolDrops, dropPosition, Vector3f.ZERO
                                    );
                                    entityStore.addEntities(itemEntityHolders, AddReason.SPAWN);
                                 }
                              }
                           }
                        }
                     } else if ((setBlockSettings & 1024) == 0) {
                        BlockSoundSet soundSet = BlockSoundSet.getAssetMap().getAsset(targetBlockType.getBlockSoundSetIndex());
                        if (soundSet != null) {
                           int soundEventIndexx = soundSet.getSoundEventIndices().getOrDefault(BlockSoundEvent.Hit, 0);
                           if (soundEventIndexx != 0) {
                              SoundUtil.playSoundEvent3d(soundEventIndexx, SoundCategory.SFX, targetBlockCenterPos, entityStore);
                           }
                        }
                     }

                     if (ref != null && entity != null) {
                        if ((setBlockSettings & 1024) == 0 && !targetBlockCenterPos.equals(Vector3d.MAX)) {
                           int hitSoundEventLayerIndex = 0;
                           if (itemToolSpec != null) {
                              hitSoundEventLayerIndex = itemToolSpec.getHitSoundLayerIndex();
                           }

                           if (hitSoundEventLayerIndex == 0 && heldItem != null && heldItem.getTool() != null) {
                              hitSoundEventLayerIndex = heldItem.getTool().getHitSoundLayerIndex();
                           }

                           if (hitSoundEventLayerIndex != 0) {
                              SoundUtil.playSoundEvent3d(
                                 ref,
                                 hitSoundEventLayerIndex,
                                 targetBlockCenterPos.getX(),
                                 targetBlockCenterPos.getY(),
                                 targetBlockCenterPos.getZ(),
                                 entityStore
                              );
                           }
                        }

                        if (itemToolSpec != null && itemToolSpec.isIncorrect()) {
                           GatheringEffectsConfig incorrectToolConfig = gameplayConfig.getGatheringConfig().getIncorrectToolConfig();
                           if (incorrectToolConfig != null) {
                              if ((setBlockSettings & 4) == 0) {
                                 String particleSystemId = incorrectToolConfig.getParticleSystemId();
                                 if (particleSystemId != null) {
                                    ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
                                    playerSpatialResource.getSpatialStructure().collect(targetBlockCenterPos, 75.0, results);
                                    ParticleUtil.spawnParticleEffect(particleSystemId, targetBlockCenterPos, results, entityStore);
                                 }
                              }

                              if ((setBlockSettings & 1024) == 0) {
                                 int soundEventIndexx = incorrectToolConfig.getSoundEventIndex();
                                 if (soundEventIndexx != 0) {
                                    SoundUtil.playSoundEvent3d(ref, soundEventIndexx, targetBlockCenterPos, entityStore);
                                 }
                              }
                           }
                        }
                     }

                     if (entity != null
                        && ref != null
                        && entity.canDecreaseItemStackDurability(ref, entityStore)
                        && itemStack != null
                        && !itemStack.isUnbreakable()) {
                        byte activeHotbarSlot = entity.getInventory().getActiveHotbarSlot();
                        if (activeHotbarSlot != -1) {
                           double durability = calculateDurabilityUse(heldItem, targetBlockType);
                           ItemContainer hotbar = entity.getInventory().getHotbar();
                           entity.updateItemStackDurability(ref, itemStack, hotbar, activeHotbarSlot, -durability, entityStore);
                        }
                     }

                     return brokeBlock;
                  }
               }
            }
         }
      }
   }

   public static void performBlockBreak(
      @Nullable Ref<EntityStore> ref,
      @Nullable ItemStack heldItemStack,
      @Nonnull Vector3i targetBlock,
      @Nonnull Ref<ChunkStore> chunkReference,
      @Nonnull ComponentAccessor<EntityStore> entityStore,
      @Nonnull ComponentAccessor<ChunkStore> chunkStore
   ) {
      performBlockBreak(ref, heldItemStack, targetBlock, 0, chunkReference, entityStore, chunkStore);
   }

   public static void performBlockBreak(
      @Nullable Ref<EntityStore> ref,
      @Nullable ItemStack heldItemStack,
      @Nonnull Vector3i targetBlock,
      int setBlockSettings,
      @Nonnull Ref<ChunkStore> chunkReference,
      @Nonnull ComponentAccessor<EntityStore> entityStore,
      @Nonnull ComponentAccessor<ChunkStore> chunkStore
   ) {
      World world = chunkStore.getExternalData().getWorld();
      int targetBlockX = targetBlock.getX();
      int targetBlockY = targetBlock.getY();
      int targetBlockZ = targetBlock.getZ();
      WorldChunk worldChunkComponent = chunkStore.getComponent(chunkReference, WorldChunk.getComponentType());

      assert worldChunkComponent != null;

      int targetBlockTypeIndex = worldChunkComponent.getBlock(targetBlockX, targetBlockY, targetBlockZ);
      BlockType targetBlockTypeAsset = BlockType.getAssetMap().getAsset(targetBlockTypeIndex);
      if (targetBlockTypeAsset != null) {
         Vector3i affectedBlock = targetBlock;
         if (!targetBlockTypeAsset.isUnknown()) {
            BlockChunk blockChunkComponent = chunkStore.getComponent(chunkReference, BlockChunk.getComponentType());

            assert blockChunkComponent != null;

            BlockSection targetBlockSection = blockChunkComponent.getSectionAtBlockY(targetBlockY);
            int filler = targetBlockSection.getFiller(targetBlockX, targetBlockY, targetBlockZ);
            int fillerX = FillerBlockUtil.unpackX(filler);
            int fillerY = FillerBlockUtil.unpackY(filler);
            int fillerZ = FillerBlockUtil.unpackZ(filler);
            if (fillerX != 0 || fillerY != 0 || fillerZ != 0) {
               affectedBlock = targetBlock.clone().subtract(fillerX, fillerY, fillerZ);
               BlockType originBlock = world.getBlockType(affectedBlock);
               if (originBlock != null && !targetBlockTypeAsset.getId().equals(originBlock.getId())) {
                  world.breakBlock(targetBlockX, targetBlockY, targetBlockZ, setBlockSettings);
                  return;
               }
            }
         }

         performBlockBreak(
            world, affectedBlock, targetBlockTypeAsset, heldItemStack, 0, null, null, setBlockSettings, ref, chunkReference, entityStore, chunkStore
         );
      }
   }

   public static void performBlockBreak(
      @Nonnull World world,
      @Nonnull Vector3i blockPosition,
      @Nonnull BlockType targetBlockTypeKey,
      @Nullable ItemStack heldItemStack,
      int dropQuantity,
      @Nullable String dropItemId,
      @Nullable String dropListId,
      int setBlockSettings,
      @Nullable Ref<EntityStore> ref,
      @Nonnull Ref<ChunkStore> chunkReference,
      @Nonnull ComponentAccessor<EntityStore> entityStore,
      @Nonnull ComponentAccessor<ChunkStore> chunkStore
   ) {
      Vector3i targetBlockPosition = blockPosition;
      Ref<ChunkStore> targetChunkReference = chunkReference;
      ComponentAccessor<ChunkStore> targetChunkStore = chunkStore;
      if (ref != null) {
         BreakBlockEvent event = new BreakBlockEvent(heldItemStack, blockPosition, targetBlockTypeKey);
         entityStore.invoke(ref, event);
         if (event.isCancelled()) {
            BlockChunk blockChunkComponent = chunkStore.getComponent(chunkReference, BlockChunk.getComponentType());

            assert blockChunkComponent != null;

            BlockSection blockSection = blockChunkComponent.getSectionAtBlockY(blockPosition.getY());
            blockSection.invalidateBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
            return;
         }

         targetBlockPosition = event.getTargetBlock();
         targetChunkStore = world.getChunkStore().getStore();
         long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlockPosition.x, targetBlockPosition.z);
         targetChunkReference = targetChunkStore.getExternalData().getChunkReference(chunkIndex);
         if (targetChunkReference == null || !targetChunkReference.isValid()) {
            return;
         }
      }

      if (!targetBlockPosition.equals(blockPosition) || !world.equals(world)) {
         BlockChunk blockChunkComponent = chunkStore.getComponent(chunkReference, BlockChunk.getComponentType());

         assert blockChunkComponent != null;

         BlockSection blockSection = blockChunkComponent.getSectionAtBlockY(blockPosition.getY());
         blockSection.invalidateBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
      }

      int x = blockPosition.getX();
      int y = blockPosition.getY();
      int z = blockPosition.getZ();
      BlockChunk blockChunkComponent = chunkStore.getComponent(targetChunkReference, BlockChunk.getComponentType());

      assert blockChunkComponent != null;

      BlockSection blockSection = blockChunkComponent.getSectionAtBlockY(y);
      int filler = blockSection.getFiller(x, y, z);
      int blockTypeIndex = blockSection.get(x, y, z);
      BlockType blockTypeAsset = BlockType.getAssetMap().getAsset(blockTypeIndex);
      boolean isNaturalBlockBreak = BlockInteractionUtils.isNaturalAction(ref, entityStore);
      setBlockSettings |= 256;
      if (!isNaturalBlockBreak) {
         setBlockSettings |= 2048;
      }

      naturallyRemoveBlock(
         targetBlockPosition,
         blockTypeAsset,
         filler,
         dropQuantity,
         dropItemId,
         dropListId,
         setBlockSettings,
         targetChunkReference,
         entityStore,
         targetChunkStore
      );
   }

   @Deprecated
   public static void naturallyRemoveBlockByPhysics(
      @Nonnull Vector3i blockPosition,
      @Nonnull BlockType blockType,
      int filler,
      int setBlockSettings,
      @Nonnull Ref<ChunkStore> chunkReference,
      @Nonnull ComponentAccessor<EntityStore> entityStore,
      @Nonnull ComponentAccessor<ChunkStore> chunkStore
   ) {
      int quantity = 1;
      String itemId = null;
      String dropListId = null;
      BlockGathering blockGathering = blockType.getGathering();
      if (blockGathering != null) {
         PhysicsDropType physics = blockGathering.getPhysics();
         BlockBreakingDropType breaking = blockGathering.getBreaking();
         SoftBlockDropType soft = blockGathering.getSoft();
         HarvestingDropType harvest = blockGathering.getHarvest();
         if (physics != null) {
            itemId = physics.getItemId();
            dropListId = physics.getDropListId();
         } else if (breaking != null) {
            quantity = breaking.getQuantity();
            itemId = breaking.getItemId();
            dropListId = breaking.getDropListId();
         } else if (soft != null) {
            itemId = soft.getItemId();
            dropListId = soft.getDropListId();
         } else if (harvest != null) {
            itemId = harvest.getItemId();
            dropListId = harvest.getDropListId();
         }
      }

      setBlockSettings |= 32;
      naturallyRemoveBlock(blockPosition, blockType, filler, quantity, itemId, dropListId, setBlockSettings, chunkReference, entityStore, chunkStore);
   }

   public static void naturallyRemoveBlock(
      @Nonnull Vector3i blockPosition,
      @Nullable BlockType blockType,
      int filler,
      int quantity,
      String itemId,
      String dropListId,
      int setBlockSettings,
      @Nonnull Ref<ChunkStore> chunkReference,
      @Nonnull ComponentAccessor<EntityStore> entityStore,
      @Nonnull ComponentAccessor<ChunkStore> chunkStore
   ) {
      if (blockType != null) {
         WorldChunk worldChunkComponent = chunkStore.getComponent(chunkReference, WorldChunk.getComponentType());

         assert worldChunkComponent != null;

         BlockChunk blockChunkComponent = chunkStore.getComponent(chunkReference, BlockChunk.getComponentType());

         assert blockChunkComponent != null;

         Vector3i affectedBlock = blockPosition;
         if (!blockType.isUnknown()) {
            int fillerX = FillerBlockUtil.unpackX(filler);
            int fillerY = FillerBlockUtil.unpackY(filler);
            int fillerZ = FillerBlockUtil.unpackZ(filler);
            if (fillerX != 0 || fillerY != 0 || fillerZ != 0) {
               affectedBlock = blockPosition.clone().subtract(fillerX, fillerY, fillerZ);
               String oldBlockTypeKey = blockType.getId();
               blockType = worldChunkComponent.getBlockType(affectedBlock.getX(), affectedBlock.getY(), affectedBlock.getZ());
               if (blockType == null) {
                  throw new IllegalStateException("Null block type fetched for " + affectedBlock + " during block break");
               }

               if (!oldBlockTypeKey.equals(blockType.getId())) {
                  worldChunkComponent.breakBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), setBlockSettings);
                  return;
               }
            }
         }

         if ((setBlockSettings & 1024) == 0) {
            BlockSoundSet soundSet = BlockSoundSet.getAssetMap().getAsset(blockType.getBlockSoundSetIndex());
            if (soundSet != null) {
               int soundEventIndex = soundSet.getSoundEventIndices().getOrDefault(BlockSoundEvent.Break, 0);
               if (soundEventIndex != 0) {
                  BlockSection section = blockChunkComponent.getSectionAtBlockY(blockPosition.getY());
                  int rotationIndex = section.getRotationIndex(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
                  Vector3d centerPosition = new Vector3d();
                  blockType.getBlockCenter(rotationIndex, centerPosition);
                  centerPosition.add(blockPosition);
                  SoundUtil.playSoundEvent3d(soundEventIndex, SoundCategory.SFX, centerPosition, entityStore);
               }
            }
         }

         removeBlock(affectedBlock, blockType, setBlockSettings, chunkReference, chunkStore);
         if ((setBlockSettings & 2048) == 0 && quantity > 0) {
            Vector3d dropPosition = blockPosition.toVector3d().add(0.5, 0.0, 0.5);
            List<ItemStack> itemStacks = getDrops(blockType, quantity, itemId, dropListId);
            Holder<EntityStore>[] itemEntityHolders = ItemComponent.generateItemDrops(entityStore, itemStacks, dropPosition, Vector3f.ZERO);
            entityStore.addEntities(itemEntityHolders, AddReason.SPAWN);
         }
      }
   }

   public static boolean shouldPickupByInteraction(@Nullable BlockType blockType) {
      return blockType != null && blockType.getGathering() != null && blockType.getGathering().isHarvestable();
   }

   public static void performPickupByInteraction(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Vector3i targetBlock,
      @Nonnull BlockType blockType,
      int filler,
      @Nonnull Ref<ChunkStore> chunkReference,
      @Nonnull ComponentAccessor<EntityStore> entityStore,
      @Nonnull ComponentAccessor<ChunkStore> chunkStore
   ) {
      WorldChunk worldChunkComponent = chunkStore.getComponent(chunkReference, WorldChunk.getComponentType());

      assert worldChunkComponent != null;

      BlockChunk blockChunkComponent = chunkStore.getComponent(chunkReference, BlockChunk.getComponentType());

      assert blockChunkComponent != null;

      Vector3i affectedBlock = targetBlock;
      if (!blockType.isUnknown()) {
         int fillerX = FillerBlockUtil.unpackX(filler);
         int fillerY = FillerBlockUtil.unpackY(filler);
         int fillerZ = FillerBlockUtil.unpackZ(filler);
         if (fillerX != 0 || fillerY != 0 || fillerZ != 0) {
            affectedBlock = targetBlock.clone().subtract(fillerX, fillerY, fillerZ);
            String oldBlockTypeKey = blockType.getId();
            blockType = worldChunkComponent.getBlockType(affectedBlock.getX(), affectedBlock.getY(), affectedBlock.getZ());
            if (blockType == null) {
               return;
            }

            if (!oldBlockTypeKey.equals(blockType.getId())) {
               worldChunkComponent.breakBlock(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
               return;
            }
         }
      }

      BlockSection section = blockChunkComponent.getSectionAtBlockY(targetBlock.getY());
      Vector3d centerPosition = new Vector3d();
      int rotationIndex = section.getRotationIndex(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
      blockType.getBlockCenter(rotationIndex, centerPosition);
      centerPosition.add(targetBlock);
      int setBlockSettings = 0;
      setBlockSettings |= 256;
      if (!BlockInteractionUtils.isNaturalAction(ref, entityStore)) {
         setBlockSettings |= 2048;
      }

      removeBlock(affectedBlock, blockType, setBlockSettings, chunkReference, chunkStore);
      HarvestingDropType harvest = blockType.getGathering().getHarvest();
      String itemId = harvest.getItemId();
      String dropListId = harvest.getDropListId();

      for (ItemStack itemStack : getDrops(blockType, 1, itemId, dropListId)) {
         ItemUtils.interactivelyPickupItem(ref, itemStack, centerPosition, entityStore);
      }

      if ((setBlockSettings & 1024) == 0) {
         BlockSoundSet soundSet = BlockSoundSet.getAssetMap().getAsset(blockType.getBlockSoundSetIndex());
         if (soundSet != null) {
            int soundEventIndex = soundSet.getSoundEventIndices().getOrDefault(BlockSoundEvent.Harvest, 0);
            if (soundEventIndex != 0) {
               SoundUtil.playSoundEvent3d(soundEventIndex, SoundCategory.SFX, centerPosition, entityStore);
            }
         }
      }
   }

   protected static void removeBlock(
      @Nonnull Vector3i blockPosition,
      @Nonnull BlockType blockType,
      int setBlockSettings,
      @Nonnull Ref<ChunkStore> chunkReference,
      @Nonnull ComponentAccessor<ChunkStore> chunkStore
   ) {
      World world = chunkStore.getExternalData().getWorld();
      ComponentType<ChunkStore, BlockHealthChunk> blockHealthComponentType = BlockHealthModule.get().getBlockHealthChunkComponentType();
      BlockHealthChunk blockHealthComponent = chunkStore.getComponent(chunkReference, blockHealthComponentType);

      assert blockHealthComponent != null;

      blockHealthComponent.removeBlock(world, blockPosition);
      WorldChunk worldChunkComponent = chunkStore.getComponent(chunkReference, WorldChunk.getComponentType());

      assert worldChunkComponent != null;

      BlockChunk blockChunkComponent = chunkStore.getComponent(chunkReference, BlockChunk.getComponentType());

      assert blockChunkComponent != null;

      worldChunkComponent.breakBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), setBlockSettings);
      if ((setBlockSettings & 256) != 0) {
         BlockSection section = blockChunkComponent.getSectionAtBlockY(blockPosition.y);
         int rotationIndex = section.getRotationIndex(blockPosition.x, blockPosition.y, blockPosition.z);
         BlockBoundingBoxes hitBoxType = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
         if (hitBoxType != null) {
            FillerBlockUtil.forEachFillerBlock(
               hitBoxType.get(rotationIndex),
               (x, y, z) -> world.performBlockUpdate(blockPosition.getX() + x, blockPosition.getY() + y, blockPosition.getZ() + z, false)
            );
         }
      }

      ConnectedBlocksUtil.setConnectedBlockAndNotifyNeighbors(
         BlockType.getAssetMap().getIndex("Empty"), RotationTuple.NONE, Vector3i.ZERO, blockPosition, worldChunkComponent, blockChunkComponent
      );
   }

   @Nonnull
   public static List<ItemStack> getDrops(@Nonnull BlockType blockType, int quantity, @Nullable String itemId, @Nullable String dropListId) {
      if (dropListId == null && itemId == null) {
         Item item = blockType.getItem();
         return item == null ? ObjectLists.emptyList() : ObjectLists.singleton(new ItemStack(item.getId(), quantity));
      } else {
         List<ItemStack> randomItemDrops = new ObjectArrayList();
         if (dropListId != null) {
            ItemModule itemModule = ItemModule.get();
            if (itemModule.isEnabled()) {
               for (int i = 0; i < quantity; i++) {
                  List<ItemStack> randomItemsToDrop = itemModule.getRandomItemDrops(dropListId);
                  randomItemDrops.addAll(randomItemsToDrop);
               }
            }
         }

         if (itemId != null) {
            randomItemDrops.add(new ItemStack(itemId, quantity));
         }

         return randomItemDrops;
      }
   }
}
