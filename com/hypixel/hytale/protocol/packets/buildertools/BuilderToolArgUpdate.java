package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderToolArgUpdate implements Packet, ToServerPacket {
   public static final int PACKET_ID = 400;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 14;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 22;
   public static final int MAX_SIZE = 32768032;
   public int token;
   public int section;
   public int slot;
   @Nonnull
   public BuilderToolArgGroup group = BuilderToolArgGroup.Tool;
   @Nullable
   public String id;
   @Nullable
   public String value;

   @Override
   public int getId() {
      return 400;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolArgUpdate() {
   }

   public BuilderToolArgUpdate(int token, int section, int slot, @Nonnull BuilderToolArgGroup group, @Nullable String id, @Nullable String value) {
      this.token = token;
      this.section = section;
      this.slot = slot;
      this.group = group;
      this.id = id;
      this.value = value;
   }

   public BuilderToolArgUpdate(@Nonnull BuilderToolArgUpdate other) {
      this.token = other.token;
      this.section = other.section;
      this.slot = other.slot;
      this.group = other.group;
      this.id = other.id;
      this.value = other.value;
   }

   @Nonnull
   public static BuilderToolArgUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolArgUpdate obj = new BuilderToolArgUpdate();
      byte nullBits = buf.getByte(offset);
      obj.token = buf.getIntLE(offset + 1);
      obj.section = buf.getIntLE(offset + 5);
      obj.slot = buf.getIntLE(offset + 9);
      obj.group = BuilderToolArgGroup.fromValue(buf.getByte(offset + 13));
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 22 + buf.getIntLE(offset + 14);
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
         int varPos1 = offset + 22 + buf.getIntLE(offset + 18);
         int valueLen = VarInt.peek(buf, varPos1);
         if (valueLen < 0) {
            throw ProtocolException.negativeLength("Value", valueLen);
         }

         if (valueLen > 4096000) {
            throw ProtocolException.stringTooLong("Value", valueLen, 4096000);
         }

         obj.value = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 22;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 14);
         int pos0 = offset + 22 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 18);
         int pos1 = offset + 22 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
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
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.value != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.token);
      buf.writeIntLE(this.section);
      buf.writeIntLE(this.slot);
      buf.writeByte(this.group.getValue());
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int valueOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.value != null) {
         buf.setIntLE(valueOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.value, 4096000);
      } else {
         buf.setIntLE(valueOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 22;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.value != null) {
         size += PacketIO.stringSize(this.value);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 22) {
         return ValidationResult.error("Buffer too small: expected at least 22 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int idOffset = buffer.getIntLE(offset + 14);
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 22 + idOffset;
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
            int valueOffset = buffer.getIntLE(offset + 18);
            if (valueOffset < 0) {
               return ValidationResult.error("Invalid offset for Value");
            }

            int posx = offset + 22 + valueOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Value");
            }

            int valueLen = VarInt.peek(buffer, posx);
            if (valueLen < 0) {
               return ValidationResult.error("Invalid string length for Value");
            }

            if (valueLen > 4096000) {
               return ValidationResult.error("Value exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += valueLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Value");
            }
         }

         return ValidationResult.OK;
      }
   }

   public BuilderToolArgUpdate clone() {
      BuilderToolArgUpdate copy = new BuilderToolArgUpdate();
      copy.token = this.token;
      copy.section = this.section;
      copy.slot = this.slot;
      copy.group = this.group;
      copy.id = this.id;
      copy.value = this.value;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolArgUpdate other)
            ? false
            : this.token == other.token
               && this.section == other.section
               && this.slot == other.slot
               && Objects.equals(this.group, other.group)
               && Objects.equals(this.id, other.id)
               && Objects.equals(this.value, other.value);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.token, this.section, this.slot, this.group, this.id, this.value);
   }
}
