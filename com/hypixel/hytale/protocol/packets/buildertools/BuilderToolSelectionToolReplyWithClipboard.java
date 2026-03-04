package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import com.hypixel.hytale.protocol.packets.interface_.BlockChange;
import com.hypixel.hytale.protocol.packets.interface_.FluidChange;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderToolSelectionToolReplyWithClipboard implements Packet, ToClientPacket {
   public static final int PACKET_ID = 411;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 139264019;
   @Nullable
   public BlockChange[] blocksChange;
   @Nullable
   public FluidChange[] fluidsChange;

   @Override
   public int getId() {
      return 411;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolSelectionToolReplyWithClipboard() {
   }

   public BuilderToolSelectionToolReplyWithClipboard(@Nullable BlockChange[] blocksChange, @Nullable FluidChange[] fluidsChange) {
      this.blocksChange = blocksChange;
      this.fluidsChange = fluidsChange;
   }

   public BuilderToolSelectionToolReplyWithClipboard(@Nonnull BuilderToolSelectionToolReplyWithClipboard other) {
      this.blocksChange = other.blocksChange;
      this.fluidsChange = other.fluidsChange;
   }

   @Nonnull
   public static BuilderToolSelectionToolReplyWithClipboard deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolSelectionToolReplyWithClipboard obj = new BuilderToolSelectionToolReplyWithClipboard();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         int blocksChangeCount = VarInt.peek(buf, varPos0);
         if (blocksChangeCount < 0) {
            throw ProtocolException.negativeLength("BlocksChange", blocksChangeCount);
         }

         if (blocksChangeCount > 4096000) {
            throw ProtocolException.arrayTooLong("BlocksChange", blocksChangeCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos0);
         if (varPos0 + varIntLen + blocksChangeCount * 17L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("BlocksChange", varPos0 + varIntLen + blocksChangeCount * 17, buf.readableBytes());
         }

         obj.blocksChange = new BlockChange[blocksChangeCount];
         int elemPos = varPos0 + varIntLen;

         for (int i = 0; i < blocksChangeCount; i++) {
            obj.blocksChange[i] = BlockChange.deserialize(buf, elemPos);
            elemPos += BlockChange.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         int fluidsChangeCount = VarInt.peek(buf, varPos1);
         if (fluidsChangeCount < 0) {
            throw ProtocolException.negativeLength("FluidsChange", fluidsChangeCount);
         }

         if (fluidsChangeCount > 4096000) {
            throw ProtocolException.arrayTooLong("FluidsChange", fluidsChangeCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + fluidsChangeCount * 17L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("FluidsChange", varPos1 + varIntLen + fluidsChangeCount * 17, buf.readableBytes());
         }

         obj.fluidsChange = new FluidChange[fluidsChangeCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < fluidsChangeCount; i++) {
            obj.fluidsChange[i] = FluidChange.deserialize(buf, elemPos);
            elemPos += FluidChange.computeBytesConsumed(buf, elemPos);
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
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0);

         for (int i = 0; i < arrLen; i++) {
            pos0 += BlockChange.computeBytesConsumed(buf, pos0);
         }

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
            pos1 += FluidChange.computeBytesConsumed(buf, pos1);
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
      if (this.blocksChange != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.fluidsChange != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int blocksChangeOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int fluidsChangeOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.blocksChange != null) {
         buf.setIntLE(blocksChangeOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.blocksChange.length > 4096000) {
            throw ProtocolException.arrayTooLong("BlocksChange", this.blocksChange.length, 4096000);
         }

         VarInt.write(buf, this.blocksChange.length);

         for (BlockChange item : this.blocksChange) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(blocksChangeOffsetSlot, -1);
      }

      if (this.fluidsChange != null) {
         buf.setIntLE(fluidsChangeOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.fluidsChange.length > 4096000) {
            throw ProtocolException.arrayTooLong("FluidsChange", this.fluidsChange.length, 4096000);
         }

         VarInt.write(buf, this.fluidsChange.length);

         for (FluidChange item : this.fluidsChange) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(fluidsChangeOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 9;
      if (this.blocksChange != null) {
         size += VarInt.size(this.blocksChange.length) + this.blocksChange.length * 17;
      }

      if (this.fluidsChange != null) {
         size += VarInt.size(this.fluidsChange.length) + this.fluidsChange.length * 17;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int blocksChangeOffset = buffer.getIntLE(offset + 1);
            if (blocksChangeOffset < 0) {
               return ValidationResult.error("Invalid offset for BlocksChange");
            }

            int pos = offset + 9 + blocksChangeOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for BlocksChange");
            }

            int blocksChangeCount = VarInt.peek(buffer, pos);
            if (blocksChangeCount < 0) {
               return ValidationResult.error("Invalid array count for BlocksChange");
            }

            if (blocksChangeCount > 4096000) {
               return ValidationResult.error("BlocksChange exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += blocksChangeCount * 17;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading BlocksChange");
            }
         }

         if ((nullBits & 2) != 0) {
            int fluidsChangeOffset = buffer.getIntLE(offset + 5);
            if (fluidsChangeOffset < 0) {
               return ValidationResult.error("Invalid offset for FluidsChange");
            }

            int posx = offset + 9 + fluidsChangeOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for FluidsChange");
            }

            int fluidsChangeCount = VarInt.peek(buffer, posx);
            if (fluidsChangeCount < 0) {
               return ValidationResult.error("Invalid array count for FluidsChange");
            }

            if (fluidsChangeCount > 4096000) {
               return ValidationResult.error("FluidsChange exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += fluidsChangeCount * 17;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading FluidsChange");
            }
         }

         return ValidationResult.OK;
      }
   }

   public BuilderToolSelectionToolReplyWithClipboard clone() {
      BuilderToolSelectionToolReplyWithClipboard copy = new BuilderToolSelectionToolReplyWithClipboard();
      copy.blocksChange = this.blocksChange != null ? Arrays.stream(this.blocksChange).map(e -> e.clone()).toArray(BlockChange[]::new) : null;
      copy.fluidsChange = this.fluidsChange != null ? Arrays.stream(this.fluidsChange).map(e -> e.clone()).toArray(FluidChange[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolSelectionToolReplyWithClipboard other)
            ? false
            : Arrays.equals((Object[])this.blocksChange, (Object[])other.blocksChange)
               && Arrays.equals((Object[])this.fluidsChange, (Object[])other.fluidsChange);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode((Object[])this.blocksChange);
      return 31 * result + Arrays.hashCode((Object[])this.fluidsChange);
   }
}
