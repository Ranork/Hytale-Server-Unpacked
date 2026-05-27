package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 69) {
         throw ProtocolException.bufferTooSmall("Trail", 69, buf.readableBytes() - offset);
      } else {
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
            int varPosBase0 = buf.getIntLE(offset + 61);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 69) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 69 + varPosBase0;
            int idLen = VarInt.peek(buf, varPos0);
            if (idLen < 0) {
               throw ProtocolException.invalidVarInt("Id");
            }

            int idVarIntLen = VarInt.size(idLen);
            if (idLen > 4096000) {
               throw ProtocolException.stringTooLong("Id", idLen, 4096000);
            }

            if (varPos0 + idVarIntLen + idLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Id", varPos0 + idVarIntLen + idLen, buf.readableBytes());
            }

            obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 64) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 65);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 69) {
               throw ProtocolException.invalidOffset("Texture", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 69 + varPosBase1;
            int textureLen = VarInt.peek(buf, varPos1);
            if (textureLen < 0) {
               throw ProtocolException.invalidVarInt("Texture");
            }

            int textureVarIntLen = VarInt.size(textureLen);
            if (textureLen > 4096000) {
               throw ProtocolException.stringTooLong("Texture", textureLen, 4096000);
            }

            if (varPos1 + textureVarIntLen + textureLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Texture", varPos1 + textureVarIntLen + textureLen, buf.readableBytes());
            }

            obj.texture = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 69;
      if ((nullBits & 32) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 61);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 69) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 69 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 64) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 65);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 69) {
            throw ProtocolException.invalidOffset("Texture", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 69 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 69L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 61, 69, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static String getTexture(MemorySegment mem) {
      return getTexture(mem, 0);
   }

   @Nullable
   public static String getTexture(MemorySegment mem, int offset) {
      return hasTexture(mem, offset)
         ? PacketIO.readVarString("Texture", mem, offset + getValidatedOffset(mem, offset, 65, 69, "Texture"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static int getLifeSpan(MemorySegment mem) {
      return getLifeSpan(mem, 0);
   }

   public static int getLifeSpan(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static float getRoll(MemorySegment mem) {
      return getRoll(mem, 0);
   }

   public static float getRoll(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5));
   }

   @Nullable
   public static Edge getStart(MemorySegment mem) {
      return getStart(mem, 0);
   }

   @Nullable
   public static Edge getStart(MemorySegment mem, int offset) {
      return hasStart(mem, offset) ? Edge.toObject(mem, offset + 9) : null;
   }

   @Nullable
   public static Edge getEnd(MemorySegment mem) {
      return getEnd(mem, 0);
   }

   @Nullable
   public static Edge getEnd(MemorySegment mem, int offset) {
      return hasEnd(mem, offset) ? Edge.toObject(mem, offset + 18) : null;
   }

   public static float getLightInfluence(MemorySegment mem) {
      return getLightInfluence(mem, 0);
   }

   public static float getLightInfluence(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 27));
   }

   public static FXRenderMode getRenderMode(MemorySegment mem) {
      return getRenderMode(mem, 0);
   }

   public static FXRenderMode getRenderMode(MemorySegment mem, int offset) {
      return FXRenderMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 31)));
   }

   @Nullable
   public static IntersectionHighlight getIntersectionHighlight(MemorySegment mem) {
      return getIntersectionHighlight(mem, 0);
   }

   @Nullable
   public static IntersectionHighlight getIntersectionHighlight(MemorySegment mem, int offset) {
      return hasIntersectionHighlight(mem, offset) ? IntersectionHighlight.toObject(mem, offset + 32) : null;
   }

   public static boolean getSmooth(MemorySegment mem) {
      return getSmooth(mem, 0);
   }

   public static boolean getSmooth(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 40));
   }

   @Nullable
   public static Vector2i getFrameSize(MemorySegment mem) {
      return getFrameSize(mem, 0);
   }

   @Nullable
   public static Vector2i getFrameSize(MemorySegment mem, int offset) {
      return hasFrameSize(mem, offset) ? Vector2i.toObject(mem, offset + 41) : null;
   }

   @Nullable
   public static Range getFrameRange(MemorySegment mem) {
      return getFrameRange(mem, 0);
   }

   @Nullable
   public static Range getFrameRange(MemorySegment mem, int offset) {
      return hasFrameRange(mem, offset) ? Range.toObject(mem, offset + 49) : null;
   }

   public static int getFrameLifeSpan(MemorySegment mem) {
      return getFrameLifeSpan(mem, 0);
   }

   public static int getFrameLifeSpan(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 57));
   }

   public static boolean hasStart(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasEnd(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasIntersectionHighlight(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasFrameSize(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasFrameRange(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasTexture(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static Trail toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Trail toObject(MemorySegment mem, int offset) {
      if (offset + 69 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Trail", offset + 69, (int)mem.byteSize());
      } else {
         return new Trail(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 61, 69, "Id"), 4096000, PacketIO.UTF8) : null,
            hasTexture(mem, offset)
               ? PacketIO.readVarString("Texture", mem, offset + getValidatedOffset(mem, offset, 65, 69, "Texture"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5)),
            hasStart(mem, offset) ? Edge.toObject(mem, offset + 9) : null,
            hasEnd(mem, offset) ? Edge.toObject(mem, offset + 18) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 27)),
            FXRenderMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 31))),
            hasIntersectionHighlight(mem, offset) ? IntersectionHighlight.toObject(mem, offset + 32) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 40)),
            hasFrameSize(mem, offset) ? Vector2i.toObject(mem, offset + 41) : null,
            hasFrameRange(mem, offset) ? Range.toObject(mem, offset + 49) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 57))
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.lifeSpan);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 5), this.roll);
      if (this.start != null) {
         this.start.serialize(mem, offset + 9);
      } else {
         mem.asSlice(offset + 9, 9L).fill((byte)0);
      }

      if (this.end != null) {
         this.end.serialize(mem, offset + 18);
      } else {
         mem.asSlice(offset + 18, 9L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 27), this.lightInfluence);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 31), (byte)this.renderMode.getValue());
      if (this.intersectionHighlight != null) {
         this.intersectionHighlight.serialize(mem, offset + 32);
      } else {
         mem.asSlice(offset + 32, 8L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BOOL, offset + 40, this.smooth);
      if (this.frameSize != null) {
         this.frameSize.serialize(mem, offset + 41);
      } else {
         mem.asSlice(offset + 41, 8L).fill((byte)0);
      }

      if (this.frameRange != null) {
         this.frameRange.serialize(mem, offset + 49);
      } else {
         mem.asSlice(offset + 49, 8L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_INT, (long)(offset + 57), this.frameLifeSpan);
      int varOffset = offset + 69;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 61), varOffset - offset - 69);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 61), -1);
      }

      if (this.texture != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 65), varOffset - offset - 69);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.texture, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 65), -1);
      }

      return varOffset - offset;
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
         int v = buffer.getByte(offset + 31) & 255;
         if (v >= 4) {
            return ValidationResult.error("Invalid FXRenderMode value for RenderMode");
         } else {
            if ((nullBits & 32) != 0) {
               v = buffer.getIntLE(offset + 61);
               if (v < 0 || v > buffer.writerIndex() - offset - 69) {
                  return ValidationResult.error("Invalid offset for Id");
               }

               int pos = offset + 69 + v;
               int idLen = VarInt.peek(buffer, pos);
               if (idLen < 0) {
                  return ValidationResult.error("Invalid string length for Id");
               }

               if (idLen > 4096000) {
                  return ValidationResult.error("Id exceeds max length 4096000");
               }

               pos += VarInt.size(idLen);
               pos += idLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Id");
               }
            }

            if ((nullBits & 64) != 0) {
               v = buffer.getIntLE(offset + 65);
               if (v < 0 || v > buffer.writerIndex() - offset - 69) {
                  return ValidationResult.error("Invalid offset for Texture");
               }

               int posx = offset + 69 + v;
               int textureLen = VarInt.peek(buffer, posx);
               if (textureLen < 0) {
                  return ValidationResult.error("Invalid string length for Texture");
               }

               if (textureLen > 4096000) {
                  return ValidationResult.error("Texture exceeds max length 4096000");
               }

               posx += VarInt.size(textureLen);
               posx += textureLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Texture");
               }
            }

            return ValidationResult.OK;
         }
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
