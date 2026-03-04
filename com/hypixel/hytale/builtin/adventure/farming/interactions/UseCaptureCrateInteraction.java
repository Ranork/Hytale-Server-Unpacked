package com.hypixel.hytale.builtin.adventure.farming.interactions;

import com.hypixel.hytale.builtin.adventure.farming.states.CoopBlock;
import com.hypixel.hytale.builtin.tagset.TagSetPlugin;
import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFace;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.metadata.CapturedNPCMetadata;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UseCaptureCrateInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final BuilderCodec<UseCaptureCrateInteraction> CODEC = BuilderCodec.builder(
         UseCaptureCrateInteraction.class, UseCaptureCrateInteraction::new, SimpleInteraction.CODEC
      )
      .appendInherited(
         new KeyedCodec<>("AcceptedNpcGroups", NPCGroup.CHILD_ASSET_CODEC_ARRAY),
         (o, v) -> o.acceptedNpcGroupIds = v,
         o -> o.acceptedNpcGroupIds,
         (o, p) -> o.acceptedNpcGroupIds = p.acceptedNpcGroupIds
      )
      .addValidator(NPCGroup.VALIDATOR_CACHE.getArrayValidator())
      .add()
      .appendInherited(new KeyedCodec<>("FullIcon", Codec.STRING), (o, v) -> o.fullIcon = v, o -> o.fullIcon, (o, p) -> o.fullIcon = p.fullIcon)
      .add()
      .afterDecode(captureData -> {
         if (captureData.acceptedNpcGroupIds != null) {
            captureData.acceptedNpcGroupIndexes = new int[captureData.acceptedNpcGroupIds.length];

            for (int i = 0; i < captureData.acceptedNpcGroupIds.length; i++) {
               int assetIdx = NPCGroup.getAssetMap().getIndex(captureData.acceptedNpcGroupIds[i]);
               captureData.acceptedNpcGroupIndexes[i] = assetIdx;
            }
         }
      })
      .build();
   protected String[] acceptedNpcGroupIds;
   protected int[] acceptedNpcGroupIndexes;
   protected String fullIcon;

   @Override
   protected void tick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      ItemStack item = context.getHeldItem();
      if (item == null) {
         context.getState().state = InteractionState.Failed;
         super.tick0(firstRun, time, type, context, cooldownHandler);
      } else {
         Ref<EntityStore> ref = context.getEntity();
         if (!(EntityUtils.getEntity(ref, commandBuffer) instanceof LivingEntity livingEntity)) {
            context.getState().state = InteractionState.Failed;
            super.tick0(firstRun, time, type, context, cooldownHandler);
         } else {
            Inventory inventory = livingEntity.getInventory();
            byte activeHotbarSlot = inventory.getActiveHotbarSlot();
            ItemStack inHandItemStack = inventory.getActiveHotbarItem();
            if (inHandItemStack == null) {
               context.getState().state = InteractionState.Failed;
               super.tick0(firstRun, time, type, context, cooldownHandler);
            } else {
               CapturedNPCMetadata existingMeta = item.getFromMetadataOrNull("CapturedEntity", CapturedNPCMetadata.CODEC);
               if (existingMeta != null) {
                  super.tick0(firstRun, time, type, context, cooldownHandler);
               } else {
                  Ref<EntityStore> targetEntity = context.getTargetEntity();
                  if (targetEntity == null) {
                     context.getState().state = InteractionState.Failed;
                     super.tick0(firstRun, time, type, context, cooldownHandler);
                  } else {
                     NPCEntity npcComponent = commandBuffer.getComponent(targetEntity, NPCEntity.getComponentType());
                     if (npcComponent == null) {
                        context.getState().state = InteractionState.Failed;
                        super.tick0(firstRun, time, type, context, cooldownHandler);
                     } else {
                        DeathComponent deathComponent = commandBuffer.getComponent(targetEntity, DeathComponent.getComponentType());
                        if (deathComponent != null) {
                           context.getState().state = InteractionState.Failed;
                           super.tick0(firstRun, time, type, context, cooldownHandler);
                        } else {
                           TagSetPlugin.TagSetLookup tagSetPlugin = TagSetPlugin.get(NPCGroup.class);
                           boolean tagFound = false;

                           for (int group : this.acceptedNpcGroupIndexes) {
                              if (tagSetPlugin.tagInSet(group, npcComponent.getRoleIndex())) {
                                 tagFound = true;
                                 break;
                              }
                           }

                           if (!tagFound) {
                              context.getState().state = InteractionState.Failed;
                              super.tick0(firstRun, time, type, context, cooldownHandler);
                           } else {
                              PersistentModel persistentModelComponent = commandBuffer.getComponent(targetEntity, PersistentModel.getComponentType());
                              if (persistentModelComponent == null) {
                                 context.getState().state = InteractionState.Failed;
                                 super.tick0(firstRun, time, type, context, cooldownHandler);
                              } else {
                                 ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(persistentModelComponent.getModelReference().getModelAssetId());
                                 CapturedNPCMetadata itemMetaData = inHandItemStack.getFromMetadataOrDefault("CapturedEntity", CapturedNPCMetadata.CODEC);
                                 if (modelAsset != null) {
                                    itemMetaData.setIconPath(modelAsset.getIcon());
                                 }

                                 String npcName = NPCPlugin.get().getName(npcComponent.getRoleIndex());
                                 if (npcName != null) {
                                    itemMetaData.setNpcNameKey(npcName);
                                 }

                                 if (this.fullIcon != null) {
                                    itemMetaData.setFullItemIcon(this.fullIcon);
                                 }

                                 ItemStack itemWithNPC = inHandItemStack.withMetadata(CapturedNPCMetadata.KEYED_CODEC, itemMetaData);
                                 inventory.getHotbar().replaceItemStackInSlot(activeHotbarSlot, item, itemWithNPC);
                                 commandBuffer.removeEntity(targetEntity, RemoveReason.REMOVE);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   protected void interactWithBlock(
      @Nonnull World world,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nullable ItemStack itemInHand,
      @Nonnull Vector3i targetBlock,
      @Nonnull CooldownHandler cooldownHandler
   ) {
      ItemStack item = context.getHeldItem();
      if (item == null) {
         context.getState().state = InteractionState.Failed;
      } else {
         Ref<EntityStore> ref = context.getEntity();
         if (EntityUtils.getEntity(ref, commandBuffer) instanceof LivingEntity livingEntity) {
            Inventory inventory = livingEntity.getInventory();
            byte activeHotbarSlot = inventory.getActiveHotbarSlot();
            CapturedNPCMetadata existingMeta = item.getFromMetadataOrNull("CapturedEntity", CapturedNPCMetadata.CODEC);
            if (existingMeta == null) {
               context.getState().state = InteractionState.Failed;
            } else {
               BlockPosition pos = context.getTargetBlock();
               if (pos == null) {
                  context.getState().state = InteractionState.Failed;
               } else {
                  long chunkIndex = ChunkUtil.indexChunkFromBlock(pos.x, pos.z);
                  WorldChunk worldChunk = world.getChunk(chunkIndex);
                  if (worldChunk == null) {
                     context.getState().state = InteractionState.Failed;
                  } else {
                     Ref<ChunkStore> blockRef = worldChunk.getBlockComponentEntity(pos.x, pos.y, pos.z);
                     if (blockRef == null || !blockRef.isValid()) {
                        blockRef = BlockModule.ensureBlockEntity(worldChunk, pos.x, pos.y, pos.z);
                     }

                     ItemStack noMetaItemStack = item.withMetadata(null);
                     if (blockRef != null && blockRef.isValid()) {
                        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
                        CoopBlock coopBlockComponent = chunkStore.getComponent(blockRef, CoopBlock.getComponentType());
                        if (coopBlockComponent != null) {
                           WorldTimeResource worldTimeResource = commandBuffer.getResource(WorldTimeResource.getResourceType());
                           if (coopBlockComponent.tryPutResident(existingMeta, worldTimeResource)) {
                              world.execute(
                                 () -> coopBlockComponent.ensureSpawnResidentsInWorld(
                                    world, world.getEntityStore().getStore(), new Vector3d(pos.x, pos.y, pos.z), new Vector3d().assign(Vector3d.FORWARD)
                                 )
                              );
                              inventory.getHotbar().replaceItemStackInSlot(activeHotbarSlot, item, noMetaItemStack);
                              context.getState().state = InteractionState.Finished;
                           } else {
                              context.getState().state = InteractionState.Failed;
                           }

                           return;
                        }
                     }

                     Vector3d spawnPos = new Vector3d(pos.x + 0.5F, pos.y, pos.z + 0.5F);
                     if (context.getClientState() != null) {
                        BlockFace blockFace = BlockFace.fromProtocolFace(context.getClientState().blockFace);
                        if (blockFace != null) {
                           spawnPos.add(blockFace.getDirection());
                        }
                     }

                     String roleId = existingMeta.getNpcNameKey();
                     int roleIndex = NPCPlugin.get().getIndex(roleId);
                     commandBuffer.run(_store -> NPCPlugin.get().spawnEntity(_store, roleIndex, spawnPos, Vector3f.ZERO, null, null));
                     inventory.getHotbar().replaceItemStackInSlot(activeHotbarSlot, item, noMetaItemStack);
                  }
               }
            }
         } else {
            context.getState().state = InteractionState.Failed;
         }
      }
   }

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
   }
}
