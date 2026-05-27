package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.joml.Vector3fc;

public class TriggerVolumeToolMultiMove implements Packet, ToServerPacket {
   public static final int PACKET_ID = 491;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public String[] volumeIds = new String[0];
   @Nonnull
   public Vector3fc moveDelta = PacketIO.ZERO_VECTOR3;

   @Override
   public int getId() {
      return 491;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public TriggerVolumeToolMultiMove() {
   }

   public TriggerVolumeToolMultiMove(@Nonnull String[] volumeIds, @Nonnull Vector3fc moveDelta) {
      this.volumeIds = volumeIds;
      this.moveDelta = moveDelta;
   }

   public TriggerVolumeToolMultiMove(@Nonnull TriggerVolumeToolMultiMove other) {
      this.volumeIds = other.volumeIds;
      this.moveDelta = other.moveDelta;
   }

   @Nonnull
   public static TriggerVolumeToolMultiMove deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 12) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolMultiMove", 12, buf.readableBytes() - offset);
      } else {
         TriggerVolumeToolMultiMove obj = new TriggerVolumeToolMultiMove();
         obj.moveDelta = PacketIO.readVector3f(buf, offset + 0);
         int pos = offset + 12;
         int volumeIdsCount = VarInt.peek(buf, pos);
         if (volumeIdsCount < 0) {
            throw ProtocolException.invalidVarInt("VolumeIds");
         } else {
            int volumeIdsVarLen = VarInt.size(volumeIdsCount);
            if (volumeIdsCount > 4096000) {
               throw ProtocolException.arrayTooLong("VolumeIds", volumeIdsCount, 4096000);
            } else if (pos + volumeIdsVarLen + volumeIdsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("VolumeIds", pos + volumeIdsVarLen + volumeIdsCount * 1, buf.readableBytes());
            } else {
               pos += volumeIdsVarLen;
               obj.volumeIds = new String[volumeIdsCount];

               for (int i = 0; i < volumeIdsCount; i++) {
                  int strLen = VarInt.peek(buf, pos);
                  if (strLen < 0) {
                     throw ProtocolException.invalidVarInt("volumeIds[" + i + "]");
                  }

                  int strVarLen = VarInt.size(strLen);
                  if (strLen > 4096000) {
                     throw ProtocolException.stringTooLong("volumeIds[" + i + "]", strLen, 4096000);
                  }

                  if (pos + strVarLen + strLen > buf.readableBytes()) {
                     throw ProtocolException.bufferTooSmall("volumeIds[" + i + "]", pos + strVarLen + strLen, buf.readableBytes());
                  }

                  obj.volumeIds[i] = PacketIO.readVarString(buf, pos);
                  pos += strVarLen + strLen;
               }

               return obj;
            }
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 12;
      int arrLen = VarInt.peek(buf, pos);
      pos += VarInt.size(arrLen);

      for (int i = 0; i < arrLen; i++) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 12L;
   }

   public static String[] getVolumeIds(MemorySegment mem) {
      return getVolumeIds(mem, 0);
   }

   public static String[] getVolumeIds(MemorySegment mem, int offset) {
      int off = offset + 12;
      long packed = VarInt.getWithLength(mem, off);
      int len = (int)packed;
      if (len < 0) {
         throw ProtocolException.negativeLength("VolumeIds", len);
      } else if (len > 4096000) {
         throw ProtocolException.arrayTooLong("VolumeIds", len, 4096000);
      } else {
         int lenOffset = (int)(packed >>> 32);
         if (off + lenOffset + len > mem.byteSize()) {
            throw ProtocolException.bufferTooSmall("VolumeIds", off + lenOffset + len, (int)mem.byteSize());
         } else {
            off += lenOffset;
            String[] data = new String[len];

            for (int i = 0; i < len; i++) {
               long sp = VarInt.getWithLength(mem, off);
               int n = (int)sp + (int)(sp >>> 32);
               data[i] = PacketIO.readVarString("VolumeIds", mem, off, 16384000, PacketIO.UTF8);
               off += n;
            }

            return data;
         }
      }
   }

   public static Vector3fc getMoveDelta(MemorySegment mem) {
      return getMoveDelta(mem, 0);
   }

   public static Vector3fc getMoveDelta(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 0);
   }

   public static TriggerVolumeToolMultiMove toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static TriggerVolumeToolMultiMove toObject(MemorySegment mem, int offset) {
      if (offset + 12 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolMultiMove", offset + 12, (int)mem.byteSize());
      } else {
         int off = offset + 12;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("VolumeIds", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("VolumeIds", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("VolumeIds", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] volumeIds = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  volumeIds[i] = PacketIO.readVarString("VolumeIds", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return new TriggerVolumeToolMultiMove(volumeIds, PacketIO.readVector3f(mem, offset + 0));
            }
         }
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      PacketIO.writeVector3f(buf, this.moveDelta);
      if (this.volumeIds.length > 4096000) {
         throw ProtocolException.arrayTooLong("VolumeIds", this.volumeIds.length, 4096000);
      } else {
         VarInt.write(buf, this.volumeIds.length);

         for (String item : this.volumeIds) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      PacketIO.writeVector3f(mem, offset + 0, this.moveDelta);
      int varOffset = offset + 12;
      if (this.volumeIds.length > 4096000) {
         throw ProtocolException.arrayTooLong("VolumeIds", this.volumeIds.length, 4096000);
      } else {
         varOffset += VarInt.set(mem, varOffset, this.volumeIds.length);
         int volumeIdsValueOffset = 0;

         for (int i = 0; i < this.volumeIds.length; i++) {
            volumeIdsValueOffset += PacketIO.writeVarString(mem, varOffset + volumeIdsValueOffset, this.volumeIds[i], 16384000);
         }

         varOffset += volumeIdsValueOffset;
         return varOffset - offset;
      }
   }

   @Override
   public int computeSize() {
      int size = 12;
      int volumeIdsSize = 0;

      for (String elem : this.volumeIds) {
         volumeIdsSize += PacketIO.stringSize(elem);
      }

      return size + VarInt.size(this.volumeIds.length) + volumeIdsSize;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 12) {
         return ValidationResult.error("Buffer too small: expected at least 12 bytes");
      } else {
         int pos = offset + 12;
         int volumeIdsCount = VarInt.peek(buffer, pos);
         if (volumeIdsCount < 0) {
            return ValidationResult.error("Invalid array count for VolumeIds");
         } else if (volumeIdsCount > 4096000) {
            return ValidationResult.error("VolumeIds exceeds max length 4096000");
         } else {
            pos += VarInt.size(volumeIdsCount);

            for (int i = 0; i < volumeIdsCount; i++) {
               int strLen = VarInt.peek(buffer, pos);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in VolumeIds");
               }

               pos += VarInt.size(strLen);
               pos += strLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in VolumeIds");
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public TriggerVolumeToolMultiMove clone() {
      TriggerVolumeToolMultiMove copy = new TriggerVolumeToolMultiMove();
      copy.volumeIds = Arrays.copyOf(this.volumeIds, this.volumeIds.length);
      copy.moveDelta = this.moveDelta;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof TriggerVolumeToolMultiMove other)
            ? false
            : Arrays.equals((Object[])this.volumeIds, (Object[])other.volumeIds) && Objects.equals(this.moveDelta, other.moveDelta);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode((Object[])this.volumeIds);
      return 31 * result + Objects.hashCode(this.moveDelta);
   }
}
