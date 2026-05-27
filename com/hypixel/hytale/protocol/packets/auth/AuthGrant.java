package com.hypixel.hytale.protocol.packets.auth;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AuthGrant implements Packet, ToClientPacket {
   public static final int PACKET_ID = 11;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 49171;
   @Nullable
   public String authorizationGrant;
   @Nullable
   public String serverIdentityToken;

   @Override
   public int getId() {
      return 11;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AuthGrant() {
   }

   public AuthGrant(@Nullable String authorizationGrant, @Nullable String serverIdentityToken) {
      this.authorizationGrant = authorizationGrant;
      this.serverIdentityToken = serverIdentityToken;
   }

   public AuthGrant(@Nonnull AuthGrant other) {
      this.authorizationGrant = other.authorizationGrant;
      this.serverIdentityToken = other.serverIdentityToken;
   }

   @Nonnull
   public static AuthGrant deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("AuthGrant", 9, buf.readableBytes() - offset);
      } else {
         AuthGrant obj = new AuthGrant();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("AuthorizationGrant", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 9 + varPosBase0;
            int authorizationGrantLen = VarInt.peek(buf, varPos0);
            if (authorizationGrantLen < 0) {
               throw ProtocolException.invalidVarInt("AuthorizationGrant");
            }

            int authorizationGrantVarIntLen = VarInt.size(authorizationGrantLen);
            if (authorizationGrantLen > 4096) {
               throw ProtocolException.stringTooLong("AuthorizationGrant", authorizationGrantLen, 4096);
            }

            if (varPos0 + authorizationGrantVarIntLen + authorizationGrantLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("AuthorizationGrant", varPos0 + authorizationGrantVarIntLen + authorizationGrantLen, buf.readableBytes());
            }

            obj.authorizationGrant = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("ServerIdentityToken", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 9 + varPosBase1;
            int serverIdentityTokenLen = VarInt.peek(buf, varPos1);
            if (serverIdentityTokenLen < 0) {
               throw ProtocolException.invalidVarInt("ServerIdentityToken");
            }

            int serverIdentityTokenVarIntLen = VarInt.size(serverIdentityTokenLen);
            if (serverIdentityTokenLen > 8192) {
               throw ProtocolException.stringTooLong("ServerIdentityToken", serverIdentityTokenLen, 8192);
            }

            if (varPos1 + serverIdentityTokenVarIntLen + serverIdentityTokenLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall(
                  "ServerIdentityToken", varPos1 + serverIdentityTokenVarIntLen + serverIdentityTokenLen, buf.readableBytes()
               );
            }

            obj.serverIdentityToken = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("AuthorizationGrant", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 9 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("ServerIdentityToken", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 9 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   @Nullable
   public static String getAuthorizationGrant(MemorySegment mem) {
      return getAuthorizationGrant(mem, 0);
   }

   @Nullable
   public static String getAuthorizationGrant(MemorySegment mem, int offset) {
      return hasAuthorizationGrant(mem, offset)
         ? PacketIO.readVarString("AuthorizationGrant", mem, offset + getValidatedOffset(mem, offset, 1, 9, "AuthorizationGrant"), 4096, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getServerIdentityToken(MemorySegment mem) {
      return getServerIdentityToken(mem, 0);
   }

   @Nullable
   public static String getServerIdentityToken(MemorySegment mem, int offset) {
      return hasServerIdentityToken(mem, offset)
         ? PacketIO.readVarString("ServerIdentityToken", mem, offset + getValidatedOffset(mem, offset, 5, 9, "ServerIdentityToken"), 8192, PacketIO.UTF8)
         : null;
   }

   public static boolean hasAuthorizationGrant(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasServerIdentityToken(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static AuthGrant toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AuthGrant toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AuthGrant", offset + 9, (int)mem.byteSize());
      } else {
         return new AuthGrant(
            hasAuthorizationGrant(mem, offset)
               ? PacketIO.readVarString("AuthorizationGrant", mem, offset + getValidatedOffset(mem, offset, 1, 9, "AuthorizationGrant"), 4096, PacketIO.UTF8)
               : null,
            hasServerIdentityToken(mem, offset)
               ? PacketIO.readVarString("ServerIdentityToken", mem, offset + getValidatedOffset(mem, offset, 5, 9, "ServerIdentityToken"), 8192, PacketIO.UTF8)
               : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.authorizationGrant != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.serverIdentityToken != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int authorizationGrantOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int serverIdentityTokenOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.authorizationGrant != null) {
         buf.setIntLE(authorizationGrantOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.authorizationGrant, 4096);
      } else {
         buf.setIntLE(authorizationGrantOffsetSlot, -1);
      }

      if (this.serverIdentityToken != null) {
         buf.setIntLE(serverIdentityTokenOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.serverIdentityToken, 8192);
      } else {
         buf.setIntLE(serverIdentityTokenOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.authorizationGrant != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.serverIdentityToken != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 9;
      if (this.authorizationGrant != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 9);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.authorizationGrant, 4096);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.serverIdentityToken != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 9);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.serverIdentityToken, 8192);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 9;
      if (this.authorizationGrant != null) {
         size += PacketIO.stringSize(this.authorizationGrant);
      }

      if (this.serverIdentityToken != null) {
         size += PacketIO.stringSize(this.serverIdentityToken);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int authorizationGrantOffset = buffer.getIntLE(offset + 1);
            if (authorizationGrantOffset < 0 || authorizationGrantOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for AuthorizationGrant");
            }

            int pos = offset + 9 + authorizationGrantOffset;
            int authorizationGrantLen = VarInt.peek(buffer, pos);
            if (authorizationGrantLen < 0) {
               return ValidationResult.error("Invalid string length for AuthorizationGrant");
            }

            if (authorizationGrantLen > 4096) {
               return ValidationResult.error("AuthorizationGrant exceeds max length 4096");
            }

            pos += VarInt.size(authorizationGrantLen);
            pos += authorizationGrantLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading AuthorizationGrant");
            }
         }

         if ((nullBits & 2) != 0) {
            int serverIdentityTokenOffset = buffer.getIntLE(offset + 5);
            if (serverIdentityTokenOffset < 0 || serverIdentityTokenOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for ServerIdentityToken");
            }

            int posx = offset + 9 + serverIdentityTokenOffset;
            int serverIdentityTokenLen = VarInt.peek(buffer, posx);
            if (serverIdentityTokenLen < 0) {
               return ValidationResult.error("Invalid string length for ServerIdentityToken");
            }

            if (serverIdentityTokenLen > 8192) {
               return ValidationResult.error("ServerIdentityToken exceeds max length 8192");
            }

            posx += VarInt.size(serverIdentityTokenLen);
            posx += serverIdentityTokenLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ServerIdentityToken");
            }
         }

         return ValidationResult.OK;
      }
   }

   public AuthGrant clone() {
      AuthGrant copy = new AuthGrant();
      copy.authorizationGrant = this.authorizationGrant;
      copy.serverIdentityToken = this.serverIdentityToken;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AuthGrant other)
            ? false
            : Objects.equals(this.authorizationGrant, other.authorizationGrant) && Objects.equals(this.serverIdentityToken, other.serverIdentityToken);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.authorizationGrant, this.serverIdentityToken);
   }
}
