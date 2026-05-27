package com.hypixel.hytale.protocol.packets.player;

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

public class UpdateTriggerVolumeDisplay implements Packet, ToClientPacket {
   public static final int PACKET_ID = 470;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public TriggerVolumeDisplayEntry[] volumes;

   @Override
   public int getId() {
      return 470;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateTriggerVolumeDisplay() {
   }

   public UpdateTriggerVolumeDisplay(@Nullable TriggerVolumeDisplayEntry[] volumes) {
      this.volumes = volumes;
   }

   public UpdateTriggerVolumeDisplay(@Nonnull UpdateTriggerVolumeDisplay other) {
      this.volumes = other.volumes;
   }

   @Nonnull
   public static UpdateTriggerVolumeDisplay deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("UpdateTriggerVolumeDisplay", 1, buf.readableBytes() - offset);
      } else {
         UpdateTriggerVolumeDisplay obj = new UpdateTriggerVolumeDisplay();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int volumesCount = VarInt.peek(buf, pos);
            if (volumesCount < 0) {
               throw ProtocolException.invalidVarInt("Volumes");
            }

            int volumesVarLen = VarInt.size(volumesCount);
            if (volumesCount > 4096000) {
               throw ProtocolException.arrayTooLong("Volumes", volumesCount, 4096000);
            }

            if (pos + volumesVarLen + volumesCount * 59L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Volumes", pos + volumesVarLen + volumesCount * 59, buf.readableBytes());
            }

            pos += volumesVarLen;
            obj.volumes = new TriggerVolumeDisplayEntry[volumesCount];

            for (int i = 0; i < volumesCount; i++) {
               obj.volumes[i] = TriggerVolumeDisplayEntry.deserialize(buf, pos);
               pos += TriggerVolumeDisplayEntry.computeBytesConsumed(buf, pos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos += TriggerVolumeDisplayEntry.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static TriggerVolumeDisplayEntry[] getVolumes(MemorySegment mem) {
      return getVolumes(mem, 0);
   }

   @Nullable
   public static TriggerVolumeDisplayEntry[] getVolumes(MemorySegment mem, int offset) {
      if (!hasVolumes(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Volumes", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Volumes", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Volumes", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               TriggerVolumeDisplayEntry[] data = new TriggerVolumeDisplayEntry[len];

               for (int i = 0; i < len; i++) {
                  data[i] = TriggerVolumeDisplayEntry.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasVolumes(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateTriggerVolumeDisplay toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateTriggerVolumeDisplay toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateTriggerVolumeDisplay", offset + 1, (int)mem.byteSize());
      } else {
         TriggerVolumeDisplayEntry[] volumes = null;
         if (hasVolumes(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Volumes", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Volumes", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Volumes", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            volumes = new TriggerVolumeDisplayEntry[len];

            for (int i = 0; i < len; i++) {
               volumes[i] = TriggerVolumeDisplayEntry.toObject(mem, off);
               off += volumes[i].computeSize();
            }
         }

         return new UpdateTriggerVolumeDisplay(volumes);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.volumes != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.volumes != null) {
         if (this.volumes.length > 4096000) {
            throw ProtocolException.arrayTooLong("Volumes", this.volumes.length, 4096000);
         }

         VarInt.write(buf, this.volumes.length);

         for (TriggerVolumeDisplayEntry item : this.volumes) {
            item.serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.volumes != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.volumes != null) {
         if (this.volumes.length > 4096000) {
            throw ProtocolException.arrayTooLong("Volumes", this.volumes.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.volumes.length);
         int volumesValueOffset = 0;

         for (int i = 0; i < this.volumes.length; i++) {
            volumesValueOffset += this.volumes[i].serialize(mem, varOffset + volumesValueOffset);
         }

         varOffset += volumesValueOffset;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.volumes != null) {
         int volumesSize = 0;

         for (TriggerVolumeDisplayEntry elem : this.volumes) {
            volumesSize += elem.computeSize();
         }

         size += VarInt.size(this.volumes.length) + volumesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int volumesCount = VarInt.peek(buffer, pos);
            if (volumesCount < 0) {
               return ValidationResult.error("Invalid array count for Volumes");
            }

            if (volumesCount > 4096000) {
               return ValidationResult.error("Volumes exceeds max length 4096000");
            }

            pos += VarInt.size(volumesCount);

            for (int i = 0; i < volumesCount; i++) {
               ValidationResult structResult = TriggerVolumeDisplayEntry.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid TriggerVolumeDisplayEntry in Volumes[" + i + "]: " + structResult.error());
               }

               pos += TriggerVolumeDisplayEntry.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateTriggerVolumeDisplay clone() {
      UpdateTriggerVolumeDisplay copy = new UpdateTriggerVolumeDisplay();
      copy.volumes = this.volumes != null ? Arrays.stream(this.volumes).map(e -> e.clone()).toArray(TriggerVolumeDisplayEntry[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof UpdateTriggerVolumeDisplay other ? Arrays.equals((Object[])this.volumes, (Object[])other.volumes) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.volumes);
   }
}
