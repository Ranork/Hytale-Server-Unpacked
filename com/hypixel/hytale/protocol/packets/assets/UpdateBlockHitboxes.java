package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.Hitbox;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
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

public class UpdateBlockHitboxes implements Packet, ToClientPacket {
   public static final int PACKET_ID = 41;
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
   public Map<Integer, Hitbox[]> blockBaseHitboxes;

   @Override
   public int getId() {
      return 41;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateBlockHitboxes() {
   }

   public UpdateBlockHitboxes(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, Hitbox[]> blockBaseHitboxes) {
      this.type = type;
      this.maxId = maxId;
      this.blockBaseHitboxes = blockBaseHitboxes;
   }

   public UpdateBlockHitboxes(@Nonnull UpdateBlockHitboxes other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.blockBaseHitboxes = other.blockBaseHitboxes;
   }

   @Nonnull
   public static UpdateBlockHitboxes deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateBlockHitboxes obj = new UpdateBlockHitboxes();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      obj.maxId = buf.getIntLE(offset + 2);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int blockBaseHitboxesCount = VarInt.peek(buf, pos);
         if (blockBaseHitboxesCount < 0) {
            throw ProtocolException.negativeLength("BlockBaseHitboxes", blockBaseHitboxesCount);
         }

         if (blockBaseHitboxesCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("BlockBaseHitboxes", blockBaseHitboxesCount, 4096000);
         }

         pos += VarInt.size(blockBaseHitboxesCount);
         obj.blockBaseHitboxes = new HashMap<>(blockBaseHitboxesCount);

         for (int i = 0; i < blockBaseHitboxesCount; i++) {
            int key = buf.getIntLE(pos);
            pos += 4;
            int valLen = VarInt.peek(buf, pos);
            if (valLen < 0) {
               throw ProtocolException.negativeLength("val", valLen);
            }

            if (valLen > 64) {
               throw ProtocolException.arrayTooLong("val", valLen, 64);
            }

            int valVarLen = VarInt.length(buf, pos);
            if (pos + valVarLen + valLen * 24L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("val", pos + valVarLen + valLen * 24, buf.readableBytes());
            }

            pos += valVarLen;
            Hitbox[] val = new Hitbox[valLen];

            for (int valIdx = 0; valIdx < valLen; valIdx++) {
               val[valIdx] = Hitbox.deserialize(buf, pos);
               pos += Hitbox.computeBytesConsumed(buf, pos);
            }

            if (obj.blockBaseHitboxes.put(key, val) != null) {
               throw ProtocolException.duplicateKey("blockBaseHitboxes", key);
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
            int al = VarInt.peek(buf, pos);
            pos += VarInt.length(buf, pos);

            for (int j = 0; j < al; j++) {
               pos += Hitbox.computeBytesConsumed(buf, pos);
            }
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.blockBaseHitboxes != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.blockBaseHitboxes != null) {
         if (this.blockBaseHitboxes.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("BlockBaseHitboxes", this.blockBaseHitboxes.size(), 4096000);
         }

         VarInt.write(buf, this.blockBaseHitboxes.size());

         for (Entry<Integer, Hitbox[]> e : this.blockBaseHitboxes.entrySet()) {
            buf.writeIntLE(e.getKey());
            VarInt.write(buf, e.getValue().length);

            for (Hitbox arrItem : e.getValue()) {
               arrItem.serialize(buf);
            }
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.blockBaseHitboxes != null) {
         int blockBaseHitboxesSize = 0;

         for (Entry<Integer, Hitbox[]> kvp : this.blockBaseHitboxes.entrySet()) {
            blockBaseHitboxesSize += 4 + VarInt.size(kvp.getValue().length) + ((Hitbox[])kvp.getValue()).length * 24;
         }

         size += VarInt.size(this.blockBaseHitboxes.size()) + blockBaseHitboxesSize;
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
            int blockBaseHitboxesCount = VarInt.peek(buffer, pos);
            if (blockBaseHitboxesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for BlockBaseHitboxes");
            }

            if (blockBaseHitboxesCount > 4096000) {
               return ValidationResult.error("BlockBaseHitboxes exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < blockBaseHitboxesCount; i++) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               int valueArrCount = VarInt.peek(buffer, pos);
               if (valueArrCount < 0) {
                  return ValidationResult.error("Invalid array count for value");
               }

               pos += VarInt.length(buffer, pos);

               for (int valueArrIdx = 0; valueArrIdx < valueArrCount; valueArrIdx++) {
                  pos += 24;
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateBlockHitboxes clone() {
      UpdateBlockHitboxes copy = new UpdateBlockHitboxes();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.blockBaseHitboxes != null) {
         Map<Integer, Hitbox[]> m = new HashMap<>();

         for (Entry<Integer, Hitbox[]> e : this.blockBaseHitboxes.entrySet()) {
            m.put(e.getKey(), Arrays.stream(e.getValue()).map(x -> x.clone()).toArray(Hitbox[]::new));
         }

         copy.blockBaseHitboxes = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateBlockHitboxes other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.blockBaseHitboxes, other.blockBaseHitboxes);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.blockBaseHitboxes);
   }
}
