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

public class SetChunk implements Packet, ToClientPacket {
   public static final int PACKET_ID = 131;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 13;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 25;
   public static final int MAX_SIZE = 12288040;
   public int x;
   public int y;
   public int z;
   @Nullable
   public byte[] localLight;
   @Nullable
   public byte[] globalLight;
   @Nullable
   public byte[] data;

   @Override
   public int getId() {
      return 131;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Chunks;
   }

   public SetChunk() {
   }

   public SetChunk(int x, int y, int z, @Nullable byte[] localLight, @Nullable byte[] globalLight, @Nullable byte[] data) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.localLight = localLight;
      this.globalLight = globalLight;
      this.data = data;
   }

   public SetChunk(@Nonnull SetChunk other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
      this.localLight = other.localLight;
      this.globalLight = other.globalLight;
      this.data = other.data;
   }

   @Nonnull
   public static SetChunk deserialize(@Nonnull ByteBuf buf, int offset) {
      SetChunk obj = new SetChunk();
      byte nullBits = buf.getByte(offset);
      obj.x = buf.getIntLE(offset + 1);
      obj.y = buf.getIntLE(offset + 5);
      obj.z = buf.getIntLE(offset + 9);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 25 + buf.getIntLE(offset + 13);
         int localLightCount = VarInt.peek(buf, varPos0);
         if (localLightCount < 0) {
            throw ProtocolException.negativeLength("LocalLight", localLightCount);
         }

         if (localLightCount > 4096000) {
            throw ProtocolException.arrayTooLong("LocalLight", localLightCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos0);
         if (varPos0 + varIntLen + localLightCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("LocalLight", varPos0 + varIntLen + localLightCount * 1, buf.readableBytes());
         }

         obj.localLight = new byte[localLightCount];

         for (int i = 0; i < localLightCount; i++) {
            obj.localLight[i] = buf.getByte(varPos0 + varIntLen + i * 1);
         }
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 25 + buf.getIntLE(offset + 17);
         int globalLightCount = VarInt.peek(buf, varPos1);
         if (globalLightCount < 0) {
            throw ProtocolException.negativeLength("GlobalLight", globalLightCount);
         }

         if (globalLightCount > 4096000) {
            throw ProtocolException.arrayTooLong("GlobalLight", globalLightCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + globalLightCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("GlobalLight", varPos1 + varIntLen + globalLightCount * 1, buf.readableBytes());
         }

         obj.globalLight = new byte[globalLightCount];

         for (int i = 0; i < globalLightCount; i++) {
            obj.globalLight[i] = buf.getByte(varPos1 + varIntLen + i * 1);
         }
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 25 + buf.getIntLE(offset + 21);
         int dataCount = VarInt.peek(buf, varPos2);
         if (dataCount < 0) {
            throw ProtocolException.negativeLength("Data", dataCount);
         }

         if (dataCount > 4096000) {
            throw ProtocolException.arrayTooLong("Data", dataCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos2);
         if (varPos2 + varIntLen + dataCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Data", varPos2 + varIntLen + dataCount * 1, buf.readableBytes());
         }

         obj.data = new byte[dataCount];

         for (int i = 0; i < dataCount; i++) {
            obj.data[i] = buf.getByte(varPos2 + varIntLen + i * 1);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 25;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 13);
         int pos0 = offset + 25 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + arrLen * 1;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 17);
         int pos1 = offset + 25 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + arrLen * 1;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 21);
         int pos2 = offset + 25 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + arrLen * 1;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.localLight != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.globalLight != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.data != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.x);
      buf.writeIntLE(this.y);
      buf.writeIntLE(this.z);
      int localLightOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int globalLightOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int dataOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.localLight != null) {
         buf.setIntLE(localLightOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.localLight.length > 4096000) {
            throw ProtocolException.arrayTooLong("LocalLight", this.localLight.length, 4096000);
         }

         VarInt.write(buf, this.localLight.length);

         for (byte item : this.localLight) {
            buf.writeByte(item);
         }
      } else {
         buf.setIntLE(localLightOffsetSlot, -1);
      }

      if (this.globalLight != null) {
         buf.setIntLE(globalLightOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.globalLight.length > 4096000) {
            throw ProtocolException.arrayTooLong("GlobalLight", this.globalLight.length, 4096000);
         }

         VarInt.write(buf, this.globalLight.length);

         for (byte item : this.globalLight) {
            buf.writeByte(item);
         }
      } else {
         buf.setIntLE(globalLightOffsetSlot, -1);
      }

      if (this.data != null) {
         buf.setIntLE(dataOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.data.length > 4096000) {
            throw ProtocolException.arrayTooLong("Data", this.data.length, 4096000);
         }

         VarInt.write(buf, this.data.length);

         for (byte item : this.data) {
            buf.writeByte(item);
         }
      } else {
         buf.setIntLE(dataOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 25;
      if (this.localLight != null) {
         size += VarInt.size(this.localLight.length) + this.localLight.length * 1;
      }

      if (this.globalLight != null) {
         size += VarInt.size(this.globalLight.length) + this.globalLight.length * 1;
      }

      if (this.data != null) {
         size += VarInt.size(this.data.length) + this.data.length * 1;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 25) {
         return ValidationResult.error("Buffer too small: expected at least 25 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int localLightOffset = buffer.getIntLE(offset + 13);
            if (localLightOffset < 0) {
               return ValidationResult.error("Invalid offset for LocalLight");
            }

            int pos = offset + 25 + localLightOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for LocalLight");
            }

            int localLightCount = VarInt.peek(buffer, pos);
            if (localLightCount < 0) {
               return ValidationResult.error("Invalid array count for LocalLight");
            }

            if (localLightCount > 4096000) {
               return ValidationResult.error("LocalLight exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += localLightCount * 1;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading LocalLight");
            }
         }

         if ((nullBits & 2) != 0) {
            int globalLightOffset = buffer.getIntLE(offset + 17);
            if (globalLightOffset < 0) {
               return ValidationResult.error("Invalid offset for GlobalLight");
            }

            int posx = offset + 25 + globalLightOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for GlobalLight");
            }

            int globalLightCount = VarInt.peek(buffer, posx);
            if (globalLightCount < 0) {
               return ValidationResult.error("Invalid array count for GlobalLight");
            }

            if (globalLightCount > 4096000) {
               return ValidationResult.error("GlobalLight exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += globalLightCount * 1;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading GlobalLight");
            }
         }

         if ((nullBits & 4) != 0) {
            int dataOffset = buffer.getIntLE(offset + 21);
            if (dataOffset < 0) {
               return ValidationResult.error("Invalid offset for Data");
            }

            int posxx = offset + 25 + dataOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Data");
            }

            int dataCount = VarInt.peek(buffer, posxx);
            if (dataCount < 0) {
               return ValidationResult.error("Invalid array count for Data");
            }

            if (dataCount > 4096000) {
               return ValidationResult.error("Data exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += dataCount * 1;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Data");
            }
         }

         return ValidationResult.OK;
      }
   }

   public SetChunk clone() {
      SetChunk copy = new SetChunk();
      copy.x = this.x;
      copy.y = this.y;
      copy.z = this.z;
      copy.localLight = this.localLight != null ? Arrays.copyOf(this.localLight, this.localLight.length) : null;
      copy.globalLight = this.globalLight != null ? Arrays.copyOf(this.globalLight, this.globalLight.length) : null;
      copy.data = this.data != null ? Arrays.copyOf(this.data, this.data.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SetChunk other)
            ? false
            : this.x == other.x
               && this.y == other.y
               && this.z == other.z
               && Arrays.equals(this.localLight, other.localLight)
               && Arrays.equals(this.globalLight, other.globalLight)
               && Arrays.equals(this.data, other.data);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.x);
      result = 31 * result + Integer.hashCode(this.y);
      result = 31 * result + Integer.hashCode(this.z);
      result = 31 * result + Arrays.hashCode(this.localLight);
      result = 31 * result + Arrays.hashCode(this.globalLight);
      return 31 * result + Arrays.hashCode(this.data);
   }
}
