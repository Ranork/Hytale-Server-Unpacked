package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorUndoRedoReply implements Packet, ToClientPacket {
   public static final int PACKET_ID = 351;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 1677721600;
   public int token;
   @Nullable
   public JsonUpdateCommand command;

   @Override
   public int getId() {
      return 351;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorUndoRedoReply() {
   }

   public AssetEditorUndoRedoReply(int token, @Nullable JsonUpdateCommand command) {
      this.token = token;
      this.command = command;
   }

   public AssetEditorUndoRedoReply(@Nonnull AssetEditorUndoRedoReply other) {
      this.token = other.token;
      this.command = other.command;
   }

   @Nonnull
   public static AssetEditorUndoRedoReply deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorUndoRedoReply obj = new AssetEditorUndoRedoReply();
      byte nullBits = buf.getByte(offset);
      obj.token = buf.getIntLE(offset + 1);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         obj.command = JsonUpdateCommand.deserialize(buf, pos);
         pos += JsonUpdateCommand.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         pos += JsonUpdateCommand.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.command != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.token);
      if (this.command != null) {
         this.command.serialize(buf);
      }
   }

   @Override
   public int computeSize() {
      int size = 5;
      if (this.command != null) {
         size += this.command.computeSize();
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
            ValidationResult commandResult = JsonUpdateCommand.validateStructure(buffer, pos);
            if (!commandResult.isValid()) {
               return ValidationResult.error("Invalid Command: " + commandResult.error());
            }

            pos += JsonUpdateCommand.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorUndoRedoReply clone() {
      AssetEditorUndoRedoReply copy = new AssetEditorUndoRedoReply();
      copy.token = this.token;
      copy.command = this.command != null ? this.command.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorUndoRedoReply other) ? false : this.token == other.token && Objects.equals(this.command, other.command);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.token, this.command);
   }
}
