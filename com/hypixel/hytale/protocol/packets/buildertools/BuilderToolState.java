package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderToolState {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 10;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   public boolean isBrush;
   @Nullable
   public BuilderToolArg[] args;

   public BuilderToolState() {
   }

   public BuilderToolState(@Nullable String id, boolean isBrush, @Nullable BuilderToolArg[] args) {
      this.id = id;
      this.isBrush = isBrush;
      this.args = args;
   }

   public BuilderToolState(@Nonnull BuilderToolState other) {
      this.id = other.id;
      this.isBrush = other.isBrush;
      this.args = other.args;
   }

   @Nonnull
   public static BuilderToolState deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolState obj = new BuilderToolState();
      byte nullBits = buf.getByte(offset);
      obj.isBrush = buf.getByte(offset + 1) != 0;
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 10 + buf.getIntLE(offset + 2);
         int idLen = VarInt.peek(buf, varPos0);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 10 + buf.getIntLE(offset + 6);
         int argsCount = VarInt.peek(buf, varPos1);
         if (argsCount < 0) {
            throw ProtocolException.negativeLength("Args", argsCount);
         }

         if (argsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Args", argsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + argsCount * 33L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Args", varPos1 + varIntLen + argsCount * 33, buf.readableBytes());
         }

         obj.args = new BuilderToolArg[argsCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < argsCount; i++) {
            obj.args[i] = BuilderToolArg.deserialize(buf, elemPos);
            elemPos += BuilderToolArg.computeBytesConsumed(buf, elemPos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 10;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 2);
         int pos0 = offset + 10 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 6);
         int pos1 = offset + 10 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            pos1 += BuilderToolArg.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.args != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.isBrush ? 1 : 0);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int argsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.args != null) {
         buf.setIntLE(argsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.args.length > 4096000) {
            throw ProtocolException.arrayTooLong("Args", this.args.length, 4096000);
         }

         VarInt.write(buf, this.args.length);

         for (BuilderToolArg item : this.args) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(argsOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 10;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.args != null) {
         int argsSize = 0;

         for (BuilderToolArg elem : this.args) {
            argsSize += elem.computeSize();
         }

         size += VarInt.size(this.args.length) + argsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 10) {
         return ValidationResult.error("Buffer too small: expected at least 10 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int idOffset = buffer.getIntLE(offset + 2);
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 10 + idOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Id");
            }

            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits & 2) != 0) {
            int argsOffset = buffer.getIntLE(offset + 6);
            if (argsOffset < 0) {
               return ValidationResult.error("Invalid offset for Args");
            }

            int posx = offset + 10 + argsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Args");
            }

            int argsCount = VarInt.peek(buffer, posx);
            if (argsCount < 0) {
               return ValidationResult.error("Invalid array count for Args");
            }

            if (argsCount > 4096000) {
               return ValidationResult.error("Args exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < argsCount; i++) {
               ValidationResult structResult = BuilderToolArg.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid BuilderToolArg in Args[" + i + "]: " + structResult.error());
               }

               posx += BuilderToolArg.computeBytesConsumed(buffer, posx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public BuilderToolState clone() {
      BuilderToolState copy = new BuilderToolState();
      copy.id = this.id;
      copy.isBrush = this.isBrush;
      copy.args = this.args != null ? Arrays.stream(this.args).map(e -> e.clone()).toArray(BuilderToolArg[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolState other)
            ? false
            : Objects.equals(this.id, other.id) && this.isBrush == other.isBrush && Arrays.equals((Object[])this.args, (Object[])other.args);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.id);
      result = 31 * result + Boolean.hashCode(this.isBrush);
      return 31 * result + Arrays.hashCode((Object[])this.args);
   }
}
