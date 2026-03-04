package com.hypixel.hytale.builtin.adventure.objectives.transaction;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.UUID;
import javax.annotation.Nonnull;

public class SpawnEntityTransactionRecord extends TransactionRecord {
   @Nonnull
   public static final BuilderCodec<SpawnEntityTransactionRecord> CODEC = BuilderCodec.builder(
         SpawnEntityTransactionRecord.class, SpawnEntityTransactionRecord::new, BASE_CODEC
      )
      .append(
         new KeyedCodec<>("WorldUUID", Codec.UUID_BINARY),
         (spawnEntityTransactionRecord, uuid) -> spawnEntityTransactionRecord.worldUUID = uuid,
         spawnEntityTransactionRecord -> spawnEntityTransactionRecord.worldUUID
      )
      .add()
      .append(
         new KeyedCodec<>("EntityUUID", Codec.UUID_BINARY),
         (spawnEntityTransactionRecord, uuid) -> spawnEntityTransactionRecord.entityUUID = uuid,
         spawnEntityTransactionRecord -> spawnEntityTransactionRecord.entityUUID
      )
      .add()
      .build();
   protected UUID worldUUID;
   protected UUID entityUUID;

   public SpawnEntityTransactionRecord(@Nonnull UUID worldUUID, @Nonnull UUID entityUUID) {
      this.worldUUID = worldUUID;
      this.entityUUID = entityUUID;
   }

   protected SpawnEntityTransactionRecord() {
   }

   @Override
   public void revert() {
      this.removeEntity();
   }

   @Override
   public void complete() {
      this.removeEntity();
   }

   @Override
   public void unload() {
   }

   @Override
   public boolean shouldBeSerialized() {
      return true;
   }

   private void removeEntity() {
      World world = Universe.get().getWorld(this.worldUUID);
      Entity entity = world.getEntity(this.entityUUID);
      if (entity != null) {
         if (!entity.remove()) {
            throw new RuntimeException("Failed to revert record!");
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "SpawnEntityTransactionRecord{worldUUID=" + this.worldUUID + ", entityUUID=" + this.entityUUID + "} " + super.toString();
   }
}
