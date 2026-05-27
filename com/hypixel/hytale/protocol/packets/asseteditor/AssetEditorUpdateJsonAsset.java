package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
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

public class AssetEditorUpdateJsonAsset implements Packet, ToServerPacket {
   public static final int PACKET_ID = 323;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 21;
   public static final int MAX_SIZE = 1677721600;
   public int token;
   @Nullable
   public String assetType;
   @Nullable
   public AssetPath path;
   public int assetIndex = Integer.MIN_VALUE;
   @Nullable
   public JsonUpdateCommand[] commands;

   @Override
   public int getId() {
      return 323;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorUpdateJsonAsset() {
   }

   public AssetEditorUpdateJsonAsset(int token, @Nullable String assetType, @Nullable AssetPath path, int assetIndex, @Nullable JsonUpdateCommand[] commands) {
      this.token = token;
      this.assetType = assetType;
      this.path = path;
      this.assetIndex = assetIndex;
      this.commands = commands;
   }

   public AssetEditorUpdateJsonAsset(@Nonnull AssetEditorUpdateJsonAsset other) {
      this.token = other.token;
      this.assetType = other.assetType;
      this.path = other.path;
      this.assetIndex = other.assetIndex;
      this.commands = other.commands;
   }

   @Nonnull
   public static AssetEditorUpdateJsonAsset deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 21) {
         throw ProtocolException.bufferTooSmall("AssetEditorUpdateJsonAsset", 21, buf.readableBytes() - offset);
      } else {
         AssetEditorUpdateJsonAsset obj = new AssetEditorUpdateJsonAsset();
         byte nullBits = buf.getByte(offset);
         obj.token = buf.getIntLE(offset + 1);
         obj.assetIndex = buf.getIntLE(offset + 5);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 9);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("AssetType", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 21 + varPosBase0;
            int assetTypeLen = VarInt.peek(buf, varPos0);
            if (assetTypeLen < 0) {
               throw ProtocolException.invalidVarInt("AssetType");
            }

            int assetTypeVarIntLen = VarInt.size(assetTypeLen);
            if (assetTypeLen > 4096000) {
               throw ProtocolException.stringTooLong("AssetType", assetTypeLen, 4096000);
            }

            if (varPos0 + assetTypeVarIntLen + assetTypeLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("AssetType", varPos0 + assetTypeVarIntLen + assetTypeLen, buf.readableBytes());
            }

            obj.assetType = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 13);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("Path", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 21 + varPosBase1;
            obj.path = AssetPath.deserialize(buf, varPos1);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 17);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("Commands", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 21 + varPosBase2;
            int commandsCount = VarInt.peek(buf, varPos2);
            if (commandsCount < 0) {
               throw ProtocolException.invalidVarInt("Commands");
            }

            int varIntLen = VarInt.size(commandsCount);
            if (commandsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Commands", commandsCount, 4096000);
            }

            if (varPos2 + varIntLen + commandsCount * 7L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Commands", varPos2 + varIntLen + commandsCount * 7, buf.readableBytes());
            }

            obj.commands = new JsonUpdateCommand[commandsCount];
            int elemPos = varPos2 + varIntLen;

            for (int i = 0; i < commandsCount; i++) {
               obj.commands[i] = JsonUpdateCommand.deserialize(buf, elemPos);
               elemPos += JsonUpdateCommand.computeBytesConsumed(buf, elemPos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 21;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 9);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("AssetType", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 21 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 13);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("Path", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 21 + fieldOffset1;
         pos1 += AssetPath.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 17);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("Commands", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 21 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos2 += JsonUpdateCommand.computeBytesConsumed(buf, pos2);
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 21L;
   }

   public static int getToken(MemorySegment mem) {
      return getToken(mem, 0);
   }

   public static int getToken(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static String getAssetType(MemorySegment mem) {
      return getAssetType(mem, 0);
   }

   @Nullable
   public static String getAssetType(MemorySegment mem, int offset) {
      return hasAssetType(mem, offset)
         ? PacketIO.readVarString("AssetType", mem, offset + getValidatedOffset(mem, offset, 9, 21, "AssetType"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static AssetPath getPath(MemorySegment mem) {
      return getPath(mem, 0);
   }

   @Nullable
   public static AssetPath getPath(MemorySegment mem, int offset) {
      return hasPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 13, 21, "Path")) : null;
   }

   public static int getAssetIndex(MemorySegment mem) {
      return getAssetIndex(mem, 0);
   }

   public static int getAssetIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   @Nullable
   public static JsonUpdateCommand[] getCommands(MemorySegment mem) {
      return getCommands(mem, 0);
   }

   @Nullable
   public static JsonUpdateCommand[] getCommands(MemorySegment mem, int offset) {
      if (!hasCommands(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 17, 21, "Commands");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Commands", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Commands", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Commands", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               JsonUpdateCommand[] data = new JsonUpdateCommand[len];

               for (int i = 0; i < len; i++) {
                  data[i] = JsonUpdateCommand.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasAssetType(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasPath(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasCommands(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static AssetEditorUpdateJsonAsset toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorUpdateJsonAsset toObject(MemorySegment mem, int offset) {
      if (offset + 21 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorUpdateJsonAsset", offset + 21, (int)mem.byteSize());
      } else {
         JsonUpdateCommand[] commands = null;
         if (hasCommands(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 17, 21, "Commands");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Commands", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Commands", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Commands", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            commands = new JsonUpdateCommand[len];

            for (int i = 0; i < len; i++) {
               commands[i] = JsonUpdateCommand.toObject(mem, off);
               off += commands[i].computeSize();
            }
         }

         return new AssetEditorUpdateJsonAsset(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            hasAssetType(mem, offset)
               ? PacketIO.readVarString("AssetType", mem, offset + getValidatedOffset(mem, offset, 9, 21, "AssetType"), 4096000, PacketIO.UTF8)
               : null,
            hasPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 13, 21, "Path")) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5)),
            commands
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.assetType != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.path != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.commands != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.token);
      buf.writeIntLE(this.assetIndex);
      int assetTypeOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int pathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int commandsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.assetType != null) {
         buf.setIntLE(assetTypeOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.assetType, 4096000);
      } else {
         buf.setIntLE(assetTypeOffsetSlot, -1);
      }

      if (this.path != null) {
         buf.setIntLE(pathOffsetSlot, buf.writerIndex() - varBlockStart);
         this.path.serialize(buf);
      } else {
         buf.setIntLE(pathOffsetSlot, -1);
      }

      if (this.commands != null) {
         buf.setIntLE(commandsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.commands.length > 4096000) {
            throw ProtocolException.arrayTooLong("Commands", this.commands.length, 4096000);
         }

         VarInt.write(buf, this.commands.length);

         for (JsonUpdateCommand item : this.commands) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(commandsOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.assetType != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.path != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.commands != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.token);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.assetIndex);
      int varOffset = offset + 21;
      if (this.assetType != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 21);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.assetType, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      if (this.path != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), varOffset - offset - 21);
         varOffset += this.path.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), -1);
      }

      if (this.commands != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), varOffset - offset - 21);
         if (this.commands.length > 4096000) {
            throw ProtocolException.arrayTooLong("Commands", this.commands.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.commands.length);
         int commandsValueOffset = 0;

         for (int i = 0; i < this.commands.length; i++) {
            commandsValueOffset += this.commands[i].serialize(mem, varOffset + commandsValueOffset);
         }

         varOffset += commandsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 21;
      if (this.assetType != null) {
         size += PacketIO.stringSize(this.assetType);
      }

      if (this.path != null) {
         size += this.path.computeSize();
      }

      if (this.commands != null) {
         int commandsSize = 0;

         for (JsonUpdateCommand elem : this.commands) {
            commandsSize += elem.computeSize();
         }

         size += VarInt.size(this.commands.length) + commandsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 21) {
         return ValidationResult.error("Buffer too small: expected at least 21 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int assetTypeOffset = buffer.getIntLE(offset + 9);
            if (assetTypeOffset < 0 || assetTypeOffset > buffer.writerIndex() - offset - 21) {
               return ValidationResult.error("Invalid offset for AssetType");
            }

            int pos = offset + 21 + assetTypeOffset;
            int assetTypeLen = VarInt.peek(buffer, pos);
            if (assetTypeLen < 0) {
               return ValidationResult.error("Invalid string length for AssetType");
            }

            if (assetTypeLen > 4096000) {
               return ValidationResult.error("AssetType exceeds max length 4096000");
            }

            pos += VarInt.size(assetTypeLen);
            pos += assetTypeLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading AssetType");
            }
         }

         if ((nullBits & 2) != 0) {
            int pathOffset = buffer.getIntLE(offset + 13);
            if (pathOffset < 0 || pathOffset > buffer.writerIndex() - offset - 21) {
               return ValidationResult.error("Invalid offset for Path");
            }

            int posx = offset + 21 + pathOffset;
            ValidationResult pathResult = AssetPath.validateStructure(buffer, posx);
            if (!pathResult.isValid()) {
               return ValidationResult.error("Invalid Path: " + pathResult.error());
            }

            posx += AssetPath.computeBytesConsumed(buffer, posx);
         }

         if ((nullBits & 4) != 0) {
            int commandsOffset = buffer.getIntLE(offset + 17);
            if (commandsOffset < 0 || commandsOffset > buffer.writerIndex() - offset - 21) {
               return ValidationResult.error("Invalid offset for Commands");
            }

            int posx = offset + 21 + commandsOffset;
            int commandsCount = VarInt.peek(buffer, posx);
            if (commandsCount < 0) {
               return ValidationResult.error("Invalid array count for Commands");
            }

            if (commandsCount > 4096000) {
               return ValidationResult.error("Commands exceeds max length 4096000");
            }

            posx += VarInt.size(commandsCount);

            for (int i = 0; i < commandsCount; i++) {
               ValidationResult structResult = JsonUpdateCommand.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid JsonUpdateCommand in Commands[" + i + "]: " + structResult.error());
               }

               posx += JsonUpdateCommand.computeBytesConsumed(buffer, posx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorUpdateJsonAsset clone() {
      AssetEditorUpdateJsonAsset copy = new AssetEditorUpdateJsonAsset();
      copy.token = this.token;
      copy.assetType = this.assetType;
      copy.path = this.path != null ? this.path.clone() : null;
      copy.assetIndex = this.assetIndex;
      copy.commands = this.commands != null ? Arrays.stream(this.commands).map(e -> e.clone()).toArray(JsonUpdateCommand[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorUpdateJsonAsset other)
            ? false
            : this.token == other.token
               && Objects.equals(this.assetType, other.assetType)
               && Objects.equals(this.path, other.path)
               && this.assetIndex == other.assetIndex
               && Arrays.equals((Object[])this.commands, (Object[])other.commands);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.token);
      result = 31 * result + Objects.hashCode(this.assetType);
      result = 31 * result + Objects.hashCode(this.path);
      result = 31 * result + Integer.hashCode(this.assetIndex);
      return 31 * result + Arrays.hashCode((Object[])this.commands);
   }
}
