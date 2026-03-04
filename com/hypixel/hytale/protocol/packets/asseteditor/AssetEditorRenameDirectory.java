package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorRenameDirectory implements Packet, ToServerPacket {
   public static final int PACKET_ID = 309;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 65536051;
   public int token;
   @Nullable
   public AssetPath path;
   @Nullable
   public AssetPath newPath;

   @Override
   public int getId() {
      return 309;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorRenameDirectory() {
   }

   public AssetEditorRenameDirectory(int token, @Nullable AssetPath path, @Nullable AssetPath newPath) {
      this.token = token;
      this.path = path;
      this.newPath = newPath;
   }

   public AssetEditorRenameDirectory(@Nonnull AssetEditorRenameDirectory other) {
      this.token = other.token;
      this.path = other.path;
      this.newPath = other.newPath;
   }

   @Nonnull
   public static AssetEditorRenameDirectory deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorRenameDirectory obj = new AssetEditorRenameDirectory();
      byte nullBits = buf.getByte(offset);
      obj.token = buf.getIntLE(offset + 1);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 13 + buf.getIntLE(offset + 5);
         obj.path = AssetPath.deserialize(buf, varPos0);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 13 + buf.getIntLE(offset + 9);
         obj.newPath = AssetPath.deserialize(buf, varPos1);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 5);
         int pos0 = offset + 13 + fieldOffset0;
         pos0 += AssetPath.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 9);
         int pos1 = offset + 13 + fieldOffset1;
         pos1 += AssetPath.computeBytesConsumed(buf, pos1);
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
      if (this.path != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.newPath != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.token);
      int pathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int newPathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.path != null) {
         buf.setIntLE(pathOffsetSlot, buf.writerIndex() - varBlockStart);
         this.path.serialize(buf);
      } else {
         buf.setIntLE(pathOffsetSlot, -1);
      }

      if (this.newPath != null) {
         buf.setIntLE(newPathOffsetSlot, buf.writerIndex() - varBlockStart);
         this.newPath.serialize(buf);
      } else {
         buf.setIntLE(newPathOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 13;
      if (this.path != null) {
         size += this.path.computeSize();
      }

      if (this.newPath != null) {
         size += this.newPath.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int pathOffset = buffer.getIntLE(offset + 5);
            if (pathOffset < 0) {
               return ValidationResult.error("Invalid offset for Path");
            }

            int pos = offset + 13 + pathOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Path");
            }

            ValidationResult pathResult = AssetPath.validateStructure(buffer, pos);
            if (!pathResult.isValid()) {
               return ValidationResult.error("Invalid Path: " + pathResult.error());
            }

            pos += AssetPath.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int newPathOffset = buffer.getIntLE(offset + 9);
            if (newPathOffset < 0) {
               return ValidationResult.error("Invalid offset for NewPath");
            }

            int posx = offset + 13 + newPathOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for NewPath");
            }

            ValidationResult newPathResult = AssetPath.validateStructure(buffer, posx);
            if (!newPathResult.isValid()) {
               return ValidationResult.error("Invalid NewPath: " + newPathResult.error());
            }

            posx += AssetPath.computeBytesConsumed(buffer, posx);
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorRenameDirectory clone() {
      AssetEditorRenameDirectory copy = new AssetEditorRenameDirectory();
      copy.token = this.token;
      copy.path = this.path != null ? this.path.clone() : null;
      copy.newPath = this.newPath != null ? this.newPath.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorRenameDirectory other)
            ? false
            : this.token == other.token && Objects.equals(this.path, other.path) && Objects.equals(this.newPath, other.newPath);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.token, this.path, this.newPath);
   }
}
