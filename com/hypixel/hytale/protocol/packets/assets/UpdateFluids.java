package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.Fluid;
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

public class UpdateFluids implements Packet, ToClientPacket {
   public static final int PACKET_ID = 83;
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
   public Map<Integer, Fluid> fluids;

   @Override
   public int getId() {
      return 83;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateFluids() {
   }

   public UpdateFluids(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, Fluid> fluids) {
      this.type = type;
      this.maxId = maxId;
      this.fluids = fluids;
   }

   public UpdateFluids(@Nonnull UpdateFluids other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.fluids = other.fluids;
   }

   @Nonnull
   public static UpdateFluids deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateFluids obj = new UpdateFluids();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      obj.maxId = buf.getIntLE(offset + 2);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int fluidsCount = VarInt.peek(buf, pos);
         if (fluidsCount < 0) {
            throw ProtocolException.negativeLength("Fluids", fluidsCount);
         }

         if (fluidsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Fluids", fluidsCount, 4096000);
         }

         pos += VarInt.size(fluidsCount);
         obj.fluids = new HashMap<>(fluidsCount);

         for (int i = 0; i < fluidsCount; i++) {
            int key = buf.getIntLE(pos);
            pos += 4;
            Fluid val = Fluid.deserialize(buf, pos);
            pos += Fluid.computeBytesConsumed(buf, pos);
            if (obj.fluids.put(key, val) != null) {
               throw ProtocolException.duplicateKey("fluids", key);
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
            pos += Fluid.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.fluids != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.fluids != null) {
         if (this.fluids.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Fluids", this.fluids.size(), 4096000);
         }

         VarInt.write(buf, this.fluids.size());

         for (Entry<Integer, Fluid> e : this.fluids.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.fluids != null) {
         int fluidsSize = 0;

         for (Entry<Integer, Fluid> kvp : this.fluids.entrySet()) {
            fluidsSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.fluids.size()) + fluidsSize;
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
            int fluidsCount = VarInt.peek(buffer, pos);
            if (fluidsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Fluids");
            }

            if (fluidsCount > 4096000) {
               return ValidationResult.error("Fluids exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < fluidsCount; i++) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += Fluid.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateFluids clone() {
      UpdateFluids copy = new UpdateFluids();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.fluids != null) {
         Map<Integer, Fluid> m = new HashMap<>();

         for (Entry<Integer, Fluid> e : this.fluids.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.fluids = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateFluids other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.fluids, other.fluids);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.fluids);
   }
}
