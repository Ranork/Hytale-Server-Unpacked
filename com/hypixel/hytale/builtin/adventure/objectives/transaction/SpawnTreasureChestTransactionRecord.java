package com.hypixel.hytale.builtin.adventure.objectives.transaction;

import com.hypixel.hytale.builtin.adventure.objectives.blockstates.TreasureChestState;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import java.util.UUID;
import javax.annotation.Nonnull;

public class SpawnTreasureChestTransactionRecord extends TransactionRecord {
   @Nonnull
   public static final BuilderCodec<SpawnTreasureChestTransactionRecord> CODEC = BuilderCodec.builder(
         SpawnTreasureChestTransactionRecord.class, SpawnTreasureChestTransactionRecord::new, BASE_CODEC
      )
      .append(
         new KeyedCodec<>("WorldUUID", Codec.UUID_BINARY),
         (spawnTreasureChestTransactionRecord, uuid) -> spawnTreasureChestTransactionRecord.worldUUID = uuid,
         spawnTreasureChestTransactionRecord -> spawnTreasureChestTransactionRecord.worldUUID
      )
      .add()
      .append(
         new KeyedCodec<>("BlockPosition", Vector3i.CODEC),
         (spawnTreasureChestTransactionRecord, vector3d) -> spawnTreasureChestTransactionRecord.blockPosition = vector3d,
         spawnTreasureChestTransactionRecord -> spawnTreasureChestTransactionRecord.blockPosition
      )
      .add()
      .build();
   protected UUID worldUUID;
   protected Vector3i blockPosition;

   public SpawnTreasureChestTransactionRecord(UUID worldUUID, Vector3i blockPosition) {
      this.worldUUID = worldUUID;
      this.blockPosition = blockPosition;
   }

   protected SpawnTreasureChestTransactionRecord() {
   }

   @Override
   public void revert() {
      World world = Universe.get().getWorld(this.worldUUID);
      if (world != null) {
         WorldChunk worldChunk = world.getChunk(ChunkUtil.indexChunkFromBlock(this.blockPosition.x, this.blockPosition.z));
         BlockState blockState = worldChunk.getState(this.blockPosition.x, this.blockPosition.y, this.blockPosition.z);
         if (blockState instanceof TreasureChestState) {
            ((TreasureChestState)blockState).setOpened(true);
         }
      }
   }

   @Override
   public void complete() {
   }

   @Override
   public void unload() {
   }

   @Override
   public boolean shouldBeSerialized() {
      return true;
   }

   @Nonnull
   @Override
   public String toString() {
      return "SpawnTreasureChestTransactionRecord{worldUUID=" + this.worldUUID + ", blockPosition=" + this.blockPosition + "} " + super.toString();
   }
}
