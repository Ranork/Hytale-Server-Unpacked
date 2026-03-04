package com.hypixel.hytale.builtin.adventure.objectives.transaction;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TransactionRecord {
   @Nonnull
   public static final CodecMapCodec<TransactionRecord> CODEC = new CodecMapCodec<>("Type");
   @Nonnull
   public static final BuilderCodec<TransactionRecord> BASE_CODEC = BuilderCodec.abstractBuilder(TransactionRecord.class)
      .append(
         new KeyedCodec<>("Status", new EnumCodec<>(TransactionStatus.class, EnumCodec.EnumStyle.LEGACY)),
         (spawnEntityTransactionRecord, status) -> spawnEntityTransactionRecord.status = status,
         spawnEntityTransactionRecord -> spawnEntityTransactionRecord.status
      )
      .add()
      .build();
   protected TransactionStatus status = TransactionStatus.SUCCESS;
   private String reason;

   public TransactionStatus getStatus() {
      return this.status;
   }

   public abstract void revert();

   public abstract void complete();

   public abstract void unload();

   public abstract boolean shouldBeSerialized();

   @Nonnull
   public TransactionRecord fail(String reason) {
      this.status = TransactionStatus.FAIL;
      this.reason = reason;
      return this;
   }

   @Nonnull
   @Override
   public String toString() {
      return "TransactionRecord{status=" + this.status + ", reason='" + this.reason + "'}";
   }

   @Nonnull
   public static <T extends TransactionRecord> TransactionRecord[] appendTransaction(@Nullable TransactionRecord[] transactions, @Nonnull T transaction) {
      return transactions == null ? new TransactionRecord[]{transaction} : ArrayUtil.append(transactions, transaction);
   }

   @Nonnull
   public static <T extends TransactionRecord> TransactionRecord[] appendFailedTransaction(
      @Nullable TransactionRecord[] transactions, @Nonnull T transaction, String reason
   ) {
      return appendTransaction(transactions, transaction.fail(reason));
   }

   static {
      CODEC.register("SpawnEntity", SpawnEntityTransactionRecord.class, SpawnEntityTransactionRecord.CODEC);
      CODEC.register("SpawnBlock", SpawnTreasureChestTransactionRecord.class, SpawnTreasureChestTransactionRecord.CODEC);
   }
}
