package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("AssetEditorUndoRedoReply", 5, buf.readableBytes() - offset);
      } else {
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
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         pos += JsonUpdateCommand.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   public static int getToken(MemorySegment mem) {
      return getToken(mem, 0);
   }

   public static int getToken(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static JsonUpdateCommand getCommand(MemorySegment mem) {
      return getCommand(mem, 0);
   }

   @Nullable
   public static JsonUpdateCommand getCommand(MemorySegment mem, int offset) {
      return hasCommand(mem, offset) ? JsonUpdateCommand.toObject(mem, offset + 5) : null;
   }

   public static boolean hasCommand(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static AssetEditorUndoRedoReply toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorUndoRedoReply toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorUndoRedoReply", offset + 5, (int)mem.byteSize());
      } else {
         return new AssetEditorUndoRedoReply(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)), hasCommand(mem, offset) ? JsonUpdateCommand.toObject(mem, offset + 5) : null
         );
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.command != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.token);
      int varOffset = offset + 5;
      if (this.command != null) {
         varOffset += this.command.serialize(mem, varOffset);
      }

      return varOffset - offset;
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
