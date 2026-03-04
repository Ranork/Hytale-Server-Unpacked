package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetPackManifest {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 7;
   public static final int VARIABLE_BLOCK_START = 29;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String name;
   @Nullable
   public String group;
   @Nullable
   public String website;
   @Nullable
   public String description;
   @Nullable
   public String version;
   @Nullable
   public AuthorInfo[] authors;
   @Nullable
   public String serverVersion;

   public AssetPackManifest() {
   }

   public AssetPackManifest(
      @Nullable String name,
      @Nullable String group,
      @Nullable String website,
      @Nullable String description,
      @Nullable String version,
      @Nullable AuthorInfo[] authors,
      @Nullable String serverVersion
   ) {
      this.name = name;
      this.group = group;
      this.website = website;
      this.description = description;
      this.version = version;
      this.authors = authors;
      this.serverVersion = serverVersion;
   }

   public AssetPackManifest(@Nonnull AssetPackManifest other) {
      this.name = other.name;
      this.group = other.group;
      this.website = other.website;
      this.description = other.description;
      this.version = other.version;
      this.authors = other.authors;
      this.serverVersion = other.serverVersion;
   }

   @Nonnull
   public static AssetPackManifest deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetPackManifest obj = new AssetPackManifest();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 29 + buf.getIntLE(offset + 1);
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
         int varPos1 = offset + 29 + buf.getIntLE(offset + 5);
         int groupLen = VarInt.peek(buf, varPos1);
         if (groupLen < 0) {
            throw ProtocolException.negativeLength("Group", groupLen);
         }

         if (groupLen > 4096000) {
            throw ProtocolException.stringTooLong("Group", groupLen, 4096000);
         }

         obj.group = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 29 + buf.getIntLE(offset + 9);
         int websiteLen = VarInt.peek(buf, varPos2);
         if (websiteLen < 0) {
            throw ProtocolException.negativeLength("Website", websiteLen);
         }

         if (websiteLen > 4096000) {
            throw ProtocolException.stringTooLong("Website", websiteLen, 4096000);
         }

         obj.website = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
      }

      if ((nullBits & 8) != 0) {
         int varPos3 = offset + 29 + buf.getIntLE(offset + 13);
         int descriptionLen = VarInt.peek(buf, varPos3);
         if (descriptionLen < 0) {
            throw ProtocolException.negativeLength("Description", descriptionLen);
         }

         if (descriptionLen > 4096000) {
            throw ProtocolException.stringTooLong("Description", descriptionLen, 4096000);
         }

         obj.description = PacketIO.readVarString(buf, varPos3, PacketIO.UTF8);
      }

      if ((nullBits & 16) != 0) {
         int varPos4 = offset + 29 + buf.getIntLE(offset + 17);
         int versionLen = VarInt.peek(buf, varPos4);
         if (versionLen < 0) {
            throw ProtocolException.negativeLength("Version", versionLen);
         }

         if (versionLen > 4096000) {
            throw ProtocolException.stringTooLong("Version", versionLen, 4096000);
         }

         obj.version = PacketIO.readVarString(buf, varPos4, PacketIO.UTF8);
      }

      if ((nullBits & 32) != 0) {
         int varPos5 = offset + 29 + buf.getIntLE(offset + 21);
         int authorsCount = VarInt.peek(buf, varPos5);
         if (authorsCount < 0) {
            throw ProtocolException.negativeLength("Authors", authorsCount);
         }

         if (authorsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Authors", authorsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos5);
         if (varPos5 + varIntLen + authorsCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Authors", varPos5 + varIntLen + authorsCount * 1, buf.readableBytes());
         }

         obj.authors = new AuthorInfo[authorsCount];
         int elemPos = varPos5 + varIntLen;

         for (int i = 0; i < authorsCount; i++) {
            obj.authors[i] = AuthorInfo.deserialize(buf, elemPos);
            elemPos += AuthorInfo.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits & 64) != 0) {
         int varPos6 = offset + 29 + buf.getIntLE(offset + 25);
         int serverVersionLen = VarInt.peek(buf, varPos6);
         if (serverVersionLen < 0) {
            throw ProtocolException.negativeLength("ServerVersion", serverVersionLen);
         }

         if (serverVersionLen > 4096000) {
            throw ProtocolException.stringTooLong("ServerVersion", serverVersionLen, 4096000);
         }

         obj.serverVersion = PacketIO.readVarString(buf, varPos6, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 29;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 29 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 29 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         int pos2 = offset + 29 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 13);
         int pos3 = offset + 29 + fieldOffset3;
         int sl = VarInt.peek(buf, pos3);
         pos3 += VarInt.length(buf, pos3) + sl;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 17);
         int pos4 = offset + 29 + fieldOffset4;
         int sl = VarInt.peek(buf, pos4);
         pos4 += VarInt.length(buf, pos4) + sl;
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 21);
         int pos5 = offset + 29 + fieldOffset5;
         int arrLen = VarInt.peek(buf, pos5);
         pos5 += VarInt.length(buf, pos5);

         for (int i = 0; i < arrLen; i++) {
            pos5 += AuthorInfo.computeBytesConsumed(buf, pos5);
         }

         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits & 64) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 25);
         int pos6 = offset + 29 + fieldOffset6;
         int sl = VarInt.peek(buf, pos6);
         pos6 += VarInt.length(buf, pos6) + sl;
         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
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

      if (this.group != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.website != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.description != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.version != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.authors != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.serverVersion != null) {
         nullBits = (byte)(nullBits | 64);
      }

      buf.writeByte(nullBits);
      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int groupOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int websiteOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int descriptionOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int versionOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int authorsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int serverVersionOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.name != null) {
         buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.name, 4096000);
      } else {
         buf.setIntLE(nameOffsetSlot, -1);
      }

      if (this.group != null) {
         buf.setIntLE(groupOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.group, 4096000);
      } else {
         buf.setIntLE(groupOffsetSlot, -1);
      }

      if (this.website != null) {
         buf.setIntLE(websiteOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.website, 4096000);
      } else {
         buf.setIntLE(websiteOffsetSlot, -1);
      }

      if (this.description != null) {
         buf.setIntLE(descriptionOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.description, 4096000);
      } else {
         buf.setIntLE(descriptionOffsetSlot, -1);
      }

      if (this.version != null) {
         buf.setIntLE(versionOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.version, 4096000);
      } else {
         buf.setIntLE(versionOffsetSlot, -1);
      }

      if (this.authors != null) {
         buf.setIntLE(authorsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.authors.length > 4096000) {
            throw ProtocolException.arrayTooLong("Authors", this.authors.length, 4096000);
         }

         VarInt.write(buf, this.authors.length);

         for (AuthorInfo item : this.authors) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(authorsOffsetSlot, -1);
      }

      if (this.serverVersion != null) {
         buf.setIntLE(serverVersionOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.serverVersion, 4096000);
      } else {
         buf.setIntLE(serverVersionOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 29;
      if (this.name != null) {
         size += PacketIO.stringSize(this.name);
      }

      if (this.group != null) {
         size += PacketIO.stringSize(this.group);
      }

      if (this.website != null) {
         size += PacketIO.stringSize(this.website);
      }

      if (this.description != null) {
         size += PacketIO.stringSize(this.description);
      }

      if (this.version != null) {
         size += PacketIO.stringSize(this.version);
      }

      if (this.authors != null) {
         int authorsSize = 0;

         for (AuthorInfo elem : this.authors) {
            authorsSize += elem.computeSize();
         }

         size += VarInt.size(this.authors.length) + authorsSize;
      }

      if (this.serverVersion != null) {
         size += PacketIO.stringSize(this.serverVersion);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 29) {
         return ValidationResult.error("Buffer too small: expected at least 29 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int nameOffset = buffer.getIntLE(offset + 1);
            if (nameOffset < 0) {
               return ValidationResult.error("Invalid offset for Name");
            }

            int pos = offset + 29 + nameOffset;
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
            int groupOffset = buffer.getIntLE(offset + 5);
            if (groupOffset < 0) {
               return ValidationResult.error("Invalid offset for Group");
            }

            int posx = offset + 29 + groupOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Group");
            }

            int groupLen = VarInt.peek(buffer, posx);
            if (groupLen < 0) {
               return ValidationResult.error("Invalid string length for Group");
            }

            if (groupLen > 4096000) {
               return ValidationResult.error("Group exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += groupLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Group");
            }
         }

         if ((nullBits & 4) != 0) {
            int websiteOffset = buffer.getIntLE(offset + 9);
            if (websiteOffset < 0) {
               return ValidationResult.error("Invalid offset for Website");
            }

            int posxx = offset + 29 + websiteOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Website");
            }

            int websiteLen = VarInt.peek(buffer, posxx);
            if (websiteLen < 0) {
               return ValidationResult.error("Invalid string length for Website");
            }

            if (websiteLen > 4096000) {
               return ValidationResult.error("Website exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += websiteLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Website");
            }
         }

         if ((nullBits & 8) != 0) {
            int descriptionOffset = buffer.getIntLE(offset + 13);
            if (descriptionOffset < 0) {
               return ValidationResult.error("Invalid offset for Description");
            }

            int posxxx = offset + 29 + descriptionOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Description");
            }

            int descriptionLen = VarInt.peek(buffer, posxxx);
            if (descriptionLen < 0) {
               return ValidationResult.error("Invalid string length for Description");
            }

            if (descriptionLen > 4096000) {
               return ValidationResult.error("Description exceeds max length 4096000");
            }

            posxxx += VarInt.length(buffer, posxxx);
            posxxx += descriptionLen;
            if (posxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Description");
            }
         }

         if ((nullBits & 16) != 0) {
            int versionOffset = buffer.getIntLE(offset + 17);
            if (versionOffset < 0) {
               return ValidationResult.error("Invalid offset for Version");
            }

            int posxxxx = offset + 29 + versionOffset;
            if (posxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Version");
            }

            int versionLen = VarInt.peek(buffer, posxxxx);
            if (versionLen < 0) {
               return ValidationResult.error("Invalid string length for Version");
            }

            if (versionLen > 4096000) {
               return ValidationResult.error("Version exceeds max length 4096000");
            }

            posxxxx += VarInt.length(buffer, posxxxx);
            posxxxx += versionLen;
            if (posxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Version");
            }
         }

         if ((nullBits & 32) != 0) {
            int authorsOffset = buffer.getIntLE(offset + 21);
            if (authorsOffset < 0) {
               return ValidationResult.error("Invalid offset for Authors");
            }

            int posxxxxx = offset + 29 + authorsOffset;
            if (posxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Authors");
            }

            int authorsCount = VarInt.peek(buffer, posxxxxx);
            if (authorsCount < 0) {
               return ValidationResult.error("Invalid array count for Authors");
            }

            if (authorsCount > 4096000) {
               return ValidationResult.error("Authors exceeds max length 4096000");
            }

            posxxxxx += VarInt.length(buffer, posxxxxx);

            for (int i = 0; i < authorsCount; i++) {
               ValidationResult structResult = AuthorInfo.validateStructure(buffer, posxxxxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid AuthorInfo in Authors[" + i + "]: " + structResult.error());
               }

               posxxxxx += AuthorInfo.computeBytesConsumed(buffer, posxxxxx);
            }
         }

         if ((nullBits & 64) != 0) {
            int serverVersionOffset = buffer.getIntLE(offset + 25);
            if (serverVersionOffset < 0) {
               return ValidationResult.error("Invalid offset for ServerVersion");
            }

            int posxxxxxx = offset + 29 + serverVersionOffset;
            if (posxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ServerVersion");
            }

            int serverVersionLen = VarInt.peek(buffer, posxxxxxx);
            if (serverVersionLen < 0) {
               return ValidationResult.error("Invalid string length for ServerVersion");
            }

            if (serverVersionLen > 4096000) {
               return ValidationResult.error("ServerVersion exceeds max length 4096000");
            }

            posxxxxxx += VarInt.length(buffer, posxxxxxx);
            posxxxxxx += serverVersionLen;
            if (posxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ServerVersion");
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetPackManifest clone() {
      AssetPackManifest copy = new AssetPackManifest();
      copy.name = this.name;
      copy.group = this.group;
      copy.website = this.website;
      copy.description = this.description;
      copy.version = this.version;
      copy.authors = this.authors != null ? Arrays.stream(this.authors).map(e -> e.clone()).toArray(AuthorInfo[]::new) : null;
      copy.serverVersion = this.serverVersion;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetPackManifest other)
            ? false
            : Objects.equals(this.name, other.name)
               && Objects.equals(this.group, other.group)
               && Objects.equals(this.website, other.website)
               && Objects.equals(this.description, other.description)
               && Objects.equals(this.version, other.version)
               && Arrays.equals((Object[])this.authors, (Object[])other.authors)
               && Objects.equals(this.serverVersion, other.serverVersion);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Objects.hashCode(this.group);
      result = 31 * result + Objects.hashCode(this.website);
      result = 31 * result + Objects.hashCode(this.description);
      result = 31 * result + Objects.hashCode(this.version);
      result = 31 * result + Arrays.hashCode((Object[])this.authors);
      return 31 * result + Objects.hashCode(this.serverVersion);
   }
}
