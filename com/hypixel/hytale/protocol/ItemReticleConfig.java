package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemReticleConfig {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 4;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   @Nullable
   public String[] base;
   @Nullable
   public Map<Integer, ItemReticle> serverEvents;
   @Nullable
   public Map<ItemReticleClientEvent, ItemReticle> clientEvents;

   public ItemReticleConfig() {
   }

   public ItemReticleConfig(
      @Nullable String id,
      @Nullable String[] base,
      @Nullable Map<Integer, ItemReticle> serverEvents,
      @Nullable Map<ItemReticleClientEvent, ItemReticle> clientEvents
   ) {
      this.id = id;
      this.base = base;
      this.serverEvents = serverEvents;
      this.clientEvents = clientEvents;
   }

   public ItemReticleConfig(@Nonnull ItemReticleConfig other) {
      this.id = other.id;
      this.base = other.base;
      this.serverEvents = other.serverEvents;
      this.clientEvents = other.clientEvents;
   }

   @Nonnull
   public static ItemReticleConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 17) {
         throw ProtocolException.bufferTooSmall("ItemReticleConfig", 17, buf.readableBytes() - offset);
      } else {
         ItemReticleConfig obj = new ItemReticleConfig();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 17 + varPosBase0;
            int idLen = VarInt.peek(buf, varPos0);
            if (idLen < 0) {
               throw ProtocolException.invalidVarInt("Id");
            }

            int idVarIntLen = VarInt.size(idLen);
            if (idLen > 4096000) {
               throw ProtocolException.stringTooLong("Id", idLen, 4096000);
            }

            if (varPos0 + idVarIntLen + idLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Id", varPos0 + idVarIntLen + idLen, buf.readableBytes());
            }

            obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("Base", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 17 + varPosBase1;
            int baseCount = VarInt.peek(buf, varPos1);
            if (baseCount < 0) {
               throw ProtocolException.invalidVarInt("Base");
            }

            int varIntLen = VarInt.size(baseCount);
            if (baseCount > 4096000) {
               throw ProtocolException.arrayTooLong("Base", baseCount, 4096000);
            }

            if (varPos1 + varIntLen + baseCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Base", varPos1 + varIntLen + baseCount * 1, buf.readableBytes());
            }

            obj.base = new String[baseCount];
            int elemPos = varPos1 + varIntLen;

            for (int i = 0; i < baseCount; i++) {
               int strLen = VarInt.peek(buf, elemPos);
               if (strLen < 0) {
                  throw ProtocolException.invalidVarInt("base[" + i + "]");
               }

               int strVarLen = VarInt.size(strLen);
               if (strLen > 4096000) {
                  throw ProtocolException.stringTooLong("base[" + i + "]", strLen, 4096000);
               }

               if (elemPos + strVarLen + strLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("base[" + i + "]", elemPos + strVarLen + strLen, buf.readableBytes());
               }

               obj.base[i] = PacketIO.readVarString(buf, elemPos);
               elemPos += strVarLen + strLen;
            }
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 9);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("ServerEvents", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 17 + varPosBase2;
            int serverEventsCount = VarInt.peek(buf, varPos2);
            if (serverEventsCount < 0) {
               throw ProtocolException.invalidVarInt("ServerEvents");
            }

            int varIntLenx = VarInt.size(serverEventsCount);
            if (serverEventsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ServerEvents", serverEventsCount, 4096000);
            }

            obj.serverEvents = new HashMap<>(serverEventsCount);
            int dictPos = varPos2 + varIntLenx;

            for (int i = 0; i < serverEventsCount; i++) {
               int key = buf.getIntLE(dictPos);
               dictPos += 4;
               ItemReticle val = ItemReticle.deserialize(buf, dictPos);
               dictPos += ItemReticle.computeBytesConsumed(buf, dictPos);
               if (obj.serverEvents.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("serverEvents", key);
               }
            }
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 13);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("ClientEvents", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 17 + varPosBase3;
            int clientEventsCount = VarInt.peek(buf, varPos3);
            if (clientEventsCount < 0) {
               throw ProtocolException.invalidVarInt("ClientEvents");
            }

            int varIntLenx = VarInt.size(clientEventsCount);
            if (clientEventsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ClientEvents", clientEventsCount, 4096000);
            }

            obj.clientEvents = new HashMap<>(clientEventsCount);
            int dictPos = varPos3 + varIntLenx;

            for (int ix = 0; ix < clientEventsCount; ix++) {
               ItemReticleClientEvent key = ItemReticleClientEvent.fromValue(buf.getByte(dictPos));
               ItemReticle val = ItemReticle.deserialize(buf, ++dictPos);
               dictPos += ItemReticle.computeBytesConsumed(buf, dictPos);
               if (obj.clientEvents.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("clientEvents", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 17;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 17 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("Base", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 17 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos1);
            pos1 += VarInt.size(sl) + sl;
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("ServerEvents", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 17 + fieldOffset2;
         int dictLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos2 += 4;
            pos2 += ItemReticle.computeBytesConsumed(buf, pos2);
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 13);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("ClientEvents", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 17 + fieldOffset3;
         int dictLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos3 = ++pos3 + ItemReticle.computeBytesConsumed(buf, pos3);
         }

         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 17L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 1, 17, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static String[] getBase(MemorySegment mem) {
      return getBase(mem, 0);
   }

   @Nullable
   public static String[] getBase(MemorySegment mem, int offset) {
      if (!hasBase(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 17, "Base");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Base", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Base", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Base", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("Base", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static Map<Integer, ItemReticle> getServerEvents(MemorySegment mem) {
      return getServerEvents(mem, 0);
   }

   @Nullable
   public static Map<Integer, ItemReticle> getServerEvents(MemorySegment mem, int offset) {
      if (!hasServerEvents(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 9, 17, "ServerEvents");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ServerEvents", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ServerEvents", len, 4096000);
         } else {
            Map<Integer, ItemReticle> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               ItemReticle value = ItemReticle.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ServerEvents", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<ItemReticleClientEvent, ItemReticle> getClientEvents(MemorySegment mem) {
      return getClientEvents(mem, 0);
   }

   @Nullable
   public static Map<ItemReticleClientEvent, ItemReticle> getClientEvents(MemorySegment mem, int offset) {
      if (!hasClientEvents(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 13, 17, "ClientEvents");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ClientEvents", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ClientEvents", len, 4096000);
         } else {
            Map<ItemReticleClientEvent, ItemReticle> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               ItemReticleClientEvent key = ItemReticleClientEvent.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               ItemReticle value = ItemReticle.toObject(mem, ++off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ClientEvents", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasBase(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasServerEvents(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasClientEvents(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static ItemReticleConfig toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ItemReticleConfig toObject(MemorySegment mem, int offset) {
      if (offset + 17 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ItemReticleConfig", offset + 17, (int)mem.byteSize());
      } else {
         String[] base = null;
         if (hasBase(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 5, 17, "Base");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Base", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Base", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Base", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            base = new String[len];

            for (int i = 0; i < len; i++) {
               long sp = VarInt.getWithLength(mem, off);
               int n = (int)sp + (int)(sp >>> 32);
               base[i] = PacketIO.readVarString("Base", mem, off, 16384000, PacketIO.UTF8);
               off += n;
            }
         }

         Map<Integer, ItemReticle> serverEvents = null;
         if (hasServerEvents(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 9, 17, "ServerEvents");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("ServerEvents", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ServerEvents", lenx, 4096000);
            }

            serverEvents = new HashMap<>(lenx);
            offx += (int)(packedx >>> 32);

            for (int i = 0; i < lenx; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)offx);
               offx += 4;
               ItemReticle value = ItemReticle.toObject(mem, offx);
               offx += value.computeSize();
               if (serverEvents.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ServerEvents", key);
               }
            }
         }

         Map<ItemReticleClientEvent, ItemReticle> clientEvents = null;
         if (hasClientEvents(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 13, 17, "ClientEvents");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("ClientEvents", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ClientEvents", lenxx, 4096000);
            }

            clientEvents = new HashMap<>(lenxx);
            offxx += (int)(packedxx >>> 32);

            for (int ix = 0; ix < lenxx; ix++) {
               ItemReticleClientEvent key = ItemReticleClientEvent.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)offxx));
               ItemReticle value = ItemReticle.toObject(mem, ++offxx);
               offxx += value.computeSize();
               if (clientEvents.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ClientEvents", key);
               }
            }
         }

         return new ItemReticleConfig(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 1, 17, "Id"), 4096000, PacketIO.UTF8) : null,
            base,
            serverEvents,
            clientEvents
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.base != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.serverEvents != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.clientEvents != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int baseOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int serverEventsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int clientEventsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.base != null) {
         buf.setIntLE(baseOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.base.length > 4096000) {
            throw ProtocolException.arrayTooLong("Base", this.base.length, 4096000);
         }

         VarInt.write(buf, this.base.length);

         for (String item : this.base) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(baseOffsetSlot, -1);
      }

      if (this.serverEvents != null) {
         buf.setIntLE(serverEventsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.serverEvents.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ServerEvents", this.serverEvents.size(), 4096000);
         }

         VarInt.write(buf, this.serverEvents.size());

         for (Entry<Integer, ItemReticle> e : this.serverEvents.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(serverEventsOffsetSlot, -1);
      }

      if (this.clientEvents != null) {
         buf.setIntLE(clientEventsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.clientEvents.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ClientEvents", this.clientEvents.size(), 4096000);
         }

         VarInt.write(buf, this.clientEvents.size());

         for (Entry<ItemReticleClientEvent, ItemReticle> e : this.clientEvents.entrySet()) {
            buf.writeByte(e.getKey().getValue());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(clientEventsOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.base != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.serverEvents != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.clientEvents != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 17;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 17);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.base != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 17);
         if (this.base.length > 4096000) {
            throw ProtocolException.arrayTooLong("Base", this.base.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.base.length);
         int baseValueOffset = 0;

         for (int i = 0; i < this.base.length; i++) {
            baseValueOffset += PacketIO.writeVarString(mem, varOffset + baseValueOffset, this.base[i], 16384000);
         }

         varOffset += baseValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.serverEvents != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 17);
         if (this.serverEvents.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ServerEvents", this.serverEvents.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.serverEvents.size());

         for (Entry<Integer, ItemReticle> e : this.serverEvents.entrySet()) {
            mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      if (this.clientEvents != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), varOffset - offset - 17);
         if (this.clientEvents.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ClientEvents", this.clientEvents.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.clientEvents.size());

         for (Entry<ItemReticleClientEvent, ItemReticle> e : this.clientEvents.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            varOffset = ++varOffset + e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 17;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.base != null) {
         int baseSize = 0;

         for (String elem : this.base) {
            baseSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.base.length) + baseSize;
      }

      if (this.serverEvents != null) {
         int serverEventsSize = 0;

         for (Entry<Integer, ItemReticle> kvp : this.serverEvents.entrySet()) {
            serverEventsSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.serverEvents.size()) + serverEventsSize;
      }

      if (this.clientEvents != null) {
         int clientEventsSize = 0;

         for (Entry<ItemReticleClientEvent, ItemReticle> kvp : this.clientEvents.entrySet()) {
            clientEventsSize += 1 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.clientEvents.size()) + clientEventsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 17) {
         return ValidationResult.error("Buffer too small: expected at least 17 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int idOffset = buffer.getIntLE(offset + 1);
            if (idOffset < 0 || idOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 17 + idOffset;
            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.size(idLen);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits & 2) != 0) {
            int baseOffset = buffer.getIntLE(offset + 5);
            if (baseOffset < 0 || baseOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for Base");
            }

            int posx = offset + 17 + baseOffset;
            int baseCount = VarInt.peek(buffer, posx);
            if (baseCount < 0) {
               return ValidationResult.error("Invalid array count for Base");
            }

            if (baseCount > 4096000) {
               return ValidationResult.error("Base exceeds max length 4096000");
            }

            posx += VarInt.size(baseCount);

            for (int i = 0; i < baseCount; i++) {
               int strLen = VarInt.peek(buffer, posx);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in Base");
               }

               posx += VarInt.size(strLen);
               posx += strLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in Base");
               }
            }
         }

         if ((nullBits & 4) != 0) {
            int serverEventsOffset = buffer.getIntLE(offset + 9);
            if (serverEventsOffset < 0 || serverEventsOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for ServerEvents");
            }

            int posxx = offset + 17 + serverEventsOffset;
            int serverEventsCount = VarInt.peek(buffer, posxx);
            if (serverEventsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for ServerEvents");
            }

            if (serverEventsCount > 4096000) {
               return ValidationResult.error("ServerEvents exceeds max length 4096000");
            }

            posxx += VarInt.size(serverEventsCount);

            for (int i = 0; i < serverEventsCount; i++) {
               posxx += 4;
               if (posxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxx += ItemReticle.computeBytesConsumed(buffer, posxx);
            }
         }

         if ((nullBits & 8) != 0) {
            int clientEventsOffset = buffer.getIntLE(offset + 13);
            if (clientEventsOffset < 0 || clientEventsOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for ClientEvents");
            }

            int posxxx = offset + 17 + clientEventsOffset;
            int clientEventsCount = VarInt.peek(buffer, posxxx);
            if (clientEventsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for ClientEvents");
            }

            if (clientEventsCount > 4096000) {
               return ValidationResult.error("ClientEvents exceeds max length 4096000");
            }

            posxxx += VarInt.size(clientEventsCount);

            for (int i = 0; i < clientEventsCount; i++) {
               int v = buffer.getByte(posxxx) & 255;
               if (v >= 5) {
                  return ValidationResult.error("Invalid ItemReticleClientEvent value for key");
               }

               posxxx = ++posxxx + ItemReticle.computeBytesConsumed(buffer, posxxx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public ItemReticleConfig clone() {
      ItemReticleConfig copy = new ItemReticleConfig();
      copy.id = this.id;
      copy.base = this.base != null ? Arrays.copyOf(this.base, this.base.length) : null;
      if (this.serverEvents != null) {
         Map<Integer, ItemReticle> m = new HashMap<>();

         for (Entry<Integer, ItemReticle> e : this.serverEvents.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.serverEvents = m;
      }

      if (this.clientEvents != null) {
         Map<ItemReticleClientEvent, ItemReticle> m = new HashMap<>();

         for (Entry<ItemReticleClientEvent, ItemReticle> e : this.clientEvents.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.clientEvents = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemReticleConfig other)
            ? false
            : Objects.equals(this.id, other.id)
               && Arrays.equals((Object[])this.base, (Object[])other.base)
               && Objects.equals(this.serverEvents, other.serverEvents)
               && Objects.equals(this.clientEvents, other.clientEvents);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.id);
      result = 31 * result + Arrays.hashCode((Object[])this.base);
      result = 31 * result + Objects.hashCode(this.serverEvents);
      return 31 * result + Objects.hashCode(this.clientEvents);
   }
}
