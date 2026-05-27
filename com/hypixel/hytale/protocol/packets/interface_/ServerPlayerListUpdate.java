package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;

public class ServerPlayerListUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 32;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 32;
   public static final int MAX_SIZE = 32;
   @Nonnull
   public UUID uuid = new UUID(0L, 0L);
   @Nonnull
   public UUID worldUuid = new UUID(0L, 0L);

   public ServerPlayerListUpdate() {
   }

   public ServerPlayerListUpdate(@Nonnull UUID uuid, @Nonnull UUID worldUuid) {
      this.uuid = uuid;
      this.worldUuid = worldUuid;
   }

   public ServerPlayerListUpdate(@Nonnull ServerPlayerListUpdate other) {
      this.uuid = other.uuid;
      this.worldUuid = other.worldUuid;
   }

   @Nonnull
   public static ServerPlayerListUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 32) {
         throw ProtocolException.bufferTooSmall("ServerPlayerListUpdate", 32, buf.readableBytes() - offset);
      } else {
         ServerPlayerListUpdate obj = new ServerPlayerListUpdate();
         obj.uuid = PacketIO.readUUID(buf, offset + 0);
         obj.worldUuid = PacketIO.readUUID(buf, offset + 16);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 32;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 32L;
   }

   public static UUID getUuid(MemorySegment mem) {
      return getUuid(mem, 0);
   }

   public static UUID getUuid(MemorySegment mem, int offset) {
      return PacketIO.readUUID(mem, offset + 0);
   }

   public static UUID getWorldUuid(MemorySegment mem) {
      return getWorldUuid(mem, 0);
   }

   public static UUID getWorldUuid(MemorySegment mem, int offset) {
      return PacketIO.readUUID(mem, offset + 16);
   }

   public static ServerPlayerListUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ServerPlayerListUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 32 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ServerPlayerListUpdate", offset + 32, (int)mem.byteSize());
      } else {
         return new ServerPlayerListUpdate(PacketIO.readUUID(mem, offset + 0), PacketIO.readUUID(mem, offset + 16));
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      PacketIO.writeUUID(buf, this.uuid);
      PacketIO.writeUUID(buf, this.worldUuid);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      PacketIO.writeUUID(mem, offset + 0, this.uuid);
      PacketIO.writeUUID(mem, offset + 16, this.worldUuid);
      return 32;
   }

   public int computeSize() {
      return 32;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 32 ? ValidationResult.error("Buffer too small: expected at least 32 bytes") : ValidationResult.OK;
   }

   public ServerPlayerListUpdate clone() {
      ServerPlayerListUpdate copy = new ServerPlayerListUpdate();
      copy.uuid = this.uuid;
      copy.worldUuid = this.worldUuid;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ServerPlayerListUpdate other)
            ? false
            : Objects.equals(this.uuid, other.uuid) && Objects.equals(this.worldUuid, other.worldUuid);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.uuid, this.worldUuid);
   }
}
