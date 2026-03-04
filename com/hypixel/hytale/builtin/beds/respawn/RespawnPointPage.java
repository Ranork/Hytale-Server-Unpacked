package com.hypixel.hytale.builtin.beds.respawn;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerRespawnPointData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.state.RespawnBlock;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class RespawnPointPage extends InteractiveCustomUIPage<RespawnPointPage.RespawnPointEventData> {
   @Nonnull
   private static final Message MESSAGE_SERVER_CUSTOM_UI_NEED_TO_SET_NAME = Message.translation("server.customUI.needToSetName");
   private static final int RESPAWN_NAME_MAX_LENGTH = 32;

   public RespawnPointPage(@Nonnull PlayerRef playerRef, @Nonnull InteractionType interactionType) {
      super(
         playerRef,
         interactionType == InteractionType.Use ? CustomPageLifetime.CanDismissOrCloseThroughInteraction : CustomPageLifetime.CanDismiss,
         RespawnPointPage.RespawnPointEventData.CODEC
      );
   }

   @Override
   public abstract void build(@Nonnull Ref<EntityStore> var1, @Nonnull UICommandBuilder var2, @Nonnull UIEventBuilder var3, @Nonnull Store<EntityStore> var4);

   protected void setRespawnPointForPlayer(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Store<EntityStore> store,
      @Nonnull Vector3i blockPosition,
      @Nonnull RespawnBlock respawnBlock,
      @Nonnull String respawnPointName,
      @Nullable PlayerRespawnPointData... respawnPointsToRemove
   ) {
      respawnPointName = respawnPointName.trim();
      if (respawnPointName.isEmpty()) {
         this.displayError(MESSAGE_SERVER_CUSTOM_UI_NEED_TO_SET_NAME);
      } else if (respawnPointName.length() > 32) {
         this.displayError(Message.translation("server.customUI.respawnNameTooLong").param("maxLength", 32));
      } else {
         respawnBlock.setOwnerUUID(this.playerRef.getUuid());
         World world = store.getExternalData().getWorld();
         Player playerComponent = store.getComponent(ref, Player.getComponentType());
         if (playerComponent != null) {
            long chunkIndex = ChunkUtil.indexChunkFromBlock(blockPosition.x, blockPosition.z);
            WorldChunk chunk = world.getChunkIfInMemory(chunkIndex);
            if (chunk != null) {
               chunk.markNeedsSaving();
               BlockType blockType = chunk.getBlockType(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
               if (blockType != null) {
                  int rotationIndex = chunk.getRotationIndex(blockPosition.x, blockPosition.y, blockPosition.z);
                  BlockBoundingBoxes blockBoundingBoxAsset = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
                  if (blockBoundingBoxAsset != null) {
                     Box hitbox = blockBoundingBoxAsset.get(rotationIndex).getBoundingBox();
                     double blockCenterWidthOffset = hitbox.min.x + hitbox.width() / 2.0;
                     double blockCenterDepthOffset = hitbox.min.z + hitbox.depth() / 2.0;
                     Vector3d respawnPosition = new Vector3d(
                        blockPosition.getX() + blockCenterWidthOffset, blockPosition.getY() + hitbox.height(), blockPosition.getZ() + blockCenterDepthOffset
                     );
                     PlayerRespawnPointData respawnPointData = new PlayerRespawnPointData(blockPosition, respawnPosition, respawnPointName);
                     PlayerWorldData perWorldData = playerComponent.getPlayerConfigData().getPerWorldData(world.getName());
                     PlayerRespawnPointData[] respawnPoints = handleRespawnPointsToRemove(world, perWorldData.getRespawnPoints(), respawnPointsToRemove);
                     if (respawnPoints != null) {
                        if (ArrayUtil.contains(respawnPoints, respawnPointData)) {
                           return;
                        }

                        if (respawnPointsToRemove == null || respawnPointsToRemove.length == 0) {
                           for (int i = 0; i < respawnPoints.length; i++) {
                              PlayerRespawnPointData savedRespawnPointData = respawnPoints[i];
                              if (savedRespawnPointData.getBlockPosition().equals(blockPosition)) {
                                 savedRespawnPointData.setName(respawnPointName);
                                 this.playerRef.sendMessage(Message.translation("server.customUI.updatedRespawnPointName").param("name", respawnPointName));
                                 playerComponent.getPageManager().setPage(ref, store, Page.None);
                                 return;
                              }
                           }
                        }
                     }

                     perWorldData.setRespawnPoints(ArrayUtil.append(respawnPoints, respawnPointData));
                     this.playerRef.sendMessage(Message.translation("server.customUI.respawnPointSet").param("name", respawnPointName));
                     playerComponent.getPageManager().setPage(ref, store, Page.None);
                  }
               }
            }
         }
      }
   }

   @Nonnull
   private static PlayerRespawnPointData[] handleRespawnPointsToRemove(
      @Nonnull World world, @Nonnull PlayerRespawnPointData[] respawnPoints, @Nullable PlayerRespawnPointData[] respawnPointsToRemove
   ) {
      if (respawnPointsToRemove == null) {
         return respawnPoints;
      } else {
         ChunkStore chunkStore = world.getChunkStore();

         for (int i = 0; i < respawnPointsToRemove.length; i++) {
            PlayerRespawnPointData respawnPointToRemove = respawnPointsToRemove[i];

            for (int j = 0; j < respawnPoints.length; j++) {
               PlayerRespawnPointData respawnPoint = respawnPoints[j];
               if (respawnPoint.getBlockPosition().equals(respawnPointToRemove.getBlockPosition())) {
                  respawnPoints = ArrayUtil.remove(respawnPoints, j);
                  break;
               }
            }

            Vector3i position = respawnPointToRemove.getBlockPosition();
            long chunkIndex = ChunkUtil.indexChunkFromBlock(position.x, position.z);
            Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(chunkIndex);
            if (chunkReference != null && chunkReference.isValid()) {
               BlockComponentChunk blockComponentChunk = chunkStore.getStore().getComponent(chunkReference, BlockComponentChunk.getComponentType());
               if (blockComponentChunk != null) {
                  int blockIndex = ChunkUtil.indexBlockInColumn(position.x, position.y, position.z);
                  Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndex);
                  if (blockRef != null && blockRef.isValid()) {
                     RespawnBlock respawnBlock = chunkStore.getStore().getComponent(blockRef, RespawnBlock.getComponentType());
                     if (respawnBlock != null) {
                        respawnBlock.setOwnerUUID(null);
                        WorldChunk worldChunk = chunkStore.getStore().getComponent(chunkReference, WorldChunk.getComponentType());
                        if (worldChunk != null) {
                           worldChunk.markNeedsSaving();
                        }
                     }
                  }
               }
            }
         }

         return respawnPoints;
      }
   }

   protected void displayError(@Nonnull Message errorMessage) {
      UICommandBuilder commandBuilder = new UICommandBuilder();
      commandBuilder.set("#Error.Visible", true);
      commandBuilder.set("#Error.Text", errorMessage);
      this.sendUpdate(commandBuilder);
   }

   public static class RespawnPointEventData {
      @Nonnull
      static final String KEY_ACTION = "Action";
      @Nonnull
      static final String ACTION_CANCEL = "Cancel";
      @Nonnull
      static final String KEY_INDEX = "Index";
      @Nonnull
      static final String KEY_RESPAWN_POINT_NAME = "@RespawnPointName";
      @Nonnull
      public static final BuilderCodec<RespawnPointPage.RespawnPointEventData> CODEC = BuilderCodec.builder(
            RespawnPointPage.RespawnPointEventData.class, RespawnPointPage.RespawnPointEventData::new
         )
         .append(new KeyedCodec<>("Action", Codec.STRING), (entry, s) -> entry.action = s, entry -> entry.action)
         .add()
         .append(new KeyedCodec<>("Index", Codec.STRING), (entry, s) -> {
            entry.indexStr = s;
            entry.index = Integer.parseInt(s);
         }, entry -> entry.indexStr)
         .add()
         .append(new KeyedCodec<>("@RespawnPointName", Codec.STRING), (entry, s) -> entry.respawnPointName = s, entry -> entry.respawnPointName)
         .add()
         .build();
      private String action;
      private String indexStr;
      private int index = -1;
      private String respawnPointName;

      public String getAction() {
         return this.action;
      }

      public int getIndex() {
         return this.index;
      }

      public String getRespawnPointName() {
         return this.respawnPointName;
      }
   }
}
