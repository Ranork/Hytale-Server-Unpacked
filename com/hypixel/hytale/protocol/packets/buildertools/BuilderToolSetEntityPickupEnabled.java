package com.hypixel.hytale.protocol.packets.buildertools;

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

public class BuilderToolSetEntityPickupEnabled implements Packet, ToServerPacket {
   public static final int PACKET_ID = 421;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 5;
   public int entityId;
   public boolean enabled;

   @Override
   public int getId() {
      return 421;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolSetEntityPickupEnabled() {
   }

   public BuilderToolSetEntityPickupEnabled(int entityId, boolean enabled) {
      this.entityId = entityId;
      this.enabled = enabled;
   }

   public BuilderToolSetEntityPickupEnabled(@Nonnull BuilderToolSetEntityPickupEnabled other) {
      this.entityId = other.entityId;
      this.enabled = other.enabled;
   }

   @Nonnull
   public static BuilderToolSetEntityPickupEnabled deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("BuilderToolSetEntityPickupEnabled", 5, buf.readableBytes() - offset);
      } else {
         BuilderToolSetEntityPickupEnabled obj = new BuilderToolSetEntityPickupEnabled();
         obj.entityId = buf.getIntLE(offset + 0);
         obj.enabled = buf.getByte(offset + 4) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 5;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   public static int getEntityId(MemorySegment mem) {
      return getEntityId(mem, 0);
   }

   public static int getEntityId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static boolean getEnabled(MemorySegment mem) {
      return getEnabled(mem, 0);
   }

   public static boolean getEnabled(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 4));
   }

   public static BuilderToolSetEntityPickupEnabled toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolSetEntityPickupEnabled toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolSetEntityPickupEnabled", offset + 5, (int)mem.byteSize());
      } else {
         return new BuilderToolSetEntityPickupEnabled(mem.get(PacketIO.PROTO_INT, (long)(offset + 0)), mem.get(PacketIO.PROTO_BOOL, (long)(offset + 4)));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.entityId);
      buf.writeByte(this.enabled ? 1 : 0);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.entityId);
      mem.set(PacketIO.PROTO_BOOL, offset + 4, this.enabled);
      return 5;
   }

   @Override
   public int computeSize() {
      return 5;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 5 ? ValidationResult.error("Buffer too small: expected at least 5 bytes") : ValidationResult.OK;
   }

   public BuilderToolSetEntityPickupEnabled clone() {
      BuilderToolSetEntityPickupEnabled copy = new BuilderToolSetEntityPickupEnabled();
      copy.entityId = this.entityId;
      copy.enabled = this.enabled;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolSetEntityPickupEnabled other) ? false : this.entityId == other.entityId && this.enabled == other.enabled;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.entityId, this.enabled);
   }
}
