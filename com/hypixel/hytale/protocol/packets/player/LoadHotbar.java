package com.hypixel.hytale.protocol.packets.player;

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

public class LoadHotbar implements Packet, ToServerPacket {
   public static final int PACKET_ID = 106;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   public byte inventoryRow;

   @Override
   public int getId() {
      return 106;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public LoadHotbar() {
   }

   public LoadHotbar(byte inventoryRow) {
      this.inventoryRow = inventoryRow;
   }

   public LoadHotbar(@Nonnull LoadHotbar other) {
      this.inventoryRow = other.inventoryRow;
   }

   @Nonnull
   public static LoadHotbar deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("LoadHotbar", 1, buf.readableBytes() - offset);
      } else {
         LoadHotbar obj = new LoadHotbar();
         obj.inventoryRow = buf.getByte(offset + 0);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   public static byte getInventoryRow(MemorySegment mem) {
      return getInventoryRow(mem, 0);
   }

   public static byte getInventoryRow(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
   }

   public static LoadHotbar toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static LoadHotbar toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("LoadHotbar", offset + 1, (int)mem.byteSize());
      } else {
         return new LoadHotbar(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.inventoryRow);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), this.inventoryRow);
      return 1;
   }

   @Override
   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 1 ? ValidationResult.error("Buffer too small: expected at least 1 bytes") : ValidationResult.OK;
   }

   public LoadHotbar clone() {
      LoadHotbar copy = new LoadHotbar();
      copy.inventoryRow = this.inventoryRow;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof LoadHotbar other ? this.inventoryRow == other.inventoryRow : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.inventoryRow);
   }
}
