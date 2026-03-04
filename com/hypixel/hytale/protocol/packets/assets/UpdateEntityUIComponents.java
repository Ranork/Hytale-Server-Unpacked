package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.EntityUIComponent;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateEntityUIComponents implements Packet, ToClientPacket {
   public static final int PACKET_ID = 73;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   public int maxId;
   @Nullable
   public Map<Integer, EntityUIComponent> components;

   @Override
   public int getId() {
      return 73;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateEntityUIComponents() {
   }

   public UpdateEntityUIComponents(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, EntityUIComponent> components) {
      this.type = type;
      this.maxId = maxId;
      this.components = components;
   }

   public UpdateEntityUIComponents(@Nonnull UpdateEntityUIComponents other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.components = other.components;
   }

   @Nonnull
   public static UpdateEntityUIComponents deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateEntityUIComponents obj = new UpdateEntityUIComponents();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      obj.maxId = buf.getIntLE(offset + 2);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int componentsCount = VarInt.peek(buf, pos);
         if (componentsCount < 0) {
            throw ProtocolException.negativeLength("Components", componentsCount);
         }

         if (componentsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Components", componentsCount, 4096000);
         }

         pos += VarInt.size(componentsCount);
         obj.components = new HashMap<>(componentsCount);

         for (int i = 0; i < componentsCount; i++) {
            int key = buf.getIntLE(pos);
            pos += 4;
            EntityUIComponent val = EntityUIComponent.deserialize(buf, pos);
            pos += EntityUIComponent.computeBytesConsumed(buf, pos);
            if (obj.components.put(key, val) != null) {
               throw ProtocolException.duplicateKey("components", key);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < dictLen; i++) {
            pos += 4;
            pos += EntityUIComponent.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.components != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.components != null) {
         if (this.components.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Components", this.components.size(), 4096000);
         }

         VarInt.write(buf, this.components.size());

         for (Entry<Integer, EntityUIComponent> e : this.components.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.components != null) {
         int componentsSize = 0;

         for (Entry<Integer, EntityUIComponent> kvp : this.components.entrySet()) {
            componentsSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.components.size()) + componentsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 6) {
         return ValidationResult.error("Buffer too small: expected at least 6 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int componentsCount = VarInt.peek(buffer, pos);
            if (componentsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Components");
            }

            if (componentsCount > 4096000) {
               return ValidationResult.error("Components exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < componentsCount; i++) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += EntityUIComponent.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateEntityUIComponents clone() {
      UpdateEntityUIComponents copy = new UpdateEntityUIComponents();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.components != null) {
         Map<Integer, EntityUIComponent> m = new HashMap<>();

         for (Entry<Integer, EntityUIComponent> e : this.components.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.components = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateEntityUIComponents other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.components, other.components);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.components);
   }
}
