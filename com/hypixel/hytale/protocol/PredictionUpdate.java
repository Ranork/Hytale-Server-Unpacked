package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;

public class PredictionUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 16;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 16;
   public static final int MAX_SIZE = 16;
   @Nonnull
   public UUID predictionId = new UUID(0L, 0L);

   public PredictionUpdate() {
   }

   public PredictionUpdate(@Nonnull UUID predictionId) {
      this.predictionId = predictionId;
   }

   public PredictionUpdate(@Nonnull PredictionUpdate other) {
      this.predictionId = other.predictionId;
   }

   @Nonnull
   public static PredictionUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      PredictionUpdate obj = new PredictionUpdate();
      obj.predictionId = PacketIO.readUUID(buf, offset + 0);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 16;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      PacketIO.writeUUID(buf, this.predictionId);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      return 16;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 16 ? ValidationResult.error("Buffer too small: expected at least 16 bytes") : ValidationResult.OK;
   }

   public PredictionUpdate clone() {
      PredictionUpdate copy = new PredictionUpdate();
      copy.predictionId = this.predictionId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof PredictionUpdate other ? Objects.equals(this.predictionId, other.predictionId) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.predictionId);
   }
}
