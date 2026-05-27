package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 25) {
         throw ProtocolException.bufferTooSmall("SetChunk", 25, buf.readableBytes() - offset);
      } else {
         SetChunk obj = new SetChunk();
         byte nullBits = buf.getByte(offset);
         obj.x = buf.getIntLE(offset + 1);
         obj.y = buf.getIntLE(offset + 5);
         obj.z = buf.getIntLE(offset + 9);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 13);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 25) {
               throw ProtocolException.invalidOffset("LocalLight", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 25 + varPosBase0;
            int localLightCount = VarInt.peek(buf, varPos0);
            if (localLightCount < 0) {
               throw ProtocolException.invalidVarInt("LocalLight");
            }

            int varIntLen = VarInt.size(localLightCount);
            if (localLightCount > 4096000) {
               throw ProtocolException.arrayTooLong("LocalLight", localLightCount, 4096000);
            }

            if (varPos0 + varIntLen + localLightCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("LocalLight", varPos0 + varIntLen + localLightCount * 1, buf.readableBytes());
            }

            obj.localLight = new byte[localLightCount];

            for (int i = 0; i < localLightCount; i++) {
               obj.localLight[i] = buf.getByte(varPos0 + varIntLen + i * 1);
            }
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 17);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 25) {
               throw ProtocolException.invalidOffset("GlobalLight", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 25 + varPosBase1;
            int globalLightCount = VarInt.peek(buf, varPos1);
            if (globalLightCount < 0) {
               throw ProtocolException.invalidVarInt("GlobalLight");
            }

            int varIntLenx = VarInt.size(globalLightCount);
            if (globalLightCount > 4096000) {
               throw ProtocolException.arrayTooLong("GlobalLight", globalLightCount, 4096000);
            }

            if (varPos1 + varIntLenx + globalLightCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("GlobalLight", varPos1 + varIntLenx + globalLightCount * 1, buf.readableBytes());
            }

            obj.globalLight = new byte[globalLightCount];

            for (int i = 0; i < globalLightCount; i++) {
               obj.globalLight[i] = buf.getByte(varPos1 + varIntLenx + i * 1);
            }
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 21);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 25) {
               throw ProtocolException.invalidOffset("Data", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 25 + varPosBase2;
            int dataCount = VarInt.peek(buf, varPos2);
            if (dataCount < 0) {
               throw ProtocolException.invalidVarInt("Data");
            }

            int varIntLenxx = VarInt.size(dataCount);
            if (dataCount > 4096000) {
               throw ProtocolException.arrayTooLong("Data", dataCount, 4096000);
            }

            if (varPos2 + varIntLenxx + dataCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Data", varPos2 + varIntLenxx + dataCount * 1, buf.readableBytes());
            }

            obj.data = new byte[dataCount];

            for (int i = 0; i < dataCount; i++) {
               obj.data[i] = buf.getByte(varPos2 + varIntLenxx + i * 1);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 25;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 13);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 25) {
            throw ProtocolException.invalidOffset("LocalLight", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 25 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen) + arrLen * 1;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 17);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 25) {
            throw ProtocolException.invalidOffset("GlobalLight", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 25 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen) + arrLen * 1;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 21);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 25) {
            throw ProtocolException.invalidOffset("Data", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 25 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(arrLen) + arrLen * 1;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 25L;
   }

   public static int getX(MemorySegment mem) {
      return getX(mem, 0);
   }

   public static int getX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getY(MemorySegment mem) {
      return getY(mem, 0);
   }

   public static int getY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   public static int getZ(MemorySegment mem) {
      return getZ(mem, 0);
   }

   public static int getZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 9));
   }

   @Nullable
   public static byte[] getLocalLight(MemorySegment mem) {
      return getLocalLight(mem, 0);
   }

   @Nullable
   public static byte[] getLocalLight(MemorySegment mem, int offset) {
      if (!hasLocalLight(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 13, 25, "LocalLight");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("LocalLight", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("LocalLight", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("LocalLight", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               byte[] data = new byte[len];
               MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
               return data;
            }
         }
      }
   }

   @Nullable
   public static byte[] getGlobalLight(MemorySegment mem) {
      return getGlobalLight(mem, 0);
   }

   @Nullable
   public static byte[] getGlobalLight(MemorySegment mem, int offset) {
      if (!hasGlobalLight(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 17, 25, "GlobalLight");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("GlobalLight", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("GlobalLight", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("GlobalLight", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               byte[] data = new byte[len];
               MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
               return data;
            }
         }
      }
   }

   @Nullable
   public static byte[] getData(MemorySegment mem) {
      return getData(mem, 0);
   }

   @Nullable
   public static byte[] getData(MemorySegment mem, int offset) {
      if (!hasData(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 21, 25, "Data");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Data", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Data", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Data", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               byte[] data = new byte[len];
               MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static boolean hasLocalLight(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasGlobalLight(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasData(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static SetChunk toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SetChunk toObject(MemorySegment mem, int offset) {
      if (offset + 25 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SetChunk", offset + 25, (int)mem.byteSize());
      } else {
         byte[] localLight = null;
         if (hasLocalLight(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 13, 25, "LocalLight");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("LocalLight", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("LocalLight", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("LocalLight", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            localLight = new byte[len];
            MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, localLight, 0, len);
         }

         byte[] globalLight = null;
         if (hasGlobalLight(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 17, 25, "GlobalLight");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("GlobalLight", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("GlobalLight", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("GlobalLight", offx + lenOffset + lenx * 1, (int)mem.byteSize());
            }

            offx += lenOffset;
            globalLight = new byte[lenx];
            MemorySegment.copy(mem, PacketIO.PROTO_BYTE, offx, globalLight, 0, lenx);
         }

         byte[] data = null;
         if (hasData(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 21, 25, "Data");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("Data", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.arrayTooLong("Data", lenxx, 4096000);
            }

            int lenOffset = (int)(packedxx >>> 32);
            if (offxx + lenOffset + lenxx * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Data", offxx + lenOffset + lenxx * 1, (int)mem.byteSize());
            }

            offxx += lenOffset;
            data = new byte[lenxx];
            MemorySegment.copy(mem, PacketIO.PROTO_BYTE, offxx, data, 0, lenxx);
         }

         return new SetChunk(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 9)),
            localLight,
            globalLight,
            data
         );
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.x);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.y);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 9), this.z);
      int varOffset = offset + 25;
      if (this.localLight != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), varOffset - offset - 25);
         if (this.localLight.length > 4096000) {
            throw ProtocolException.arrayTooLong("LocalLight", this.localLight.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.localLight.length);
         MemorySegment.copy(this.localLight, 0, mem, PacketIO.PROTO_BYTE, varOffset, this.localLight.length);
         varOffset += this.localLight.length * 1;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), -1);
      }

      if (this.globalLight != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), varOffset - offset - 25);
         if (this.globalLight.length > 4096000) {
            throw ProtocolException.arrayTooLong("GlobalLight", this.globalLight.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.globalLight.length);
         MemorySegment.copy(this.globalLight, 0, mem, PacketIO.PROTO_BYTE, varOffset, this.globalLight.length);
         varOffset += this.globalLight.length * 1;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), -1);
      }

      if (this.data != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), varOffset - offset - 25);
         if (this.data.length > 4096000) {
            throw ProtocolException.arrayTooLong("Data", this.data.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.data.length);
         MemorySegment.copy(this.data, 0, mem, PacketIO.PROTO_BYTE, varOffset, this.data.length);
         varOffset += this.data.length * 1;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), -1);
      }

      return varOffset - offset;
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
            if (localLightOffset < 0 || localLightOffset > buffer.writerIndex() - offset - 25) {
               return ValidationResult.error("Invalid offset for LocalLight");
            }

            int pos = offset + 25 + localLightOffset;
            int localLightCount = VarInt.peek(buffer, pos);
            if (localLightCount < 0) {
               return ValidationResult.error("Invalid array count for LocalLight");
            }

            if (localLightCount > 4096000) {
               return ValidationResult.error("LocalLight exceeds max length 4096000");
            }

            pos += VarInt.size(localLightCount);
            pos += localLightCount * 1;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading LocalLight");
            }
         }

         if ((nullBits & 2) != 0) {
            int globalLightOffset = buffer.getIntLE(offset + 17);
            if (globalLightOffset < 0 || globalLightOffset > buffer.writerIndex() - offset - 25) {
               return ValidationResult.error("Invalid offset for GlobalLight");
            }

            int posx = offset + 25 + globalLightOffset;
            int globalLightCount = VarInt.peek(buffer, posx);
            if (globalLightCount < 0) {
               return ValidationResult.error("Invalid array count for GlobalLight");
            }

            if (globalLightCount > 4096000) {
               return ValidationResult.error("GlobalLight exceeds max length 4096000");
            }

            posx += VarInt.size(globalLightCount);
            posx += globalLightCount * 1;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading GlobalLight");
            }
         }

         if ((nullBits & 4) != 0) {
            int dataOffset = buffer.getIntLE(offset + 21);
            if (dataOffset < 0 || dataOffset > buffer.writerIndex() - offset - 25) {
               return ValidationResult.error("Invalid offset for Data");
            }

            int posxx = offset + 25 + dataOffset;
            int dataCount = VarInt.peek(buffer, posxx);
            if (dataCount < 0) {
               return ValidationResult.error("Invalid array count for Data");
            }

            if (dataCount > 4096000) {
               return ValidationResult.error("Data exceeds max length 4096000");
            }

            posxx += VarInt.size(dataCount);
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
