package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorAssetListSetup implements Packet, ToClientPacket {
   public static final int PACKET_ID = 319;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String pack;
   public boolean isReadOnly;
   public boolean canBeDeleted;
   @Nonnull
   public AssetEditorFileTree tree = AssetEditorFileTree.Server;
   @Nullable
   public AssetEditorFileEntry[] paths;

   @Override
   public int getId() {
      return 319;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorAssetListSetup() {
   }

   public AssetEditorAssetListSetup(
      @Nullable String pack, boolean isReadOnly, boolean canBeDeleted, @Nonnull AssetEditorFileTree tree, @Nullable AssetEditorFileEntry[] paths
   ) {
      this.pack = pack;
      this.isReadOnly = isReadOnly;
      this.canBeDeleted = canBeDeleted;
      this.tree = tree;
      this.paths = paths;
   }

   public AssetEditorAssetListSetup(@Nonnull AssetEditorAssetListSetup other) {
      this.pack = other.pack;
      this.isReadOnly = other.isReadOnly;
      this.canBeDeleted = other.canBeDeleted;
      this.tree = other.tree;
      this.paths = other.paths;
   }

   @Nonnull
   public static AssetEditorAssetListSetup deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorAssetListSetup obj = new AssetEditorAssetListSetup();
      byte nullBits = buf.getByte(offset);
      obj.isReadOnly = buf.getByte(offset + 1) != 0;
      obj.canBeDeleted = buf.getByte(offset + 2) != 0;
      obj.tree = AssetEditorFileTree.fromValue(buf.getByte(offset + 3));
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 12 + buf.getIntLE(offset + 4);
         int packLen = VarInt.peek(buf, varPos0);
         if (packLen < 0) {
            throw ProtocolException.negativeLength("Pack", packLen);
         }

         if (packLen > 4096000) {
            throw ProtocolException.stringTooLong("Pack", packLen, 4096000);
         }

         obj.pack = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 12 + buf.getIntLE(offset + 8);
         int pathsCount = VarInt.peek(buf, varPos1);
         if (pathsCount < 0) {
            throw ProtocolException.negativeLength("Paths", pathsCount);
         }

         if (pathsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Paths", pathsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + pathsCount * 2L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Paths", varPos1 + varIntLen + pathsCount * 2, buf.readableBytes());
         }

         obj.paths = new AssetEditorFileEntry[pathsCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < pathsCount; i++) {
            obj.paths[i] = AssetEditorFileEntry.deserialize(buf, elemPos);
            elemPos += AssetEditorFileEntry.computeBytesConsumed(buf, elemPos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 12;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 4);
         int pos0 = offset + 12 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 8);
         int pos1 = offset + 12 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            pos1 += AssetEditorFileEntry.computeBytesConsumed(buf, pos1);
         }

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
      if (this.pack != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.paths != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.isReadOnly ? 1 : 0);
      buf.writeByte(this.canBeDeleted ? 1 : 0);
      buf.writeByte(this.tree.getValue());
      int packOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int pathsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.pack != null) {
         buf.setIntLE(packOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.pack, 4096000);
      } else {
         buf.setIntLE(packOffsetSlot, -1);
      }

      if (this.paths != null) {
         buf.setIntLE(pathsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.paths.length > 4096000) {
            throw ProtocolException.arrayTooLong("Paths", this.paths.length, 4096000);
         }

         VarInt.write(buf, this.paths.length);

         for (AssetEditorFileEntry item : this.paths) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(pathsOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 12;
      if (this.pack != null) {
         size += PacketIO.stringSize(this.pack);
      }

      if (this.paths != null) {
         int pathsSize = 0;

         for (AssetEditorFileEntry elem : this.paths) {
            pathsSize += elem.computeSize();
         }

         size += VarInt.size(this.paths.length) + pathsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 12) {
         return ValidationResult.error("Buffer too small: expected at least 12 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int packOffset = buffer.getIntLE(offset + 4);
            if (packOffset < 0) {
               return ValidationResult.error("Invalid offset for Pack");
            }

            int pos = offset + 12 + packOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Pack");
            }

            int packLen = VarInt.peek(buffer, pos);
            if (packLen < 0) {
               return ValidationResult.error("Invalid string length for Pack");
            }

            if (packLen > 4096000) {
               return ValidationResult.error("Pack exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += packLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Pack");
            }
         }

         if ((nullBits & 2) != 0) {
            int pathsOffset = buffer.getIntLE(offset + 8);
            if (pathsOffset < 0) {
               return ValidationResult.error("Invalid offset for Paths");
            }

            int posx = offset + 12 + pathsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Paths");
            }

            int pathsCount = VarInt.peek(buffer, posx);
            if (pathsCount < 0) {
               return ValidationResult.error("Invalid array count for Paths");
            }

            if (pathsCount > 4096000) {
               return ValidationResult.error("Paths exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < pathsCount; i++) {
               ValidationResult structResult = AssetEditorFileEntry.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid AssetEditorFileEntry in Paths[" + i + "]: " + structResult.error());
               }

               posx += AssetEditorFileEntry.computeBytesConsumed(buffer, posx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorAssetListSetup clone() {
      AssetEditorAssetListSetup copy = new AssetEditorAssetListSetup();
      copy.pack = this.pack;
      copy.isReadOnly = this.isReadOnly;
      copy.canBeDeleted = this.canBeDeleted;
      copy.tree = this.tree;
      copy.paths = this.paths != null ? Arrays.stream(this.paths).map(e -> e.clone()).toArray(AssetEditorFileEntry[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorAssetListSetup other)
            ? false
            : Objects.equals(this.pack, other.pack)
               && this.isReadOnly == other.isReadOnly
               && this.canBeDeleted == other.canBeDeleted
               && Objects.equals(this.tree, other.tree)
               && Arrays.equals((Object[])this.paths, (Object[])other.paths);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.pack);
      result = 31 * result + Boolean.hashCode(this.isReadOnly);
      result = 31 * result + Boolean.hashCode(this.canBeDeleted);
      result = 31 * result + Objects.hashCode(this.tree);
      return 31 * result + Arrays.hashCode((Object[])this.paths);
   }
}
