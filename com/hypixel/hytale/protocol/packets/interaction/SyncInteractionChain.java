package com.hypixel.hytale.protocol.packets.interaction;

import com.hypixel.hytale.protocol.ForkedChainId;
import com.hypixel.hytale.protocol.InteractionChainData;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SyncInteractionChain {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 33;
   public static final int VARIABLE_FIELD_COUNT = 7;
   public static final int VARIABLE_BLOCK_START = 61;
   public static final int MAX_SIZE = 1677721600;
   public int activeHotbarSlot;
   public int activeUtilitySlot;
   public int activeToolsSlot;
   @Nullable
   public String itemInHandId;
   @Nullable
   public String utilityItemId;
   @Nullable
   public String toolsItemId;
   public boolean initial;
   public boolean desync;
   public int overrideRootInteraction = Integer.MIN_VALUE;
   @Nonnull
   public InteractionType interactionType = InteractionType.Primary;
   public int equipSlot;
   public int chainId;
   @Nullable
   public ForkedChainId forkedId;
   @Nullable
   public InteractionChainData data;
   @Nonnull
   public InteractionState state = InteractionState.Finished;
   @Nullable
   public SyncInteractionChain[] newForks;
   public int operationBaseIndex;
   @Nullable
   public InteractionSyncData[] interactionData;

   public SyncInteractionChain() {
   }

   public SyncInteractionChain(
      int activeHotbarSlot,
      int activeUtilitySlot,
      int activeToolsSlot,
      @Nullable String itemInHandId,
      @Nullable String utilityItemId,
      @Nullable String toolsItemId,
      boolean initial,
      boolean desync,
      int overrideRootInteraction,
      @Nonnull InteractionType interactionType,
      int equipSlot,
      int chainId,
      @Nullable ForkedChainId forkedId,
      @Nullable InteractionChainData data,
      @Nonnull InteractionState state,
      @Nullable SyncInteractionChain[] newForks,
      int operationBaseIndex,
      @Nullable InteractionSyncData[] interactionData
   ) {
      this.activeHotbarSlot = activeHotbarSlot;
      this.activeUtilitySlot = activeUtilitySlot;
      this.activeToolsSlot = activeToolsSlot;
      this.itemInHandId = itemInHandId;
      this.utilityItemId = utilityItemId;
      this.toolsItemId = toolsItemId;
      this.initial = initial;
      this.desync = desync;
      this.overrideRootInteraction = overrideRootInteraction;
      this.interactionType = interactionType;
      this.equipSlot = equipSlot;
      this.chainId = chainId;
      this.forkedId = forkedId;
      this.data = data;
      this.state = state;
      this.newForks = newForks;
      this.operationBaseIndex = operationBaseIndex;
      this.interactionData = interactionData;
   }

   public SyncInteractionChain(@Nonnull SyncInteractionChain other) {
      this.activeHotbarSlot = other.activeHotbarSlot;
      this.activeUtilitySlot = other.activeUtilitySlot;
      this.activeToolsSlot = other.activeToolsSlot;
      this.itemInHandId = other.itemInHandId;
      this.utilityItemId = other.utilityItemId;
      this.toolsItemId = other.toolsItemId;
      this.initial = other.initial;
      this.desync = other.desync;
      this.overrideRootInteraction = other.overrideRootInteraction;
      this.interactionType = other.interactionType;
      this.equipSlot = other.equipSlot;
      this.chainId = other.chainId;
      this.forkedId = other.forkedId;
      this.data = other.data;
      this.state = other.state;
      this.newForks = other.newForks;
      this.operationBaseIndex = other.operationBaseIndex;
      this.interactionData = other.interactionData;
   }

   @Nonnull
   public static SyncInteractionChain deserialize(@Nonnull ByteBuf buf, int offset) {
      SyncInteractionChain obj = new SyncInteractionChain();
      byte nullBits = buf.getByte(offset);
      obj.activeHotbarSlot = buf.getIntLE(offset + 1);
      obj.activeUtilitySlot = buf.getIntLE(offset + 5);
      obj.activeToolsSlot = buf.getIntLE(offset + 9);
      obj.initial = buf.getByte(offset + 13) != 0;
      obj.desync = buf.getByte(offset + 14) != 0;
      obj.overrideRootInteraction = buf.getIntLE(offset + 15);
      obj.interactionType = InteractionType.fromValue(buf.getByte(offset + 19));
      obj.equipSlot = buf.getIntLE(offset + 20);
      obj.chainId = buf.getIntLE(offset + 24);
      obj.state = InteractionState.fromValue(buf.getByte(offset + 28));
      obj.operationBaseIndex = buf.getIntLE(offset + 29);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 61 + buf.getIntLE(offset + 33);
         int itemInHandIdLen = VarInt.peek(buf, varPos0);
         if (itemInHandIdLen < 0) {
            throw ProtocolException.negativeLength("ItemInHandId", itemInHandIdLen);
         }

         if (itemInHandIdLen > 4096000) {
            throw ProtocolException.stringTooLong("ItemInHandId", itemInHandIdLen, 4096000);
         }

         obj.itemInHandId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 61 + buf.getIntLE(offset + 37);
         int utilityItemIdLen = VarInt.peek(buf, varPos1);
         if (utilityItemIdLen < 0) {
            throw ProtocolException.negativeLength("UtilityItemId", utilityItemIdLen);
         }

         if (utilityItemIdLen > 4096000) {
            throw ProtocolException.stringTooLong("UtilityItemId", utilityItemIdLen, 4096000);
         }

         obj.utilityItemId = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 61 + buf.getIntLE(offset + 41);
         int toolsItemIdLen = VarInt.peek(buf, varPos2);
         if (toolsItemIdLen < 0) {
            throw ProtocolException.negativeLength("ToolsItemId", toolsItemIdLen);
         }

         if (toolsItemIdLen > 4096000) {
            throw ProtocolException.stringTooLong("ToolsItemId", toolsItemIdLen, 4096000);
         }

         obj.toolsItemId = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
      }

      if ((nullBits & 8) != 0) {
         int varPos3 = offset + 61 + buf.getIntLE(offset + 45);
         obj.forkedId = ForkedChainId.deserialize(buf, varPos3);
      }

      if ((nullBits & 16) != 0) {
         int varPos4 = offset + 61 + buf.getIntLE(offset + 49);
         obj.data = InteractionChainData.deserialize(buf, varPos4);
      }

      if ((nullBits & 32) != 0) {
         int varPos5 = offset + 61 + buf.getIntLE(offset + 53);
         int newForksCount = VarInt.peek(buf, varPos5);
         if (newForksCount < 0) {
            throw ProtocolException.negativeLength("NewForks", newForksCount);
         }

         if (newForksCount > 4096000) {
            throw ProtocolException.arrayTooLong("NewForks", newForksCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos5);
         if (varPos5 + varIntLen + newForksCount * 33L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("NewForks", varPos5 + varIntLen + newForksCount * 33, buf.readableBytes());
         }

         obj.newForks = new SyncInteractionChain[newForksCount];
         int elemPos = varPos5 + varIntLen;

         for (int i = 0; i < newForksCount; i++) {
            obj.newForks[i] = deserialize(buf, elemPos);
            elemPos += computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits & 64) != 0) {
         int varPos6 = offset + 61 + buf.getIntLE(offset + 57);
         int interactionDataCount = VarInt.peek(buf, varPos6);
         if (interactionDataCount < 0) {
            throw ProtocolException.negativeLength("InteractionData", interactionDataCount);
         }

         if (interactionDataCount > 4096000) {
            throw ProtocolException.arrayTooLong("InteractionData", interactionDataCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos6);
         int interactionDataBitfieldSize = (interactionDataCount + 7) / 8;
         byte[] interactionDataBitfield = PacketIO.readBytes(buf, varPos6 + varIntLen, interactionDataBitfieldSize);
         obj.interactionData = new InteractionSyncData[interactionDataCount];
         int elemPos = varPos6 + varIntLen + interactionDataBitfieldSize;

         for (int i = 0; i < interactionDataCount; i++) {
            if ((interactionDataBitfield[i / 8] & 1 << i % 8) != 0) {
               obj.interactionData[i] = InteractionSyncData.deserialize(buf, elemPos);
               elemPos += InteractionSyncData.computeBytesConsumed(buf, elemPos);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 61;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 33);
         int pos0 = offset + 61 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 37);
         int pos1 = offset + 61 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 41);
         int pos2 = offset + 61 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 45);
         int pos3 = offset + 61 + fieldOffset3;
         pos3 += ForkedChainId.computeBytesConsumed(buf, pos3);
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 49);
         int pos4 = offset + 61 + fieldOffset4;
         pos4 += InteractionChainData.computeBytesConsumed(buf, pos4);
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 53);
         int pos5 = offset + 61 + fieldOffset5;
         int arrLen = VarInt.peek(buf, pos5);
         pos5 += VarInt.length(buf, pos5);

         for (int i = 0; i < arrLen; i++) {
            pos5 += computeBytesConsumed(buf, pos5);
         }

         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits & 64) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 57);
         int pos6 = offset + 61 + fieldOffset6;
         int arrLen = VarInt.peek(buf, pos6);
         pos6 += VarInt.length(buf, pos6);
         int bitfieldSize = (arrLen + 7) / 8;
         byte[] bitfield = PacketIO.readBytes(buf, pos6, bitfieldSize);
         pos6 += bitfieldSize;

         for (int i = 0; i < arrLen; i++) {
            if ((bitfield[i / 8] & 1 << i % 8) != 0) {
               pos6 += InteractionSyncData.computeBytesConsumed(buf, pos6);
            }
         }

         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.itemInHandId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.utilityItemId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.toolsItemId != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.forkedId != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.data != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.newForks != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.interactionData != null) {
         nullBits = (byte)(nullBits | 64);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.activeHotbarSlot);
      buf.writeIntLE(this.activeUtilitySlot);
      buf.writeIntLE(this.activeToolsSlot);
      buf.writeByte(this.initial ? 1 : 0);
      buf.writeByte(this.desync ? 1 : 0);
      buf.writeIntLE(this.overrideRootInteraction);
      buf.writeByte(this.interactionType.getValue());
      buf.writeIntLE(this.equipSlot);
      buf.writeIntLE(this.chainId);
      buf.writeByte(this.state.getValue());
      buf.writeIntLE(this.operationBaseIndex);
      int itemInHandIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int utilityItemIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int toolsItemIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int forkedIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int dataOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int newForksOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int interactionDataOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.itemInHandId != null) {
         buf.setIntLE(itemInHandIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.itemInHandId, 4096000);
      } else {
         buf.setIntLE(itemInHandIdOffsetSlot, -1);
      }

      if (this.utilityItemId != null) {
         buf.setIntLE(utilityItemIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.utilityItemId, 4096000);
      } else {
         buf.setIntLE(utilityItemIdOffsetSlot, -1);
      }

      if (this.toolsItemId != null) {
         buf.setIntLE(toolsItemIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.toolsItemId, 4096000);
      } else {
         buf.setIntLE(toolsItemIdOffsetSlot, -1);
      }

      if (this.forkedId != null) {
         buf.setIntLE(forkedIdOffsetSlot, buf.writerIndex() - varBlockStart);
         this.forkedId.serialize(buf);
      } else {
         buf.setIntLE(forkedIdOffsetSlot, -1);
      }

      if (this.data != null) {
         buf.setIntLE(dataOffsetSlot, buf.writerIndex() - varBlockStart);
         this.data.serialize(buf);
      } else {
         buf.setIntLE(dataOffsetSlot, -1);
      }

      if (this.newForks != null) {
         buf.setIntLE(newForksOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.newForks.length > 4096000) {
            throw ProtocolException.arrayTooLong("NewForks", this.newForks.length, 4096000);
         }

         VarInt.write(buf, this.newForks.length);

         for (SyncInteractionChain item : this.newForks) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(newForksOffsetSlot, -1);
      }

      if (this.interactionData != null) {
         buf.setIntLE(interactionDataOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.interactionData.length > 4096000) {
            throw ProtocolException.arrayTooLong("InteractionData", this.interactionData.length, 4096000);
         }

         VarInt.write(buf, this.interactionData.length);
         int interactionDataBitfieldSize = (this.interactionData.length + 7) / 8;
         byte[] interactionDataBitfield = new byte[interactionDataBitfieldSize];

         for (int i = 0; i < this.interactionData.length; i++) {
            if (this.interactionData[i] != null) {
               interactionDataBitfield[i / 8] = (byte)(interactionDataBitfield[i / 8] | (byte)(1 << i % 8));
            }
         }

         buf.writeBytes(interactionDataBitfield);

         for (int ix = 0; ix < this.interactionData.length; ix++) {
            if (this.interactionData[ix] != null) {
               this.interactionData[ix].serialize(buf);
            }
         }
      } else {
         buf.setIntLE(interactionDataOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 61;
      if (this.itemInHandId != null) {
         size += PacketIO.stringSize(this.itemInHandId);
      }

      if (this.utilityItemId != null) {
         size += PacketIO.stringSize(this.utilityItemId);
      }

      if (this.toolsItemId != null) {
         size += PacketIO.stringSize(this.toolsItemId);
      }

      if (this.forkedId != null) {
         size += this.forkedId.computeSize();
      }

      if (this.data != null) {
         size += this.data.computeSize();
      }

      if (this.newForks != null) {
         int newForksSize = 0;

         for (SyncInteractionChain elem : this.newForks) {
            newForksSize += elem.computeSize();
         }

         size += VarInt.size(this.newForks.length) + newForksSize;
      }

      if (this.interactionData != null) {
         int interactionDataSize = 0;

         for (InteractionSyncData elem : this.interactionData) {
            if (elem != null) {
               interactionDataSize += elem.computeSize();
            }
         }

         size += VarInt.size(this.interactionData.length) + (this.interactionData.length + 7) / 8 + interactionDataSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 61) {
         return ValidationResult.error("Buffer too small: expected at least 61 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int itemInHandIdOffset = buffer.getIntLE(offset + 33);
            if (itemInHandIdOffset < 0) {
               return ValidationResult.error("Invalid offset for ItemInHandId");
            }

            int pos = offset + 61 + itemInHandIdOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ItemInHandId");
            }

            int itemInHandIdLen = VarInt.peek(buffer, pos);
            if (itemInHandIdLen < 0) {
               return ValidationResult.error("Invalid string length for ItemInHandId");
            }

            if (itemInHandIdLen > 4096000) {
               return ValidationResult.error("ItemInHandId exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += itemInHandIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ItemInHandId");
            }
         }

         if ((nullBits & 2) != 0) {
            int utilityItemIdOffset = buffer.getIntLE(offset + 37);
            if (utilityItemIdOffset < 0) {
               return ValidationResult.error("Invalid offset for UtilityItemId");
            }

            int posx = offset + 61 + utilityItemIdOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for UtilityItemId");
            }

            int utilityItemIdLen = VarInt.peek(buffer, posx);
            if (utilityItemIdLen < 0) {
               return ValidationResult.error("Invalid string length for UtilityItemId");
            }

            if (utilityItemIdLen > 4096000) {
               return ValidationResult.error("UtilityItemId exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += utilityItemIdLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading UtilityItemId");
            }
         }

         if ((nullBits & 4) != 0) {
            int toolsItemIdOffset = buffer.getIntLE(offset + 41);
            if (toolsItemIdOffset < 0) {
               return ValidationResult.error("Invalid offset for ToolsItemId");
            }

            int posxx = offset + 61 + toolsItemIdOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ToolsItemId");
            }

            int toolsItemIdLen = VarInt.peek(buffer, posxx);
            if (toolsItemIdLen < 0) {
               return ValidationResult.error("Invalid string length for ToolsItemId");
            }

            if (toolsItemIdLen > 4096000) {
               return ValidationResult.error("ToolsItemId exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += toolsItemIdLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ToolsItemId");
            }
         }

         if ((nullBits & 8) != 0) {
            int forkedIdOffset = buffer.getIntLE(offset + 45);
            if (forkedIdOffset < 0) {
               return ValidationResult.error("Invalid offset for ForkedId");
            }

            int posxxx = offset + 61 + forkedIdOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ForkedId");
            }

            ValidationResult forkedIdResult = ForkedChainId.validateStructure(buffer, posxxx);
            if (!forkedIdResult.isValid()) {
               return ValidationResult.error("Invalid ForkedId: " + forkedIdResult.error());
            }

            posxxx += ForkedChainId.computeBytesConsumed(buffer, posxxx);
         }

         if ((nullBits & 16) != 0) {
            int dataOffset = buffer.getIntLE(offset + 49);
            if (dataOffset < 0) {
               return ValidationResult.error("Invalid offset for Data");
            }

            int posxxxx = offset + 61 + dataOffset;
            if (posxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Data");
            }

            ValidationResult dataResult = InteractionChainData.validateStructure(buffer, posxxxx);
            if (!dataResult.isValid()) {
               return ValidationResult.error("Invalid Data: " + dataResult.error());
            }

            posxxxx += InteractionChainData.computeBytesConsumed(buffer, posxxxx);
         }

         if ((nullBits & 32) != 0) {
            int newForksOffset = buffer.getIntLE(offset + 53);
            if (newForksOffset < 0) {
               return ValidationResult.error("Invalid offset for NewForks");
            }

            int posxxxxx = offset + 61 + newForksOffset;
            if (posxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for NewForks");
            }

            int newForksCount = VarInt.peek(buffer, posxxxxx);
            if (newForksCount < 0) {
               return ValidationResult.error("Invalid array count for NewForks");
            }

            if (newForksCount > 4096000) {
               return ValidationResult.error("NewForks exceeds max length 4096000");
            }

            posxxxxx += VarInt.length(buffer, posxxxxx);

            for (int i = 0; i < newForksCount; i++) {
               ValidationResult structResult = validateStructure(buffer, posxxxxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid SyncInteractionChain in NewForks[" + i + "]: " + structResult.error());
               }

               posxxxxx += computeBytesConsumed(buffer, posxxxxx);
            }
         }

         if ((nullBits & 64) != 0) {
            int interactionDataOffset = buffer.getIntLE(offset + 57);
            if (interactionDataOffset < 0) {
               return ValidationResult.error("Invalid offset for InteractionData");
            }

            int posxxxxxx = offset + 61 + interactionDataOffset;
            if (posxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for InteractionData");
            }

            int interactionDataCount = VarInt.peek(buffer, posxxxxxx);
            if (interactionDataCount < 0) {
               return ValidationResult.error("Invalid array count for InteractionData");
            }

            if (interactionDataCount > 4096000) {
               return ValidationResult.error("InteractionData exceeds max length 4096000");
            }

            posxxxxxx += VarInt.length(buffer, posxxxxxx);

            for (int i = 0; i < interactionDataCount; i++) {
               ValidationResult structResult = InteractionSyncData.validateStructure(buffer, posxxxxxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid InteractionSyncData in InteractionData[" + i + "]: " + structResult.error());
               }

               posxxxxxx += InteractionSyncData.computeBytesConsumed(buffer, posxxxxxx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public SyncInteractionChain clone() {
      SyncInteractionChain copy = new SyncInteractionChain();
      copy.activeHotbarSlot = this.activeHotbarSlot;
      copy.activeUtilitySlot = this.activeUtilitySlot;
      copy.activeToolsSlot = this.activeToolsSlot;
      copy.itemInHandId = this.itemInHandId;
      copy.utilityItemId = this.utilityItemId;
      copy.toolsItemId = this.toolsItemId;
      copy.initial = this.initial;
      copy.desync = this.desync;
      copy.overrideRootInteraction = this.overrideRootInteraction;
      copy.interactionType = this.interactionType;
      copy.equipSlot = this.equipSlot;
      copy.chainId = this.chainId;
      copy.forkedId = this.forkedId != null ? this.forkedId.clone() : null;
      copy.data = this.data != null ? this.data.clone() : null;
      copy.state = this.state;
      copy.newForks = this.newForks != null ? Arrays.stream(this.newForks).map(e -> e.clone()).toArray(SyncInteractionChain[]::new) : null;
      copy.operationBaseIndex = this.operationBaseIndex;
      copy.interactionData = this.interactionData != null
         ? Arrays.stream(this.interactionData).map(e -> e != null ? e.clone() : null).toArray(InteractionSyncData[]::new)
         : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SyncInteractionChain other)
            ? false
            : this.activeHotbarSlot == other.activeHotbarSlot
               && this.activeUtilitySlot == other.activeUtilitySlot
               && this.activeToolsSlot == other.activeToolsSlot
               && Objects.equals(this.itemInHandId, other.itemInHandId)
               && Objects.equals(this.utilityItemId, other.utilityItemId)
               && Objects.equals(this.toolsItemId, other.toolsItemId)
               && this.initial == other.initial
               && this.desync == other.desync
               && this.overrideRootInteraction == other.overrideRootInteraction
               && Objects.equals(this.interactionType, other.interactionType)
               && this.equipSlot == other.equipSlot
               && this.chainId == other.chainId
               && Objects.equals(this.forkedId, other.forkedId)
               && Objects.equals(this.data, other.data)
               && Objects.equals(this.state, other.state)
               && Arrays.equals((Object[])this.newForks, (Object[])other.newForks)
               && this.operationBaseIndex == other.operationBaseIndex
               && Arrays.equals((Object[])this.interactionData, (Object[])other.interactionData);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.activeHotbarSlot);
      result = 31 * result + Integer.hashCode(this.activeUtilitySlot);
      result = 31 * result + Integer.hashCode(this.activeToolsSlot);
      result = 31 * result + Objects.hashCode(this.itemInHandId);
      result = 31 * result + Objects.hashCode(this.utilityItemId);
      result = 31 * result + Objects.hashCode(this.toolsItemId);
      result = 31 * result + Boolean.hashCode(this.initial);
      result = 31 * result + Boolean.hashCode(this.desync);
      result = 31 * result + Integer.hashCode(this.overrideRootInteraction);
      result = 31 * result + Objects.hashCode(this.interactionType);
      result = 31 * result + Integer.hashCode(this.equipSlot);
      result = 31 * result + Integer.hashCode(this.chainId);
      result = 31 * result + Objects.hashCode(this.forkedId);
      result = 31 * result + Objects.hashCode(this.data);
      result = 31 * result + Objects.hashCode(this.state);
      result = 31 * result + Arrays.hashCode((Object[])this.newForks);
      result = 31 * result + Integer.hashCode(this.operationBaseIndex);
      return 31 * result + Arrays.hashCode((Object[])this.interactionData);
   }
}
