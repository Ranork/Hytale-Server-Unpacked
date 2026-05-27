package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
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

public class AssetEditorRequestChildrenListReply implements Packet, ToClientPacket {
   public static final int PACKET_ID = 322;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public AssetPath path;
   @Nullable
   public String[] childrenIds;

   @Override
   public int getId() {
      return 322;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorRequestChildrenListReply() {
   }

   public AssetEditorRequestChildrenListReply(@Nullable AssetPath path, @Nullable String[] childrenIds) {
      this.path = path;
      this.childrenIds = childrenIds;
   }

   public AssetEditorRequestChildrenListReply(@Nonnull AssetEditorRequestChildrenListReply other) {
      this.path = other.path;
      this.childrenIds = other.childrenIds;
   }

   @Nonnull
   public static AssetEditorRequestChildrenListReply deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("AssetEditorRequestChildrenListReply", 9, buf.readableBytes() - offset);
      } else {
         AssetEditorRequestChildrenListReply obj = new AssetEditorRequestChildrenListReply();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("Path", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 9 + varPosBase0;
            obj.path = AssetPath.deserialize(buf, varPos0);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("ChildrenIds", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 9 + varPosBase1;
            int childrenIdsCount = VarInt.peek(buf, varPos1);
            if (childrenIdsCount < 0) {
               throw ProtocolException.invalidVarInt("ChildrenIds");
            }

            int varIntLen = VarInt.size(childrenIdsCount);
            if (childrenIdsCount > 4096000) {
               throw ProtocolException.arrayTooLong("ChildrenIds", childrenIdsCount, 4096000);
            }

            if (varPos1 + varIntLen + childrenIdsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ChildrenIds", varPos1 + varIntLen + childrenIdsCount * 1, buf.readableBytes());
            }

            obj.childrenIds = new String[childrenIdsCount];
            int elemPos = varPos1 + varIntLen;

            for (int i = 0; i < childrenIdsCount; i++) {
               int strLen = VarInt.peek(buf, elemPos);
               if (strLen < 0) {
                  throw ProtocolException.invalidVarInt("childrenIds[" + i + "]");
               }

               int strVarLen = VarInt.size(strLen);
               if (strLen > 4096000) {
                  throw ProtocolException.stringTooLong("childrenIds[" + i + "]", strLen, 4096000);
               }

               if (elemPos + strVarLen + strLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("childrenIds[" + i + "]", elemPos + strVarLen + strLen, buf.readableBytes());
               }

               obj.childrenIds[i] = PacketIO.readVarString(buf, elemPos);
               elemPos += strVarLen + strLen;
            }
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
            throw ProtocolException.invalidOffset("Path", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 9 + fieldOffset0;
         pos0 += AssetPath.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("ChildrenIds", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 9 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos1);
            pos1 += VarInt.size(sl) + sl;
         }

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
   public static AssetPath getPath(MemorySegment mem) {
      return getPath(mem, 0);
   }

   @Nullable
   public static AssetPath getPath(MemorySegment mem, int offset) {
      return hasPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 1, 9, "Path")) : null;
   }

   @Nullable
   public static String[] getChildrenIds(MemorySegment mem) {
      return getChildrenIds(mem, 0);
   }

   @Nullable
   public static String[] getChildrenIds(MemorySegment mem, int offset) {
      if (!hasChildrenIds(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 9, "ChildrenIds");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ChildrenIds", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("ChildrenIds", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ChildrenIds", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("ChildrenIds", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   public static boolean hasPath(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasChildrenIds(MemorySegment mem, int offset) {
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

   public static AssetEditorRequestChildrenListReply toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorRequestChildrenListReply toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorRequestChildrenListReply", offset + 9, (int)mem.byteSize());
      } else {
         String[] childrenIds = null;
         if (hasChildrenIds(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 5, 9, "ChildrenIds");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("ChildrenIds", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("ChildrenIds", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ChildrenIds", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            childrenIds = new String[len];

            for (int i = 0; i < len; i++) {
               long sp = VarInt.getWithLength(mem, off);
               int n = (int)sp + (int)(sp >>> 32);
               childrenIds[i] = PacketIO.readVarString("ChildrenIds", mem, off, 16384000, PacketIO.UTF8);
               off += n;
            }
         }

         return new AssetEditorRequestChildrenListReply(
            hasPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 1, 9, "Path")) : null, childrenIds
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.path != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.childrenIds != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int pathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int childrenIdsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.path != null) {
         buf.setIntLE(pathOffsetSlot, buf.writerIndex() - varBlockStart);
         this.path.serialize(buf);
      } else {
         buf.setIntLE(pathOffsetSlot, -1);
      }

      if (this.childrenIds != null) {
         buf.setIntLE(childrenIdsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.childrenIds.length > 4096000) {
            throw ProtocolException.arrayTooLong("ChildrenIds", this.childrenIds.length, 4096000);
         }

         VarInt.write(buf, this.childrenIds.length);

         for (String item : this.childrenIds) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(childrenIdsOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.path != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.childrenIds != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 9;
      if (this.path != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 9);
         varOffset += this.path.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.childrenIds != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 9);
         if (this.childrenIds.length > 4096000) {
            throw ProtocolException.arrayTooLong("ChildrenIds", this.childrenIds.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.childrenIds.length);
         int childrenIdsValueOffset = 0;

         for (int i = 0; i < this.childrenIds.length; i++) {
            childrenIdsValueOffset += PacketIO.writeVarString(mem, varOffset + childrenIdsValueOffset, this.childrenIds[i], 16384000);
         }

         varOffset += childrenIdsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 9;
      if (this.path != null) {
         size += this.path.computeSize();
      }

      if (this.childrenIds != null) {
         int childrenIdsSize = 0;

         for (String elem : this.childrenIds) {
            childrenIdsSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.childrenIds.length) + childrenIdsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int pathOffset = buffer.getIntLE(offset + 1);
            if (pathOffset < 0 || pathOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for Path");
            }

            int pos = offset + 9 + pathOffset;
            ValidationResult pathResult = AssetPath.validateStructure(buffer, pos);
            if (!pathResult.isValid()) {
               return ValidationResult.error("Invalid Path: " + pathResult.error());
            }

            pos += AssetPath.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int childrenIdsOffset = buffer.getIntLE(offset + 5);
            if (childrenIdsOffset < 0 || childrenIdsOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for ChildrenIds");
            }

            int pos = offset + 9 + childrenIdsOffset;
            int childrenIdsCount = VarInt.peek(buffer, pos);
            if (childrenIdsCount < 0) {
               return ValidationResult.error("Invalid array count for ChildrenIds");
            }

            if (childrenIdsCount > 4096000) {
               return ValidationResult.error("ChildrenIds exceeds max length 4096000");
            }

            pos += VarInt.size(childrenIdsCount);

            for (int i = 0; i < childrenIdsCount; i++) {
               int strLen = VarInt.peek(buffer, pos);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in ChildrenIds");
               }

               pos += VarInt.size(strLen);
               pos += strLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in ChildrenIds");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorRequestChildrenListReply clone() {
      AssetEditorRequestChildrenListReply copy = new AssetEditorRequestChildrenListReply();
      copy.path = this.path != null ? this.path.clone() : null;
      copy.childrenIds = this.childrenIds != null ? Arrays.copyOf(this.childrenIds, this.childrenIds.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorRequestChildrenListReply other)
            ? false
            : Objects.equals(this.path, other.path) && Arrays.equals((Object[])this.childrenIds, (Object[])other.childrenIds);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.path);
      return 31 * result + Arrays.hashCode((Object[])this.childrenIds);
   }
}
