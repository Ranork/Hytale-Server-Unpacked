package com.hypixel.hytale.protocol.packets.connection;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.protocol.ToServerPacket;
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

public class InsecurePlayerOptions implements Packet, ToServerPacket {
   public static final int PACKET_ID = 363;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 17;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 25;
   public static final int MAX_SIZE = 2149;
   @Nonnull
   public UUID uuid = new UUID(0L, 0L);
   @Nonnull
   public String username = "";
   @Nullable
   public PlayerSkin skin;

   @Override
   public int getId() {
      return 363;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public InsecurePlayerOptions() {
   }

   public InsecurePlayerOptions(@Nonnull UUID uuid, @Nonnull String username, @Nullable PlayerSkin skin) {
      this.uuid = uuid;
      this.username = username;
      this.skin = skin;
   }

   public InsecurePlayerOptions(@Nonnull InsecurePlayerOptions other) {
      this.uuid = other.uuid;
      this.username = other.username;
      this.skin = other.skin;
   }

   @Nonnull
   public static InsecurePlayerOptions deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 25) {
         throw ProtocolException.bufferTooSmall("InsecurePlayerOptions", 25, buf.readableBytes() - offset);
      } else {
         InsecurePlayerOptions obj = new InsecurePlayerOptions();
         byte nullBits = buf.getByte(offset);
         obj.uuid = PacketIO.readUUID(buf, offset + 1);
         int varPosBase0 = buf.getIntLE(offset + 17);
         if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 25) {
            int varPos0 = offset + 25 + varPosBase0;
            int usernameLen = VarInt.peek(buf, varPos0);
            if (usernameLen < 0) {
               throw ProtocolException.invalidVarInt("Username");
            } else {
               int usernameVarIntLen = VarInt.size(usernameLen);
               if (usernameLen > 16) {
                  throw ProtocolException.stringTooLong("Username", usernameLen, 16);
               } else if (varPos0 + usernameVarIntLen + usernameLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("Username", varPos0 + usernameVarIntLen + usernameLen, buf.readableBytes());
               } else {
                  obj.username = PacketIO.readValidatedAsciiString(buf, varPos0 + usernameVarIntLen, usernameLen, "Username");
                  if ((nullBits & 1) != 0) {
                     varPosBase0 = buf.getIntLE(offset + 21);
                     if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 25) {
                        throw ProtocolException.invalidOffset("Skin", varPosBase0, buf.readableBytes());
                     }

                     varPos0 = offset + 25 + varPosBase0;
                     obj.skin = PlayerSkin.deserialize(buf, varPos0);
                  }

                  return obj;
               }
            }
         } else {
            throw ProtocolException.invalidOffset("Username", varPosBase0, buf.readableBytes());
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 25;
      int fieldOffset0 = buf.getIntLE(offset + 17);
      if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 25) {
         int pos0 = offset + 25 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }

         if ((nullBits & 1) != 0) {
            fieldOffset0 = buf.getIntLE(offset + 21);
            if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 25) {
               throw ProtocolException.invalidOffset("Skin", fieldOffset0, maxEnd);
            }

            pos0 = offset + 25 + fieldOffset0;
            pos0 += PlayerSkin.computeBytesConsumed(buf, pos0);
            if (pos0 - offset > maxEnd) {
               maxEnd = pos0 - offset;
            }
         }

         return maxEnd;
      } else {
         throw ProtocolException.invalidOffset("Username", fieldOffset0, maxEnd);
      }
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 25L;
   }

   public static UUID getUuid(MemorySegment mem) {
      return getUuid(mem, 0);
   }

   public static UUID getUuid(MemorySegment mem, int offset) {
      return PacketIO.readUUID(mem, offset + 1);
   }

   public static String getUsername(MemorySegment mem) {
      return getUsername(mem, 0);
   }

   public static String getUsername(MemorySegment mem, int offset) {
      return PacketIO.readValidatedAsciiString("Username", mem, offset + getValidatedOffset(mem, offset, 17, 25, "Username"), 16);
   }

   @Nullable
   public static PlayerSkin getSkin(MemorySegment mem) {
      return getSkin(mem, 0);
   }

   @Nullable
   public static PlayerSkin getSkin(MemorySegment mem, int offset) {
      return hasSkin(mem, offset) ? PlayerSkin.toObject(mem, offset + getValidatedOffset(mem, offset, 21, 25, "Skin")) : null;
   }

   public static boolean hasSkin(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static InsecurePlayerOptions toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static InsecurePlayerOptions toObject(MemorySegment mem, int offset) {
      if (offset + 25 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("InsecurePlayerOptions", offset + 25, (int)mem.byteSize());
      } else {
         return new InsecurePlayerOptions(
            PacketIO.readUUID(mem, offset + 1),
            PacketIO.readValidatedAsciiString("Username", mem, offset + getValidatedOffset(mem, offset, 17, 25, "Username"), 16),
            hasSkin(mem, offset) ? PlayerSkin.toObject(mem, offset + getValidatedOffset(mem, offset, 21, 25, "Skin")) : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.skin != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      PacketIO.writeUUID(buf, this.uuid);
      int usernameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int skinOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      buf.setIntLE(usernameOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarAsciiString(buf, this.username, 16);
      if (this.skin != null) {
         buf.setIntLE(skinOffsetSlot, buf.writerIndex() - varBlockStart);
         this.skin.serialize(buf);
      } else {
         buf.setIntLE(skinOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.skin != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      PacketIO.writeUUID(mem, offset + 1, this.uuid);
      int varOffset = offset + 25;
      mem.set(PacketIO.PROTO_INT, (long)(offset + 17), varOffset - offset - 25);
      varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.username, 16);
      if (this.skin != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), varOffset - offset - 25);
         varOffset += this.skin.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 25;
      size += VarInt.size(this.username.length()) + this.username.length();
      if (this.skin != null) {
         size += this.skin.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 25) {
         return ValidationResult.error("Buffer too small: expected at least 25 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int usernameOffset = buffer.getIntLE(offset + 17);
         if (usernameOffset >= 0 && usernameOffset <= buffer.writerIndex() - offset - 25) {
            int pos = offset + 25 + usernameOffset;
            int usernameLen = VarInt.peek(buffer, pos);
            if (usernameLen < 0) {
               return ValidationResult.error("Invalid string length for Username");
            } else if (usernameLen > 16) {
               return ValidationResult.error("Username exceeds max length 16");
            } else {
               pos += VarInt.size(usernameLen);
               pos += usernameLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Username");
               } else if (!PacketIO.isValidAscii(buffer, pos - usernameLen, usernameLen)) {
                  return ValidationResult.error("Username contains non-ASCII bytes");
               } else {
                  if ((nullBits & 1) != 0) {
                     usernameOffset = buffer.getIntLE(offset + 21);
                     if (usernameOffset < 0 || usernameOffset > buffer.writerIndex() - offset - 25) {
                        return ValidationResult.error("Invalid offset for Skin");
                     }

                     pos = offset + 25 + usernameOffset;
                     ValidationResult skinResult = PlayerSkin.validateStructure(buffer, pos);
                     if (!skinResult.isValid()) {
                        return ValidationResult.error("Invalid Skin: " + skinResult.error());
                     }

                     pos += PlayerSkin.computeBytesConsumed(buffer, pos);
                  }

                  return ValidationResult.OK;
               }
            }
         } else {
            return ValidationResult.error("Invalid offset for Username");
         }
      }
   }

   public InsecurePlayerOptions clone() {
      InsecurePlayerOptions copy = new InsecurePlayerOptions();
      copy.uuid = this.uuid;
      copy.username = this.username;
      copy.skin = this.skin != null ? this.skin.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InsecurePlayerOptions other)
            ? false
            : Objects.equals(this.uuid, other.uuid) && Objects.equals(this.username, other.username) && Objects.equals(this.skin, other.skin);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.uuid, this.username, this.skin);
   }
}
