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
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3fc;

public class TriggerVolumeToolResize implements Packet, ToServerPacket {
   public static final int PACKET_ID = 482;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 38;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 38;
   public static final int MAX_SIZE = 16384043;
   @Nonnull
   public String volumeId = "";
   @Nonnull
   public TriggerVolumeShapeType shapeType = TriggerVolumeShapeType.Box;
   @Nonnull
   public Vector3fc param1 = PacketIO.ZERO_VECTOR3;
   @Nonnull
   public Vector3fc param2 = PacketIO.ZERO_VECTOR3;
   @Nullable
   public Vector3fc newPosition;

   @Override
   public int getId() {
      return 482;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public TriggerVolumeToolResize() {
   }

   public TriggerVolumeToolResize(
      @Nonnull String volumeId,
      @Nonnull TriggerVolumeShapeType shapeType,
      @Nonnull Vector3fc param1,
      @Nonnull Vector3fc param2,
      @Nullable Vector3fc newPosition
   ) {
      this.volumeId = volumeId;
      this.shapeType = shapeType;
      this.param1 = param1;
      this.param2 = param2;
      this.newPosition = newPosition;
   }

   public TriggerVolumeToolResize(@Nonnull TriggerVolumeToolResize other) {
      this.volumeId = other.volumeId;
      this.shapeType = other.shapeType;
      this.param1 = other.param1;
      this.param2 = other.param2;
      this.newPosition = other.newPosition;
   }

   @Nonnull
   public static TriggerVolumeToolResize deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 38) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolResize", 38, buf.readableBytes() - offset);
      } else {
         TriggerVolumeToolResize obj = new TriggerVolumeToolResize();
         byte nullBits = buf.getByte(offset);
         obj.shapeType = TriggerVolumeShapeType.fromValue(buf.getByte(offset + 1));
         obj.param1 = PacketIO.readVector3f(buf, offset + 2);
         obj.param2 = PacketIO.readVector3f(buf, offset + 14);
         if ((nullBits & 1) != 0) {
            obj.newPosition = PacketIO.readVector3f(buf, offset + 26);
         }

         int pos = offset + 38;
         int volumeIdLen = VarInt.peek(buf, pos);
         if (volumeIdLen < 0) {
            throw ProtocolException.invalidVarInt("VolumeId");
         } else {
            int volumeIdVarLen = VarInt.size(volumeIdLen);
            if (volumeIdLen > 4096000) {
               throw ProtocolException.stringTooLong("VolumeId", volumeIdLen, 4096000);
            } else if (pos + volumeIdVarLen + volumeIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("VolumeId", pos + volumeIdVarLen + volumeIdLen, buf.readableBytes());
            } else {
               obj.volumeId = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
               pos += volumeIdVarLen + volumeIdLen;
               return obj;
            }
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 38;
      int sl = VarInt.peek(buf, pos);
      pos += VarInt.size(sl) + sl;
      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 38L;
   }

   public static String getVolumeId(MemorySegment mem) {
      return getVolumeId(mem, 0);
   }

   public static String getVolumeId(MemorySegment mem, int offset) {
      return PacketIO.readVarString("VolumeId", mem, offset + 38, 4096000, PacketIO.UTF8);
   }

   public static TriggerVolumeShapeType getShapeType(MemorySegment mem) {
      return getShapeType(mem, 0);
   }

   public static TriggerVolumeShapeType getShapeType(MemorySegment mem, int offset) {
      return TriggerVolumeShapeType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static Vector3fc getParam1(MemorySegment mem) {
      return getParam1(mem, 0);
   }

   public static Vector3fc getParam1(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 2);
   }

   public static Vector3fc getParam2(MemorySegment mem) {
      return getParam2(mem, 0);
   }

   public static Vector3fc getParam2(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 14);
   }

   @Nullable
   public static Vector3fc getNewPosition(MemorySegment mem) {
      return getNewPosition(mem, 0);
   }

   @Nullable
   public static Vector3fc getNewPosition(MemorySegment mem, int offset) {
      return hasNewPosition(mem, offset) ? PacketIO.readVector3f(mem, offset + 26) : null;
   }

   public static boolean hasNewPosition(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static TriggerVolumeToolResize toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static TriggerVolumeToolResize toObject(MemorySegment mem, int offset) {
      if (offset + 38 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolResize", offset + 38, (int)mem.byteSize());
      } else {
         return new TriggerVolumeToolResize(
            PacketIO.readVarString("VolumeId", mem, offset + 38, 4096000, PacketIO.UTF8),
            TriggerVolumeShapeType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            PacketIO.readVector3f(mem, offset + 2),
            PacketIO.readVector3f(mem, offset + 14),
            hasNewPosition(mem, offset) ? PacketIO.readVector3f(mem, offset + 26) : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.newPosition != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.shapeType.getValue());
      PacketIO.writeVector3f(buf, this.param1);
      PacketIO.writeVector3f(buf, this.param2);
      if (this.newPosition != null) {
         PacketIO.writeVector3f(buf, this.newPosition);
      } else {
         buf.writeZero(12);
      }

      PacketIO.writeVarString(buf, this.volumeId, 4096000);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.newPosition != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.shapeType.getValue());
      PacketIO.writeVector3f(mem, offset + 2, this.param1);
      PacketIO.writeVector3f(mem, offset + 14, this.param2);
      if (this.newPosition != null) {
         PacketIO.writeVector3f(mem, offset + 26, this.newPosition);
      } else {
         mem.asSlice(offset + 26, 12L).fill((byte)0);
      }

      int varOffset = offset + 38;
      varOffset += PacketIO.writeVarString(mem, varOffset, this.volumeId, 4096000);
      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 38;
      return size + PacketIO.stringSize(this.volumeId);
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 38) {
         return ValidationResult.error("Buffer too small: expected at least 38 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid TriggerVolumeShapeType value for ShapeType");
         } else {
            v = offset + 38;
            int volumeIdLen = VarInt.peek(buffer, v);
            if (volumeIdLen < 0) {
               return ValidationResult.error("Invalid string length for VolumeId");
            } else if (volumeIdLen > 4096000) {
               return ValidationResult.error("VolumeId exceeds max length 4096000");
            } else {
               v += VarInt.size(volumeIdLen);
               v += volumeIdLen;
               return v > buffer.writerIndex() ? ValidationResult.error("Buffer overflow reading VolumeId") : ValidationResult.OK;
            }
         }
      }
   }

   public TriggerVolumeToolResize clone() {
      TriggerVolumeToolResize copy = new TriggerVolumeToolResize();
      copy.volumeId = this.volumeId;
      copy.shapeType = this.shapeType;
      copy.param1 = this.param1;
      copy.param2 = this.param2;
      copy.newPosition = this.newPosition;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof TriggerVolumeToolResize other)
            ? false
            : Objects.equals(this.volumeId, other.volumeId)
               && Objects.equals(this.shapeType, other.shapeType)
               && Objects.equals(this.param1, other.param1)
               && Objects.equals(this.param2, other.param2)
               && Objects.equals(this.newPosition, other.newPosition);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.volumeId, this.shapeType, this.param1, this.param2, this.newPosition);
   }
}
