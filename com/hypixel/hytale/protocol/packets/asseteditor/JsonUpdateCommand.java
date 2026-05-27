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

public class JsonUpdateCommand {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 7;
   public static final int VARIABLE_FIELD_COUNT = 4;
   public static final int VARIABLE_BLOCK_START = 23;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public JsonUpdateType type = JsonUpdateType.SetProperty;
   @Nullable
   public String[] path;
   @Nullable
   public String value;
   @Nullable
   public String previousValue;
   @Nullable
   public String[] firstCreatedProperty;
   @Nullable
   public AssetEditorRebuildCaches rebuildCaches;

   public JsonUpdateCommand() {
   }

   public JsonUpdateCommand(
      @Nonnull JsonUpdateType type,
      @Nullable String[] path,
      @Nullable String value,
      @Nullable String previousValue,
      @Nullable String[] firstCreatedProperty,
      @Nullable AssetEditorRebuildCaches rebuildCaches
   ) {
      this.type = type;
      this.path = path;
      this.value = value;
      this.previousValue = previousValue;
      this.firstCreatedProperty = firstCreatedProperty;
      this.rebuildCaches = rebuildCaches;
   }

   public JsonUpdateCommand(@Nonnull JsonUpdateCommand other) {
      this.type = other.type;
      this.path = other.path;
      this.value = other.value;
      this.previousValue = other.previousValue;
      this.firstCreatedProperty = other.firstCreatedProperty;
      this.rebuildCaches = other.rebuildCaches;
   }

   @Nonnull
   public static JsonUpdateCommand deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 23) {
         throw ProtocolException.bufferTooSmall("JsonUpdateCommand", 23, buf.readableBytes() - offset);
      } else {
         JsonUpdateCommand obj = new JsonUpdateCommand();
         byte nullBits = buf.getByte(offset);
         obj.type = JsonUpdateType.fromValue(buf.getByte(offset + 1));
         if ((nullBits & 1) != 0) {
            obj.rebuildCaches = AssetEditorRebuildCaches.deserialize(buf, offset + 2);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 7);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 23) {
               throw ProtocolException.invalidOffset("Path", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 23 + varPosBase0;
            int pathCount = VarInt.peek(buf, varPos0);
            if (pathCount < 0) {
               throw ProtocolException.invalidVarInt("Path");
            }

            int varIntLen = VarInt.size(pathCount);
            if (pathCount > 4096000) {
               throw ProtocolException.arrayTooLong("Path", pathCount, 4096000);
            }

            if (varPos0 + varIntLen + pathCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Path", varPos0 + varIntLen + pathCount * 1, buf.readableBytes());
            }

            obj.path = new String[pathCount];
            int elemPos = varPos0 + varIntLen;

            for (int i = 0; i < pathCount; i++) {
               int strLen = VarInt.peek(buf, elemPos);
               if (strLen < 0) {
                  throw ProtocolException.invalidVarInt("path[" + i + "]");
               }

               int strVarLen = VarInt.size(strLen);
               if (strLen > 4096000) {
                  throw ProtocolException.stringTooLong("path[" + i + "]", strLen, 4096000);
               }

               if (elemPos + strVarLen + strLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("path[" + i + "]", elemPos + strVarLen + strLen, buf.readableBytes());
               }

               obj.path[i] = PacketIO.readVarString(buf, elemPos);
               elemPos += strVarLen + strLen;
            }
         }

         if ((nullBits & 4) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 11);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 23) {
               throw ProtocolException.invalidOffset("Value", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 23 + varPosBase1;
            int valueLen = VarInt.peek(buf, varPos1);
            if (valueLen < 0) {
               throw ProtocolException.invalidVarInt("Value");
            }

            int valueVarIntLen = VarInt.size(valueLen);
            if (valueLen > 4096000) {
               throw ProtocolException.stringTooLong("Value", valueLen, 4096000);
            }

            if (varPos1 + valueVarIntLen + valueLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Value", varPos1 + valueVarIntLen + valueLen, buf.readableBytes());
            }

            obj.value = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 15);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 23) {
               throw ProtocolException.invalidOffset("PreviousValue", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 23 + varPosBase2;
            int previousValueLen = VarInt.peek(buf, varPos2);
            if (previousValueLen < 0) {
               throw ProtocolException.invalidVarInt("PreviousValue");
            }

            int previousValueVarIntLen = VarInt.size(previousValueLen);
            if (previousValueLen > 4096000) {
               throw ProtocolException.stringTooLong("PreviousValue", previousValueLen, 4096000);
            }

            if (varPos2 + previousValueVarIntLen + previousValueLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("PreviousValue", varPos2 + previousValueVarIntLen + previousValueLen, buf.readableBytes());
            }

            obj.previousValue = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         if ((nullBits & 16) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 19);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 23) {
               throw ProtocolException.invalidOffset("FirstCreatedProperty", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 23 + varPosBase3;
            int firstCreatedPropertyCount = VarInt.peek(buf, varPos3);
            if (firstCreatedPropertyCount < 0) {
               throw ProtocolException.invalidVarInt("FirstCreatedProperty");
            }

            int varIntLenx = VarInt.size(firstCreatedPropertyCount);
            if (firstCreatedPropertyCount > 4096000) {
               throw ProtocolException.arrayTooLong("FirstCreatedProperty", firstCreatedPropertyCount, 4096000);
            }

            if (varPos3 + varIntLenx + firstCreatedPropertyCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("FirstCreatedProperty", varPos3 + varIntLenx + firstCreatedPropertyCount * 1, buf.readableBytes());
            }

            obj.firstCreatedProperty = new String[firstCreatedPropertyCount];
            int elemPos = varPos3 + varIntLenx;

            for (int i = 0; i < firstCreatedPropertyCount; i++) {
               int strLenx = VarInt.peek(buf, elemPos);
               if (strLenx < 0) {
                  throw ProtocolException.invalidVarInt("firstCreatedProperty[" + i + "]");
               }

               int strVarLenx = VarInt.size(strLenx);
               if (strLenx > 4096000) {
                  throw ProtocolException.stringTooLong("firstCreatedProperty[" + i + "]", strLenx, 4096000);
               }

               if (elemPos + strVarLenx + strLenx > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("firstCreatedProperty[" + i + "]", elemPos + strVarLenx + strLenx, buf.readableBytes());
               }

               obj.firstCreatedProperty[i] = PacketIO.readVarString(buf, elemPos);
               elemPos += strVarLenx + strLenx;
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 23;
      if ((nullBits & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 7);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 23) {
            throw ProtocolException.invalidOffset("Path", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 23 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.size(sl) + sl;
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 11);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 23) {
            throw ProtocolException.invalidOffset("Value", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 23 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 15);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 23) {
            throw ProtocolException.invalidOffset("PreviousValue", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 23 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 19);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 23) {
            throw ProtocolException.invalidOffset("FirstCreatedProperty", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 23 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos3);
            pos3 += VarInt.size(sl) + sl;
         }

         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 23L;
   }

   public static JsonUpdateType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static JsonUpdateType getType(MemorySegment mem, int offset) {
      return JsonUpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   @Nullable
   public static String[] getPath(MemorySegment mem) {
      return getPath(mem, 0);
   }

   @Nullable
   public static String[] getPath(MemorySegment mem, int offset) {
      if (!hasPath(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 7, 23, "Path");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Path", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Path", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Path", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("Path", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static String getValue(MemorySegment mem) {
      return getValue(mem, 0);
   }

   @Nullable
   public static String getValue(MemorySegment mem, int offset) {
      return hasValue(mem, offset)
         ? PacketIO.readVarString("Value", mem, offset + getValidatedOffset(mem, offset, 11, 23, "Value"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getPreviousValue(MemorySegment mem) {
      return getPreviousValue(mem, 0);
   }

   @Nullable
   public static String getPreviousValue(MemorySegment mem, int offset) {
      return hasPreviousValue(mem, offset)
         ? PacketIO.readVarString("PreviousValue", mem, offset + getValidatedOffset(mem, offset, 15, 23, "PreviousValue"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String[] getFirstCreatedProperty(MemorySegment mem) {
      return getFirstCreatedProperty(mem, 0);
   }

   @Nullable
   public static String[] getFirstCreatedProperty(MemorySegment mem, int offset) {
      if (!hasFirstCreatedProperty(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 19, 23, "FirstCreatedProperty");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("FirstCreatedProperty", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("FirstCreatedProperty", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("FirstCreatedProperty", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("FirstCreatedProperty", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static AssetEditorRebuildCaches getRebuildCaches(MemorySegment mem) {
      return getRebuildCaches(mem, 0);
   }

   @Nullable
   public static AssetEditorRebuildCaches getRebuildCaches(MemorySegment mem, int offset) {
      return hasRebuildCaches(mem, offset) ? AssetEditorRebuildCaches.toObject(mem, offset + 2) : null;
   }

   public static boolean hasRebuildCaches(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasPath(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasValue(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasPreviousValue(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasFirstCreatedProperty(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static JsonUpdateCommand toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static JsonUpdateCommand toObject(MemorySegment mem, int offset) {
      if (offset + 23 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("JsonUpdateCommand", offset + 23, (int)mem.byteSize());
      } else {
         String[] path = null;
         if (hasPath(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 7, 23, "Path");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Path", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Path", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Path", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            path = new String[len];

            for (int i = 0; i < len; i++) {
               long sp = VarInt.getWithLength(mem, off);
               int n = (int)sp + (int)(sp >>> 32);
               path[i] = PacketIO.readVarString("Path", mem, off, 16384000, PacketIO.UTF8);
               off += n;
            }
         }

         String[] firstCreatedProperty = null;
         if (hasFirstCreatedProperty(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 19, 23, "FirstCreatedProperty");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("FirstCreatedProperty", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("FirstCreatedProperty", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("FirstCreatedProperty", offx + lenOffset + lenx, (int)mem.byteSize());
            }

            offx += lenOffset;
            firstCreatedProperty = new String[lenx];

            for (int i = 0; i < lenx; i++) {
               long sp = VarInt.getWithLength(mem, offx);
               int n = (int)sp + (int)(sp >>> 32);
               firstCreatedProperty[i] = PacketIO.readVarString("FirstCreatedProperty", mem, offx, 16384000, PacketIO.UTF8);
               offx += n;
            }
         }

         return new JsonUpdateCommand(
            JsonUpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            path,
            hasValue(mem, offset)
               ? PacketIO.readVarString("Value", mem, offset + getValidatedOffset(mem, offset, 11, 23, "Value"), 4096000, PacketIO.UTF8)
               : null,
            hasPreviousValue(mem, offset)
               ? PacketIO.readVarString("PreviousValue", mem, offset + getValidatedOffset(mem, offset, 15, 23, "PreviousValue"), 4096000, PacketIO.UTF8)
               : null,
            firstCreatedProperty,
            hasRebuildCaches(mem, offset) ? AssetEditorRebuildCaches.toObject(mem, offset + 2) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.rebuildCaches != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.path != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.value != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.previousValue != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.firstCreatedProperty != null) {
         nullBits = (byte)(nullBits | 16);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      if (this.rebuildCaches != null) {
         this.rebuildCaches.serialize(buf);
      } else {
         buf.writeZero(5);
      }

      int pathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int valueOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int previousValueOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int firstCreatedPropertyOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.path != null) {
         buf.setIntLE(pathOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.path.length > 4096000) {
            throw ProtocolException.arrayTooLong("Path", this.path.length, 4096000);
         }

         VarInt.write(buf, this.path.length);

         for (String item : this.path) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(pathOffsetSlot, -1);
      }

      if (this.value != null) {
         buf.setIntLE(valueOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.value, 4096000);
      } else {
         buf.setIntLE(valueOffsetSlot, -1);
      }

      if (this.previousValue != null) {
         buf.setIntLE(previousValueOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.previousValue, 4096000);
      } else {
         buf.setIntLE(previousValueOffsetSlot, -1);
      }

      if (this.firstCreatedProperty != null) {
         buf.setIntLE(firstCreatedPropertyOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.firstCreatedProperty.length > 4096000) {
            throw ProtocolException.arrayTooLong("FirstCreatedProperty", this.firstCreatedProperty.length, 4096000);
         }

         VarInt.write(buf, this.firstCreatedProperty.length);

         for (String item : this.firstCreatedProperty) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(firstCreatedPropertyOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.rebuildCaches != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.path != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.value != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.previousValue != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.firstCreatedProperty != null) {
         nullBits = (byte)(nullBits | 16);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      if (this.rebuildCaches != null) {
         this.rebuildCaches.serialize(mem, offset + 2);
      } else {
         mem.asSlice(offset + 2, 5L).fill((byte)0);
      }

      int varOffset = offset + 23;
      if (this.path != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 7), varOffset - offset - 23);
         if (this.path.length > 4096000) {
            throw ProtocolException.arrayTooLong("Path", this.path.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.path.length);
         int pathValueOffset = 0;

         for (int i = 0; i < this.path.length; i++) {
            pathValueOffset += PacketIO.writeVarString(mem, varOffset + pathValueOffset, this.path[i], 16384000);
         }

         varOffset += pathValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 7), -1);
      }

      if (this.value != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 11), varOffset - offset - 23);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.value, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 11), -1);
      }

      if (this.previousValue != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 15), varOffset - offset - 23);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.previousValue, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 15), -1);
      }

      if (this.firstCreatedProperty != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 19), varOffset - offset - 23);
         if (this.firstCreatedProperty.length > 4096000) {
            throw ProtocolException.arrayTooLong("FirstCreatedProperty", this.firstCreatedProperty.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.firstCreatedProperty.length);
         int firstCreatedPropertyValueOffset = 0;

         for (int i = 0; i < this.firstCreatedProperty.length; i++) {
            firstCreatedPropertyValueOffset += PacketIO.writeVarString(mem, varOffset + firstCreatedPropertyValueOffset, this.firstCreatedProperty[i], 16384000);
         }

         varOffset += firstCreatedPropertyValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 19), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 23;
      if (this.path != null) {
         int pathSize = 0;

         for (String elem : this.path) {
            pathSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.path.length) + pathSize;
      }

      if (this.value != null) {
         size += PacketIO.stringSize(this.value);
      }

      if (this.previousValue != null) {
         size += PacketIO.stringSize(this.previousValue);
      }

      if (this.firstCreatedProperty != null) {
         int firstCreatedPropertySize = 0;

         for (String elem : this.firstCreatedProperty) {
            firstCreatedPropertySize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.firstCreatedProperty.length) + firstCreatedPropertySize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 23) {
         return ValidationResult.error("Buffer too small: expected at least 23 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid JsonUpdateType value for Type");
         } else {
            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 7);
               if (v < 0 || v > buffer.writerIndex() - offset - 23) {
                  return ValidationResult.error("Invalid offset for Path");
               }

               int pos = offset + 23 + v;
               int pathCount = VarInt.peek(buffer, pos);
               if (pathCount < 0) {
                  return ValidationResult.error("Invalid array count for Path");
               }

               if (pathCount > 4096000) {
                  return ValidationResult.error("Path exceeds max length 4096000");
               }

               pos += VarInt.size(pathCount);

               for (int i = 0; i < pathCount; i++) {
                  int strLen = VarInt.peek(buffer, pos);
                  if (strLen < 0) {
                     return ValidationResult.error("Invalid string length in Path");
                  }

                  pos += VarInt.size(strLen);
                  pos += strLen;
                  if (pos > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading string in Path");
                  }
               }
            }

            if ((nullBits & 4) != 0) {
               v = buffer.getIntLE(offset + 11);
               if (v < 0 || v > buffer.writerIndex() - offset - 23) {
                  return ValidationResult.error("Invalid offset for Value");
               }

               int posx = offset + 23 + v;
               int valueLen = VarInt.peek(buffer, posx);
               if (valueLen < 0) {
                  return ValidationResult.error("Invalid string length for Value");
               }

               if (valueLen > 4096000) {
                  return ValidationResult.error("Value exceeds max length 4096000");
               }

               posx += VarInt.size(valueLen);
               posx += valueLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Value");
               }
            }

            if ((nullBits & 8) != 0) {
               v = buffer.getIntLE(offset + 15);
               if (v < 0 || v > buffer.writerIndex() - offset - 23) {
                  return ValidationResult.error("Invalid offset for PreviousValue");
               }

               int posxx = offset + 23 + v;
               int previousValueLen = VarInt.peek(buffer, posxx);
               if (previousValueLen < 0) {
                  return ValidationResult.error("Invalid string length for PreviousValue");
               }

               if (previousValueLen > 4096000) {
                  return ValidationResult.error("PreviousValue exceeds max length 4096000");
               }

               posxx += VarInt.size(previousValueLen);
               posxx += previousValueLen;
               if (posxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading PreviousValue");
               }
            }

            if ((nullBits & 16) != 0) {
               v = buffer.getIntLE(offset + 19);
               if (v < 0 || v > buffer.writerIndex() - offset - 23) {
                  return ValidationResult.error("Invalid offset for FirstCreatedProperty");
               }

               int posxxx = offset + 23 + v;
               int firstCreatedPropertyCount = VarInt.peek(buffer, posxxx);
               if (firstCreatedPropertyCount < 0) {
                  return ValidationResult.error("Invalid array count for FirstCreatedProperty");
               }

               if (firstCreatedPropertyCount > 4096000) {
                  return ValidationResult.error("FirstCreatedProperty exceeds max length 4096000");
               }

               posxxx += VarInt.size(firstCreatedPropertyCount);

               for (int i = 0; i < firstCreatedPropertyCount; i++) {
                  int strLenx = VarInt.peek(buffer, posxxx);
                  if (strLenx < 0) {
                     return ValidationResult.error("Invalid string length in FirstCreatedProperty");
                  }

                  posxxx += VarInt.size(strLenx);
                  posxxx += strLenx;
                  if (posxxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading string in FirstCreatedProperty");
                  }
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public JsonUpdateCommand clone() {
      JsonUpdateCommand copy = new JsonUpdateCommand();
      copy.type = this.type;
      copy.path = this.path != null ? Arrays.copyOf(this.path, this.path.length) : null;
      copy.value = this.value;
      copy.previousValue = this.previousValue;
      copy.firstCreatedProperty = this.firstCreatedProperty != null ? Arrays.copyOf(this.firstCreatedProperty, this.firstCreatedProperty.length) : null;
      copy.rebuildCaches = this.rebuildCaches != null ? this.rebuildCaches.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof JsonUpdateCommand other)
            ? false
            : Objects.equals(this.type, other.type)
               && Arrays.equals((Object[])this.path, (Object[])other.path)
               && Objects.equals(this.value, other.value)
               && Objects.equals(this.previousValue, other.previousValue)
               && Arrays.equals((Object[])this.firstCreatedProperty, (Object[])other.firstCreatedProperty)
               && Objects.equals(this.rebuildCaches, other.rebuildCaches);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Arrays.hashCode((Object[])this.path);
      result = 31 * result + Objects.hashCode(this.value);
      result = 31 * result + Objects.hashCode(this.previousValue);
      result = 31 * result + Arrays.hashCode((Object[])this.firstCreatedProperty);
      return 31 * result + Objects.hashCode(this.rebuildCaches);
   }
}
