package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StairConnectedBlockRuleSet {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 21;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 21;
   public static final int MAX_SIZE = 16384026;
   public int straightBlockId;
   public int cornerLeftBlockId;
   public int cornerRightBlockId;
   public int invertedCornerLeftBlockId;
   public int invertedCornerRightBlockId;
   @Nullable
   public String materialName;

   public StairConnectedBlockRuleSet() {
   }

   public StairConnectedBlockRuleSet(
      int straightBlockId,
      int cornerLeftBlockId,
      int cornerRightBlockId,
      int invertedCornerLeftBlockId,
      int invertedCornerRightBlockId,
      @Nullable String materialName
   ) {
      this.straightBlockId = straightBlockId;
      this.cornerLeftBlockId = cornerLeftBlockId;
      this.cornerRightBlockId = cornerRightBlockId;
      this.invertedCornerLeftBlockId = invertedCornerLeftBlockId;
      this.invertedCornerRightBlockId = invertedCornerRightBlockId;
      this.materialName = materialName;
   }

   public StairConnectedBlockRuleSet(@Nonnull StairConnectedBlockRuleSet other) {
      this.straightBlockId = other.straightBlockId;
      this.cornerLeftBlockId = other.cornerLeftBlockId;
      this.cornerRightBlockId = other.cornerRightBlockId;
      this.invertedCornerLeftBlockId = other.invertedCornerLeftBlockId;
      this.invertedCornerRightBlockId = other.invertedCornerRightBlockId;
      this.materialName = other.materialName;
   }

   @Nonnull
   public static StairConnectedBlockRuleSet deserialize(@Nonnull ByteBuf buf, int offset) {
      StairConnectedBlockRuleSet obj = new StairConnectedBlockRuleSet();
      byte nullBits = buf.getByte(offset);
      obj.straightBlockId = buf.getIntLE(offset + 1);
      obj.cornerLeftBlockId = buf.getIntLE(offset + 5);
      obj.cornerRightBlockId = buf.getIntLE(offset + 9);
      obj.invertedCornerLeftBlockId = buf.getIntLE(offset + 13);
      obj.invertedCornerRightBlockId = buf.getIntLE(offset + 17);
      int pos = offset + 21;
      if ((nullBits & 1) != 0) {
         int materialNameLen = VarInt.peek(buf, pos);
         if (materialNameLen < 0) {
            throw ProtocolException.negativeLength("MaterialName", materialNameLen);
         }

         if (materialNameLen > 4096000) {
            throw ProtocolException.stringTooLong("MaterialName", materialNameLen, 4096000);
         }

         int materialNameVarLen = VarInt.length(buf, pos);
         obj.materialName = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += materialNameVarLen + materialNameLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 21;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.materialName != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.straightBlockId);
      buf.writeIntLE(this.cornerLeftBlockId);
      buf.writeIntLE(this.cornerRightBlockId);
      buf.writeIntLE(this.invertedCornerLeftBlockId);
      buf.writeIntLE(this.invertedCornerRightBlockId);
      if (this.materialName != null) {
         PacketIO.writeVarString(buf, this.materialName, 4096000);
      }
   }

   public int computeSize() {
      int size = 21;
      if (this.materialName != null) {
         size += PacketIO.stringSize(this.materialName);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 21) {
         return ValidationResult.error("Buffer too small: expected at least 21 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 21;
         if ((nullBits & 1) != 0) {
            int materialNameLen = VarInt.peek(buffer, pos);
            if (materialNameLen < 0) {
               return ValidationResult.error("Invalid string length for MaterialName");
            }

            if (materialNameLen > 4096000) {
               return ValidationResult.error("MaterialName exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += materialNameLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading MaterialName");
            }
         }

         return ValidationResult.OK;
      }
   }

   public StairConnectedBlockRuleSet clone() {
      StairConnectedBlockRuleSet copy = new StairConnectedBlockRuleSet();
      copy.straightBlockId = this.straightBlockId;
      copy.cornerLeftBlockId = this.cornerLeftBlockId;
      copy.cornerRightBlockId = this.cornerRightBlockId;
      copy.invertedCornerLeftBlockId = this.invertedCornerLeftBlockId;
      copy.invertedCornerRightBlockId = this.invertedCornerRightBlockId;
      copy.materialName = this.materialName;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof StairConnectedBlockRuleSet other)
            ? false
            : this.straightBlockId == other.straightBlockId
               && this.cornerLeftBlockId == other.cornerLeftBlockId
               && this.cornerRightBlockId == other.cornerRightBlockId
               && this.invertedCornerLeftBlockId == other.invertedCornerLeftBlockId
               && this.invertedCornerRightBlockId == other.invertedCornerRightBlockId
               && Objects.equals(this.materialName, other.materialName);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.straightBlockId,
         this.cornerLeftBlockId,
         this.cornerRightBlockId,
         this.invertedCornerLeftBlockId,
         this.invertedCornerRightBlockId,
         this.materialName
      );
   }
}
