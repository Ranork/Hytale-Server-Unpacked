package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ServerPlayerListPlayer {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 37;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 37;
   public static final int MAX_SIZE = 16384042;
   @Nonnull
   public UUID uuid = new UUID(0L, 0L);
   @Nullable
   public String username;
   @Nullable
   public UUID worldUuid;
   public int ping;

   public ServerPlayerListPlayer() {
   }

   public ServerPlayerListPlayer(@Nonnull UUID uuid, @Nullable String username, @Nullable UUID worldUuid, int ping) {
      this.uuid = uuid;
      this.username = username;
      this.worldUuid = worldUuid;
      this.ping = ping;
   }

   public ServerPlayerListPlayer(@Nonnull ServerPlayerListPlayer other) {
      this.uuid = other.uuid;
      this.username = other.username;
      this.worldUuid = other.worldUuid;
      this.ping = other.ping;
   }

   @Nonnull
   public static ServerPlayerListPlayer deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 37) {
         throw ProtocolException.bufferTooSmall("ServerPlayerListPlayer", 37, buf.readableBytes() - offset);
      } else {
         ServerPlayerListPlayer obj = new ServerPlayerListPlayer();
         byte nullBits = buf.getByte(offset);
         obj.uuid = PacketIO.readUUID(buf, offset + 1);
         if ((nullBits & 1) != 0) {
            obj.worldUuid = PacketIO.readUUID(buf, offset + 17);
         }

         obj.ping = buf.getIntLE(offset + 33);
         int pos = offset + 37;
         if ((nullBits & 2) != 0) {
            int usernameLen = VarInt.peek(buf, pos);
            if (usernameLen < 0) {
               throw ProtocolException.invalidVarInt("Username");
            }

            int usernameVarLen = VarInt.size(usernameLen);
            if (usernameLen > 4096000) {
               throw ProtocolException.stringTooLong("Username", usernameLen, 4096000);
            }

            if (pos + usernameVarLen + usernameLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Username", pos + usernameVarLen + usernameLen, buf.readableBytes());
            }

            obj.username = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += usernameVarLen + usernameLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 37;
      if ((nullBits & 2) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 37L;
   }

   public static UUID getUuid(MemorySegment mem) {
      return getUuid(mem, 0);
   }

   public static UUID getUuid(MemorySegment mem, int offset) {
      return PacketIO.readUUID(mem, offset + 1);
   }

   @Nullable
   public static String getUsername(MemorySegment mem) {
      return getUsername(mem, 0);
   }

   @Nullable
   public static String getUsername(MemorySegment mem, int offset) {
      return hasUsername(mem, offset) ? PacketIO.readVarString("Username", mem, offset + 37, 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static UUID getWorldUuid(MemorySegment mem) {
      return getWorldUuid(mem, 0);
   }

   @Nullable
   public static UUID getWorldUuid(MemorySegment mem, int offset) {
      return hasWorldUuid(mem, offset) ? PacketIO.readUUID(mem, offset + 17) : null;
   }

   public static int getPing(MemorySegment mem) {
      return getPing(mem, 0);
   }

   public static int getPing(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 33));
   }

   public static boolean hasWorldUuid(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasUsername(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static ServerPlayerListPlayer toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ServerPlayerListPlayer toObject(MemorySegment mem, int offset) {
      if (offset + 37 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ServerPlayerListPlayer", offset + 37, (int)mem.byteSize());
      } else {
         return new ServerPlayerListPlayer(
            PacketIO.readUUID(mem, offset + 1),
            hasUsername(mem, offset) ? PacketIO.readVarString("Username", mem, offset + 37, 4096000, PacketIO.UTF8) : null,
            hasWorldUuid(mem, offset) ? PacketIO.readUUID(mem, offset + 17) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 33))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.worldUuid != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.username != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      PacketIO.writeUUID(buf, this.uuid);
      if (this.worldUuid != null) {
         PacketIO.writeUUID(buf, this.worldUuid);
      } else {
         buf.writeZero(16);
      }

      buf.writeIntLE(this.ping);
      if (this.username != null) {
         PacketIO.writeVarString(buf, this.username, 4096000);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.worldUuid != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.username != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      PacketIO.writeUUID(mem, offset + 1, this.uuid);
      if (this.worldUuid != null) {
         PacketIO.writeUUID(mem, offset + 17, this.worldUuid);
      } else {
         mem.asSlice(offset + 17, 16L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_INT, (long)(offset + 33), this.ping);
      int varOffset = offset + 37;
      if (this.username != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.username, 4096000);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 37;
      if (this.username != null) {
         size += PacketIO.stringSize(this.username);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 37) {
         return ValidationResult.error("Buffer too small: expected at least 37 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 37;
         if ((nullBits & 2) != 0) {
            int usernameLen = VarInt.peek(buffer, pos);
            if (usernameLen < 0) {
               return ValidationResult.error("Invalid string length for Username");
            }

            if (usernameLen > 4096000) {
               return ValidationResult.error("Username exceeds max length 4096000");
            }

            pos += VarInt.size(usernameLen);
            pos += usernameLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Username");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ServerPlayerListPlayer clone() {
      ServerPlayerListPlayer copy = new ServerPlayerListPlayer();
      copy.uuid = this.uuid;
      copy.username = this.username;
      copy.worldUuid = this.worldUuid;
      copy.ping = this.ping;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ServerPlayerListPlayer other)
            ? false
            : Objects.equals(this.uuid, other.uuid)
               && Objects.equals(this.username, other.username)
               && Objects.equals(this.worldUuid, other.worldUuid)
               && this.ping == other.ping;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.uuid, this.username, this.worldUuid, this.ping);
   }
}
