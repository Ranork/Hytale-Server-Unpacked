package com.hypixel.hytale.protocol.packets.auth;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
      AuthGrant obj = new AuthGrant();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         int authorizationGrantLen = VarInt.peek(buf, varPos0);
         if (authorizationGrantLen < 0) {
            throw ProtocolException.negativeLength("AuthorizationGrant", authorizationGrantLen);
         }

         if (authorizationGrantLen > 4096) {
            throw ProtocolException.stringTooLong("AuthorizationGrant", authorizationGrantLen, 4096);
         }

         obj.authorizationGrant = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         int serverIdentityTokenLen = VarInt.peek(buf, varPos1);
         if (serverIdentityTokenLen < 0) {
            throw ProtocolException.negativeLength("ServerIdentityToken", serverIdentityTokenLen);
         }

         if (serverIdentityTokenLen > 8192) {
            throw ProtocolException.stringTooLong("ServerIdentityToken", serverIdentityTokenLen, 8192);
         }

         obj.serverIdentityToken = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 9 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 9 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
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
            if (authorizationGrantOffset < 0) {
               return ValidationResult.error("Invalid offset for AuthorizationGrant");
            }

            int pos = offset + 9 + authorizationGrantOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for AuthorizationGrant");
            }

            int authorizationGrantLen = VarInt.peek(buffer, pos);
            if (authorizationGrantLen < 0) {
               return ValidationResult.error("Invalid string length for AuthorizationGrant");
            }

            if (authorizationGrantLen > 4096) {
               return ValidationResult.error("AuthorizationGrant exceeds max length 4096");
            }

            pos += VarInt.length(buffer, pos);
            pos += authorizationGrantLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading AuthorizationGrant");
            }
         }

         if ((nullBits & 2) != 0) {
            int serverIdentityTokenOffset = buffer.getIntLE(offset + 5);
            if (serverIdentityTokenOffset < 0) {
               return ValidationResult.error("Invalid offset for ServerIdentityToken");
            }

            int posx = offset + 9 + serverIdentityTokenOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ServerIdentityToken");
            }

            int serverIdentityTokenLen = VarInt.peek(buffer, posx);
            if (serverIdentityTokenLen < 0) {
               return ValidationResult.error("Invalid string length for ServerIdentityToken");
            }

            if (serverIdentityTokenLen > 8192) {
               return ValidationResult.error("ServerIdentityToken exceeds max length 8192");
            }

            posx += VarInt.length(buffer, posx);
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
