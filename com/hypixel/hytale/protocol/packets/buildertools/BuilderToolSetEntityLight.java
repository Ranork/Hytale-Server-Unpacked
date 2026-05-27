package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderToolSetEntityLight implements Packet, ToServerPacket {
   public static final int PACKET_ID = 422;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 9;
   public int entityId;
   @Nullable
   public ColorLight light;

   @Override
   public int getId() {
      return 422;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolSetEntityLight() {
   }

   public BuilderToolSetEntityLight(int entityId, @Nullable ColorLight light) {
      this.entityId = entityId;
      this.light = light;
   }

   public BuilderToolSetEntityLight(@Nonnull BuilderToolSetEntityLight other) {
      this.entityId = other.entityId;
      this.light = other.light;
   }

   @Nonnull
   public static BuilderToolSetEntityLight deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("BuilderToolSetEntityLight", 9, buf.readableBytes() - offset);
      } else {
         BuilderToolSetEntityLight obj = new BuilderToolSetEntityLight();
         byte nullBits = buf.getByte(offset);
         obj.entityId = buf.getIntLE(offset + 1);
         if ((nullBits & 1) != 0) {
            obj.light = ColorLight.deserialize(buf, offset + 5);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 9;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   public static int getEntityId(MemorySegment mem) {
      return getEntityId(mem, 0);
   }

   public static int getEntityId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static ColorLight getLight(MemorySegment mem) {
      return getLight(mem, 0);
   }

   @Nullable
   public static ColorLight getLight(MemorySegment mem, int offset) {
      return hasLight(mem, offset) ? ColorLight.toObject(mem, offset + 5) : null;
   }

   public static boolean hasLight(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static BuilderToolSetEntityLight toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolSetEntityLight toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolSetEntityLight", offset + 9, (int)mem.byteSize());
      } else {
         return new BuilderToolSetEntityLight(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)), hasLight(mem, offset) ? ColorLight.toObject(mem, offset + 5) : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.light != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.entityId);
      if (this.light != null) {
         this.light.serialize(buf);
      } else {
         buf.writeZero(4);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.light != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.entityId);
      if (this.light != null) {
         this.light.serialize(mem, offset + 5);
      } else {
         mem.asSlice(offset + 5, 4L).fill((byte)0);
      }

      return 9;
   }

   @Override
   public int computeSize() {
      return 9;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public BuilderToolSetEntityLight clone() {
      BuilderToolSetEntityLight copy = new BuilderToolSetEntityLight();
      copy.entityId = this.entityId;
      copy.light = this.light != null ? this.light.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolSetEntityLight other) ? false : this.entityId == other.entityId && Objects.equals(this.light, other.light);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.entityId, this.light);
   }
}
