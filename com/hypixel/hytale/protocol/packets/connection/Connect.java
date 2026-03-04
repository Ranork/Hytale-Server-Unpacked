package com.hypixel.hytale.protocol.packets.connection;

import com.hypixel.hytale.protocol.HostAddress;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Connect implements Packet, ToServerPacket {
   public static final int PACKET_ID = 0;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 46;
   public static final int VARIABLE_FIELD_COUNT = 5;
   public static final int VARIABLE_BLOCK_START = 66;
   public static final int MAX_SIZE = 38013;
   public int protocolCrc;
   public int protocolBuildNumber;
   @Nonnull
   public String clientVersion = "";
   @Nonnull
   public ClientType clientType = ClientType.Game;
   @Nonnull
   public UUID uuid = new UUID(0L, 0L);
   @Nonnull
   public String username = "";
   @Nullable
   public String identityToken;
   @Nonnull
   public String language = "";
   @Nullable
   public byte[] referralData;
   @Nullable
   public HostAddress referralSource;

   @Override
   public int getId() {
      return 0;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public Connect() {
   }

   public Connect(
      int protocolCrc,
      int protocolBuildNumber,
      @Nonnull String clientVersion,
      @Nonnull ClientType clientType,
      @Nonnull UUID uuid,
      @Nonnull String username,
      @Nullable String identityToken,
      @Nonnull String language,
      @Nullable byte[] referralData,
      @Nullable HostAddress referralSource
   ) {
      this.protocolCrc = protocolCrc;
      this.protocolBuildNumber = protocolBuildNumber;
      this.clientVersion = clientVersion;
      this.clientType = clientType;
      this.uuid = uuid;
      this.username = username;
      this.identityToken = identityToken;
      this.language = language;
      this.referralData = referralData;
      this.referralSource = referralSource;
   }

   public Connect(@Nonnull Connect other) {
      this.protocolCrc = other.protocolCrc;
      this.protocolBuildNumber = other.protocolBuildNumber;
      this.clientVersion = other.clientVersion;
      this.clientType = other.clientType;
      this.uuid = other.uuid;
      this.username = other.username;
      this.identityToken = other.identityToken;
      this.language = other.language;
      this.referralData = other.referralData;
      this.referralSource = other.referralSource;
   }

   @Nonnull
   public static Connect deserialize(@Nonnull ByteBuf buf, int offset) {
      Connect obj = new Connect();
      byte nullBits = buf.getByte(offset);
      obj.protocolCrc = buf.getIntLE(offset + 1);
      obj.protocolBuildNumber = buf.getIntLE(offset + 5);
      obj.clientVersion = PacketIO.readFixedAsciiString(buf, offset + 9, 20);
      obj.clientType = ClientType.fromValue(buf.getByte(offset + 29));
      obj.uuid = PacketIO.readUUID(buf, offset + 30);
      int varPos0 = offset + 66 + buf.getIntLE(offset + 46);
      int usernameLen = VarInt.peek(buf, varPos0);
      if (usernameLen < 0) {
         throw ProtocolException.negativeLength("Username", usernameLen);
      } else if (usernameLen > 16) {
         throw ProtocolException.stringTooLong("Username", usernameLen, 16);
      } else {
         obj.username = PacketIO.readVarString(buf, varPos0, PacketIO.ASCII);
         if ((nullBits & 1) != 0) {
            varPos0 = offset + 66 + buf.getIntLE(offset + 50);
            usernameLen = VarInt.peek(buf, varPos0);
            if (usernameLen < 0) {
               throw ProtocolException.negativeLength("IdentityToken", usernameLen);
            }

            if (usernameLen > 8192) {
               throw ProtocolException.stringTooLong("IdentityToken", usernameLen, 8192);
            }

            obj.identityToken = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         varPos0 = offset + 66 + buf.getIntLE(offset + 54);
         usernameLen = VarInt.peek(buf, varPos0);
         if (usernameLen < 0) {
            throw ProtocolException.negativeLength("Language", usernameLen);
         } else if (usernameLen > 16) {
            throw ProtocolException.stringTooLong("Language", usernameLen, 16);
         } else {
            obj.language = PacketIO.readVarString(buf, varPos0, PacketIO.ASCII);
            if ((nullBits & 2) != 0) {
               varPos0 = offset + 66 + buf.getIntLE(offset + 58);
               usernameLen = VarInt.peek(buf, varPos0);
               if (usernameLen < 0) {
                  throw ProtocolException.negativeLength("ReferralData", usernameLen);
               }

               if (usernameLen > 4096) {
                  throw ProtocolException.arrayTooLong("ReferralData", usernameLen, 4096);
               }

               int varIntLen = VarInt.length(buf, varPos0);
               if (varPos0 + varIntLen + usernameLen * 1L > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("ReferralData", varPos0 + varIntLen + usernameLen * 1, buf.readableBytes());
               }

               obj.referralData = new byte[usernameLen];

               for (int i = 0; i < usernameLen; i++) {
                  obj.referralData[i] = buf.getByte(varPos0 + varIntLen + i * 1);
               }
            }

            if ((nullBits & 4) != 0) {
               varPos0 = offset + 66 + buf.getIntLE(offset + 62);
               obj.referralSource = HostAddress.deserialize(buf, varPos0);
            }

            return obj;
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 66;
      int fieldOffset0 = buf.getIntLE(offset + 46);
      int pos0 = offset + 66 + fieldOffset0;
      int sl = VarInt.peek(buf, pos0);
      pos0 += VarInt.length(buf, pos0) + sl;
      if (pos0 - offset > maxEnd) {
         maxEnd = pos0 - offset;
      }

      if ((nullBits & 1) != 0) {
         fieldOffset0 = buf.getIntLE(offset + 50);
         pos0 = offset + 66 + fieldOffset0;
         sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      fieldOffset0 = buf.getIntLE(offset + 54);
      pos0 = offset + 66 + fieldOffset0;
      sl = VarInt.peek(buf, pos0);
      pos0 += VarInt.length(buf, pos0) + sl;
      if (pos0 - offset > maxEnd) {
         maxEnd = pos0 - offset;
      }

      if ((nullBits & 2) != 0) {
         fieldOffset0 = buf.getIntLE(offset + 58);
         pos0 = offset + 66 + fieldOffset0;
         sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl * 1;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         fieldOffset0 = buf.getIntLE(offset + 62);
         pos0 = offset + 66 + fieldOffset0;
         pos0 += HostAddress.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      return maxEnd;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.identityToken != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.referralData != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.referralSource != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.protocolCrc);
      buf.writeIntLE(this.protocolBuildNumber);
      PacketIO.writeFixedAsciiString(buf, this.clientVersion, 20);
      buf.writeByte(this.clientType.getValue());
      PacketIO.writeUUID(buf, this.uuid);
      int usernameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int identityTokenOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int languageOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int referralDataOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int referralSourceOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      buf.setIntLE(usernameOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarAsciiString(buf, this.username, 16);
      if (this.identityToken != null) {
         buf.setIntLE(identityTokenOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.identityToken, 8192);
      } else {
         buf.setIntLE(identityTokenOffsetSlot, -1);
      }

      buf.setIntLE(languageOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarAsciiString(buf, this.language, 16);
      if (this.referralData != null) {
         buf.setIntLE(referralDataOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.referralData.length > 4096) {
            throw ProtocolException.arrayTooLong("ReferralData", this.referralData.length, 4096);
         }

         VarInt.write(buf, this.referralData.length);

         for (byte item : this.referralData) {
            buf.writeByte(item);
         }
      } else {
         buf.setIntLE(referralDataOffsetSlot, -1);
      }

      if (this.referralSource != null) {
         buf.setIntLE(referralSourceOffsetSlot, buf.writerIndex() - varBlockStart);
         this.referralSource.serialize(buf);
      } else {
         buf.setIntLE(referralSourceOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 66;
      size += VarInt.size(this.username.length()) + this.username.length();
      if (this.identityToken != null) {
         size += PacketIO.stringSize(this.identityToken);
      }

      size += VarInt.size(this.language.length()) + this.language.length();
      if (this.referralData != null) {
         size += VarInt.size(this.referralData.length) + this.referralData.length * 1;
      }

      if (this.referralSource != null) {
         size += this.referralSource.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 66) {
         return ValidationResult.error("Buffer too small: expected at least 66 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int usernameOffset = buffer.getIntLE(offset + 46);
         if (usernameOffset < 0) {
            return ValidationResult.error("Invalid offset for Username");
         } else {
            int pos = offset + 66 + usernameOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Username");
            } else {
               int usernameLen = VarInt.peek(buffer, pos);
               if (usernameLen < 0) {
                  return ValidationResult.error("Invalid string length for Username");
               } else if (usernameLen > 16) {
                  return ValidationResult.error("Username exceeds max length 16");
               } else {
                  pos += VarInt.length(buffer, pos);
                  pos += usernameLen;
                  if (pos > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading Username");
                  } else {
                     if ((nullBits & 1) != 0) {
                        usernameOffset = buffer.getIntLE(offset + 50);
                        if (usernameOffset < 0) {
                           return ValidationResult.error("Invalid offset for IdentityToken");
                        }

                        pos = offset + 66 + usernameOffset;
                        if (pos >= buffer.writerIndex()) {
                           return ValidationResult.error("Offset out of bounds for IdentityToken");
                        }

                        usernameLen = VarInt.peek(buffer, pos);
                        if (usernameLen < 0) {
                           return ValidationResult.error("Invalid string length for IdentityToken");
                        }

                        if (usernameLen > 8192) {
                           return ValidationResult.error("IdentityToken exceeds max length 8192");
                        }

                        pos += VarInt.length(buffer, pos);
                        pos += usernameLen;
                        if (pos > buffer.writerIndex()) {
                           return ValidationResult.error("Buffer overflow reading IdentityToken");
                        }
                     }

                     usernameOffset = buffer.getIntLE(offset + 54);
                     if (usernameOffset < 0) {
                        return ValidationResult.error("Invalid offset for Language");
                     } else {
                        pos = offset + 66 + usernameOffset;
                        if (pos >= buffer.writerIndex()) {
                           return ValidationResult.error("Offset out of bounds for Language");
                        } else {
                           usernameLen = VarInt.peek(buffer, pos);
                           if (usernameLen < 0) {
                              return ValidationResult.error("Invalid string length for Language");
                           } else if (usernameLen > 16) {
                              return ValidationResult.error("Language exceeds max length 16");
                           } else {
                              pos += VarInt.length(buffer, pos);
                              pos += usernameLen;
                              if (pos > buffer.writerIndex()) {
                                 return ValidationResult.error("Buffer overflow reading Language");
                              } else {
                                 if ((nullBits & 2) != 0) {
                                    usernameOffset = buffer.getIntLE(offset + 58);
                                    if (usernameOffset < 0) {
                                       return ValidationResult.error("Invalid offset for ReferralData");
                                    }

                                    pos = offset + 66 + usernameOffset;
                                    if (pos >= buffer.writerIndex()) {
                                       return ValidationResult.error("Offset out of bounds for ReferralData");
                                    }

                                    usernameLen = VarInt.peek(buffer, pos);
                                    if (usernameLen < 0) {
                                       return ValidationResult.error("Invalid array count for ReferralData");
                                    }

                                    if (usernameLen > 4096) {
                                       return ValidationResult.error("ReferralData exceeds max length 4096");
                                    }

                                    pos += VarInt.length(buffer, pos);
                                    pos += usernameLen * 1;
                                    if (pos > buffer.writerIndex()) {
                                       return ValidationResult.error("Buffer overflow reading ReferralData");
                                    }
                                 }

                                 if ((nullBits & 4) != 0) {
                                    usernameOffset = buffer.getIntLE(offset + 62);
                                    if (usernameOffset < 0) {
                                       return ValidationResult.error("Invalid offset for ReferralSource");
                                    }

                                    pos = offset + 66 + usernameOffset;
                                    if (pos >= buffer.writerIndex()) {
                                       return ValidationResult.error("Offset out of bounds for ReferralSource");
                                    }

                                    ValidationResult referralSourceResult = HostAddress.validateStructure(buffer, pos);
                                    if (!referralSourceResult.isValid()) {
                                       return ValidationResult.error("Invalid ReferralSource: " + referralSourceResult.error());
                                    }

                                    pos += HostAddress.computeBytesConsumed(buffer, pos);
                                 }

                                 return ValidationResult.OK;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public Connect clone() {
      Connect copy = new Connect();
      copy.protocolCrc = this.protocolCrc;
      copy.protocolBuildNumber = this.protocolBuildNumber;
      copy.clientVersion = this.clientVersion;
      copy.clientType = this.clientType;
      copy.uuid = this.uuid;
      copy.username = this.username;
      copy.identityToken = this.identityToken;
      copy.language = this.language;
      copy.referralData = this.referralData != null ? Arrays.copyOf(this.referralData, this.referralData.length) : null;
      copy.referralSource = this.referralSource != null ? this.referralSource.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Connect other)
            ? false
            : this.protocolCrc == other.protocolCrc
               && this.protocolBuildNumber == other.protocolBuildNumber
               && Objects.equals(this.clientVersion, other.clientVersion)
               && Objects.equals(this.clientType, other.clientType)
               && Objects.equals(this.uuid, other.uuid)
               && Objects.equals(this.username, other.username)
               && Objects.equals(this.identityToken, other.identityToken)
               && Objects.equals(this.language, other.language)
               && Arrays.equals(this.referralData, other.referralData)
               && Objects.equals(this.referralSource, other.referralSource);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.protocolCrc);
      result = 31 * result + Integer.hashCode(this.protocolBuildNumber);
      result = 31 * result + Objects.hashCode(this.clientVersion);
      result = 31 * result + Objects.hashCode(this.clientType);
      result = 31 * result + Objects.hashCode(this.uuid);
      result = 31 * result + Objects.hashCode(this.username);
      result = 31 * result + Objects.hashCode(this.identityToken);
      result = 31 * result + Objects.hashCode(this.language);
      result = 31 * result + Arrays.hashCode(this.referralData);
      return 31 * result + Objects.hashCode(this.referralSource);
   }
}
