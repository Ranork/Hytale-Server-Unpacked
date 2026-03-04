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

public class Particle {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 133;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 141;
   public static final int MAX_SIZE = 270336151;
   @Nullable
   public String texturePath;
   @Nullable
   public Size frameSize;
   @Nonnull
   public ParticleUVOption uvOption = ParticleUVOption.None;
   @Nonnull
   public ParticleScaleRatioConstraint scaleRatioConstraint = ParticleScaleRatioConstraint.OneToOne;
   @Nonnull
   public SoftParticle softParticles = SoftParticle.Enable;
   public float softParticlesFadeFactor;
   public boolean useSpriteBlending;
   @Nullable
   public ParticleAnimationFrame initialAnimationFrame;
   @Nullable
   public ParticleAnimationFrame collisionAnimationFrame;
   @Nullable
   public Map<Integer, ParticleAnimationFrame> animationFrames;

   public Particle() {
   }

   public Particle(
      @Nullable String texturePath,
      @Nullable Size frameSize,
      @Nonnull ParticleUVOption uvOption,
      @Nonnull ParticleScaleRatioConstraint scaleRatioConstraint,
      @Nonnull SoftParticle softParticles,
      float softParticlesFadeFactor,
      boolean useSpriteBlending,
      @Nullable ParticleAnimationFrame initialAnimationFrame,
      @Nullable ParticleAnimationFrame collisionAnimationFrame,
      @Nullable Map<Integer, ParticleAnimationFrame> animationFrames
   ) {
      this.texturePath = texturePath;
      this.frameSize = frameSize;
      this.uvOption = uvOption;
      this.scaleRatioConstraint = scaleRatioConstraint;
      this.softParticles = softParticles;
      this.softParticlesFadeFactor = softParticlesFadeFactor;
      this.useSpriteBlending = useSpriteBlending;
      this.initialAnimationFrame = initialAnimationFrame;
      this.collisionAnimationFrame = collisionAnimationFrame;
      this.animationFrames = animationFrames;
   }

   public Particle(@Nonnull Particle other) {
      this.texturePath = other.texturePath;
      this.frameSize = other.frameSize;
      this.uvOption = other.uvOption;
      this.scaleRatioConstraint = other.scaleRatioConstraint;
      this.softParticles = other.softParticles;
      this.softParticlesFadeFactor = other.softParticlesFadeFactor;
      this.useSpriteBlending = other.useSpriteBlending;
      this.initialAnimationFrame = other.initialAnimationFrame;
      this.collisionAnimationFrame = other.collisionAnimationFrame;
      this.animationFrames = other.animationFrames;
   }

   @Nonnull
   public static Particle deserialize(@Nonnull ByteBuf buf, int offset) {
      Particle obj = new Particle();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.frameSize = Size.deserialize(buf, offset + 1);
      }

      obj.uvOption = ParticleUVOption.fromValue(buf.getByte(offset + 9));
      obj.scaleRatioConstraint = ParticleScaleRatioConstraint.fromValue(buf.getByte(offset + 10));
      obj.softParticles = SoftParticle.fromValue(buf.getByte(offset + 11));
      obj.softParticlesFadeFactor = buf.getFloatLE(offset + 12);
      obj.useSpriteBlending = buf.getByte(offset + 16) != 0;
      if ((nullBits & 2) != 0) {
         obj.initialAnimationFrame = ParticleAnimationFrame.deserialize(buf, offset + 17);
      }

      if ((nullBits & 4) != 0) {
         obj.collisionAnimationFrame = ParticleAnimationFrame.deserialize(buf, offset + 75);
      }

      if ((nullBits & 8) != 0) {
         int varPos0 = offset + 141 + buf.getIntLE(offset + 133);
         int texturePathLen = VarInt.peek(buf, varPos0);
         if (texturePathLen < 0) {
            throw ProtocolException.negativeLength("TexturePath", texturePathLen);
         }

         if (texturePathLen > 4096000) {
            throw ProtocolException.stringTooLong("TexturePath", texturePathLen, 4096000);
         }

         obj.texturePath = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 16) != 0) {
         int varPos1 = offset + 141 + buf.getIntLE(offset + 137);
         int animationFramesCount = VarInt.peek(buf, varPos1);
         if (animationFramesCount < 0) {
            throw ProtocolException.negativeLength("AnimationFrames", animationFramesCount);
         }

         if (animationFramesCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("AnimationFrames", animationFramesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         obj.animationFrames = new HashMap<>(animationFramesCount);
         int dictPos = varPos1 + varIntLen;

         for (int i = 0; i < animationFramesCount; i++) {
            int key = buf.getIntLE(dictPos);
            dictPos += 4;
            ParticleAnimationFrame val = ParticleAnimationFrame.deserialize(buf, dictPos);
            dictPos += ParticleAnimationFrame.computeBytesConsumed(buf, dictPos);
            if (obj.animationFrames.put(key, val) != null) {
               throw ProtocolException.duplicateKey("animationFrames", key);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 141;
      if ((nullBits & 8) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 133);
         int pos0 = offset + 141 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 137);
         int pos1 = offset + 141 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < dictLen; i++) {
            pos1 += 4;
            pos1 += ParticleAnimationFrame.computeBytesConsumed(buf, pos1);
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
      if (this.frameSize != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.initialAnimationFrame != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.collisionAnimationFrame != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.texturePath != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.animationFrames != null) {
         nullBits = (byte)(nullBits | 16);
      }

      buf.writeByte(nullBits);
      if (this.frameSize != null) {
         this.frameSize.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      buf.writeByte(this.uvOption.getValue());
      buf.writeByte(this.scaleRatioConstraint.getValue());
      buf.writeByte(this.softParticles.getValue());
      buf.writeFloatLE(this.softParticlesFadeFactor);
      buf.writeByte(this.useSpriteBlending ? 1 : 0);
      if (this.initialAnimationFrame != null) {
         this.initialAnimationFrame.serialize(buf);
      } else {
         buf.writeZero(58);
      }

      if (this.collisionAnimationFrame != null) {
         this.collisionAnimationFrame.serialize(buf);
      } else {
         buf.writeZero(58);
      }

      int texturePathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int animationFramesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.texturePath != null) {
         buf.setIntLE(texturePathOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.texturePath, 4096000);
      } else {
         buf.setIntLE(texturePathOffsetSlot, -1);
      }

      if (this.animationFrames != null) {
         buf.setIntLE(animationFramesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.animationFrames.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("AnimationFrames", this.animationFrames.size(), 4096000);
         }

         VarInt.write(buf, this.animationFrames.size());

         for (Entry<Integer, ParticleAnimationFrame> e : this.animationFrames.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(animationFramesOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 141;
      if (this.texturePath != null) {
         size += PacketIO.stringSize(this.texturePath);
      }

      if (this.animationFrames != null) {
         size += VarInt.size(this.animationFrames.size()) + this.animationFrames.size() * 62;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 141) {
         return ValidationResult.error("Buffer too small: expected at least 141 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 8) != 0) {
            int texturePathOffset = buffer.getIntLE(offset + 133);
            if (texturePathOffset < 0) {
               return ValidationResult.error("Invalid offset for TexturePath");
            }

            int pos = offset + 141 + texturePathOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for TexturePath");
            }

            int texturePathLen = VarInt.peek(buffer, pos);
            if (texturePathLen < 0) {
               return ValidationResult.error("Invalid string length for TexturePath");
            }

            if (texturePathLen > 4096000) {
               return ValidationResult.error("TexturePath exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += texturePathLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading TexturePath");
            }
         }

         if ((nullBits & 16) != 0) {
            int animationFramesOffset = buffer.getIntLE(offset + 137);
            if (animationFramesOffset < 0) {
               return ValidationResult.error("Invalid offset for AnimationFrames");
            }

            int posx = offset + 141 + animationFramesOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for AnimationFrames");
            }

            int animationFramesCount = VarInt.peek(buffer, posx);
            if (animationFramesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for AnimationFrames");
            }

            if (animationFramesCount > 4096000) {
               return ValidationResult.error("AnimationFrames exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < animationFramesCount; i++) {
               posx += 4;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posx += 58;
            }
         }

         return ValidationResult.OK;
      }
   }

   public Particle clone() {
      Particle copy = new Particle();
      copy.texturePath = this.texturePath;
      copy.frameSize = this.frameSize != null ? this.frameSize.clone() : null;
      copy.uvOption = this.uvOption;
      copy.scaleRatioConstraint = this.scaleRatioConstraint;
      copy.softParticles = this.softParticles;
      copy.softParticlesFadeFactor = this.softParticlesFadeFactor;
      copy.useSpriteBlending = this.useSpriteBlending;
      copy.initialAnimationFrame = this.initialAnimationFrame != null ? this.initialAnimationFrame.clone() : null;
      copy.collisionAnimationFrame = this.collisionAnimationFrame != null ? this.collisionAnimationFrame.clone() : null;
      if (this.animationFrames != null) {
         Map<Integer, ParticleAnimationFrame> m = new HashMap<>();

         for (Entry<Integer, ParticleAnimationFrame> e : this.animationFrames.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.animationFrames = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Particle other)
            ? false
            : Objects.equals(this.texturePath, other.texturePath)
               && Objects.equals(this.frameSize, other.frameSize)
               && Objects.equals(this.uvOption, other.uvOption)
               && Objects.equals(this.scaleRatioConstraint, other.scaleRatioConstraint)
               && Objects.equals(this.softParticles, other.softParticles)
               && this.softParticlesFadeFactor == other.softParticlesFadeFactor
               && this.useSpriteBlending == other.useSpriteBlending
               && Objects.equals(this.initialAnimationFrame, other.initialAnimationFrame)
               && Objects.equals(this.collisionAnimationFrame, other.collisionAnimationFrame)
               && Objects.equals(this.animationFrames, other.animationFrames);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.texturePath,
         this.frameSize,
         this.uvOption,
         this.scaleRatioConstraint,
         this.softParticles,
         this.softParticlesFadeFactor,
         this.useSpriteBlending,
         this.initialAnimationFrame,
         this.collisionAnimationFrame,
         this.animationFrames
      );
   }
}
