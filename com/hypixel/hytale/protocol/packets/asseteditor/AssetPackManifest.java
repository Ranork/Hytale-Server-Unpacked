package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 29) {
         throw ProtocolException.bufferTooSmall("AssetPackManifest", 29, buf.readableBytes() - offset);
      } else {
         AssetPackManifest obj = new AssetPackManifest();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 29) {
               throw ProtocolException.invalidOffset("Name", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 29 + varPosBase0;
            int nameLen = VarInt.peek(buf, varPos0);
            if (nameLen < 0) {
               throw ProtocolException.invalidVarInt("Name");
            }

            int nameVarIntLen = VarInt.size(nameLen);
            if (nameLen > 4096000) {
               throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
            }

            if (varPos0 + nameVarIntLen + nameLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Name", varPos0 + nameVarIntLen + nameLen, buf.readableBytes());
            }

            obj.name = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 29) {
               throw ProtocolException.invalidOffset("Group", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 29 + varPosBase1;
            int groupLen = VarInt.peek(buf, varPos1);
            if (groupLen < 0) {
               throw ProtocolException.invalidVarInt("Group");
            }

            int groupVarIntLen = VarInt.size(groupLen);
            if (groupLen > 4096000) {
               throw ProtocolException.stringTooLong("Group", groupLen, 4096000);
            }

            if (varPos1 + groupVarIntLen + groupLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Group", varPos1 + groupVarIntLen + groupLen, buf.readableBytes());
            }

            obj.group = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 9);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 29) {
               throw ProtocolException.invalidOffset("Website", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 29 + varPosBase2;
            int websiteLen = VarInt.peek(buf, varPos2);
            if (websiteLen < 0) {
               throw ProtocolException.invalidVarInt("Website");
            }

            int websiteVarIntLen = VarInt.size(websiteLen);
            if (websiteLen > 4096000) {
               throw ProtocolException.stringTooLong("Website", websiteLen, 4096000);
            }

            if (varPos2 + websiteVarIntLen + websiteLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Website", varPos2 + websiteVarIntLen + websiteLen, buf.readableBytes());
            }

            obj.website = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 13);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 29) {
               throw ProtocolException.invalidOffset("Description", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 29 + varPosBase3;
            int descriptionLen = VarInt.peek(buf, varPos3);
            if (descriptionLen < 0) {
               throw ProtocolException.invalidVarInt("Description");
            }

            int descriptionVarIntLen = VarInt.size(descriptionLen);
            if (descriptionLen > 4096000) {
               throw ProtocolException.stringTooLong("Description", descriptionLen, 4096000);
            }

            if (varPos3 + descriptionVarIntLen + descriptionLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Description", varPos3 + descriptionVarIntLen + descriptionLen, buf.readableBytes());
            }

            obj.description = PacketIO.readVarString(buf, varPos3, PacketIO.UTF8);
         }

         if ((nullBits & 16) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 17);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 29) {
               throw ProtocolException.invalidOffset("Version", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 29 + varPosBase4;
            int versionLen = VarInt.peek(buf, varPos4);
            if (versionLen < 0) {
               throw ProtocolException.invalidVarInt("Version");
            }

            int versionVarIntLen = VarInt.size(versionLen);
            if (versionLen > 4096000) {
               throw ProtocolException.stringTooLong("Version", versionLen, 4096000);
            }

            if (varPos4 + versionVarIntLen + versionLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Version", varPos4 + versionVarIntLen + versionLen, buf.readableBytes());
            }

            obj.version = PacketIO.readVarString(buf, varPos4, PacketIO.UTF8);
         }

         if ((nullBits & 32) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 21);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 29) {
               throw ProtocolException.invalidOffset("Authors", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 29 + varPosBase5;
            int authorsCount = VarInt.peek(buf, varPos5);
            if (authorsCount < 0) {
               throw ProtocolException.invalidVarInt("Authors");
            }

            int varIntLen = VarInt.size(authorsCount);
            if (authorsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Authors", authorsCount, 4096000);
            }

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
            int varPosBase6 = buf.getIntLE(offset + 25);
            if (varPosBase6 < 0 || varPosBase6 > buf.writerIndex() - offset - 29) {
               throw ProtocolException.invalidOffset("ServerVersion", varPosBase6, buf.readableBytes());
            }

            int varPos6 = offset + 29 + varPosBase6;
            int serverVersionLen = VarInt.peek(buf, varPos6);
            if (serverVersionLen < 0) {
               throw ProtocolException.invalidVarInt("ServerVersion");
            }

            int serverVersionVarIntLen = VarInt.size(serverVersionLen);
            if (serverVersionLen > 4096000) {
               throw ProtocolException.stringTooLong("ServerVersion", serverVersionLen, 4096000);
            }

            if (varPos6 + serverVersionVarIntLen + serverVersionLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ServerVersion", varPos6 + serverVersionVarIntLen + serverVersionLen, buf.readableBytes());
            }

            obj.serverVersion = PacketIO.readVarString(buf, varPos6, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 29;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 29) {
            throw ProtocolException.invalidOffset("Name", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 29 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 29) {
            throw ProtocolException.invalidOffset("Group", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 29 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 29) {
            throw ProtocolException.invalidOffset("Website", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 29 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 13);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 29) {
            throw ProtocolException.invalidOffset("Description", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 29 + fieldOffset3;
         int sl = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(sl) + sl;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 17);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 29) {
            throw ProtocolException.invalidOffset("Version", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 29 + fieldOffset4;
         int sl = VarInt.peek(buf, pos4);
         pos4 += VarInt.size(sl) + sl;
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 21);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 29) {
            throw ProtocolException.invalidOffset("Authors", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 29 + fieldOffset5;
         int arrLen = VarInt.peek(buf, pos5);
         pos5 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos5 += AuthorInfo.computeBytesConsumed(buf, pos5);
         }

         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits & 64) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 25);
         if (fieldOffset6 < 0 || fieldOffset6 > buf.writerIndex() - offset - 29) {
            throw ProtocolException.invalidOffset("ServerVersion", fieldOffset6, maxEnd);
         }

         int pos6 = offset + 29 + fieldOffset6;
         int sl = VarInt.peek(buf, pos6);
         pos6 += VarInt.size(sl) + sl;
         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 29L;
   }

   @Nullable
   public static String getName(MemorySegment mem) {
      return getName(mem, 0);
   }

   @Nullable
   public static String getName(MemorySegment mem, int offset) {
      return hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 1, 29, "Name"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static String getGroup(MemorySegment mem) {
      return getGroup(mem, 0);
   }

   @Nullable
   public static String getGroup(MemorySegment mem, int offset) {
      return hasGroup(mem, offset)
         ? PacketIO.readVarString("Group", mem, offset + getValidatedOffset(mem, offset, 5, 29, "Group"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getWebsite(MemorySegment mem) {
      return getWebsite(mem, 0);
   }

   @Nullable
   public static String getWebsite(MemorySegment mem, int offset) {
      return hasWebsite(mem, offset)
         ? PacketIO.readVarString("Website", mem, offset + getValidatedOffset(mem, offset, 9, 29, "Website"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getDescription(MemorySegment mem) {
      return getDescription(mem, 0);
   }

   @Nullable
   public static String getDescription(MemorySegment mem, int offset) {
      return hasDescription(mem, offset)
         ? PacketIO.readVarString("Description", mem, offset + getValidatedOffset(mem, offset, 13, 29, "Description"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getVersion(MemorySegment mem) {
      return getVersion(mem, 0);
   }

   @Nullable
   public static String getVersion(MemorySegment mem, int offset) {
      return hasVersion(mem, offset)
         ? PacketIO.readVarString("Version", mem, offset + getValidatedOffset(mem, offset, 17, 29, "Version"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static AuthorInfo[] getAuthors(MemorySegment mem) {
      return getAuthors(mem, 0);
   }

   @Nullable
   public static AuthorInfo[] getAuthors(MemorySegment mem, int offset) {
      if (!hasAuthors(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 21, 29, "Authors");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Authors", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Authors", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Authors", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               AuthorInfo[] data = new AuthorInfo[len];

               for (int i = 0; i < len; i++) {
                  data[i] = AuthorInfo.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static String getServerVersion(MemorySegment mem) {
      return getServerVersion(mem, 0);
   }

   @Nullable
   public static String getServerVersion(MemorySegment mem, int offset) {
      return hasServerVersion(mem, offset)
         ? PacketIO.readVarString("ServerVersion", mem, offset + getValidatedOffset(mem, offset, 25, 29, "ServerVersion"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean hasName(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasGroup(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasWebsite(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasDescription(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasVersion(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasAuthors(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasServerVersion(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static AssetPackManifest toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetPackManifest toObject(MemorySegment mem, int offset) {
      if (offset + 29 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetPackManifest", offset + 29, (int)mem.byteSize());
      } else {
         AuthorInfo[] authors = null;
         if (hasAuthors(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 21, 29, "Authors");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Authors", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Authors", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Authors", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            authors = new AuthorInfo[len];

            for (int i = 0; i < len; i++) {
               authors[i] = AuthorInfo.toObject(mem, off);
               off += authors[i].computeSize();
            }
         }

         return new AssetPackManifest(
            hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 1, 29, "Name"), 4096000, PacketIO.UTF8) : null,
            hasGroup(mem, offset)
               ? PacketIO.readVarString("Group", mem, offset + getValidatedOffset(mem, offset, 5, 29, "Group"), 4096000, PacketIO.UTF8)
               : null,
            hasWebsite(mem, offset)
               ? PacketIO.readVarString("Website", mem, offset + getValidatedOffset(mem, offset, 9, 29, "Website"), 4096000, PacketIO.UTF8)
               : null,
            hasDescription(mem, offset)
               ? PacketIO.readVarString("Description", mem, offset + getValidatedOffset(mem, offset, 13, 29, "Description"), 4096000, PacketIO.UTF8)
               : null,
            hasVersion(mem, offset)
               ? PacketIO.readVarString("Version", mem, offset + getValidatedOffset(mem, offset, 17, 29, "Version"), 4096000, PacketIO.UTF8)
               : null,
            authors,
            hasServerVersion(mem, offset)
               ? PacketIO.readVarString("ServerVersion", mem, offset + getValidatedOffset(mem, offset, 25, 29, "ServerVersion"), 4096000, PacketIO.UTF8)
               : null
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 29;
      if (this.name != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 29);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.name, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.group != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 29);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.group, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.website != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 29);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.website, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      if (this.description != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), varOffset - offset - 29);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.description, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), -1);
      }

      if (this.version != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), varOffset - offset - 29);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.version, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), -1);
      }

      if (this.authors != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), varOffset - offset - 29);
         if (this.authors.length > 4096000) {
            throw ProtocolException.arrayTooLong("Authors", this.authors.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.authors.length);
         int authorsValueOffset = 0;

         for (int i = 0; i < this.authors.length; i++) {
            authorsValueOffset += this.authors[i].serialize(mem, varOffset + authorsValueOffset);
         }

         varOffset += authorsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), -1);
      }

      if (this.serverVersion != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 25), varOffset - offset - 29);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.serverVersion, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 25), -1);
      }

      return varOffset - offset;
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
            if (nameOffset < 0 || nameOffset > buffer.writerIndex() - offset - 29) {
               return ValidationResult.error("Invalid offset for Name");
            }

            int pos = offset + 29 + nameOffset;
            int nameLen = VarInt.peek(buffer, pos);
            if (nameLen < 0) {
               return ValidationResult.error("Invalid string length for Name");
            }

            if (nameLen > 4096000) {
               return ValidationResult.error("Name exceeds max length 4096000");
            }

            pos += VarInt.size(nameLen);
            pos += nameLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Name");
            }
         }

         if ((nullBits & 2) != 0) {
            int groupOffset = buffer.getIntLE(offset + 5);
            if (groupOffset < 0 || groupOffset > buffer.writerIndex() - offset - 29) {
               return ValidationResult.error("Invalid offset for Group");
            }

            int posx = offset + 29 + groupOffset;
            int groupLen = VarInt.peek(buffer, posx);
            if (groupLen < 0) {
               return ValidationResult.error("Invalid string length for Group");
            }

            if (groupLen > 4096000) {
               return ValidationResult.error("Group exceeds max length 4096000");
            }

            posx += VarInt.size(groupLen);
            posx += groupLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Group");
            }
         }

         if ((nullBits & 4) != 0) {
            int websiteOffset = buffer.getIntLE(offset + 9);
            if (websiteOffset < 0 || websiteOffset > buffer.writerIndex() - offset - 29) {
               return ValidationResult.error("Invalid offset for Website");
            }

            int posxx = offset + 29 + websiteOffset;
            int websiteLen = VarInt.peek(buffer, posxx);
            if (websiteLen < 0) {
               return ValidationResult.error("Invalid string length for Website");
            }

            if (websiteLen > 4096000) {
               return ValidationResult.error("Website exceeds max length 4096000");
            }

            posxx += VarInt.size(websiteLen);
            posxx += websiteLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Website");
            }
         }

         if ((nullBits & 8) != 0) {
            int descriptionOffset = buffer.getIntLE(offset + 13);
            if (descriptionOffset < 0 || descriptionOffset > buffer.writerIndex() - offset - 29) {
               return ValidationResult.error("Invalid offset for Description");
            }

            int posxxx = offset + 29 + descriptionOffset;
            int descriptionLen = VarInt.peek(buffer, posxxx);
            if (descriptionLen < 0) {
               return ValidationResult.error("Invalid string length for Description");
            }

            if (descriptionLen > 4096000) {
               return ValidationResult.error("Description exceeds max length 4096000");
            }

            posxxx += VarInt.size(descriptionLen);
            posxxx += descriptionLen;
            if (posxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Description");
            }
         }

         if ((nullBits & 16) != 0) {
            int versionOffset = buffer.getIntLE(offset + 17);
            if (versionOffset < 0 || versionOffset > buffer.writerIndex() - offset - 29) {
               return ValidationResult.error("Invalid offset for Version");
            }

            int posxxxx = offset + 29 + versionOffset;
            int versionLen = VarInt.peek(buffer, posxxxx);
            if (versionLen < 0) {
               return ValidationResult.error("Invalid string length for Version");
            }

            if (versionLen > 4096000) {
               return ValidationResult.error("Version exceeds max length 4096000");
            }

            posxxxx += VarInt.size(versionLen);
            posxxxx += versionLen;
            if (posxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Version");
            }
         }

         if ((nullBits & 32) != 0) {
            int authorsOffset = buffer.getIntLE(offset + 21);
            if (authorsOffset < 0 || authorsOffset > buffer.writerIndex() - offset - 29) {
               return ValidationResult.error("Invalid offset for Authors");
            }

            int posxxxxx = offset + 29 + authorsOffset;
            int authorsCount = VarInt.peek(buffer, posxxxxx);
            if (authorsCount < 0) {
               return ValidationResult.error("Invalid array count for Authors");
            }

            if (authorsCount > 4096000) {
               return ValidationResult.error("Authors exceeds max length 4096000");
            }

            posxxxxx += VarInt.size(authorsCount);

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
            if (serverVersionOffset < 0 || serverVersionOffset > buffer.writerIndex() - offset - 29) {
               return ValidationResult.error("Invalid offset for ServerVersion");
            }

            int posxxxxxx = offset + 29 + serverVersionOffset;
            int serverVersionLen = VarInt.peek(buffer, posxxxxxx);
            if (serverVersionLen < 0) {
               return ValidationResult.error("Invalid string length for ServerVersion");
            }

            if (serverVersionLen > 4096000) {
               return ValidationResult.error("ServerVersion exceeds max length 4096000");
            }

            posxxxxxx += VarInt.size(serverVersionLen);
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
