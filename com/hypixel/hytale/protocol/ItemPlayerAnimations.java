package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
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

public class ItemPlayerAnimations {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 91;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 103;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   @Nullable
   public Map<String, ItemAnimation> animations;
   @Nullable
   public WiggleWeights wiggleWeights;
   @Nullable
   public CameraSettings camera;
   @Nullable
   public ItemPullbackConfiguration pullbackConfig;
   public boolean useFirstPersonOverride;

   public ItemPlayerAnimations() {
   }

   public ItemPlayerAnimations(
      @Nullable String id,
      @Nullable Map<String, ItemAnimation> animations,
      @Nullable WiggleWeights wiggleWeights,
      @Nullable CameraSettings camera,
      @Nullable ItemPullbackConfiguration pullbackConfig,
      boolean useFirstPersonOverride
   ) {
      this.id = id;
      this.animations = animations;
      this.wiggleWeights = wiggleWeights;
      this.camera = camera;
      this.pullbackConfig = pullbackConfig;
      this.useFirstPersonOverride = useFirstPersonOverride;
   }

   public ItemPlayerAnimations(@Nonnull ItemPlayerAnimations other) {
      this.id = other.id;
      this.animations = other.animations;
      this.wiggleWeights = other.wiggleWeights;
      this.camera = other.camera;
      this.pullbackConfig = other.pullbackConfig;
      this.useFirstPersonOverride = other.useFirstPersonOverride;
   }

   @Nonnull
   public static ItemPlayerAnimations deserialize(@Nonnull ByteBuf buf, int offset) {
      ItemPlayerAnimations obj = new ItemPlayerAnimations();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.wiggleWeights = WiggleWeights.deserialize(buf, offset + 1);
      }

      if ((nullBits & 2) != 0) {
         obj.pullbackConfig = ItemPullbackConfiguration.deserialize(buf, offset + 41);
      }

      obj.useFirstPersonOverride = buf.getByte(offset + 90) != 0;
      if ((nullBits & 4) != 0) {
         int varPos0 = offset + 103 + buf.getIntLE(offset + 91);
         int idLen = VarInt.peek(buf, varPos0);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 8) != 0) {
         int varPos1 = offset + 103 + buf.getIntLE(offset + 95);
         int animationsCount = VarInt.peek(buf, varPos1);
         if (animationsCount < 0) {
            throw ProtocolException.negativeLength("Animations", animationsCount);
         }

         if (animationsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Animations", animationsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         obj.animations = new HashMap<>(animationsCount);
         int dictPos = varPos1 + varIntLen;

         for (int i = 0; i < animationsCount; i++) {
            int keyLen = VarInt.peek(buf, dictPos);
            if (keyLen < 0) {
               throw ProtocolException.negativeLength("key", keyLen);
            }

            if (keyLen > 4096000) {
               throw ProtocolException.stringTooLong("key", keyLen, 4096000);
            }

            int keyVarLen = VarInt.length(buf, dictPos);
            String key = PacketIO.readVarString(buf, dictPos);
            dictPos += keyVarLen + keyLen;
            ItemAnimation val = ItemAnimation.deserialize(buf, dictPos);
            dictPos += ItemAnimation.computeBytesConsumed(buf, dictPos);
            if (obj.animations.put(key, val) != null) {
               throw ProtocolException.duplicateKey("animations", key);
            }
         }
      }

      if ((nullBits & 16) != 0) {
         int varPos2 = offset + 103 + buf.getIntLE(offset + 99);
         obj.camera = CameraSettings.deserialize(buf, varPos2);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 103;
      if ((nullBits & 4) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 91);
         int pos0 = offset + 103 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 95);
         int pos1 = offset + 103 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos1);
            pos1 += VarInt.length(buf, pos1) + sl;
            pos1 += ItemAnimation.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 99);
         int pos2 = offset + 103 + fieldOffset2;
         pos2 += CameraSettings.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.wiggleWeights != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.pullbackConfig != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.id != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.animations != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.camera != null) {
         nullBits = (byte)(nullBits | 16);
      }

      buf.writeByte(nullBits);
      if (this.wiggleWeights != null) {
         this.wiggleWeights.serialize(buf);
      } else {
         buf.writeZero(40);
      }

      if (this.pullbackConfig != null) {
         this.pullbackConfig.serialize(buf);
      } else {
         buf.writeZero(49);
      }

      buf.writeByte(this.useFirstPersonOverride ? 1 : 0);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int animationsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int cameraOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.animations != null) {
         buf.setIntLE(animationsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.animations.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Animations", this.animations.size(), 4096000);
         }

         VarInt.write(buf, this.animations.size());

         for (Entry<String, ItemAnimation> e : this.animations.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(animationsOffsetSlot, -1);
      }

      if (this.camera != null) {
         buf.setIntLE(cameraOffsetSlot, buf.writerIndex() - varBlockStart);
         this.camera.serialize(buf);
      } else {
         buf.setIntLE(cameraOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 103;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.animations != null) {
         int animationsSize = 0;

         for (Entry<String, ItemAnimation> kvp : this.animations.entrySet()) {
            animationsSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.animations.size()) + animationsSize;
      }

      if (this.camera != null) {
         size += this.camera.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 103) {
         return ValidationResult.error("Buffer too small: expected at least 103 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 4) != 0) {
            int idOffset = buffer.getIntLE(offset + 91);
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 103 + idOffset;
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

         if ((nullBits & 8) != 0) {
            int animationsOffset = buffer.getIntLE(offset + 95);
            if (animationsOffset < 0) {
               return ValidationResult.error("Invalid offset for Animations");
            }

            int posx = offset + 103 + animationsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Animations");
            }

            int animationsCount = VarInt.peek(buffer, posx);
            if (animationsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Animations");
            }

            if (animationsCount > 4096000) {
               return ValidationResult.error("Animations exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < animationsCount; i++) {
               int keyLen = VarInt.peek(buffer, posx);
               if (keyLen < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLen > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               posx += VarInt.length(buffer, posx);
               posx += keyLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posx += ItemAnimation.computeBytesConsumed(buffer, posx);
            }
         }

         if ((nullBits & 16) != 0) {
            int cameraOffset = buffer.getIntLE(offset + 99);
            if (cameraOffset < 0) {
               return ValidationResult.error("Invalid offset for Camera");
            }

            int posxx = offset + 103 + cameraOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Camera");
            }

            ValidationResult cameraResult = CameraSettings.validateStructure(buffer, posxx);
            if (!cameraResult.isValid()) {
               return ValidationResult.error("Invalid Camera: " + cameraResult.error());
            }

            posxx += CameraSettings.computeBytesConsumed(buffer, posxx);
         }

         return ValidationResult.OK;
      }
   }

   public ItemPlayerAnimations clone() {
      ItemPlayerAnimations copy = new ItemPlayerAnimations();
      copy.id = this.id;
      if (this.animations != null) {
         Map<String, ItemAnimation> m = new HashMap<>();

         for (Entry<String, ItemAnimation> e : this.animations.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.animations = m;
      }

      copy.wiggleWeights = this.wiggleWeights != null ? this.wiggleWeights.clone() : null;
      copy.camera = this.camera != null ? this.camera.clone() : null;
      copy.pullbackConfig = this.pullbackConfig != null ? this.pullbackConfig.clone() : null;
      copy.useFirstPersonOverride = this.useFirstPersonOverride;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemPlayerAnimations other)
            ? false
            : Objects.equals(this.id, other.id)
               && Objects.equals(this.animations, other.animations)
               && Objects.equals(this.wiggleWeights, other.wiggleWeights)
               && Objects.equals(this.camera, other.camera)
               && Objects.equals(this.pullbackConfig, other.pullbackConfig)
               && this.useFirstPersonOverride == other.useFirstPersonOverride;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.animations, this.wiggleWeights, this.camera, this.pullbackConfig, this.useFirstPersonOverride);
   }
}
