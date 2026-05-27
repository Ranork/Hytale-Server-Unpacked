package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import com.hypixel.hytale.protocol.packets.interface_.BlockChange;
import com.hypixel.hytale.protocol.packets.interface_.FluidChange;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderToolSelectionToolReplyWithClipboard implements Packet, ToClientPacket {
   public static final int PACKET_ID = 411;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public BlockChange[] blocksChange;
   @Nullable
   public FluidChange[] fluidsChange;
   @Nullable
   public ClipboardEntityChange[] entityChanges;

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

   public BuilderToolSelectionToolReplyWithClipboard(
      @Nullable BlockChange[] blocksChange, @Nullable FluidChange[] fluidsChange, @Nullable ClipboardEntityChange[] entityChanges
   ) {
      this.blocksChange = blocksChange;
      this.fluidsChange = fluidsChange;
      this.entityChanges = entityChanges;
   }

   public BuilderToolSelectionToolReplyWithClipboard(@Nonnull BuilderToolSelectionToolReplyWithClipboard other) {
      this.blocksChange = other.blocksChange;
      this.fluidsChange = other.fluidsChange;
      this.entityChanges = other.entityChanges;
   }

   @Nonnull
   public static BuilderToolSelectionToolReplyWithClipboard deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("BuilderToolSelectionToolReplyWithClipboard", 13, buf.readableBytes() - offset);
      } else {
         BuilderToolSelectionToolReplyWithClipboard obj = new BuilderToolSelectionToolReplyWithClipboard();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("BlocksChange", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 13 + varPosBase0;
            int blocksChangeCount = VarInt.peek(buf, varPos0);
            if (blocksChangeCount < 0) {
               throw ProtocolException.invalidVarInt("BlocksChange");
            }

            int varIntLen = VarInt.size(blocksChangeCount);
            if (blocksChangeCount > 4096000) {
               throw ProtocolException.arrayTooLong("BlocksChange", blocksChangeCount, 4096000);
            }

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
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("FluidsChange", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 13 + varPosBase1;
            int fluidsChangeCount = VarInt.peek(buf, varPos1);
            if (fluidsChangeCount < 0) {
               throw ProtocolException.invalidVarInt("FluidsChange");
            }

            int varIntLenx = VarInt.size(fluidsChangeCount);
            if (fluidsChangeCount > 4096000) {
               throw ProtocolException.arrayTooLong("FluidsChange", fluidsChangeCount, 4096000);
            }

            if (varPos1 + varIntLenx + fluidsChangeCount * 17L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("FluidsChange", varPos1 + varIntLenx + fluidsChangeCount * 17, buf.readableBytes());
            }

            obj.fluidsChange = new FluidChange[fluidsChangeCount];
            int elemPos = varPos1 + varIntLenx;

            for (int i = 0; i < fluidsChangeCount; i++) {
               obj.fluidsChange[i] = FluidChange.deserialize(buf, elemPos);
               elemPos += FluidChange.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 9);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("EntityChanges", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 13 + varPosBase2;
            int entityChangesCount = VarInt.peek(buf, varPos2);
            if (entityChangesCount < 0) {
               throw ProtocolException.invalidVarInt("EntityChanges");
            }

            int varIntLenxx = VarInt.size(entityChangesCount);
            if (entityChangesCount > 4096000) {
               throw ProtocolException.arrayTooLong("EntityChanges", entityChangesCount, 4096000);
            }

            if (varPos2 + varIntLenxx + entityChangesCount * 45L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("EntityChanges", varPos2 + varIntLenxx + entityChangesCount * 45, buf.readableBytes());
            }

            obj.entityChanges = new ClipboardEntityChange[entityChangesCount];
            int elemPos = varPos2 + varIntLenxx;

            for (int i = 0; i < entityChangesCount; i++) {
               obj.entityChanges[i] = ClipboardEntityChange.deserialize(buf, elemPos);
               elemPos += ClipboardEntityChange.computeBytesConsumed(buf, elemPos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("BlocksChange", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 13 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos0 += BlockChange.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("FluidsChange", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 13 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += FluidChange.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("EntityChanges", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 13 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos2 += ClipboardEntityChange.computeBytesConsumed(buf, pos2);
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   @Nullable
   public static BlockChange[] getBlocksChange(MemorySegment mem) {
      return getBlocksChange(mem, 0);
   }

   @Nullable
   public static BlockChange[] getBlocksChange(MemorySegment mem, int offset) {
      if (!hasBlocksChange(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 1, 13, "BlocksChange");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("BlocksChange", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("BlocksChange", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 17L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("BlocksChange", off + lenOffset + len * 17, (int)mem.byteSize());
            } else {
               off += lenOffset;
               BlockChange[] data = new BlockChange[len];

               for (int i = 0; i < len; i++) {
                  data[i] = BlockChange.toObject(mem, off + i * 17);
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static FluidChange[] getFluidsChange(MemorySegment mem) {
      return getFluidsChange(mem, 0);
   }

   @Nullable
   public static FluidChange[] getFluidsChange(MemorySegment mem, int offset) {
      if (!hasFluidsChange(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 13, "FluidsChange");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("FluidsChange", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("FluidsChange", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 17L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("FluidsChange", off + lenOffset + len * 17, (int)mem.byteSize());
            } else {
               off += lenOffset;
               FluidChange[] data = new FluidChange[len];

               for (int i = 0; i < len; i++) {
                  data[i] = FluidChange.toObject(mem, off + i * 17);
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static ClipboardEntityChange[] getEntityChanges(MemorySegment mem) {
      return getEntityChanges(mem, 0);
   }

   @Nullable
   public static ClipboardEntityChange[] getEntityChanges(MemorySegment mem, int offset) {
      if (!hasEntityChanges(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 9, 13, "EntityChanges");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("EntityChanges", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("EntityChanges", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("EntityChanges", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ClipboardEntityChange[] data = new ClipboardEntityChange[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ClipboardEntityChange.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasBlocksChange(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasFluidsChange(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasEntityChanges(MemorySegment mem, int offset) {
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

   public static BuilderToolSelectionToolReplyWithClipboard toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolSelectionToolReplyWithClipboard toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolSelectionToolReplyWithClipboard", offset + 13, (int)mem.byteSize());
      } else {
         BlockChange[] blocksChange = null;
         if (hasBlocksChange(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 1, 13, "BlocksChange");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("BlocksChange", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("BlocksChange", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 17L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("BlocksChange", off + lenOffset + len * 17, (int)mem.byteSize());
            }

            off += lenOffset;
            blocksChange = new BlockChange[len];

            for (int i = 0; i < len; i++) {
               blocksChange[i] = BlockChange.toObject(mem, off + i * 17);
            }
         }

         FluidChange[] fluidsChange = null;
         if (hasFluidsChange(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 5, 13, "FluidsChange");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("FluidsChange", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("FluidsChange", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx * 17L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("FluidsChange", offx + lenOffset + lenx * 17, (int)mem.byteSize());
            }

            offx += lenOffset;
            fluidsChange = new FluidChange[lenx];

            for (int i = 0; i < lenx; i++) {
               fluidsChange[i] = FluidChange.toObject(mem, offx + i * 17);
            }
         }

         ClipboardEntityChange[] entityChanges = null;
         if (hasEntityChanges(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 9, 13, "EntityChanges");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("EntityChanges", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.arrayTooLong("EntityChanges", lenxx, 4096000);
            }

            int lenOffset = (int)(packedxx >>> 32);
            if (offxx + lenOffset + lenxx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("EntityChanges", offxx + lenOffset + lenxx, (int)mem.byteSize());
            }

            offxx += lenOffset;
            entityChanges = new ClipboardEntityChange[lenxx];

            for (int i = 0; i < lenxx; i++) {
               entityChanges[i] = ClipboardEntityChange.toObject(mem, offxx);
               offxx += entityChanges[i].computeSize();
            }
         }

         return new BuilderToolSelectionToolReplyWithClipboard(blocksChange, fluidsChange, entityChanges);
      }
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

      if (this.entityChanges != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      int blocksChangeOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int fluidsChangeOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int entityChangesOffsetSlot = buf.writerIndex();
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

      if (this.entityChanges != null) {
         buf.setIntLE(entityChangesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.entityChanges.length > 4096000) {
            throw ProtocolException.arrayTooLong("EntityChanges", this.entityChanges.length, 4096000);
         }

         VarInt.write(buf, this.entityChanges.length);

         for (ClipboardEntityChange item : this.entityChanges) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(entityChangesOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.blocksChange != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.fluidsChange != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.entityChanges != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 13;
      if (this.blocksChange != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 13);
         if (this.blocksChange.length > 4096000) {
            throw ProtocolException.arrayTooLong("BlocksChange", this.blocksChange.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.blocksChange.length);
         int blocksChangeValueOffset = 0;

         for (int i = 0; i < this.blocksChange.length; i++) {
            blocksChangeValueOffset += this.blocksChange[i].serialize(mem, varOffset + blocksChangeValueOffset);
         }

         varOffset += blocksChangeValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.fluidsChange != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 13);
         if (this.fluidsChange.length > 4096000) {
            throw ProtocolException.arrayTooLong("FluidsChange", this.fluidsChange.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.fluidsChange.length);
         int fluidsChangeValueOffset = 0;

         for (int i = 0; i < this.fluidsChange.length; i++) {
            fluidsChangeValueOffset += this.fluidsChange[i].serialize(mem, varOffset + fluidsChangeValueOffset);
         }

         varOffset += fluidsChangeValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.entityChanges != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 13);
         if (this.entityChanges.length > 4096000) {
            throw ProtocolException.arrayTooLong("EntityChanges", this.entityChanges.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.entityChanges.length);
         int entityChangesValueOffset = 0;

         for (int i = 0; i < this.entityChanges.length; i++) {
            entityChangesValueOffset += this.entityChanges[i].serialize(mem, varOffset + entityChangesValueOffset);
         }

         varOffset += entityChangesValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 13;
      if (this.blocksChange != null) {
         size += VarInt.size(this.blocksChange.length) + this.blocksChange.length * 17;
      }

      if (this.fluidsChange != null) {
         size += VarInt.size(this.fluidsChange.length) + this.fluidsChange.length * 17;
      }

      if (this.entityChanges != null) {
         int entityChangesSize = 0;

         for (ClipboardEntityChange elem : this.entityChanges) {
            entityChangesSize += elem.computeSize();
         }

         size += VarInt.size(this.entityChanges.length) + entityChangesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int blocksChangeOffset = buffer.getIntLE(offset + 1);
            if (blocksChangeOffset < 0 || blocksChangeOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for BlocksChange");
            }

            int pos = offset + 13 + blocksChangeOffset;
            int blocksChangeCount = VarInt.peek(buffer, pos);
            if (blocksChangeCount < 0) {
               return ValidationResult.error("Invalid array count for BlocksChange");
            }

            if (blocksChangeCount > 4096000) {
               return ValidationResult.error("BlocksChange exceeds max length 4096000");
            }

            pos += VarInt.size(blocksChangeCount);
            pos += blocksChangeCount * 17;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading BlocksChange");
            }
         }

         if ((nullBits & 2) != 0) {
            int fluidsChangeOffset = buffer.getIntLE(offset + 5);
            if (fluidsChangeOffset < 0 || fluidsChangeOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for FluidsChange");
            }

            int posx = offset + 13 + fluidsChangeOffset;
            int fluidsChangeCount = VarInt.peek(buffer, posx);
            if (fluidsChangeCount < 0) {
               return ValidationResult.error("Invalid array count for FluidsChange");
            }

            if (fluidsChangeCount > 4096000) {
               return ValidationResult.error("FluidsChange exceeds max length 4096000");
            }

            posx += VarInt.size(fluidsChangeCount);
            posx += fluidsChangeCount * 17;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading FluidsChange");
            }
         }

         if ((nullBits & 4) != 0) {
            int entityChangesOffset = buffer.getIntLE(offset + 9);
            if (entityChangesOffset < 0 || entityChangesOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for EntityChanges");
            }

            int posxx = offset + 13 + entityChangesOffset;
            int entityChangesCount = VarInt.peek(buffer, posxx);
            if (entityChangesCount < 0) {
               return ValidationResult.error("Invalid array count for EntityChanges");
            }

            if (entityChangesCount > 4096000) {
               return ValidationResult.error("EntityChanges exceeds max length 4096000");
            }

            posxx += VarInt.size(entityChangesCount);

            for (int i = 0; i < entityChangesCount; i++) {
               ValidationResult structResult = ClipboardEntityChange.validateStructure(buffer, posxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ClipboardEntityChange in EntityChanges[" + i + "]: " + structResult.error());
               }

               posxx += ClipboardEntityChange.computeBytesConsumed(buffer, posxx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public BuilderToolSelectionToolReplyWithClipboard clone() {
      BuilderToolSelectionToolReplyWithClipboard copy = new BuilderToolSelectionToolReplyWithClipboard();
      copy.blocksChange = this.blocksChange != null ? Arrays.stream(this.blocksChange).map(e -> e.clone()).toArray(BlockChange[]::new) : null;
      copy.fluidsChange = this.fluidsChange != null ? Arrays.stream(this.fluidsChange).map(e -> e.clone()).toArray(FluidChange[]::new) : null;
      copy.entityChanges = this.entityChanges != null ? Arrays.stream(this.entityChanges).map(e -> e.clone()).toArray(ClipboardEntityChange[]::new) : null;
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
               && Arrays.equals((Object[])this.fluidsChange, (Object[])other.fluidsChange)
               && Arrays.equals((Object[])this.entityChanges, (Object[])other.entityChanges);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode((Object[])this.blocksChange);
      result = 31 * result + Arrays.hashCode((Object[])this.fluidsChange);
      return 31 * result + Arrays.hashCode((Object[])this.entityChanges);
   }
}
