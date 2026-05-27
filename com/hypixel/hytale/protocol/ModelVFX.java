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
import org.joml.Vector2fc;

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
   public Vector2fc animationRange;
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
   public Vector2fc noiseScale;
   @Nullable
   public Vector2fc noiseScrollSpeed;
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
      @Nullable Vector2fc animationRange,
      @Nonnull LoopOption loopOption,
      @Nonnull CurveType curveType,
      @Nullable Color highlightColor,
      float highlightThickness,
      boolean useBloomOnHighlight,
      boolean useProgessiveHighlight,
      @Nullable Vector2fc noiseScale,
      @Nullable Vector2fc noiseScrollSpeed,
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
      if (buf.readableBytes() - offset < 49) {
         throw ProtocolException.bufferTooSmall("ModelVFX", 49, buf.readableBytes() - offset);
      } else {
         ModelVFX obj = new ModelVFX();
         byte nullBits = buf.getByte(offset);
         obj.switchTo = SwitchTo.fromValue(buf.getByte(offset + 1));
         obj.effectDirection = EffectDirection.fromValue(buf.getByte(offset + 2));
         obj.animationDuration = buf.getFloatLE(offset + 3);
         if ((nullBits & 1) != 0) {
            obj.animationRange = PacketIO.readVector2f(buf, offset + 7);
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
            obj.noiseScale = PacketIO.readVector2f(buf, offset + 26);
         }

         if ((nullBits & 8) != 0) {
            obj.noiseScrollSpeed = PacketIO.readVector2f(buf, offset + 34);
         }

         if ((nullBits & 16) != 0) {
            obj.postColor = Color.deserialize(buf, offset + 42);
         }

         obj.postColorOpacity = buf.getFloatLE(offset + 45);
         int pos = offset + 49;
         if ((nullBits & 32) != 0) {
            int idLen = VarInt.peek(buf, pos);
            if (idLen < 0) {
               throw ProtocolException.invalidVarInt("Id");
            }

            int idVarLen = VarInt.size(idLen);
            if (idLen > 4096000) {
               throw ProtocolException.stringTooLong("Id", idLen, 4096000);
            }

            if (pos + idVarLen + idLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Id", pos + idVarLen + idLen, buf.readableBytes());
            }

            obj.id = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += idVarLen + idLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 49;
      if ((nullBits & 32) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 49L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + 49, 4096000, PacketIO.UTF8) : null;
   }

   public static SwitchTo getSwitchTo(MemorySegment mem) {
      return getSwitchTo(mem, 0);
   }

   public static SwitchTo getSwitchTo(MemorySegment mem, int offset) {
      return SwitchTo.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static EffectDirection getEffectDirection(MemorySegment mem) {
      return getEffectDirection(mem, 0);
   }

   public static EffectDirection getEffectDirection(MemorySegment mem, int offset) {
      return EffectDirection.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2)));
   }

   public static float getAnimationDuration(MemorySegment mem) {
      return getAnimationDuration(mem, 0);
   }

   public static float getAnimationDuration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 3));
   }

   @Nullable
   public static Vector2fc getAnimationRange(MemorySegment mem) {
      return getAnimationRange(mem, 0);
   }

   @Nullable
   public static Vector2fc getAnimationRange(MemorySegment mem, int offset) {
      return hasAnimationRange(mem, offset) ? PacketIO.readVector2f(mem, offset + 7) : null;
   }

   public static LoopOption getLoopOption(MemorySegment mem) {
      return getLoopOption(mem, 0);
   }

   public static LoopOption getLoopOption(MemorySegment mem, int offset) {
      return LoopOption.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 15)));
   }

   public static CurveType getCurveType(MemorySegment mem) {
      return getCurveType(mem, 0);
   }

   public static CurveType getCurveType(MemorySegment mem, int offset) {
      return CurveType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 16)));
   }

   @Nullable
   public static Color getHighlightColor(MemorySegment mem) {
      return getHighlightColor(mem, 0);
   }

   @Nullable
   public static Color getHighlightColor(MemorySegment mem, int offset) {
      return hasHighlightColor(mem, offset) ? Color.toObject(mem, offset + 17) : null;
   }

   public static float getHighlightThickness(MemorySegment mem) {
      return getHighlightThickness(mem, 0);
   }

   public static float getHighlightThickness(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 20));
   }

   public static boolean getUseBloomOnHighlight(MemorySegment mem) {
      return getUseBloomOnHighlight(mem, 0);
   }

   public static boolean getUseBloomOnHighlight(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 24));
   }

   public static boolean getUseProgessiveHighlight(MemorySegment mem) {
      return getUseProgessiveHighlight(mem, 0);
   }

   public static boolean getUseProgessiveHighlight(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 25));
   }

   @Nullable
   public static Vector2fc getNoiseScale(MemorySegment mem) {
      return getNoiseScale(mem, 0);
   }

   @Nullable
   public static Vector2fc getNoiseScale(MemorySegment mem, int offset) {
      return hasNoiseScale(mem, offset) ? PacketIO.readVector2f(mem, offset + 26) : null;
   }

   @Nullable
   public static Vector2fc getNoiseScrollSpeed(MemorySegment mem) {
      return getNoiseScrollSpeed(mem, 0);
   }

   @Nullable
   public static Vector2fc getNoiseScrollSpeed(MemorySegment mem, int offset) {
      return hasNoiseScrollSpeed(mem, offset) ? PacketIO.readVector2f(mem, offset + 34) : null;
   }

   @Nullable
   public static Color getPostColor(MemorySegment mem) {
      return getPostColor(mem, 0);
   }

   @Nullable
   public static Color getPostColor(MemorySegment mem, int offset) {
      return hasPostColor(mem, offset) ? Color.toObject(mem, offset + 42) : null;
   }

   public static float getPostColorOpacity(MemorySegment mem) {
      return getPostColorOpacity(mem, 0);
   }

   public static float getPostColorOpacity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 45));
   }

   public static boolean hasAnimationRange(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasHighlightColor(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasNoiseScale(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasNoiseScrollSpeed(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasPostColor(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static ModelVFX toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ModelVFX toObject(MemorySegment mem, int offset) {
      if (offset + 49 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ModelVFX", offset + 49, (int)mem.byteSize());
      } else {
         return new ModelVFX(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + 49, 4096000, PacketIO.UTF8) : null,
            SwitchTo.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            EffectDirection.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2))),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 3)),
            hasAnimationRange(mem, offset) ? PacketIO.readVector2f(mem, offset + 7) : null,
            LoopOption.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 15))),
            CurveType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 16))),
            hasHighlightColor(mem, offset) ? Color.toObject(mem, offset + 17) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 20)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 24)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 25)),
            hasNoiseScale(mem, offset) ? PacketIO.readVector2f(mem, offset + 26) : null,
            hasNoiseScrollSpeed(mem, offset) ? PacketIO.readVector2f(mem, offset + 34) : null,
            hasPostColor(mem, offset) ? Color.toObject(mem, offset + 42) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 45))
         );
      }
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
         PacketIO.writeVector2f(buf, this.animationRange);
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
         PacketIO.writeVector2f(buf, this.noiseScale);
      } else {
         buf.writeZero(8);
      }

      if (this.noiseScrollSpeed != null) {
         PacketIO.writeVector2f(buf, this.noiseScrollSpeed);
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.switchTo.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 2), (byte)this.effectDirection.getValue());
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 3), this.animationDuration);
      if (this.animationRange != null) {
         PacketIO.writeVector2f(mem, offset + 7, this.animationRange);
      } else {
         mem.asSlice(offset + 7, 8L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 15), (byte)this.loopOption.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 16), (byte)this.curveType.getValue());
      if (this.highlightColor != null) {
         this.highlightColor.serialize(mem, offset + 17);
      } else {
         mem.asSlice(offset + 17, 3L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 20), this.highlightThickness);
      mem.set(PacketIO.PROTO_BOOL, offset + 24, this.useBloomOnHighlight);
      mem.set(PacketIO.PROTO_BOOL, offset + 25, this.useProgessiveHighlight);
      if (this.noiseScale != null) {
         PacketIO.writeVector2f(mem, offset + 26, this.noiseScale);
      } else {
         mem.asSlice(offset + 26, 8L).fill((byte)0);
      }

      if (this.noiseScrollSpeed != null) {
         PacketIO.writeVector2f(mem, offset + 34, this.noiseScrollSpeed);
      } else {
         mem.asSlice(offset + 34, 8L).fill((byte)0);
      }

      if (this.postColor != null) {
         this.postColor.serialize(mem, offset + 42);
      } else {
         mem.asSlice(offset + 42, 3L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 45), this.postColorOpacity);
      int varOffset = offset + 49;
      if (this.id != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      }

      return varOffset - offset;
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
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 4) {
            return ValidationResult.error("Invalid SwitchTo value for SwitchTo");
         } else {
            v = buffer.getByte(offset + 2) & 255;
            if (v >= 5) {
               return ValidationResult.error("Invalid EffectDirection value for EffectDirection");
            } else {
               v = buffer.getByte(offset + 15) & 255;
               if (v >= 3) {
                  return ValidationResult.error("Invalid LoopOption value for LoopOption");
               } else {
                  v = buffer.getByte(offset + 16) & 255;
                  if (v >= 4) {
                     return ValidationResult.error("Invalid CurveType value for CurveType");
                  } else {
                     v = offset + 49;
                     if ((nullBits & 32) != 0) {
                        int idLen = VarInt.peek(buffer, v);
                        if (idLen < 0) {
                           return ValidationResult.error("Invalid string length for Id");
                        }

                        if (idLen > 4096000) {
                           return ValidationResult.error("Id exceeds max length 4096000");
                        }

                        v += VarInt.size(idLen);
                        v += idLen;
                        if (v > buffer.writerIndex()) {
                           return ValidationResult.error("Buffer overflow reading Id");
                        }
                     }

                     return ValidationResult.OK;
                  }
               }
            }
         }
      }
   }

   public ModelVFX clone() {
      ModelVFX copy = new ModelVFX();
      copy.id = this.id;
      copy.switchTo = this.switchTo;
      copy.effectDirection = this.effectDirection;
      copy.animationDuration = this.animationDuration;
      copy.animationRange = this.animationRange;
      copy.loopOption = this.loopOption;
      copy.curveType = this.curveType;
      copy.highlightColor = this.highlightColor != null ? this.highlightColor.clone() : null;
      copy.highlightThickness = this.highlightThickness;
      copy.useBloomOnHighlight = this.useBloomOnHighlight;
      copy.useProgessiveHighlight = this.useProgessiveHighlight;
      copy.noiseScale = this.noiseScale;
      copy.noiseScrollSpeed = this.noiseScrollSpeed;
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
