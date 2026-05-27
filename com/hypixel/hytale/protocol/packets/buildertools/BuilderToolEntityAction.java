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

public class BuilderToolEntityAction implements Packet, ToServerPacket {
   public static final int PACKET_ID = 401;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 5;
   public int entityId;
   @Nonnull
   public EntityToolAction action = EntityToolAction.Remove;

   @Override
   public int getId() {
      return 401;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolEntityAction() {
   }

   public BuilderToolEntityAction(int entityId, @Nonnull EntityToolAction action) {
      this.entityId = entityId;
      this.action = action;
   }

   public BuilderToolEntityAction(@Nonnull BuilderToolEntityAction other) {
      this.entityId = other.entityId;
      this.action = other.action;
   }

   @Nonnull
   public static BuilderToolEntityAction deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("BuilderToolEntityAction", 5, buf.readableBytes() - offset);
      } else {
         BuilderToolEntityAction obj = new BuilderToolEntityAction();
         obj.entityId = buf.getIntLE(offset + 0);
         obj.action = EntityToolAction.fromValue(buf.getByte(offset + 4));
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

   public static EntityToolAction getAction(MemorySegment mem) {
      return getAction(mem, 0);
   }

   public static EntityToolAction getAction(MemorySegment mem, int offset) {
      return EntityToolAction.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 4)));
   }

   public static BuilderToolEntityAction toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolEntityAction toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolEntityAction", offset + 5, (int)mem.byteSize());
      } else {
         return new BuilderToolEntityAction(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)), EntityToolAction.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 4)))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.entityId);
      buf.writeByte(this.action.getValue());
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.entityId);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 4), (byte)this.action.getValue());
      return 5;
   }

   @Override
   public int computeSize() {
      return 5;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         int v = buffer.getByte(offset + 4) & 255;
         return v >= 3 ? ValidationResult.error("Invalid EntityToolAction value for Action") : ValidationResult.OK;
      }
   }

   public BuilderToolEntityAction clone() {
      BuilderToolEntityAction copy = new BuilderToolEntityAction();
      copy.entityId = this.entityId;
      copy.action = this.action;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolEntityAction other) ? false : this.entityId == other.entityId && Objects.equals(this.action, other.action);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.entityId, this.action);
   }
}
