package com.hypixel.hytale.protocol.packets.worldmap;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;

public class PlayerMarkerComponent extends MapMarkerComponent {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 16;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 16;
   public static final int MAX_SIZE = 16;
   @Nonnull
   public UUID playerId = new UUID(0L, 0L);

   public PlayerMarkerComponent() {
   }

   public PlayerMarkerComponent(@Nonnull UUID playerId) {
      this.playerId = playerId;
   }

   public PlayerMarkerComponent(@Nonnull PlayerMarkerComponent other) {
      this.playerId = other.playerId;
   }

   @Nonnull
   public static PlayerMarkerComponent deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 16) {
         throw ProtocolException.bufferTooSmall("PlayerMarkerComponent", 16, buf.readableBytes() - offset);
      } else {
         PlayerMarkerComponent obj = new PlayerMarkerComponent();
         obj.playerId = PacketIO.readUUID(buf, offset + 0);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 16;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 16L;
   }

   public static UUID getPlayerId(MemorySegment mem) {
      return getPlayerId(mem, 0);
   }

   public static UUID getPlayerId(MemorySegment mem, int offset) {
      return PacketIO.readUUID(mem, offset + 0);
   }

   public static PlayerMarkerComponent toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static PlayerMarkerComponent toObject(MemorySegment mem, int offset) {
      if (offset + 16 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("PlayerMarkerComponent", offset + 16, (int)mem.byteSize());
      } else {
         return new PlayerMarkerComponent(PacketIO.readUUID(mem, offset + 0));
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      PacketIO.writeUUID(buf, this.playerId);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      PacketIO.writeUUID(mem, offset + 0, this.playerId);
      return 16;
   }

   @Override
   public int computeSize() {
      return 16;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 16 ? ValidationResult.error("Buffer too small: expected at least 16 bytes") : ValidationResult.OK;
   }

   public PlayerMarkerComponent clone() {
      PlayerMarkerComponent copy = new PlayerMarkerComponent();
      copy.playerId = this.playerId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof PlayerMarkerComponent other ? Objects.equals(this.playerId, other.playerId) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.playerId);
   }
}
