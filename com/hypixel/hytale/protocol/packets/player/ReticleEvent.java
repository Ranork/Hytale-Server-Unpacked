package com.hypixel.hytale.protocol.packets.player;

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

public class ReticleEvent implements Packet, ToClientPacket {
   public static final int PACKET_ID = 113;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 4;
   public int eventIndex;

   @Override
   public int getId() {
      return 113;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ReticleEvent() {
   }

   public ReticleEvent(int eventIndex) {
      this.eventIndex = eventIndex;
   }

   public ReticleEvent(@Nonnull ReticleEvent other) {
      this.eventIndex = other.eventIndex;
   }

   @Nonnull
   public static ReticleEvent deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 4) {
         throw ProtocolException.bufferTooSmall("ReticleEvent", 4, buf.readableBytes() - offset);
      } else {
         ReticleEvent obj = new ReticleEvent();
         obj.eventIndex = buf.getIntLE(offset + 0);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 4L;
   }

   public static int getEventIndex(MemorySegment mem) {
      return getEventIndex(mem, 0);
   }

   public static int getEventIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static ReticleEvent toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ReticleEvent toObject(MemorySegment mem, int offset) {
      if (offset + 4 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ReticleEvent", offset + 4, (int)mem.byteSize());
      } else {
         return new ReticleEvent(mem.get(PacketIO.PROTO_INT, (long)(offset + 0)));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.eventIndex);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.eventIndex);
      return 4;
   }

   @Override
   public int computeSize() {
      return 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 4 ? ValidationResult.error("Buffer too small: expected at least 4 bytes") : ValidationResult.OK;
   }

   public ReticleEvent clone() {
      ReticleEvent copy = new ReticleEvent();
      copy.eventIndex = this.eventIndex;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof ReticleEvent other ? this.eventIndex == other.eventIndex : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.eventIndex);
   }
}
