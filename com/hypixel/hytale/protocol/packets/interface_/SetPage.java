package com.hypixel.hytale.protocol.packets.interface_;

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

public class SetPage implements Packet, ToClientPacket {
   public static final int PACKET_ID = 216;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 2;
   @Nonnull
   public Page page = Page.None;
   public boolean canCloseThroughInteraction;

   @Override
   public int getId() {
      return 216;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SetPage() {
   }

   public SetPage(@Nonnull Page page, boolean canCloseThroughInteraction) {
      this.page = page;
      this.canCloseThroughInteraction = canCloseThroughInteraction;
   }

   public SetPage(@Nonnull SetPage other) {
      this.page = other.page;
      this.canCloseThroughInteraction = other.canCloseThroughInteraction;
   }

   @Nonnull
   public static SetPage deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 2) {
         throw ProtocolException.bufferTooSmall("SetPage", 2, buf.readableBytes() - offset);
      } else {
         SetPage obj = new SetPage();
         obj.page = Page.fromValue(buf.getByte(offset + 0));
         obj.canCloseThroughInteraction = buf.getByte(offset + 1) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 2;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 2L;
   }

   public static Page getPage(MemorySegment mem) {
      return getPage(mem, 0);
   }

   public static Page getPage(MemorySegment mem, int offset) {
      return Page.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static boolean getCanCloseThroughInteraction(MemorySegment mem) {
      return getCanCloseThroughInteraction(mem, 0);
   }

   public static boolean getCanCloseThroughInteraction(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static SetPage toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SetPage toObject(MemorySegment mem, int offset) {
      if (offset + 2 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SetPage", offset + 2, (int)mem.byteSize());
      } else {
         return new SetPage(Page.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0))), mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.page.getValue());
      buf.writeByte(this.canCloseThroughInteraction ? 1 : 0);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), (byte)this.page.getValue());
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.canCloseThroughInteraction);
      return 2;
   }

   @Override
   public int computeSize() {
      return 2;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 2) {
         return ValidationResult.error("Buffer too small: expected at least 2 bytes");
      } else {
         int v = buffer.getByte(offset + 0) & 255;
         return v >= 9 ? ValidationResult.error("Invalid Page value for Page") : ValidationResult.OK;
      }
   }

   public SetPage clone() {
      SetPage copy = new SetPage();
      copy.page = this.page;
      copy.canCloseThroughInteraction = this.canCloseThroughInteraction;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SetPage other)
            ? false
            : Objects.equals(this.page, other.page) && this.canCloseThroughInteraction == other.canCloseThroughInteraction;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.page, this.canCloseThroughInteraction);
   }
}
