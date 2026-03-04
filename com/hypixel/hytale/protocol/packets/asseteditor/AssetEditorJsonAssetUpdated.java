package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorJsonAssetUpdated implements Packet, ToClientPacket {
   public static final int PACKET_ID = 325;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public AssetPath path;
   @Nullable
   public JsonUpdateCommand[] commands;

   @Override
   public int getId() {
      return 325;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorJsonAssetUpdated() {
   }

   public AssetEditorJsonAssetUpdated(@Nullable AssetPath path, @Nullable JsonUpdateCommand[] commands) {
      this.path = path;
      this.commands = commands;
   }

   public AssetEditorJsonAssetUpdated(@Nonnull AssetEditorJsonAssetUpdated other) {
      this.path = other.path;
      this.commands = other.commands;
   }

   @Nonnull
   public static AssetEditorJsonAssetUpdated deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorJsonAssetUpdated obj = new AssetEditorJsonAssetUpdated();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         obj.path = AssetPath.deserialize(buf, varPos0);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         int commandsCount = VarInt.peek(buf, varPos1);
         if (commandsCount < 0) {
            throw ProtocolException.negativeLength("Commands", commandsCount);
         }

         if (commandsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Commands", commandsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + commandsCount * 7L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Commands", varPos1 + varIntLen + commandsCount * 7, buf.readableBytes());
         }

         obj.commands = new JsonUpdateCommand[commandsCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < commandsCount; i++) {
            obj.commands[i] = JsonUpdateCommand.deserialize(buf, elemPos);
            elemPos += JsonUpdateCommand.computeBytesConsumed(buf, elemPos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 9 + fieldOffset0;
         pos0 += AssetPath.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 9 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            pos1 += JsonUpdateCommand.computeBytesConsumed(buf, pos1);
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
      if (this.path != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.commands != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int pathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int commandsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
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
   public int computeSize() {
      int size = 9;
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
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int pathOffset = buffer.getIntLE(offset + 1);
            if (pathOffset < 0) {
               return ValidationResult.error("Invalid offset for Path");
            }

            int pos = offset + 9 + pathOffset;
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
            int commandsOffset = buffer.getIntLE(offset + 5);
            if (commandsOffset < 0) {
               return ValidationResult.error("Invalid offset for Commands");
            }

            int posx = offset + 9 + commandsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Commands");
            }

            int commandsCount = VarInt.peek(buffer, posx);
            if (commandsCount < 0) {
               return ValidationResult.error("Invalid array count for Commands");
            }

            if (commandsCount > 4096000) {
               return ValidationResult.error("Commands exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

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

   public AssetEditorJsonAssetUpdated clone() {
      AssetEditorJsonAssetUpdated copy = new AssetEditorJsonAssetUpdated();
      copy.path = this.path != null ? this.path.clone() : null;
      copy.commands = this.commands != null ? Arrays.stream(this.commands).map(e -> e.clone()).toArray(JsonUpdateCommand[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorJsonAssetUpdated other)
            ? false
            : Objects.equals(this.path, other.path) && Arrays.equals((Object[])this.commands, (Object[])other.commands);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.path);
      return 31 * result + Arrays.hashCode((Object[])this.commands);
   }
}
