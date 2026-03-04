package com.hypixel.hytale.protocol;

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

public class ProjectileConfig {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 163;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 171;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public PhysicsConfig physicsConfig;
   @Nullable
   public Model model;
   public double launchForce;
   @Nullable
   public Vector3f spawnOffset;
   @Nullable
   public Direction rotationOffset;
   @Nullable
   public Map<InteractionType, Integer> interactions;
   public int launchLocalSoundEventIndex;
   public int projectileSoundEventIndex;

   public ProjectileConfig() {
   }

   public ProjectileConfig(
      @Nullable PhysicsConfig physicsConfig,
      @Nullable Model model,
      double launchForce,
      @Nullable Vector3f spawnOffset,
      @Nullable Direction rotationOffset,
      @Nullable Map<InteractionType, Integer> interactions,
      int launchLocalSoundEventIndex,
      int projectileSoundEventIndex
   ) {
      this.physicsConfig = physicsConfig;
      this.model = model;
      this.launchForce = launchForce;
      this.spawnOffset = spawnOffset;
      this.rotationOffset = rotationOffset;
      this.interactions = interactions;
      this.launchLocalSoundEventIndex = launchLocalSoundEventIndex;
      this.projectileSoundEventIndex = projectileSoundEventIndex;
   }

   public ProjectileConfig(@Nonnull ProjectileConfig other) {
      this.physicsConfig = other.physicsConfig;
      this.model = other.model;
      this.launchForce = other.launchForce;
      this.spawnOffset = other.spawnOffset;
      this.rotationOffset = other.rotationOffset;
      this.interactions = other.interactions;
      this.launchLocalSoundEventIndex = other.launchLocalSoundEventIndex;
      this.projectileSoundEventIndex = other.projectileSoundEventIndex;
   }

   @Nonnull
   public static ProjectileConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      ProjectileConfig obj = new ProjectileConfig();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.physicsConfig = PhysicsConfig.deserialize(buf, offset + 1);
      }

      obj.launchForce = buf.getDoubleLE(offset + 123);
      if ((nullBits & 2) != 0) {
         obj.spawnOffset = Vector3f.deserialize(buf, offset + 131);
      }

      if ((nullBits & 4) != 0) {
         obj.rotationOffset = Direction.deserialize(buf, offset + 143);
      }

      obj.launchLocalSoundEventIndex = buf.getIntLE(offset + 155);
      obj.projectileSoundEventIndex = buf.getIntLE(offset + 159);
      if ((nullBits & 8) != 0) {
         int varPos0 = offset + 171 + buf.getIntLE(offset + 163);
         obj.model = Model.deserialize(buf, varPos0);
      }

      if ((nullBits & 16) != 0) {
         int varPos1 = offset + 171 + buf.getIntLE(offset + 167);
         int interactionsCount = VarInt.peek(buf, varPos1);
         if (interactionsCount < 0) {
            throw ProtocolException.negativeLength("Interactions", interactionsCount);
         }

         if (interactionsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Interactions", interactionsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         obj.interactions = new HashMap<>(interactionsCount);
         int dictPos = varPos1 + varIntLen;

         for (int i = 0; i < interactionsCount; i++) {
            InteractionType key = InteractionType.fromValue(buf.getByte(dictPos));
            int val = buf.getIntLE(++dictPos);
            dictPos += 4;
            if (obj.interactions.put(key, val) != null) {
               throw ProtocolException.duplicateKey("interactions", key);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 171;
      if ((nullBits & 8) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 163);
         int pos0 = offset + 171 + fieldOffset0;
         pos0 += Model.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 167);
         int pos1 = offset + 171 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < dictLen; i++) {
            pos1 = ++pos1 + 4;
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.physicsConfig != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.spawnOffset != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.rotationOffset != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.model != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.interactions != null) {
         nullBits = (byte)(nullBits | 16);
      }

      buf.writeByte(nullBits);
      if (this.physicsConfig != null) {
         this.physicsConfig.serialize(buf);
      } else {
         buf.writeZero(122);
      }

      buf.writeDoubleLE(this.launchForce);
      if (this.spawnOffset != null) {
         this.spawnOffset.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.rotationOffset != null) {
         this.rotationOffset.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeIntLE(this.launchLocalSoundEventIndex);
      buf.writeIntLE(this.projectileSoundEventIndex);
      int modelOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int interactionsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.model != null) {
         buf.setIntLE(modelOffsetSlot, buf.writerIndex() - varBlockStart);
         this.model.serialize(buf);
      } else {
         buf.setIntLE(modelOffsetSlot, -1);
      }

      if (this.interactions != null) {
         buf.setIntLE(interactionsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.interactions.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Interactions", this.interactions.size(), 4096000);
         }

         VarInt.write(buf, this.interactions.size());

         for (Entry<InteractionType, Integer> e : this.interactions.entrySet()) {
            buf.writeByte(e.getKey().getValue());
            buf.writeIntLE(e.getValue());
         }
      } else {
         buf.setIntLE(interactionsOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 171;
      if (this.model != null) {
         size += this.model.computeSize();
      }

      if (this.interactions != null) {
         size += VarInt.size(this.interactions.size()) + this.interactions.size() * 5;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 171) {
         return ValidationResult.error("Buffer too small: expected at least 171 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 8) != 0) {
            int modelOffset = buffer.getIntLE(offset + 163);
            if (modelOffset < 0) {
               return ValidationResult.error("Invalid offset for Model");
            }

            int pos = offset + 171 + modelOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Model");
            }

            ValidationResult modelResult = Model.validateStructure(buffer, pos);
            if (!modelResult.isValid()) {
               return ValidationResult.error("Invalid Model: " + modelResult.error());
            }

            pos += Model.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 16) != 0) {
            int interactionsOffset = buffer.getIntLE(offset + 167);
            if (interactionsOffset < 0) {
               return ValidationResult.error("Invalid offset for Interactions");
            }

            int posx = offset + 171 + interactionsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Interactions");
            }

            int interactionsCount = VarInt.peek(buffer, posx);
            if (interactionsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Interactions");
            }

            if (interactionsCount > 4096000) {
               return ValidationResult.error("Interactions exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < interactionsCount; i++) {
               posx = ++posx + 4;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public ProjectileConfig clone() {
      ProjectileConfig copy = new ProjectileConfig();
      copy.physicsConfig = this.physicsConfig != null ? this.physicsConfig.clone() : null;
      copy.model = this.model != null ? this.model.clone() : null;
      copy.launchForce = this.launchForce;
      copy.spawnOffset = this.spawnOffset != null ? this.spawnOffset.clone() : null;
      copy.rotationOffset = this.rotationOffset != null ? this.rotationOffset.clone() : null;
      copy.interactions = this.interactions != null ? new HashMap<>(this.interactions) : null;
      copy.launchLocalSoundEventIndex = this.launchLocalSoundEventIndex;
      copy.projectileSoundEventIndex = this.projectileSoundEventIndex;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ProjectileConfig other)
            ? false
            : Objects.equals(this.physicsConfig, other.physicsConfig)
               && Objects.equals(this.model, other.model)
               && this.launchForce == other.launchForce
               && Objects.equals(this.spawnOffset, other.spawnOffset)
               && Objects.equals(this.rotationOffset, other.rotationOffset)
               && Objects.equals(this.interactions, other.interactions)
               && this.launchLocalSoundEventIndex == other.launchLocalSoundEventIndex
               && this.projectileSoundEventIndex == other.projectileSoundEventIndex;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.physicsConfig,
         this.model,
         this.launchForce,
         this.spawnOffset,
         this.rotationOffset,
         this.interactions,
         this.launchLocalSoundEventIndex,
         this.projectileSoundEventIndex
      );
   }
}
