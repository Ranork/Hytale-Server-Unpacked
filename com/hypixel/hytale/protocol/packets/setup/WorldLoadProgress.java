package com.hypixel.hytale.protocol.packets.setup;

import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldLoadProgress implements Packet, ToClientPacket {
   public static final int PACKET_ID = 21;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public FormattedMessage status;
   public int percentComplete;
   public int percentCompleteSubitem;

   @Override
   public int getId() {
      return 21;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public WorldLoadProgress() {
   }

   public WorldLoadProgress(@Nullable FormattedMessage status, int percentComplete, int percentCompleteSubitem) {
      this.status = status;
      this.percentComplete = percentComplete;
      this.percentCompleteSubitem = percentCompleteSubitem;
   }

   public WorldLoadProgress(@Nonnull WorldLoadProgress other) {
      this.status = other.status;
      this.percentComplete = other.percentComplete;
      this.percentCompleteSubitem = other.percentCompleteSubitem;
   }

   @Nonnull
   public static WorldLoadProgress deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("WorldLoadProgress", 9, buf.readableBytes() - offset);
      } else {
         WorldLoadProgress obj = new WorldLoadProgress();
         byte nullBits = buf.getByte(offset);
         obj.percentComplete = buf.getIntLE(offset + 1);
         obj.percentCompleteSubitem = buf.getIntLE(offset + 5);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            obj.status = FormattedMessage.deserialize(buf, pos);
            pos += FormattedMessage.computeBytesConsumed(buf, pos);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         pos += FormattedMessage.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   @Nullable
   public static FormattedMessage getStatus(MemorySegment mem) {
      return getStatus(mem, 0);
   }

   @Nullable
   public static FormattedMessage getStatus(MemorySegment mem, int offset) {
      return hasStatus(mem, offset) ? FormattedMessage.toObject(mem, offset + 9) : null;
   }

   public static int getPercentComplete(MemorySegment mem) {
      return getPercentComplete(mem, 0);
   }

   public static int getPercentComplete(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getPercentCompleteSubitem(MemorySegment mem) {
      return getPercentCompleteSubitem(mem, 0);
   }

   public static int getPercentCompleteSubitem(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   public static boolean hasStatus(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static WorldLoadProgress toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static WorldLoadProgress toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("WorldLoadProgress", offset + 9, (int)mem.byteSize());
      } else {
         return new WorldLoadProgress(
            hasStatus(mem, offset) ? FormattedMessage.toObject(mem, offset + 9) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.status != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.percentComplete);
      buf.writeIntLE(this.percentCompleteSubitem);
      if (this.status != null) {
         this.status.serialize(buf);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.status != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.percentComplete);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.percentCompleteSubitem);
      int varOffset = offset + 9;
      if (this.status != null) {
         varOffset += this.status.serialize(mem, varOffset);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 9;
      if (this.status != null) {
         size += this.status.computeSize();
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
            ValidationResult statusResult = FormattedMessage.validateStructure(buffer, pos);
            if (!statusResult.isValid()) {
               return ValidationResult.error("Invalid Status: " + statusResult.error());
            }

            pos += FormattedMessage.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public WorldLoadProgress clone() {
      WorldLoadProgress copy = new WorldLoadProgress();
      copy.status = this.status != null ? this.status.clone() : null;
      copy.percentComplete = this.percentComplete;
      copy.percentCompleteSubitem = this.percentCompleteSubitem;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof WorldLoadProgress other)
            ? false
            : Objects.equals(this.status, other.status)
               && this.percentComplete == other.percentComplete
               && this.percentCompleteSubitem == other.percentCompleteSubitem;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.status, this.percentComplete, this.percentCompleteSubitem);
   }
}
