package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModelVFX {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 49;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 49;
   public static final int MAX_SIZE = 16384054;
   @Nullable
   public String id;
   @Nonnull
   public SwitchTo switchTo = SwitchTo.Disappear;
   @Nonnull
   public EffectDirection effectDirection = EffectDirection.None;
   public float animationDuration;
   @Nullable
   public Vector2f animationRange;
   @Nonnull
   public LoopOption loopOption = LoopOption.PlayOnce;
   @Nonnull
   public CurveType curveType = CurveType.Linear;
   @Nullable
   public Color highlightColor;
   public float highlightThickness;
   public boolean useBloomOnHighlight;
   public boolean useProgessiveHighlight;
   @Nullable
   public Vector2f noiseScale;
   @Nullable
   public Vector2f noiseScrollSpeed;
   @Nullable
   public Color postColor;
   public float postColorOpacity;

   public ModelVFX() {
   }

   public ModelVFX(
      @Nullable String id,
      @Nonnull SwitchTo switchTo,
      @Nonnull EffectDirection effectDirection,
      float animationDuration,
      @Nullable Vector2f animationRange,
      @Nonnull LoopOption loopOption,
      @Nonnull CurveType curveType,
      @Nullable Color highlightColor,
      float highlightThickness,
      boolean useBloomOnHighlight,
      boolean useProgessiveHighlight,
      @Nullable Vector2f noiseScale,
      @Nullable Vector2f noiseScrollSpeed,
      @Nullable Color postColor,
      float postColorOpacity
   ) {
      this.id = id;
      this.switchTo = switchTo;
      this.effectDirection = effectDirection;
      this.animationDuration = animationDuration;
      this.animationRange = animationRange;
      this.loopOption = loopOption;
      this.curveType = curveType;
      this.highlightColor = highlightColor;
      this.highlightThickness = highlightThickness;
      this.useBloomOnHighlight = useBloomOnHighlight;
      this.useProgessiveHighlight = useProgessiveHighlight;
      this.noiseScale = noiseScale;
      this.noiseScrollSpeed = noiseScrollSpeed;
      this.postColor = postColor;
      this.postColorOpacity = postColorOpacity;
   }

   public ModelVFX(@Nonnull ModelVFX other) {
      this.id = other.id;
      this.switchTo = other.switchTo;
      this.effectDirection = other.effectDirection;
      this.animationDuration = other.animationDuration;
      this.animationRange = other.animationRange;
      this.loopOption = other.loopOption;
      this.curveType = other.curveType;
      this.highlightColor = other.highlightColor;
      this.highlightThickness = other.highlightThickness;
      this.useBloomOnHighlight = other.useBloomOnHighlight;
      this.useProgessiveHighlight = other.useProgessiveHighlight;
      this.noiseScale = other.noiseScale;
      this.noiseScrollSpeed = other.noiseScrollSpeed;
      this.postColor = other.postColor;
      this.postColorOpacity = other.postColorOpacity;
   }

   @Nonnull
   public static ModelVFX deserialize(@Nonnull ByteBuf buf, int offset) {
      ModelVFX obj = new ModelVFX();
      byte nullBits = buf.getByte(offset);
      obj.switchTo = SwitchTo.fromValue(buf.getByte(offset + 1));
      obj.effectDirection = EffectDirection.fromValue(buf.getByte(offset + 2));
      obj.animationDuration = buf.getFloatLE(offset + 3);
      if ((nullBits & 1) != 0) {
         obj.animationRange = Vector2f.deserialize(buf, offset + 7);
      }

      obj.loopOption = LoopOption.fromValue(buf.getByte(offset + 15));
      obj.curveType = CurveType.fromValue(buf.getByte(offset + 16));
      if ((nullBits & 2) != 0) {
         obj.highlightColor = Color.deserialize(buf, offset + 17);
      }

      obj.highlightThickness = buf.getFloatLE(offset + 20);
      obj.useBloomOnHighlight = buf.getByte(offset + 24) != 0;
      obj.useProgessiveHighlight = buf.getByte(offset + 25) != 0;
      if ((nullBits & 4) != 0) {
         obj.noiseScale = Vector2f.deserialize(buf, offset + 26);
      }

      if ((nullBits & 8) != 0) {
         obj.noiseScrollSpeed = Vector2f.deserialize(buf, offset + 34);
      }

      if ((nullBits & 16) != 0) {
         obj.postColor = Color.deserialize(buf, offset + 42);
      }

      obj.postColorOpacity = buf.getFloatLE(offset + 45);
      int pos = offset + 49;
      if ((nullBits & 32) != 0) {
         int idLen = VarInt.peek(buf, pos);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         int idVarLen = VarInt.length(buf, pos);
         obj.id = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += idVarLen + idLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 49;
      if ((nullBits & 32) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.animationRange != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.highlightColor != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.noiseScale != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.noiseScrollSpeed != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.postColor != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.id != null) {
         nullBits = (byte)(nullBits | 32);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.switchTo.getValue());
      buf.writeByte(this.effectDirection.getValue());
      buf.writeFloatLE(this.animationDuration);
      if (this.animationRange != null) {
         this.animationRange.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      buf.writeByte(this.loopOption.getValue());
      buf.writeByte(this.curveType.getValue());
      if (this.highlightColor != null) {
         this.highlightColor.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      buf.writeFloatLE(this.highlightThickness);
      buf.writeByte(this.useBloomOnHighlight ? 1 : 0);
      buf.writeByte(this.useProgessiveHighlight ? 1 : 0);
      if (this.noiseScale != null) {
         this.noiseScale.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.noiseScrollSpeed != null) {
         this.noiseScrollSpeed.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.postColor != null) {
         this.postColor.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      buf.writeFloatLE(this.postColorOpacity);
      if (this.id != null) {
         PacketIO.writeVarString(buf, this.id, 4096000);
      }
   }

   public int computeSize() {
      int size = 49;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 49) {
         return ValidationResult.error("Buffer too small: expected at least 49 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 49;
         if ((nullBits & 32) != 0) {
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

         return ValidationResult.OK;
      }
   }

   public ModelVFX clone() {
      ModelVFX copy = new ModelVFX();
      copy.id = this.id;
      copy.switchTo = this.switchTo;
      copy.effectDirection = this.effectDirection;
      copy.animationDuration = this.animationDuration;
      copy.animationRange = this.animationRange != null ? this.animationRange.clone() : null;
      copy.loopOption = this.loopOption;
      copy.curveType = this.curveType;
      copy.highlightColor = this.highlightColor != null ? this.highlightColor.clone() : null;
      copy.highlightThickness = this.highlightThickness;
      copy.useBloomOnHighlight = this.useBloomOnHighlight;
      copy.useProgessiveHighlight = this.useProgessiveHighlight;
      copy.noiseScale = this.noiseScale != null ? this.noiseScale.clone() : null;
      copy.noiseScrollSpeed = this.noiseScrollSpeed != null ? this.noiseScrollSpeed.clone() : null;
      copy.postColor = this.postColor != null ? this.postColor.clone() : null;
      copy.postColorOpacity = this.postColorOpacity;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ModelVFX other)
            ? false
            : Objects.equals(this.id, other.id)
               && Objects.equals(this.switchTo, other.switchTo)
               && Objects.equals(this.effectDirection, other.effectDirection)
               && this.animationDuration == other.animationDuration
               && Objects.equals(this.animationRange, other.animationRange)
               && Objects.equals(this.loopOption, other.loopOption)
               && Objects.equals(this.curveType, other.curveType)
               && Objects.equals(this.highlightColor, other.highlightColor)
               && this.highlightThickness == other.highlightThickness
               && this.useBloomOnHighlight == other.useBloomOnHighlight
               && this.useProgessiveHighlight == other.useProgessiveHighlight
               && Objects.equals(this.noiseScale, other.noiseScale)
               && Objects.equals(this.noiseScrollSpeed, other.noiseScrollSpeed)
               && Objects.equals(this.postColor, other.postColor)
               && this.postColorOpacity == other.postColorOpacity;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.id,
         this.switchTo,
         this.effectDirection,
         this.animationDuration,
         this.animationRange,
         this.loopOption,
         this.curveType,
         this.highlightColor,
         this.highlightThickness,
         this.useBloomOnHighlight,
         this.useProgessiveHighlight,
         this.noiseScale,
         this.noiseScrollSpeed,
         this.postColor,
         this.postColorOpacity
      );
   }
}
