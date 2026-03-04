package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AuthorInfo {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 49152028;
   @Nullable
   public String name;
   @Nullable
   public String email;
   @Nullable
   public String url;

   public AuthorInfo() {
   }

   public AuthorInfo(@Nullable String name, @Nullable String email, @Nullable String url) {
      this.name = name;
      this.email = email;
      this.url = url;
   }

   public AuthorInfo(@Nonnull AuthorInfo other) {
      this.name = other.name;
      this.email = other.email;
      this.url = other.url;
   }

   @Nonnull
   public static AuthorInfo deserialize(@Nonnull ByteBuf buf, int offset) {
      AuthorInfo obj = new AuthorInfo();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 13 + buf.getIntLE(offset + 1);
         int nameLen = VarInt.peek(buf, varPos0);
         if (nameLen < 0) {
            throw ProtocolException.negativeLength("Name", nameLen);
         }

         if (nameLen > 4096000) {
            throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
         }

         obj.name = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 13 + buf.getIntLE(offset + 5);
         int emailLen = VarInt.peek(buf, varPos1);
         if (emailLen < 0) {
            throw ProtocolException.negativeLength("Email", emailLen);
         }

         if (emailLen > 4096000) {
            throw ProtocolException.stringTooLong("Email", emailLen, 4096000);
         }

         obj.email = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 13 + buf.getIntLE(offset + 9);
         int urlLen = VarInt.peek(buf, varPos2);
         if (urlLen < 0) {
            throw ProtocolException.negativeLength("Url", urlLen);
         }

         if (urlLen > 4096000) {
            throw ProtocolException.stringTooLong("Url", urlLen, 4096000);
         }

         obj.url = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 13 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 13 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         int pos2 = offset + 13 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.email != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.url != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int emailOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int urlOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.name != null) {
         buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.name, 4096000);
      } else {
         buf.setIntLE(nameOffsetSlot, -1);
      }

      if (this.email != null) {
         buf.setIntLE(emailOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.email, 4096000);
      } else {
         buf.setIntLE(emailOffsetSlot, -1);
      }

      if (this.url != null) {
         buf.setIntLE(urlOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.url, 4096000);
      } else {
         buf.setIntLE(urlOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 13;
      if (this.name != null) {
         size += PacketIO.stringSize(this.name);
      }

      if (this.email != null) {
         size += PacketIO.stringSize(this.email);
      }

      if (this.url != null) {
         size += PacketIO.stringSize(this.url);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int nameOffset = buffer.getIntLE(offset + 1);
            if (nameOffset < 0) {
               return ValidationResult.error("Invalid offset for Name");
            }

            int pos = offset + 13 + nameOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Name");
            }

            int nameLen = VarInt.peek(buffer, pos);
            if (nameLen < 0) {
               return ValidationResult.error("Invalid string length for Name");
            }

            if (nameLen > 4096000) {
               return ValidationResult.error("Name exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += nameLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Name");
            }
         }

         if ((nullBits & 2) != 0) {
            int emailOffset = buffer.getIntLE(offset + 5);
            if (emailOffset < 0) {
               return ValidationResult.error("Invalid offset for Email");
            }

            int posx = offset + 13 + emailOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Email");
            }

            int emailLen = VarInt.peek(buffer, posx);
            if (emailLen < 0) {
               return ValidationResult.error("Invalid string length for Email");
            }

            if (emailLen > 4096000) {
               return ValidationResult.error("Email exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += emailLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Email");
            }
         }

         if ((nullBits & 4) != 0) {
            int urlOffset = buffer.getIntLE(offset + 9);
            if (urlOffset < 0) {
               return ValidationResult.error("Invalid offset for Url");
            }

            int posxx = offset + 13 + urlOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Url");
            }

            int urlLen = VarInt.peek(buffer, posxx);
            if (urlLen < 0) {
               return ValidationResult.error("Invalid string length for Url");
            }

            if (urlLen > 4096000) {
               return ValidationResult.error("Url exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += urlLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Url");
            }
         }

         return ValidationResult.OK;
      }
   }

   public AuthorInfo clone() {
      AuthorInfo copy = new AuthorInfo();
      copy.name = this.name;
      copy.email = this.email;
      copy.url = this.url;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AuthorInfo other)
            ? false
            : Objects.equals(this.name, other.name) && Objects.equals(this.email, other.email) && Objects.equals(this.url, other.url);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name, this.email, this.url);
   }
}
