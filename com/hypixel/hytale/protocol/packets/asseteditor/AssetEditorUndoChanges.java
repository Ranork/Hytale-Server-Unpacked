package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorUndoChanges implements Packet, ToServerPacket {
   public static final int PACKET_ID = 349;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 32768024;
   public int token;
   @Nullable
   public AssetPath path;

   @Override
   public int getId() {
      return 349;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorUndoChanges() {
   }

   public AssetEditorUndoChanges(int token, @Nullable AssetPath path) {
      this.token = token;
      this.path = path;
   }

   public AssetEditorUndoChanges(@Nonnull AssetEditorUndoChanges other) {
      this.token = other.token;
      this.path = other.path;
   }

   @Nonnull
   public static AssetEditorUndoChanges deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorUndoChanges obj = new AssetEditorUndoChanges();
      byte nullBits = buf.getByte(offset);
      obj.token = buf.getIntLE(offset + 1);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         obj.path = AssetPath.deserialize(buf, pos);
         pos += AssetPath.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         pos += AssetPath.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.path != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.token);
      if (this.path != null) {
         this.path.serialize(buf);
      }
   }

   @Override
   public int computeSize() {
      int size = 5;
      if (this.path != null) {
         size += this.path.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            ValidationResult pathResult = AssetPath.validateStructure(buffer, pos);
            if (!pathResult.isValid()) {
               return ValidationResult.error("Invalid Path: " + pathResult.error());
            }

            pos += AssetPath.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorUndoChanges clone() {
      AssetEditorUndoChanges copy = new AssetEditorUndoChanges();
      copy.token = this.token;
      copy.path = this.path != null ? this.path.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorUndoChanges other) ? false : this.token == other.token && Objects.equals(this.path, other.path);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.token, this.path);
   }
}
