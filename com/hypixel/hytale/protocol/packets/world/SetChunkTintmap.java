package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SetChunkTintmap implements Packet, ToClientPacket {
   public static final int PACKET_ID = 133;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 4096014;
   public int x;
   public int z;
   @Nullable
   public byte[] tintmap;

   @Override
   public int getId() {
      return 133;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Chunks;
   }

   public SetChunkTintmap() {
   }

   public SetChunkTintmap(int x, int z, @Nullable byte[] tintmap) {
      this.x = x;
      this.z = z;
      this.tintmap = tintmap;
   }

   public SetChunkTintmap(@Nonnull SetChunkTintmap other) {
      this.x = other.x;
      this.z = other.z;
      this.tintmap = other.tintmap;
   }

   @Nonnull
   public static SetChunkTintmap deserialize(@Nonnull ByteBuf buf, int offset) {
      SetChunkTintmap obj = new SetChunkTintmap();
      byte nullBits = buf.getByte(offset);
      obj.x = buf.getIntLE(offset + 1);
      obj.z = buf.getIntLE(offset + 5);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         int tintmapCount = VarInt.peek(buf, pos);
         if (tintmapCount < 0) {
            throw ProtocolException.negativeLength("Tintmap", tintmapCount);
         }

         if (tintmapCount > 4096000) {
            throw ProtocolException.arrayTooLong("Tintmap", tintmapCount, 4096000);
         }

         int tintmapVarLen = VarInt.size(tintmapCount);
         if (pos + tintmapVarLen + tintmapCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Tintmap", pos + tintmapVarLen + tintmapCount * 1, buf.readableBytes());
         }

         pos += tintmapVarLen;
         obj.tintmap = new byte[tintmapCount];

         for (int i = 0; i < tintmapCount; i++) {
            obj.tintmap[i] = buf.getByte(pos + i * 1);
         }

         pos += tintmapCount * 1;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + arrLen * 1;
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.tintmap != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.x);
      buf.writeIntLE(this.z);
      if (this.tintmap != null) {
         if (this.tintmap.length > 4096000) {
            throw ProtocolException.arrayTooLong("Tintmap", this.tintmap.length, 4096000);
         }

         VarInt.write(buf, this.tintmap.length);

         for (byte item : this.tintmap) {
            buf.writeByte(item);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 9;
      if (this.tintmap != null) {
         size += VarInt.size(this.tintmap.length) + this.tintmap.length * 1;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            int tintmapCount = VarInt.peek(buffer, pos);
            if (tintmapCount < 0) {
               return ValidationResult.error("Invalid array count for Tintmap");
            }

            if (tintmapCount > 4096000) {
               return ValidationResult.error("Tintmap exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += tintmapCount * 1;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Tintmap");
            }
         }

         return ValidationResult.OK;
      }
   }

   public SetChunkTintmap clone() {
      SetChunkTintmap copy = new SetChunkTintmap();
      copy.x = this.x;
      copy.z = this.z;
      copy.tintmap = this.tintmap != null ? Arrays.copyOf(this.tintmap, this.tintmap.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SetChunkTintmap other) ? false : this.x == other.x && this.z == other.z && Arrays.equals(this.tintmap, other.tintmap);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.x);
      result = 31 * result + Integer.hashCode(this.z);
      return 31 * result + Arrays.hashCode(this.tintmap);
   }
}
