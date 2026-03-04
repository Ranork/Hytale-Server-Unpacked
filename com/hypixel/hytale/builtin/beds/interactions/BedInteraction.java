package com.hypixel.hytale.builtin.beds.interactions;

import com.hypixel.hytale.builtin.beds.respawn.OverrideNearbyRespawnPointPage;
import com.hypixel.hytale.builtin.beds.respawn.SelectOverrideRespawnPointPage;
import com.hypixel.hytale.builtin.beds.respawn.SetNameRespawnPointPage;
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSleep;
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSomnolence;
import com.hypixel.hytale.builtin.beds.sleep.systems.player.SleepNotificationSystem;
import com.hypixel.hytale.builtin.mounts.BlockMountAPI;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.gameplay.RespawnConfig;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerRespawnPointData;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.meta.state.RespawnBlock;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BedInteraction extends SimpleBlockInteraction {
   @Nonnull
   private static final Message MESSAGE_SERVER_CUSTOM_UI_RESPAWN_POINT_CLAIMED = Message.translation("server.customUI.respawnPointClaimed");
   @Nonnull
   public static final BuilderCodec<BedInteraction> CODEC = BuilderCodec.builder(BedInteraction.class, BedInteraction::new, SimpleBlockInteraction.CODEC)
      .documentation("Interact with a bed block, ostensibly to sleep in it.")
      .build();

   @Override
   protected void interactWithBlock(
      @Nonnull World world,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nullable ItemStack itemInHand,
      @Nonnull Vector3i pos,
      @Nonnull CooldownHandler cooldownHandler
   ) {
      Ref<EntityStore> ref = context.getEntity();
      Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         Store<EntityStore> store = commandBuffer.getStore();
         PlayerRef playerRefComponent = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
         if (playerRefComponent != null) {
            UUIDComponent playerUuidComponent = commandBuffer.getComponent(ref, UUIDComponent.getComponentType());
            if (playerUuidComponent != null) {
               UUID playerUuid = playerUuidComponent.getUuid();
               long chunkIndex = ChunkUtil.indexChunkFromBlock(pos.x, pos.z);
               Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(chunkIndex);
               if (chunkRef != null && chunkRef.isValid()) {
                  Store<ChunkStore> chunkStore = chunkRef.getStore();
                  BlockComponentChunk blockComponentChunk = chunkStore.getComponent(chunkRef, BlockComponentChunk.getComponentType());
                  if (blockComponentChunk != null) {
                     int blockIndex = ChunkUtil.indexBlockInColumn(pos.x, pos.y, pos.z);
                     Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndex);
                     if (blockRef == null || !blockRef.isValid()) {
                        Holder<ChunkStore> holder = ChunkStore.REGISTRY.newHolder();
                        holder.putComponent(BlockModule.BlockStateInfo.getComponentType(), new BlockModule.BlockStateInfo(blockIndex, chunkRef));
                        holder.ensureComponent(RespawnBlock.getComponentType());
                        blockRef = chunkStore.addEntity(holder, AddReason.SPAWN);
                     }

                     if (blockRef != null && blockRef.isValid()) {
                        RespawnBlock respawnBlockComponent = chunkStore.getComponent(blockRef, RespawnBlock.getComponentType());
                        if (respawnBlockComponent != null) {
                           UUID ownerUUID = respawnBlockComponent.getOwnerUUID();
                           PageManager pageManager = playerComponent.getPageManager();
                           boolean isOwner = playerUuid.equals(ownerUUID);
                           if (isOwner) {
                              BlockPosition targetBlockPosition = context.getMetaStore().getMetaObject(TARGET_BLOCK_RAW);
                              Vector3f whereWasHit = new Vector3f(targetBlockPosition.x + 0.5F, targetBlockPosition.y + 0.5F, targetBlockPosition.z + 0.5F);
                              BlockMountAPI.BlockMountResult result = BlockMountAPI.mountOnBlock(ref, commandBuffer, pos, whereWasHit);
                              if (result instanceof BlockMountAPI.DidNotMount) {
                                 playerComponent.sendMessage(Message.translation("server.interactions.didNotMount").param("state", result.toString()));
                              } else if (result instanceof BlockMountAPI.Mounted) {
                                 commandBuffer.putComponent(ref, PlayerSomnolence.getComponentType(), PlayerSleep.NoddingOff.createComponent());
                                 commandBuffer.run(s -> SleepNotificationSystem.maybeDoNotification(s, false));
                              }
                           } else if (ownerUUID != null) {
                              playerComponent.sendMessage(MESSAGE_SERVER_CUSTOM_UI_RESPAWN_POINT_CLAIMED);
                           } else {
                              PlayerRespawnPointData[] respawnPoints = playerComponent.getPlayerConfigData()
                                 .getPerWorldData(world.getName())
                                 .getRespawnPoints();
                              RespawnConfig respawnConfig = world.getGameplayConfig().getRespawnConfig();
                              int radiusLimitRespawnPoint = respawnConfig.getRadiusLimitRespawnPoint();
                              PlayerRespawnPointData[] nearbyRespawnPoints = getNearbySavedRespawnPoints(pos, respawnPoints, radiusLimitRespawnPoint);
                              if (nearbyRespawnPoints != null) {
                                 pageManager.openCustomPage(
                                    ref,
                                    store,
                                    new OverrideNearbyRespawnPointPage(
                                       playerRefComponent, type, pos, respawnBlockComponent, nearbyRespawnPoints, radiusLimitRespawnPoint
                                    )
                                 );
                              } else if (respawnPoints != null && respawnPoints.length >= respawnConfig.getMaxRespawnPointsPerPlayer()) {
                                 pageManager.openCustomPage(
                                    ref, store, new SelectOverrideRespawnPointPage(playerRefComponent, type, pos, respawnBlockComponent, respawnPoints)
                                 );
                              } else {
                                 pageManager.openCustomPage(ref, store, new SetNameRespawnPointPage(playerRefComponent, type, pos, respawnBlockComponent));
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
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
   }

   @Nullable
   private static PlayerRespawnPointData[] getNearbySavedRespawnPoints(
      @Nonnull Vector3i currentRespawnPointPosition, @Nullable PlayerRespawnPointData[] respawnPoints, int radiusLimitRespawnPoint
   ) {
      if (respawnPoints != null && respawnPoints.length != 0) {
         ObjectArrayList<PlayerRespawnPointData> nearbyRespawnPointList = new ObjectArrayList();

         for (int i = 0; i < respawnPoints.length; i++) {
            PlayerRespawnPointData respawnPoint = respawnPoints[i];
            Vector3i respawnPointPosition = respawnPoint.getBlockPosition();
            if (respawnPointPosition.distanceTo(currentRespawnPointPosition.x, respawnPointPosition.y, currentRespawnPointPosition.z) < radiusLimitRespawnPoint
               )
             {
               nearbyRespawnPointList.add(respawnPoint);
            }
         }

         return nearbyRespawnPointList.isEmpty() ? null : (PlayerRespawnPointData[])nearbyRespawnPointList.toArray(PlayerRespawnPointData[]::new);
      } else {
         return null;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "BedInteraction{} " + super.toString();
   }
}
