package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.FluidFX;
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

public class UpdateFluidFX implements Packet, ToClientPacket {
   public static final int PACKET_ID = 63;
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
   public Map<Integer, FluidFX> fluidFX;

   @Override
   public int getId() {
      return 63;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateFluidFX() {
   }

   public UpdateFluidFX(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, FluidFX> fluidFX) {
      this.type = type;
      this.maxId = maxId;
      this.fluidFX = fluidFX;
   }

   public UpdateFluidFX(@Nonnull UpdateFluidFX other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.fluidFX = other.fluidFX;
   }

   @Nonnull
   public static UpdateFluidFX deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateFluidFX obj = new UpdateFluidFX();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      obj.maxId = buf.getIntLE(offset + 2);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int fluidFXCount = VarInt.peek(buf, pos);
         if (fluidFXCount < 0) {
            throw ProtocolException.negativeLength("FluidFX", fluidFXCount);
         }

         if (fluidFXCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("FluidFX", fluidFXCount, 4096000);
         }

         pos += VarInt.size(fluidFXCount);
         obj.fluidFX = new HashMap<>(fluidFXCount);

         for (int i = 0; i < fluidFXCount; i++) {
            int key = buf.getIntLE(pos);
            pos += 4;
            FluidFX val = FluidFX.deserialize(buf, pos);
            pos += FluidFX.computeBytesConsumed(buf, pos);
            if (obj.fluidFX.put(key, val) != null) {
               throw ProtocolException.duplicateKey("fluidFX", key);
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
            pos += FluidFX.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.fluidFX != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.fluidFX != null) {
         if (this.fluidFX.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("FluidFX", this.fluidFX.size(), 4096000);
         }

         VarInt.write(buf, this.fluidFX.size());

         for (Entry<Integer, FluidFX> e : this.fluidFX.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.fluidFX != null) {
         int fluidFXSize = 0;

         for (Entry<Integer, FluidFX> kvp : this.fluidFX.entrySet()) {
            fluidFXSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.fluidFX.size()) + fluidFXSize;
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
            int fluidFXCount = VarInt.peek(buffer, pos);
            if (fluidFXCount < 0) {
               return ValidationResult.error("Invalid dictionary count for FluidFX");
            }

            if (fluidFXCount > 4096000) {
               return ValidationResult.error("FluidFX exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < fluidFXCount; i++) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += FluidFX.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateFluidFX clone() {
      UpdateFluidFX copy = new UpdateFluidFX();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.fluidFX != null) {
         Map<Integer, FluidFX> m = new HashMap<>();

         for (Entry<Integer, FluidFX> e : this.fluidFX.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.fluidFX = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateFluidFX other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.fluidFX, other.fluidFX);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.fluidFX);
   }
}
