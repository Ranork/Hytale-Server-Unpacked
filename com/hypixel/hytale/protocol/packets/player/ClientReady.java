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

public class ClientReady implements Packet, ToServerPacket {
   public static final int PACKET_ID = 105;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 2;
   public boolean readyForChunks;
   public boolean readyForGameplay;

   @Override
   public int getId() {
      return 105;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ClientReady() {
   }

   public ClientReady(boolean readyForChunks, boolean readyForGameplay) {
      this.readyForChunks = readyForChunks;
      this.readyForGameplay = readyForGameplay;
   }

   public ClientReady(@Nonnull ClientReady other) {
      this.readyForChunks = other.readyForChunks;
      this.readyForGameplay = other.readyForGameplay;
   }

   @Nonnull
   public static ClientReady deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 2) {
         throw ProtocolException.bufferTooSmall("ClientReady", 2, buf.readableBytes() - offset);
      } else {
         ClientReady obj = new ClientReady();
         obj.readyForChunks = buf.getByte(offset + 0) != 0;
         obj.readyForGameplay = buf.getByte(offset + 1) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 2;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 2L;
   }

   public static boolean getReadyForChunks(MemorySegment mem) {
      return getReadyForChunks(mem, 0);
   }

   public static boolean getReadyForChunks(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0));
   }

   public static boolean getReadyForGameplay(MemorySegment mem) {
      return getReadyForGameplay(mem, 0);
   }

   public static boolean getReadyForGameplay(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static ClientReady toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ClientReady toObject(MemorySegment mem, int offset) {
      if (offset + 2 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ClientReady", offset + 2, (int)mem.byteSize());
      } else {
         return new ClientReady(mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0)), mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.readyForChunks ? 1 : 0);
      buf.writeByte(this.readyForGameplay ? 1 : 0);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BOOL, offset + 0, this.readyForChunks);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.readyForGameplay);
      return 2;
   }

   @Override
   public int computeSize() {
      return 2;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 2 ? ValidationResult.error("Buffer too small: expected at least 2 bytes") : ValidationResult.OK;
   }

   public ClientReady clone() {
      ClientReady copy = new ClientReady();
      copy.readyForChunks = this.readyForChunks;
      copy.readyForGameplay = this.readyForGameplay;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ClientReady other) ? false : this.readyForChunks == other.readyForChunks && this.readyForGameplay == other.readyForGameplay;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.readyForChunks, this.readyForGameplay);
   }
}
