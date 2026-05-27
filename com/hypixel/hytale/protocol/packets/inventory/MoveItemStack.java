package com.hypixel.hytale.protocol.packets.inventory;

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

public class MoveItemStack implements Packet, ToServerPacket {
   public static final int PACKET_ID = 175;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 20;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 20;
   public static final int MAX_SIZE = 20;
   public int fromSectionId;
   public int fromSlotId;
   public int quantity;
   public int toSectionId;
   public int toSlotId;

   @Override
   public int getId() {
      return 175;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public MoveItemStack() {
   }

   public MoveItemStack(int fromSectionId, int fromSlotId, int quantity, int toSectionId, int toSlotId) {
      this.fromSectionId = fromSectionId;
      this.fromSlotId = fromSlotId;
      this.quantity = quantity;
      this.toSectionId = toSectionId;
      this.toSlotId = toSlotId;
   }

   public MoveItemStack(@Nonnull MoveItemStack other) {
      this.fromSectionId = other.fromSectionId;
      this.fromSlotId = other.fromSlotId;
      this.quantity = other.quantity;
      this.toSectionId = other.toSectionId;
      this.toSlotId = other.toSlotId;
   }

   @Nonnull
   public static MoveItemStack deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 20) {
         throw ProtocolException.bufferTooSmall("MoveItemStack", 20, buf.readableBytes() - offset);
      } else {
         MoveItemStack obj = new MoveItemStack();
         obj.fromSectionId = buf.getIntLE(offset + 0);
         obj.fromSlotId = buf.getIntLE(offset + 4);
         obj.quantity = buf.getIntLE(offset + 8);
         obj.toSectionId = buf.getIntLE(offset + 12);
         obj.toSlotId = buf.getIntLE(offset + 16);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 20;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 20L;
   }

   public static int getFromSectionId(MemorySegment mem) {
      return getFromSectionId(mem, 0);
   }

   public static int getFromSectionId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getFromSlotId(MemorySegment mem) {
      return getFromSlotId(mem, 0);
   }

   public static int getFromSlotId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static int getQuantity(MemorySegment mem) {
      return getQuantity(mem, 0);
   }

   public static int getQuantity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 8));
   }

   public static int getToSectionId(MemorySegment mem) {
      return getToSectionId(mem, 0);
   }

   public static int getToSectionId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 12));
   }

   public static int getToSlotId(MemorySegment mem) {
      return getToSlotId(mem, 0);
   }

   public static int getToSlotId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 16));
   }

   public static MoveItemStack toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static MoveItemStack toObject(MemorySegment mem, int offset) {
      if (offset + 20 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("MoveItemStack", offset + 20, (int)mem.byteSize());
      } else {
         return new MoveItemStack(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 12)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 16))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.fromSectionId);
      buf.writeIntLE(this.fromSlotId);
      buf.writeIntLE(this.quantity);
      buf.writeIntLE(this.toSectionId);
      buf.writeIntLE(this.toSlotId);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.fromSectionId);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.fromSlotId);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 8), this.quantity);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 12), this.toSectionId);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 16), this.toSlotId);
      return 20;
   }

   @Override
   public int computeSize() {
      return 20;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 20 ? ValidationResult.error("Buffer too small: expected at least 20 bytes") : ValidationResult.OK;
   }

   public MoveItemStack clone() {
      MoveItemStack copy = new MoveItemStack();
      copy.fromSectionId = this.fromSectionId;
      copy.fromSlotId = this.fromSlotId;
      copy.quantity = this.quantity;
      copy.toSectionId = this.toSectionId;
      copy.toSlotId = this.toSlotId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof MoveItemStack other)
            ? false
            : this.fromSectionId == other.fromSectionId
               && this.fromSlotId == other.fromSlotId
               && this.quantity == other.quantity
               && this.toSectionId == other.toSectionId
               && this.toSlotId == other.toSlotId;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.fromSectionId, this.fromSlotId, this.quantity, this.toSectionId, this.toSlotId);
   }
}
