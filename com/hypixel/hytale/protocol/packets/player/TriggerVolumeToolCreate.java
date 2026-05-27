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

public class TriggerVolumeToolCreate implements Packet, ToServerPacket {
   public static final int PACKET_ID = 480;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 38;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 38;
   public static final int MAX_SIZE = 16384043;
   @Nonnull
   public TriggerVolumeShapeType shapeType = TriggerVolumeShapeType.Box;
   @Nonnull
   public Vector3fc position = PacketIO.ZERO_VECTOR3;
   @Nonnull
   public Vector3fc param1 = PacketIO.ZERO_VECTOR3;
   @Nonnull
   public Vector3fc param2 = PacketIO.ZERO_VECTOR3;
   @Nullable
   public String name;

   @Override
   public int getId() {
      return 480;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public TriggerVolumeToolCreate() {
   }

   public TriggerVolumeToolCreate(
      @Nonnull TriggerVolumeShapeType shapeType, @Nonnull Vector3fc position, @Nonnull Vector3fc param1, @Nonnull Vector3fc param2, @Nullable String name
   ) {
      this.shapeType = shapeType;
      this.position = position;
      this.param1 = param1;
      this.param2 = param2;
      this.name = name;
   }

   public TriggerVolumeToolCreate(@Nonnull TriggerVolumeToolCreate other) {
      this.shapeType = other.shapeType;
      this.position = other.position;
      this.param1 = other.param1;
      this.param2 = other.param2;
      this.name = other.name;
   }

   @Nonnull
   public static TriggerVolumeToolCreate deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 38) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolCreate", 38, buf.readableBytes() - offset);
      } else {
         TriggerVolumeToolCreate obj = new TriggerVolumeToolCreate();
         byte nullBits = buf.getByte(offset);
         obj.shapeType = TriggerVolumeShapeType.fromValue(buf.getByte(offset + 1));
         obj.position = PacketIO.readVector3f(buf, offset + 2);
         obj.param1 = PacketIO.readVector3f(buf, offset + 14);
         obj.param2 = PacketIO.readVector3f(buf, offset + 26);
         int pos = offset + 38;
         if ((nullBits & 1) != 0) {
            int nameLen = VarInt.peek(buf, pos);
            if (nameLen < 0) {
               throw ProtocolException.invalidVarInt("Name");
            }

            int nameVarLen = VarInt.size(nameLen);
            if (nameLen > 4096000) {
               throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
            }

            if (pos + nameVarLen + nameLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Name", pos + nameVarLen + nameLen, buf.readableBytes());
            }

            obj.name = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += nameVarLen + nameLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 38;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 38L;
   }

   public static TriggerVolumeShapeType getShapeType(MemorySegment mem) {
      return getShapeType(mem, 0);
   }

   public static TriggerVolumeShapeType getShapeType(MemorySegment mem, int offset) {
      return TriggerVolumeShapeType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static Vector3fc getPosition(MemorySegment mem) {
      return getPosition(mem, 0);
   }

   public static Vector3fc getPosition(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 2);
   }

   public static Vector3fc getParam1(MemorySegment mem) {
      return getParam1(mem, 0);
   }

   public static Vector3fc getParam1(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 14);
   }

   public static Vector3fc getParam2(MemorySegment mem) {
      return getParam2(mem, 0);
   }

   public static Vector3fc getParam2(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 26);
   }

   @Nullable
   public static String getName(MemorySegment mem) {
      return getName(mem, 0);
   }

   @Nullable
   public static String getName(MemorySegment mem, int offset) {
      return hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + 38, 4096000, PacketIO.UTF8) : null;
   }

   public static boolean hasName(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static TriggerVolumeToolCreate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static TriggerVolumeToolCreate toObject(MemorySegment mem, int offset) {
      if (offset + 38 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolCreate", offset + 38, (int)mem.byteSize());
      } else {
         return new TriggerVolumeToolCreate(
            TriggerVolumeShapeType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            PacketIO.readVector3f(mem, offset + 2),
            PacketIO.readVector3f(mem, offset + 14),
            PacketIO.readVector3f(mem, offset + 26),
            hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + 38, 4096000, PacketIO.UTF8) : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.shapeType.getValue());
      PacketIO.writeVector3f(buf, this.position);
      PacketIO.writeVector3f(buf, this.param1);
      PacketIO.writeVector3f(buf, this.param2);
      if (this.name != null) {
         PacketIO.writeVarString(buf, this.name, 4096000);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.shapeType.getValue());
      PacketIO.writeVector3f(mem, offset + 2, this.position);
      PacketIO.writeVector3f(mem, offset + 14, this.param1);
      PacketIO.writeVector3f(mem, offset + 26, this.param2);
      int varOffset = offset + 38;
      if (this.name != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.name, 4096000);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 38;
      if (this.name != null) {
         size += PacketIO.stringSize(this.name);
      }

      return size;
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
            if ((nullBits & 1) != 0) {
               int nameLen = VarInt.peek(buffer, v);
               if (nameLen < 0) {
                  return ValidationResult.error("Invalid string length for Name");
               }

               if (nameLen > 4096000) {
                  return ValidationResult.error("Name exceeds max length 4096000");
               }

               v += VarInt.size(nameLen);
               v += nameLen;
               if (v > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Name");
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public TriggerVolumeToolCreate clone() {
      TriggerVolumeToolCreate copy = new TriggerVolumeToolCreate();
      copy.shapeType = this.shapeType;
      copy.position = this.position;
      copy.param1 = this.param1;
      copy.param2 = this.param2;
      copy.name = this.name;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof TriggerVolumeToolCreate other)
            ? false
            : Objects.equals(this.shapeType, other.shapeType)
               && Objects.equals(this.position, other.position)
               && Objects.equals(this.param1, other.param1)
               && Objects.equals(this.param2, other.param2)
               && Objects.equals(this.name, other.name);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.shapeType, this.position, this.param1, this.param2, this.name);
   }
}
