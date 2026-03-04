package com.hypixel.hytale.protocol.packets.auth;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AuthToken implements Packet, ToServerPacket {
   public static final int PACKET_ID = 12;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 49171;
   @Nullable
   public String accessToken;
   @Nullable
   public String serverAuthorizationGrant;

   @Override
   public int getId() {
      return 12;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AuthToken() {
   }

   public AuthToken(@Nullable String accessToken, @Nullable String serverAuthorizationGrant) {
      this.accessToken = accessToken;
      this.serverAuthorizationGrant = serverAuthorizationGrant;
   }

   public AuthToken(@Nonnull AuthToken other) {
      this.accessToken = other.accessToken;
      this.serverAuthorizationGrant = other.serverAuthorizationGrant;
   }

   @Nonnull
   public static AuthToken deserialize(@Nonnull ByteBuf buf, int offset) {
      AuthToken obj = new AuthToken();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         int accessTokenLen = VarInt.peek(buf, varPos0);
         if (accessTokenLen < 0) {
            throw ProtocolException.negativeLength("AccessToken", accessTokenLen);
         }

         if (accessTokenLen > 8192) {
            throw ProtocolException.stringTooLong("AccessToken", accessTokenLen, 8192);
         }

         obj.accessToken = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         int serverAuthorizationGrantLen = VarInt.peek(buf, varPos1);
         if (serverAuthorizationGrantLen < 0) {
            throw ProtocolException.negativeLength("ServerAuthorizationGrant", serverAuthorizationGrantLen);
         }

         if (serverAuthorizationGrantLen > 4096) {
            throw ProtocolException.stringTooLong("ServerAuthorizationGrant", serverAuthorizationGrantLen, 4096);
         }

         obj.serverAuthorizationGrant = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
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
      if (this.accessToken != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.serverAuthorizationGrant != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int accessTokenOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int serverAuthorizationGrantOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.accessToken != null) {
         buf.setIntLE(accessTokenOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.accessToken, 8192);
      } else {
         buf.setIntLE(accessTokenOffsetSlot, -1);
      }

      if (this.serverAuthorizationGrant != null) {
         buf.setIntLE(serverAuthorizationGrantOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.serverAuthorizationGrant, 4096);
      } else {
         buf.setIntLE(serverAuthorizationGrantOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 9;
      if (this.accessToken != null) {
         size += PacketIO.stringSize(this.accessToken);
      }

      if (this.serverAuthorizationGrant != null) {
         size += PacketIO.stringSize(this.serverAuthorizationGrant);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int accessTokenOffset = buffer.getIntLE(offset + 1);
            if (accessTokenOffset < 0) {
               return ValidationResult.error("Invalid offset for AccessToken");
            }

            int pos = offset + 9 + accessTokenOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for AccessToken");
            }

            int accessTokenLen = VarInt.peek(buffer, pos);
            if (accessTokenLen < 0) {
               return ValidationResult.error("Invalid string length for AccessToken");
            }

            if (accessTokenLen > 8192) {
               return ValidationResult.error("AccessToken exceeds max length 8192");
            }

            pos += VarInt.length(buffer, pos);
            pos += accessTokenLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading AccessToken");
            }
         }

         if ((nullBits & 2) != 0) {
            int serverAuthorizationGrantOffset = buffer.getIntLE(offset + 5);
            if (serverAuthorizationGrantOffset < 0) {
               return ValidationResult.error("Invalid offset for ServerAuthorizationGrant");
            }

            int posx = offset + 9 + serverAuthorizationGrantOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ServerAuthorizationGrant");
            }

            int serverAuthorizationGrantLen = VarInt.peek(buffer, posx);
            if (serverAuthorizationGrantLen < 0) {
               return ValidationResult.error("Invalid string length for ServerAuthorizationGrant");
            }

            if (serverAuthorizationGrantLen > 4096) {
               return ValidationResult.error("ServerAuthorizationGrant exceeds max length 4096");
            }

            posx += VarInt.length(buffer, posx);
            posx += serverAuthorizationGrantLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ServerAuthorizationGrant");
            }
         }

         return ValidationResult.OK;
      }
   }

   public AuthToken clone() {
      AuthToken copy = new AuthToken();
      copy.accessToken = this.accessToken;
      copy.serverAuthorizationGrant = this.serverAuthorizationGrant;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AuthToken other)
            ? false
            : Objects.equals(this.accessToken, other.accessToken) && Objects.equals(this.serverAuthorizationGrant, other.serverAuthorizationGrant);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.accessToken, this.serverAuthorizationGrant);
   }
}
