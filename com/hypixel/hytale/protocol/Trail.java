package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Trail {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 61;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 69;
   public static final int MAX_SIZE = 32768079;
   @Nullable
   public String id;
   @Nullable
   public String texture;
   public int lifeSpan;
   public float roll;
   @Nullable
   public Edge start;
   @Nullable
   public Edge end;
   public float lightInfluence;
   @Nonnull
   public FXRenderMode renderMode = FXRenderMode.BlendLinear;
   @Nullable
   public IntersectionHighlight intersectionHighlight;
   public boolean smooth;
   @Nullable
   public Vector2i frameSize;
   @Nullable
   public Range frameRange;
   public int frameLifeSpan;

   public Trail() {
   }

   public Trail(
      @Nullable String id,
      @Nullable String texture,
      int lifeSpan,
      float roll,
      @Nullable Edge start,
      @Nullable Edge end,
      float lightInfluence,
      @Nonnull FXRenderMode renderMode,
      @Nullable IntersectionHighlight intersectionHighlight,
      boolean smooth,
      @Nullable Vector2i frameSize,
      @Nullable Range frameRange,
      int frameLifeSpan
   ) {
      this.id = id;
      this.texture = texture;
      this.lifeSpan = lifeSpan;
      this.roll = roll;
      this.start = start;
      this.end = end;
      this.lightInfluence = lightInfluence;
      this.renderMode = renderMode;
      this.intersectionHighlight = intersectionHighlight;
      this.smooth = smooth;
      this.frameSize = frameSize;
      this.frameRange = frameRange;
      this.frameLifeSpan = frameLifeSpan;
   }

   public Trail(@Nonnull Trail other) {
      this.id = other.id;
      this.texture = other.texture;
      this.lifeSpan = other.lifeSpan;
      this.roll = other.roll;
      this.start = other.start;
      this.end = other.end;
      this.lightInfluence = other.lightInfluence;
      this.renderMode = other.renderMode;
      this.intersectionHighlight = other.intersectionHighlight;
      this.smooth = other.smooth;
      this.frameSize = other.frameSize;
      this.frameRange = other.frameRange;
      this.frameLifeSpan = other.frameLifeSpan;
   }

   @Nonnull
   public static Trail deserialize(@Nonnull ByteBuf buf, int offset) {
      Trail obj = new Trail();
      byte nullBits = buf.getByte(offset);
      obj.lifeSpan = buf.getIntLE(offset + 1);
      obj.roll = buf.getFloatLE(offset + 5);
      if ((nullBits & 1) != 0) {
         obj.start = Edge.deserialize(buf, offset + 9);
      }

      if ((nullBits & 2) != 0) {
         obj.end = Edge.deserialize(buf, offset + 18);
      }

      obj.lightInfluence = buf.getFloatLE(offset + 27);
      obj.renderMode = FXRenderMode.fromValue(buf.getByte(offset + 31));
      if ((nullBits & 4) != 0) {
         obj.intersectionHighlight = IntersectionHighlight.deserialize(buf, offset + 32);
      }

      obj.smooth = buf.getByte(offset + 40) != 0;
      if ((nullBits & 8) != 0) {
         obj.frameSize = Vector2i.deserialize(buf, offset + 41);
      }

      if ((nullBits & 16) != 0) {
         obj.frameRange = Range.deserialize(buf, offset + 49);
      }

      obj.frameLifeSpan = buf.getIntLE(offset + 57);
      if ((nullBits & 32) != 0) {
         int varPos0 = offset + 69 + buf.getIntLE(offset + 61);
         int idLen = VarInt.peek(buf, varPos0);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 64) != 0) {
         int varPos1 = offset + 69 + buf.getIntLE(offset + 65);
         int textureLen = VarInt.peek(buf, varPos1);
         if (textureLen < 0) {
            throw ProtocolException.negativeLength("Texture", textureLen);
         }

         if (textureLen > 4096000) {
            throw ProtocolException.stringTooLong("Texture", textureLen, 4096000);
         }

         obj.texture = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 69;
      if ((nullBits & 32) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 61);
         int pos0 = offset + 69 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 64) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 65);
         int pos1 = offset + 69 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.start != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.end != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.intersectionHighlight != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.frameSize != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.frameRange != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.id != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.texture != null) {
         nullBits = (byte)(nullBits | 64);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.lifeSpan);
      buf.writeFloatLE(this.roll);
      if (this.start != null) {
         this.start.serialize(buf);
      } else {
         buf.writeZero(9);
      }

      if (this.end != null) {
         this.end.serialize(buf);
      } else {
         buf.writeZero(9);
      }

      buf.writeFloatLE(this.lightInfluence);
      buf.writeByte(this.renderMode.getValue());
      if (this.intersectionHighlight != null) {
         this.intersectionHighlight.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      buf.writeByte(this.smooth ? 1 : 0);
      if (this.frameSize != null) {
         this.frameSize.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.frameRange != null) {
         this.frameRange.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      buf.writeIntLE(this.frameLifeSpan);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int textureOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.texture != null) {
         buf.setIntLE(textureOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.texture, 4096000);
      } else {
         buf.setIntLE(textureOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 69;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.texture != null) {
         size += PacketIO.stringSize(this.texture);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 69) {
         return ValidationResult.error("Buffer too small: expected at least 69 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 32) != 0) {
            int idOffset = buffer.getIntLE(offset + 61);
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 69 + idOffset;
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

         if ((nullBits & 64) != 0) {
            int textureOffset = buffer.getIntLE(offset + 65);
            if (textureOffset < 0) {
               return ValidationResult.error("Invalid offset for Texture");
            }

            int posx = offset + 69 + textureOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Texture");
            }

            int textureLen = VarInt.peek(buffer, posx);
            if (textureLen < 0) {
               return ValidationResult.error("Invalid string length for Texture");
            }

            if (textureLen > 4096000) {
               return ValidationResult.error("Texture exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += textureLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Texture");
            }
         }

         return ValidationResult.OK;
      }
   }

   public Trail clone() {
      Trail copy = new Trail();
      copy.id = this.id;
      copy.texture = this.texture;
      copy.lifeSpan = this.lifeSpan;
      copy.roll = this.roll;
      copy.start = this.start != null ? this.start.clone() : null;
      copy.end = this.end != null ? this.end.clone() : null;
      copy.lightInfluence = this.lightInfluence;
      copy.renderMode = this.renderMode;
      copy.intersectionHighlight = this.intersectionHighlight != null ? this.intersectionHighlight.clone() : null;
      copy.smooth = this.smooth;
      copy.frameSize = this.frameSize != null ? this.frameSize.clone() : null;
      copy.frameRange = this.frameRange != null ? this.frameRange.clone() : null;
      copy.frameLifeSpan = this.frameLifeSpan;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Trail other)
            ? false
            : Objects.equals(this.id, other.id)
               && Objects.equals(this.texture, other.texture)
               && this.lifeSpan == other.lifeSpan
               && this.roll == other.roll
               && Objects.equals(this.start, other.start)
               && Objects.equals(this.end, other.end)
               && this.lightInfluence == other.lightInfluence
               && Objects.equals(this.renderMode, other.renderMode)
               && Objects.equals(this.intersectionHighlight, other.intersectionHighlight)
               && this.smooth == other.smooth
               && Objects.equals(this.frameSize, other.frameSize)
               && Objects.equals(this.frameRange, other.frameRange)
               && this.frameLifeSpan == other.frameLifeSpan;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.id,
         this.texture,
         this.lifeSpan,
         this.roll,
         this.start,
         this.end,
         this.lightInfluence,
         this.renderMode,
         this.intersectionHighlight,
         this.smooth,
         this.frameSize,
         this.frameRange,
         this.frameLifeSpan
      );
   }
}
