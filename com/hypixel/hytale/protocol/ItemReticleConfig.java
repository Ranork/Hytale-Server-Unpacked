package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
      ItemReticleConfig obj = new ItemReticleConfig();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 17 + buf.getIntLE(offset + 1);
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
         int varPos1 = offset + 17 + buf.getIntLE(offset + 5);
         int baseCount = VarInt.peek(buf, varPos1);
         if (baseCount < 0) {
            throw ProtocolException.negativeLength("Base", baseCount);
         }

         if (baseCount > 4096000) {
            throw ProtocolException.arrayTooLong("Base", baseCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + baseCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Base", varPos1 + varIntLen + baseCount * 1, buf.readableBytes());
         }

         obj.base = new String[baseCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < baseCount; i++) {
            int strLen = VarInt.peek(buf, elemPos);
            if (strLen < 0) {
               throw ProtocolException.negativeLength("base[" + i + "]", strLen);
            }

            if (strLen > 4096000) {
               throw ProtocolException.stringTooLong("base[" + i + "]", strLen, 4096000);
            }

            int strVarLen = VarInt.length(buf, elemPos);
            obj.base[i] = PacketIO.readVarString(buf, elemPos);
            elemPos += strVarLen + strLen;
         }
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 17 + buf.getIntLE(offset + 9);
         int serverEventsCount = VarInt.peek(buf, varPos2);
         if (serverEventsCount < 0) {
            throw ProtocolException.negativeLength("ServerEvents", serverEventsCount);
         }

         if (serverEventsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ServerEvents", serverEventsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos2);
         obj.serverEvents = new HashMap<>(serverEventsCount);
         int dictPos = varPos2 + varIntLen;

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
         int varPos3 = offset + 17 + buf.getIntLE(offset + 13);
         int clientEventsCount = VarInt.peek(buf, varPos3);
         if (clientEventsCount < 0) {
            throw ProtocolException.negativeLength("ClientEvents", clientEventsCount);
         }

         if (clientEventsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ClientEvents", clientEventsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos3);
         obj.clientEvents = new HashMap<>(clientEventsCount);
         int dictPos = varPos3 + varIntLen;

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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 17;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 17 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 17 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos1);
            pos1 += VarInt.length(buf, pos1) + sl;
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         int pos2 = offset + 17 + fieldOffset2;
         int dictLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2);

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
         int pos3 = offset + 17 + fieldOffset3;
         int dictLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.length(buf, pos3);

         for (int i = 0; i < dictLen; i++) {
            pos3 = ++pos3 + ItemReticle.computeBytesConsumed(buf, pos3);
         }

         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
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
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 17 + idOffset;
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
            int baseOffset = buffer.getIntLE(offset + 5);
            if (baseOffset < 0) {
               return ValidationResult.error("Invalid offset for Base");
            }

            int posx = offset + 17 + baseOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Base");
            }

            int baseCount = VarInt.peek(buffer, posx);
            if (baseCount < 0) {
               return ValidationResult.error("Invalid array count for Base");
            }

            if (baseCount > 4096000) {
               return ValidationResult.error("Base exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < baseCount; i++) {
               int strLen = VarInt.peek(buffer, posx);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in Base");
               }

               posx += VarInt.length(buffer, posx);
               posx += strLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in Base");
               }
            }
         }

         if ((nullBits & 4) != 0) {
            int serverEventsOffset = buffer.getIntLE(offset + 9);
            if (serverEventsOffset < 0) {
               return ValidationResult.error("Invalid offset for ServerEvents");
            }

            int posxx = offset + 17 + serverEventsOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ServerEvents");
            }

            int serverEventsCount = VarInt.peek(buffer, posxx);
            if (serverEventsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for ServerEvents");
            }

            if (serverEventsCount > 4096000) {
               return ValidationResult.error("ServerEvents exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);

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
            if (clientEventsOffset < 0) {
               return ValidationResult.error("Invalid offset for ClientEvents");
            }

            int posxxx = offset + 17 + clientEventsOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ClientEvents");
            }

            int clientEventsCount = VarInt.peek(buffer, posxxx);
            if (clientEventsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for ClientEvents");
            }

            if (clientEventsCount > 4096000) {
               return ValidationResult.error("ClientEvents exceeds max length 4096000");
            }

            posxxx += VarInt.length(buffer, posxxx);

            for (int i = 0; i < clientEventsCount; i++) {
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
