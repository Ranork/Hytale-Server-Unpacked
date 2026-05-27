package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.ModelTransform;
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

public class BuilderToolSetEntityTransform implements Packet, ToServerPacket {
   public static final int PACKET_ID = 402;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 55;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 55;
   public static final int MAX_SIZE = 55;
   public int entityId;
   @Nullable
   public ModelTransform modelTransform;
   public boolean isSessionEnd;

   @Override
   public int getId() {
      return 402;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolSetEntityTransform() {
   }

   public BuilderToolSetEntityTransform(int entityId, @Nullable ModelTransform modelTransform, boolean isSessionEnd) {
      this.entityId = entityId;
      this.modelTransform = modelTransform;
      this.isSessionEnd = isSessionEnd;
   }

   public BuilderToolSetEntityTransform(@Nonnull BuilderToolSetEntityTransform other) {
      this.entityId = other.entityId;
      this.modelTransform = other.modelTransform;
      this.isSessionEnd = other.isSessionEnd;
   }

   @Nonnull
   public static BuilderToolSetEntityTransform deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 55) {
         throw ProtocolException.bufferTooSmall("BuilderToolSetEntityTransform", 55, buf.readableBytes() - offset);
      } else {
         BuilderToolSetEntityTransform obj = new BuilderToolSetEntityTransform();
         byte nullBits = buf.getByte(offset);
         obj.entityId = buf.getIntLE(offset + 1);
         if ((nullBits & 1) != 0) {
            obj.modelTransform = ModelTransform.deserialize(buf, offset + 5);
         }

         obj.isSessionEnd = buf.getByte(offset + 54) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 55;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 55L;
   }

   public static int getEntityId(MemorySegment mem) {
      return getEntityId(mem, 0);
   }

   public static int getEntityId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static ModelTransform getModelTransform(MemorySegment mem) {
      return getModelTransform(mem, 0);
   }

   @Nullable
   public static ModelTransform getModelTransform(MemorySegment mem, int offset) {
      return hasModelTransform(mem, offset) ? ModelTransform.toObject(mem, offset + 5) : null;
   }

   public static boolean getIsSessionEnd(MemorySegment mem) {
      return getIsSessionEnd(mem, 0);
   }

   public static boolean getIsSessionEnd(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 54));
   }

   public static boolean hasModelTransform(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static BuilderToolSetEntityTransform toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolSetEntityTransform toObject(MemorySegment mem, int offset) {
      if (offset + 55 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolSetEntityTransform", offset + 55, (int)mem.byteSize());
      } else {
         return new BuilderToolSetEntityTransform(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            hasModelTransform(mem, offset) ? ModelTransform.toObject(mem, offset + 5) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 54))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.modelTransform != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.entityId);
      if (this.modelTransform != null) {
         this.modelTransform.serialize(buf);
      } else {
         buf.writeZero(49);
      }

      buf.writeByte(this.isSessionEnd ? 1 : 0);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.modelTransform != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.entityId);
      if (this.modelTransform != null) {
         this.modelTransform.serialize(mem, offset + 5);
      } else {
         mem.asSlice(offset + 5, 49L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BOOL, offset + 54, this.isSessionEnd);
      return 55;
   }

   @Override
   public int computeSize() {
      return 55;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 55) {
         return ValidationResult.error("Buffer too small: expected at least 55 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public BuilderToolSetEntityTransform clone() {
      BuilderToolSetEntityTransform copy = new BuilderToolSetEntityTransform();
      copy.entityId = this.entityId;
      copy.modelTransform = this.modelTransform != null ? this.modelTransform.clone() : null;
      copy.isSessionEnd = this.isSessionEnd;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolSetEntityTransform other)
            ? false
            : this.entityId == other.entityId && Objects.equals(this.modelTransform, other.modelTransform) && this.isSessionEnd == other.isSessionEnd;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.entityId, this.modelTransform, this.isSessionEnd);
   }
}
