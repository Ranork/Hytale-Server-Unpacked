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
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 61) {
         throw ProtocolException.bufferTooSmall("SyncInteractionChain", 61, buf.readableBytes() - offset);
      } else {
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
            int varPosBase0 = buf.getIntLE(offset + 33);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 61) {
               throw ProtocolException.invalidOffset("ItemInHandId", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 61 + varPosBase0;
            int itemInHandIdLen = VarInt.peek(buf, varPos0);
            if (itemInHandIdLen < 0) {
               throw ProtocolException.invalidVarInt("ItemInHandId");
            }

            int itemInHandIdVarIntLen = VarInt.size(itemInHandIdLen);
            if (itemInHandIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ItemInHandId", itemInHandIdLen, 4096000);
            }

            if (varPos0 + itemInHandIdVarIntLen + itemInHandIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ItemInHandId", varPos0 + itemInHandIdVarIntLen + itemInHandIdLen, buf.readableBytes());
            }

            obj.itemInHandId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 37);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 61) {
               throw ProtocolException.invalidOffset("UtilityItemId", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 61 + varPosBase1;
            int utilityItemIdLen = VarInt.peek(buf, varPos1);
            if (utilityItemIdLen < 0) {
               throw ProtocolException.invalidVarInt("UtilityItemId");
            }

            int utilityItemIdVarIntLen = VarInt.size(utilityItemIdLen);
            if (utilityItemIdLen > 4096000) {
               throw ProtocolException.stringTooLong("UtilityItemId", utilityItemIdLen, 4096000);
            }

            if (varPos1 + utilityItemIdVarIntLen + utilityItemIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("UtilityItemId", varPos1 + utilityItemIdVarIntLen + utilityItemIdLen, buf.readableBytes());
            }

            obj.utilityItemId = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 41);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 61) {
               throw ProtocolException.invalidOffset("ToolsItemId", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 61 + varPosBase2;
            int toolsItemIdLen = VarInt.peek(buf, varPos2);
            if (toolsItemIdLen < 0) {
               throw ProtocolException.invalidVarInt("ToolsItemId");
            }

            int toolsItemIdVarIntLen = VarInt.size(toolsItemIdLen);
            if (toolsItemIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ToolsItemId", toolsItemIdLen, 4096000);
            }

            if (varPos2 + toolsItemIdVarIntLen + toolsItemIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ToolsItemId", varPos2 + toolsItemIdVarIntLen + toolsItemIdLen, buf.readableBytes());
            }

            obj.toolsItemId = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 45);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 61) {
               throw ProtocolException.invalidOffset("ForkedId", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 61 + varPosBase3;
            obj.forkedId = ForkedChainId.deserialize(buf, varPos3);
         }

         if ((nullBits & 16) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 49);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 61) {
               throw ProtocolException.invalidOffset("Data", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 61 + varPosBase4;
            obj.data = InteractionChainData.deserialize(buf, varPos4);
         }

         if ((nullBits & 32) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 53);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 61) {
               throw ProtocolException.invalidOffset("NewForks", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 61 + varPosBase5;
            int newForksCount = VarInt.peek(buf, varPos5);
            if (newForksCount < 0) {
               throw ProtocolException.invalidVarInt("NewForks");
            }

            int varIntLen = VarInt.size(newForksCount);
            if (newForksCount > 4096000) {
               throw ProtocolException.arrayTooLong("NewForks", newForksCount, 4096000);
            }

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
            int varPosBase6 = buf.getIntLE(offset + 57);
            if (varPosBase6 < 0 || varPosBase6 > buf.writerIndex() - offset - 61) {
               throw ProtocolException.invalidOffset("InteractionData", varPosBase6, buf.readableBytes());
            }

            int varPos6 = offset + 61 + varPosBase6;
            int interactionDataCount = VarInt.peek(buf, varPos6);
            if (interactionDataCount < 0) {
               throw ProtocolException.invalidVarInt("InteractionData");
            }

            int varIntLenx = VarInt.size(interactionDataCount);
            if (interactionDataCount > 4096000) {
               throw ProtocolException.arrayTooLong("InteractionData", interactionDataCount, 4096000);
            }

            int interactionDataBitfieldSize = (interactionDataCount + 7) / 8;
            if (varPos6 + varIntLenx + interactionDataBitfieldSize > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("InteractionData", varPos6 + varIntLenx + interactionDataBitfieldSize, buf.readableBytes());
            }

            byte[] interactionDataBitfield = PacketIO.readBytes(buf, varPos6 + varIntLenx, interactionDataBitfieldSize);
            obj.interactionData = new InteractionSyncData[interactionDataCount];
            int elemPos = varPos6 + varIntLenx + interactionDataBitfieldSize;

            for (int i = 0; i < interactionDataCount; i++) {
               if ((interactionDataBitfield[i / 8] & 1 << i % 8) != 0) {
                  obj.interactionData[i] = InteractionSyncData.deserialize(buf, elemPos);
                  elemPos += InteractionSyncData.computeBytesConsumed(buf, elemPos);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 61;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 33);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 61) {
            throw ProtocolException.invalidOffset("ItemInHandId", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 61 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 37);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 61) {
            throw ProtocolException.invalidOffset("UtilityItemId", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 61 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 41);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 61) {
            throw ProtocolException.invalidOffset("ToolsItemId", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 61 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 45);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 61) {
            throw ProtocolException.invalidOffset("ForkedId", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 61 + fieldOffset3;
         pos3 += ForkedChainId.computeBytesConsumed(buf, pos3);
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 49);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 61) {
            throw ProtocolException.invalidOffset("Data", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 61 + fieldOffset4;
         pos4 += InteractionChainData.computeBytesConsumed(buf, pos4);
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 53);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 61) {
            throw ProtocolException.invalidOffset("NewForks", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 61 + fieldOffset5;
         int arrLen = VarInt.peek(buf, pos5);
         pos5 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos5 += computeBytesConsumed(buf, pos5);
         }

         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits & 64) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 57);
         if (fieldOffset6 < 0 || fieldOffset6 > buf.writerIndex() - offset - 61) {
            throw ProtocolException.invalidOffset("InteractionData", fieldOffset6, maxEnd);
         }

         int pos6 = offset + 61 + fieldOffset6;
         int arrLen = VarInt.peek(buf, pos6);
         pos6 += VarInt.size(arrLen);
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

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 61L;
   }

   public static int getActiveHotbarSlot(MemorySegment mem) {
      return getActiveHotbarSlot(mem, 0);
   }

   public static int getActiveHotbarSlot(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getActiveUtilitySlot(MemorySegment mem) {
      return getActiveUtilitySlot(mem, 0);
   }

   public static int getActiveUtilitySlot(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   public static int getActiveToolsSlot(MemorySegment mem) {
      return getActiveToolsSlot(mem, 0);
   }

   public static int getActiveToolsSlot(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 9));
   }

   @Nullable
   public static String getItemInHandId(MemorySegment mem) {
      return getItemInHandId(mem, 0);
   }

   @Nullable
   public static String getItemInHandId(MemorySegment mem, int offset) {
      return hasItemInHandId(mem, offset)
         ? PacketIO.readVarString("ItemInHandId", mem, offset + getValidatedOffset(mem, offset, 33, 61, "ItemInHandId"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getUtilityItemId(MemorySegment mem) {
      return getUtilityItemId(mem, 0);
   }

   @Nullable
   public static String getUtilityItemId(MemorySegment mem, int offset) {
      return hasUtilityItemId(mem, offset)
         ? PacketIO.readVarString("UtilityItemId", mem, offset + getValidatedOffset(mem, offset, 37, 61, "UtilityItemId"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getToolsItemId(MemorySegment mem) {
      return getToolsItemId(mem, 0);
   }

   @Nullable
   public static String getToolsItemId(MemorySegment mem, int offset) {
      return hasToolsItemId(mem, offset)
         ? PacketIO.readVarString("ToolsItemId", mem, offset + getValidatedOffset(mem, offset, 41, 61, "ToolsItemId"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean getInitial(MemorySegment mem) {
      return getInitial(mem, 0);
   }

   public static boolean getInitial(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 13));
   }

   public static boolean getDesync(MemorySegment mem) {
      return getDesync(mem, 0);
   }

   public static boolean getDesync(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 14));
   }

   public static int getOverrideRootInteraction(MemorySegment mem) {
      return getOverrideRootInteraction(mem, 0);
   }

   public static int getOverrideRootInteraction(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 15));
   }

   public static InteractionType getInteractionType(MemorySegment mem) {
      return getInteractionType(mem, 0);
   }

   public static InteractionType getInteractionType(MemorySegment mem, int offset) {
      return InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 19)));
   }

   public static int getEquipSlot(MemorySegment mem) {
      return getEquipSlot(mem, 0);
   }

   public static int getEquipSlot(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 20));
   }

   public static int getChainId(MemorySegment mem) {
      return getChainId(mem, 0);
   }

   public static int getChainId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 24));
   }

   @Nullable
   public static ForkedChainId getForkedId(MemorySegment mem) {
      return getForkedId(mem, 0);
   }

   @Nullable
   public static ForkedChainId getForkedId(MemorySegment mem, int offset) {
      return hasForkedId(mem, offset) ? ForkedChainId.toObject(mem, offset + getValidatedOffset(mem, offset, 45, 61, "ForkedId")) : null;
   }

   @Nullable
   public static InteractionChainData getData(MemorySegment mem) {
      return getData(mem, 0);
   }

   @Nullable
   public static InteractionChainData getData(MemorySegment mem, int offset) {
      return hasData(mem, offset) ? InteractionChainData.toObject(mem, offset + getValidatedOffset(mem, offset, 49, 61, "Data")) : null;
   }

   public static InteractionState getState(MemorySegment mem) {
      return getState(mem, 0);
   }

   public static InteractionState getState(MemorySegment mem, int offset) {
      return InteractionState.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 28)));
   }

   @Nullable
   public static SyncInteractionChain[] getNewForks(MemorySegment mem) {
      return getNewForks(mem, 0);
   }

   @Nullable
   public static SyncInteractionChain[] getNewForks(MemorySegment mem, int offset) {
      if (!hasNewForks(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 53, 61, "NewForks");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("NewForks", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("NewForks", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("NewForks", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               SyncInteractionChain[] data = new SyncInteractionChain[len];

               for (int i = 0; i < len; i++) {
                  data[i] = toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static int getOperationBaseIndex(MemorySegment mem) {
      return getOperationBaseIndex(mem, 0);
   }

   public static int getOperationBaseIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 29));
   }

   @Nullable
   public static InteractionSyncData[] getInteractionData(MemorySegment mem) {
      return getInteractionData(mem, 0);
   }

   @Nullable
   public static InteractionSyncData[] getInteractionData(MemorySegment mem, int offset) {
      if (!hasInteractionData(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 57, 61, "InteractionData");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("InteractionData", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("InteractionData", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + (len + 7) / 8 > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("InteractionData", off + lenOffset + (len + 7) / 8, (int)mem.byteSize());
            } else {
               off += lenOffset;
               InteractionSyncData[] data = new InteractionSyncData[len];
               int bitfieldSize = (len + 7) / 8;
               int bitfieldOff = off;
               off += bitfieldSize;
               if (off + bitfieldSize > mem.byteSize()) {
                  throw ProtocolException.bufferTooSmall("InteractionData", off + bitfieldSize, (int)mem.byteSize());
               } else {
                  int i = 0;

                  while (i < len) {
                     byte bits = mem.get(PacketIO.PROTO_BYTE, (long)(bitfieldOff + i / 8));

                     for (int batchEnd = Math.min(len, (i & -8) + 8); i < batchEnd; i++) {
                        if ((bits & 1 << (i & 7)) != 0) {
                           data[i] = InteractionSyncData.toObject(mem, off);
                           off += data[i].computeSize();
                        }
                     }
                  }

                  return data;
               }
            }
         }
      }
   }

   public static boolean hasItemInHandId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasUtilityItemId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasToolsItemId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasForkedId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasData(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasNewForks(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasInteractionData(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static SyncInteractionChain toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SyncInteractionChain toObject(MemorySegment mem, int offset) {
      if (offset + 61 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SyncInteractionChain", offset + 61, (int)mem.byteSize());
      } else {
         SyncInteractionChain[] newForks = null;
         if (hasNewForks(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 53, 61, "NewForks");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("NewForks", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("NewForks", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("NewForks", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            newForks = new SyncInteractionChain[len];

            for (int i = 0; i < len; i++) {
               newForks[i] = toObject(mem, off);
               off += newForks[i].computeSize();
            }
         }

         InteractionSyncData[] interactionData = null;
         if (hasInteractionData(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 57, 61, "InteractionData");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("InteractionData", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("InteractionData", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + (lenx + 7) / 8 > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("InteractionData", offx + lenOffset + (lenx + 7) / 8, (int)mem.byteSize());
            }

            offx += lenOffset;
            interactionData = new InteractionSyncData[lenx];
            int bitfieldSize = (lenx + 7) / 8;
            int bitfieldOff = offx;
            offx += bitfieldSize;
            if (offx + bitfieldSize > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("InteractionData", offx + bitfieldSize, (int)mem.byteSize());
            }

            int i = 0;

            while (i < lenx) {
               byte bits = mem.get(PacketIO.PROTO_BYTE, (long)(bitfieldOff + i / 8));

               for (int batchEnd = Math.min(lenx, (i & -8) + 8); i < batchEnd; i++) {
                  if ((bits & 1 << (i & 7)) != 0) {
                     interactionData[i] = InteractionSyncData.toObject(mem, offx);
                     offx += interactionData[i].computeSize();
                  }
               }
            }
         }

         return new SyncInteractionChain(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 9)),
            hasItemInHandId(mem, offset)
               ? PacketIO.readVarString("ItemInHandId", mem, offset + getValidatedOffset(mem, offset, 33, 61, "ItemInHandId"), 4096000, PacketIO.UTF8)
               : null,
            hasUtilityItemId(mem, offset)
               ? PacketIO.readVarString("UtilityItemId", mem, offset + getValidatedOffset(mem, offset, 37, 61, "UtilityItemId"), 4096000, PacketIO.UTF8)
               : null,
            hasToolsItemId(mem, offset)
               ? PacketIO.readVarString("ToolsItemId", mem, offset + getValidatedOffset(mem, offset, 41, 61, "ToolsItemId"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 13)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 14)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 15)),
            InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 19))),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 20)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 24)),
            hasForkedId(mem, offset) ? ForkedChainId.toObject(mem, offset + getValidatedOffset(mem, offset, 45, 61, "ForkedId")) : null,
            hasData(mem, offset) ? InteractionChainData.toObject(mem, offset + getValidatedOffset(mem, offset, 49, 61, "Data")) : null,
            InteractionState.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 28))),
            newForks,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 29)),
            interactionData
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.activeHotbarSlot);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.activeUtilitySlot);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 9), this.activeToolsSlot);
      mem.set(PacketIO.PROTO_BOOL, offset + 13, this.initial);
      mem.set(PacketIO.PROTO_BOOL, offset + 14, this.desync);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 15), this.overrideRootInteraction);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 19), (byte)this.interactionType.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 20), this.equipSlot);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 24), this.chainId);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 28), (byte)this.state.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 29), this.operationBaseIndex);
      int varOffset = offset + 61;
      if (this.itemInHandId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 33), varOffset - offset - 61);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.itemInHandId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 33), -1);
      }

      if (this.utilityItemId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 37), varOffset - offset - 61);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.utilityItemId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 37), -1);
      }

      if (this.toolsItemId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 41), varOffset - offset - 61);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.toolsItemId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 41), -1);
      }

      if (this.forkedId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 45), varOffset - offset - 61);
         varOffset += this.forkedId.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 45), -1);
      }

      if (this.data != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 49), varOffset - offset - 61);
         varOffset += this.data.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 49), -1);
      }

      if (this.newForks != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 53), varOffset - offset - 61);
         if (this.newForks.length > 4096000) {
            throw ProtocolException.arrayTooLong("NewForks", this.newForks.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.newForks.length);
         int newForksValueOffset = 0;

         for (int i = 0; i < this.newForks.length; i++) {
            newForksValueOffset += this.newForks[i].serialize(mem, varOffset + newForksValueOffset);
         }

         varOffset += newForksValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 53), -1);
      }

      if (this.interactionData != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 57), varOffset - offset - 61);
         if (this.interactionData.length > 4096000) {
            throw ProtocolException.arrayTooLong("InteractionData", this.interactionData.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.interactionData.length);
         int interactionDataBitfieldSize = (this.interactionData.length + 7) / 8;

         for (int bi = 0; bi < interactionDataBitfieldSize; bi++) {
            byte bits = 0;

            for (int j = bi * 8; j < Math.min(this.interactionData.length, bi * 8 + 8); j++) {
               if (this.interactionData[j] != null) {
                  bits |= (byte)(1 << (j & 7));
               }
            }

            mem.set(PacketIO.PROTO_BYTE, (long)(varOffset + bi), bits);
         }

         varOffset += interactionDataBitfieldSize;

         for (int i = 0; i < this.interactionData.length; i++) {
            if (this.interactionData[i] != null) {
               varOffset += this.interactionData[i].serialize(mem, varOffset);
            }
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 57), -1);
      }

      return varOffset - offset;
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
         int v = buffer.getByte(offset + 19) & 255;
         if (v >= 25) {
            return ValidationResult.error("Invalid InteractionType value for InteractionType");
         } else {
            v = buffer.getByte(offset + 28) & 255;
            if (v >= 5) {
               return ValidationResult.error("Invalid InteractionState value for State");
            } else {
               if ((nullBits & 1) != 0) {
                  v = buffer.getIntLE(offset + 33);
                  if (v < 0 || v > buffer.writerIndex() - offset - 61) {
                     return ValidationResult.error("Invalid offset for ItemInHandId");
                  }

                  int pos = offset + 61 + v;
                  int itemInHandIdLen = VarInt.peek(buffer, pos);
                  if (itemInHandIdLen < 0) {
                     return ValidationResult.error("Invalid string length for ItemInHandId");
                  }

                  if (itemInHandIdLen > 4096000) {
                     return ValidationResult.error("ItemInHandId exceeds max length 4096000");
                  }

                  pos += VarInt.size(itemInHandIdLen);
                  pos += itemInHandIdLen;
                  if (pos > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading ItemInHandId");
                  }
               }

               if ((nullBits & 2) != 0) {
                  v = buffer.getIntLE(offset + 37);
                  if (v < 0 || v > buffer.writerIndex() - offset - 61) {
                     return ValidationResult.error("Invalid offset for UtilityItemId");
                  }

                  int posx = offset + 61 + v;
                  int utilityItemIdLen = VarInt.peek(buffer, posx);
                  if (utilityItemIdLen < 0) {
                     return ValidationResult.error("Invalid string length for UtilityItemId");
                  }

                  if (utilityItemIdLen > 4096000) {
                     return ValidationResult.error("UtilityItemId exceeds max length 4096000");
                  }

                  posx += VarInt.size(utilityItemIdLen);
                  posx += utilityItemIdLen;
                  if (posx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading UtilityItemId");
                  }
               }

               if ((nullBits & 4) != 0) {
                  v = buffer.getIntLE(offset + 41);
                  if (v < 0 || v > buffer.writerIndex() - offset - 61) {
                     return ValidationResult.error("Invalid offset for ToolsItemId");
                  }

                  int posxx = offset + 61 + v;
                  int toolsItemIdLen = VarInt.peek(buffer, posxx);
                  if (toolsItemIdLen < 0) {
                     return ValidationResult.error("Invalid string length for ToolsItemId");
                  }

                  if (toolsItemIdLen > 4096000) {
                     return ValidationResult.error("ToolsItemId exceeds max length 4096000");
                  }

                  posxx += VarInt.size(toolsItemIdLen);
                  posxx += toolsItemIdLen;
                  if (posxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading ToolsItemId");
                  }
               }

               if ((nullBits & 8) != 0) {
                  v = buffer.getIntLE(offset + 45);
                  if (v < 0 || v > buffer.writerIndex() - offset - 61) {
                     return ValidationResult.error("Invalid offset for ForkedId");
                  }

                  int posxxx = offset + 61 + v;
                  ValidationResult forkedIdResult = ForkedChainId.validateStructure(buffer, posxxx);
                  if (!forkedIdResult.isValid()) {
                     return ValidationResult.error("Invalid ForkedId: " + forkedIdResult.error());
                  }

                  posxxx += ForkedChainId.computeBytesConsumed(buffer, posxxx);
               }

               if ((nullBits & 16) != 0) {
                  v = buffer.getIntLE(offset + 49);
                  if (v < 0 || v > buffer.writerIndex() - offset - 61) {
                     return ValidationResult.error("Invalid offset for Data");
                  }

                  int posxxx = offset + 61 + v;
                  ValidationResult dataResult = InteractionChainData.validateStructure(buffer, posxxx);
                  if (!dataResult.isValid()) {
                     return ValidationResult.error("Invalid Data: " + dataResult.error());
                  }

                  posxxx += InteractionChainData.computeBytesConsumed(buffer, posxxx);
               }

               if ((nullBits & 32) != 0) {
                  v = buffer.getIntLE(offset + 53);
                  if (v < 0 || v > buffer.writerIndex() - offset - 61) {
                     return ValidationResult.error("Invalid offset for NewForks");
                  }

                  int posxxx = offset + 61 + v;
                  int newForksCount = VarInt.peek(buffer, posxxx);
                  if (newForksCount < 0) {
                     return ValidationResult.error("Invalid array count for NewForks");
                  }

                  if (newForksCount > 4096000) {
                     return ValidationResult.error("NewForks exceeds max length 4096000");
                  }

                  posxxx += VarInt.size(newForksCount);

                  for (int i = 0; i < newForksCount; i++) {
                     ValidationResult structResult = validateStructure(buffer, posxxx);
                     if (!structResult.isValid()) {
                        return ValidationResult.error("Invalid SyncInteractionChain in NewForks[" + i + "]: " + structResult.error());
                     }

                     posxxx += computeBytesConsumed(buffer, posxxx);
                  }
               }

               if ((nullBits & 64) != 0) {
                  v = buffer.getIntLE(offset + 57);
                  if (v < 0 || v > buffer.writerIndex() - offset - 61) {
                     return ValidationResult.error("Invalid offset for InteractionData");
                  }

                  int posxxxx = offset + 61 + v;
                  int interactionDataCount = VarInt.peek(buffer, posxxxx);
                  if (interactionDataCount < 0) {
                     return ValidationResult.error("Invalid array count for InteractionData");
                  }

                  if (interactionDataCount > 4096000) {
                     return ValidationResult.error("InteractionData exceeds max length 4096000");
                  }

                  posxxxx += VarInt.size(interactionDataCount);
                  int interactionDataBitfieldSize = (interactionDataCount + 7) / 8;
                  if (posxxxx + interactionDataBitfieldSize > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading bitfield for InteractionData");
                  }

                  byte[] interactionDataBitfield = PacketIO.readBytes(buffer, posxxxx, interactionDataBitfieldSize);
                  posxxxx += interactionDataBitfieldSize;

                  for (int i = 0; i < interactionDataCount; i++) {
                     if ((interactionDataBitfield[i / 8] & 1 << i % 8) != 0) {
                        ValidationResult structResult = InteractionSyncData.validateStructure(buffer, posxxxx);
                        if (!structResult.isValid()) {
                           return ValidationResult.error("Invalid InteractionSyncData in interactionData[" + i + "]: " + structResult.error());
                        }

                        posxxxx += InteractionSyncData.computeBytesConsumed(buffer, posxxxx);
                     }
                  }
               }

               return ValidationResult.OK;
            }
         }
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
