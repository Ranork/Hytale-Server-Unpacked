package com.hypixel.hytale.protocol.packets.entities;

import com.hypixel.hytale.protocol.ModelParticle;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpawnModelParticles implements Packet, ToClientPacket {
   public static final int PACKET_ID = 165;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 1677721600;
   public int entityId;
   @Nullable
   public ModelParticle[] modelParticles;

   @Override
   public int getId() {
      return 165;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SpawnModelParticles() {
   }

   public SpawnModelParticles(int entityId, @Nullable ModelParticle[] modelParticles) {
      this.entityId = entityId;
      this.modelParticles = modelParticles;
   }

   public SpawnModelParticles(@Nonnull SpawnModelParticles other) {
      this.entityId = other.entityId;
      this.modelParticles = other.modelParticles;
   }

   @Nonnull
   public static SpawnModelParticles deserialize(@Nonnull ByteBuf buf, int offset) {
      SpawnModelParticles obj = new SpawnModelParticles();
      byte nullBits = buf.getByte(offset);
      obj.entityId = buf.getIntLE(offset + 1);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int modelParticlesCount = VarInt.peek(buf, pos);
         if (modelParticlesCount < 0) {
            throw ProtocolException.negativeLength("ModelParticles", modelParticlesCount);
         }

         if (modelParticlesCount > 4096000) {
            throw ProtocolException.arrayTooLong("ModelParticles", modelParticlesCount, 4096000);
         }

         int modelParticlesVarLen = VarInt.size(modelParticlesCount);
         if (pos + modelParticlesVarLen + modelParticlesCount * 34L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("ModelParticles", pos + modelParticlesVarLen + modelParticlesCount * 34, buf.readableBytes());
         }

         pos += modelParticlesVarLen;
         obj.modelParticles = new ModelParticle[modelParticlesCount];

         for (int i = 0; i < modelParticlesCount; i++) {
            obj.modelParticles[i] = ModelParticle.deserialize(buf, pos);
            pos += ModelParticle.computeBytesConsumed(buf, pos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < arrLen; i++) {
            pos += ModelParticle.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.modelParticles != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.entityId);
      if (this.modelParticles != null) {
         if (this.modelParticles.length > 4096000) {
            throw ProtocolException.arrayTooLong("ModelParticles", this.modelParticles.length, 4096000);
         }

         VarInt.write(buf, this.modelParticles.length);

         for (ModelParticle item : this.modelParticles) {
            item.serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 5;
      if (this.modelParticles != null) {
         int modelParticlesSize = 0;

         for (ModelParticle elem : this.modelParticles) {
            modelParticlesSize += elem.computeSize();
         }

         size += VarInt.size(this.modelParticles.length) + modelParticlesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            int modelParticlesCount = VarInt.peek(buffer, pos);
            if (modelParticlesCount < 0) {
               return ValidationResult.error("Invalid array count for ModelParticles");
            }

            if (modelParticlesCount > 4096000) {
               return ValidationResult.error("ModelParticles exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < modelParticlesCount; i++) {
               ValidationResult structResult = ModelParticle.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ModelParticle in ModelParticles[" + i + "]: " + structResult.error());
               }

               pos += ModelParticle.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public SpawnModelParticles clone() {
      SpawnModelParticles copy = new SpawnModelParticles();
      copy.entityId = this.entityId;
      copy.modelParticles = this.modelParticles != null ? Arrays.stream(this.modelParticles).map(e -> e.clone()).toArray(ModelParticle[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SpawnModelParticles other)
            ? false
            : this.entityId == other.entityId && Arrays.equals((Object[])this.modelParticles, (Object[])other.modelParticles);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.entityId);
      return 31 * result + Arrays.hashCode((Object[])this.modelParticles);
   }
}
