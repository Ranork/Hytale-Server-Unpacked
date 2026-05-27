package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector2fc;

public class EntityUIComponent {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 51;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 51;
   public static final int MAX_SIZE = 135168056;
   @Nonnull
   public EntityUIType type = EntityUIType.EntityStat;
   @Nonnull
   public Vector2fc hitboxOffset = PacketIO.ZERO_VECTOR2;
   public boolean unknown;
   public int entityStatIndex;
   @Nullable
   public RangeVector2f combatTextRandomPositionOffsetRange;
   public float combatTextViewportMargin;
   public float combatTextDuration;
   public float combatTextHitAngleModifierStrength;
   public float combatTextFontSize;
   @Nullable
   public Color combatTextColor;
   @Nullable
   public CombatTextEntityUIComponentAnimationEvent[] combatTextAnimationEvents;

   public EntityUIComponent() {
   }

   public EntityUIComponent(
      @Nonnull EntityUIType type,
      @Nonnull Vector2fc hitboxOffset,
      boolean unknown,
      int entityStatIndex,
      @Nullable RangeVector2f combatTextRandomPositionOffsetRange,
      float combatTextViewportMargin,
      float combatTextDuration,
      float combatTextHitAngleModifierStrength,
      float combatTextFontSize,
      @Nullable Color combatTextColor,
      @Nullable CombatTextEntityUIComponentAnimationEvent[] combatTextAnimationEvents
   ) {
      this.type = type;
      this.hitboxOffset = hitboxOffset;
      this.unknown = unknown;
      this.entityStatIndex = entityStatIndex;
      this.combatTextRandomPositionOffsetRange = combatTextRandomPositionOffsetRange;
      this.combatTextViewportMargin = combatTextViewportMargin;
      this.combatTextDuration = combatTextDuration;
      this.combatTextHitAngleModifierStrength = combatTextHitAngleModifierStrength;
      this.combatTextFontSize = combatTextFontSize;
      this.combatTextColor = combatTextColor;
      this.combatTextAnimationEvents = combatTextAnimationEvents;
   }

   public EntityUIComponent(@Nonnull EntityUIComponent other) {
      this.type = other.type;
      this.hitboxOffset = other.hitboxOffset;
      this.unknown = other.unknown;
      this.entityStatIndex = other.entityStatIndex;
      this.combatTextRandomPositionOffsetRange = other.combatTextRandomPositionOffsetRange;
      this.combatTextViewportMargin = other.combatTextViewportMargin;
      this.combatTextDuration = other.combatTextDuration;
      this.combatTextHitAngleModifierStrength = other.combatTextHitAngleModifierStrength;
      this.combatTextFontSize = other.combatTextFontSize;
      this.combatTextColor = other.combatTextColor;
      this.combatTextAnimationEvents = other.combatTextAnimationEvents;
   }

   @Nonnull
   public static EntityUIComponent deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 51) {
         throw ProtocolException.bufferTooSmall("EntityUIComponent", 51, buf.readableBytes() - offset);
      } else {
         EntityUIComponent obj = new EntityUIComponent();
         byte nullBits = buf.getByte(offset);
         obj.type = EntityUIType.fromValue(buf.getByte(offset + 1));
         obj.hitboxOffset = PacketIO.readVector2f(buf, offset + 2);
         obj.unknown = buf.getByte(offset + 10) != 0;
         obj.entityStatIndex = buf.getIntLE(offset + 11);
         if ((nullBits & 1) != 0) {
            obj.combatTextRandomPositionOffsetRange = RangeVector2f.deserialize(buf, offset + 15);
         }

         obj.combatTextViewportMargin = buf.getFloatLE(offset + 32);
         obj.combatTextDuration = buf.getFloatLE(offset + 36);
         obj.combatTextHitAngleModifierStrength = buf.getFloatLE(offset + 40);
         obj.combatTextFontSize = buf.getFloatLE(offset + 44);
         if ((nullBits & 2) != 0) {
            obj.combatTextColor = Color.deserialize(buf, offset + 48);
         }

         int pos = offset + 51;
         if ((nullBits & 4) != 0) {
            int combatTextAnimationEventsCount = VarInt.peek(buf, pos);
            if (combatTextAnimationEventsCount < 0) {
               throw ProtocolException.invalidVarInt("CombatTextAnimationEvents");
            }

            int combatTextAnimationEventsVarLen = VarInt.size(combatTextAnimationEventsCount);
            if (combatTextAnimationEventsCount > 4096000) {
               throw ProtocolException.arrayTooLong("CombatTextAnimationEvents", combatTextAnimationEventsCount, 4096000);
            }

            if (pos + combatTextAnimationEventsVarLen + combatTextAnimationEventsCount * 33L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall(
                  "CombatTextAnimationEvents", pos + combatTextAnimationEventsVarLen + combatTextAnimationEventsCount * 33, buf.readableBytes()
               );
            }

            pos += combatTextAnimationEventsVarLen;
            obj.combatTextAnimationEvents = new CombatTextEntityUIComponentAnimationEvent[combatTextAnimationEventsCount];

            for (int i = 0; i < combatTextAnimationEventsCount; i++) {
               obj.combatTextAnimationEvents[i] = CombatTextEntityUIComponentAnimationEvent.deserialize(buf, pos);
               pos += CombatTextEntityUIComponentAnimationEvent.computeBytesConsumed(buf, pos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 51;
      if ((nullBits & 4) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos += CombatTextEntityUIComponentAnimationEvent.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 51L;
   }

   public static EntityUIType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static EntityUIType getType(MemorySegment mem, int offset) {
      return EntityUIType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static Vector2fc getHitboxOffset(MemorySegment mem) {
      return getHitboxOffset(mem, 0);
   }

   public static Vector2fc getHitboxOffset(MemorySegment mem, int offset) {
      return PacketIO.readVector2f(mem, offset + 2);
   }

   public static boolean getUnknown(MemorySegment mem) {
      return getUnknown(mem, 0);
   }

   public static boolean getUnknown(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 10));
   }

   public static int getEntityStatIndex(MemorySegment mem) {
      return getEntityStatIndex(mem, 0);
   }

   public static int getEntityStatIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 11));
   }

   @Nullable
   public static RangeVector2f getCombatTextRandomPositionOffsetRange(MemorySegment mem) {
      return getCombatTextRandomPositionOffsetRange(mem, 0);
   }

   @Nullable
   public static RangeVector2f getCombatTextRandomPositionOffsetRange(MemorySegment mem, int offset) {
      return hasCombatTextRandomPositionOffsetRange(mem, offset) ? RangeVector2f.toObject(mem, offset + 15) : null;
   }

   public static float getCombatTextViewportMargin(MemorySegment mem) {
      return getCombatTextViewportMargin(mem, 0);
   }

   public static float getCombatTextViewportMargin(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 32));
   }

   public static float getCombatTextDuration(MemorySegment mem) {
      return getCombatTextDuration(mem, 0);
   }

   public static float getCombatTextDuration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 36));
   }

   public static float getCombatTextHitAngleModifierStrength(MemorySegment mem) {
      return getCombatTextHitAngleModifierStrength(mem, 0);
   }

   public static float getCombatTextHitAngleModifierStrength(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 40));
   }

   public static float getCombatTextFontSize(MemorySegment mem) {
      return getCombatTextFontSize(mem, 0);
   }

   public static float getCombatTextFontSize(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 44));
   }

   @Nullable
   public static Color getCombatTextColor(MemorySegment mem) {
      return getCombatTextColor(mem, 0);
   }

   @Nullable
   public static Color getCombatTextColor(MemorySegment mem, int offset) {
      return hasCombatTextColor(mem, offset) ? Color.toObject(mem, offset + 48) : null;
   }

   @Nullable
   public static CombatTextEntityUIComponentAnimationEvent[] getCombatTextAnimationEvents(MemorySegment mem) {
      return getCombatTextAnimationEvents(mem, 0);
   }

   @Nullable
   public static CombatTextEntityUIComponentAnimationEvent[] getCombatTextAnimationEvents(MemorySegment mem, int offset) {
      if (!hasCombatTextAnimationEvents(mem, offset)) {
         return null;
      } else {
         int off = offset + 51;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("CombatTextAnimationEvents", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("CombatTextAnimationEvents", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 33L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("CombatTextAnimationEvents", off + lenOffset + len * 33, (int)mem.byteSize());
            } else {
               off += lenOffset;
               CombatTextEntityUIComponentAnimationEvent[] data = new CombatTextEntityUIComponentAnimationEvent[len];

               for (int i = 0; i < len; i++) {
                  data[i] = CombatTextEntityUIComponentAnimationEvent.toObject(mem, off + i * 33);
               }

               return data;
            }
         }
      }
   }

   public static boolean hasCombatTextRandomPositionOffsetRange(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasCombatTextColor(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasCombatTextAnimationEvents(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static EntityUIComponent toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static EntityUIComponent toObject(MemorySegment mem, int offset) {
      if (offset + 51 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("EntityUIComponent", offset + 51, (int)mem.byteSize());
      } else {
         CombatTextEntityUIComponentAnimationEvent[] combatTextAnimationEvents = null;
         if (hasCombatTextAnimationEvents(mem, offset)) {
            int off = offset + 51;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("CombatTextAnimationEvents", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("CombatTextAnimationEvents", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 33L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("CombatTextAnimationEvents", off + lenOffset + len * 33, (int)mem.byteSize());
            }

            off += lenOffset;
            combatTextAnimationEvents = new CombatTextEntityUIComponentAnimationEvent[len];

            for (int i = 0; i < len; i++) {
               combatTextAnimationEvents[i] = CombatTextEntityUIComponentAnimationEvent.toObject(mem, off + i * 33);
            }
         }

         return new EntityUIComponent(
            EntityUIType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            PacketIO.readVector2f(mem, offset + 2),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 10)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 11)),
            hasCombatTextRandomPositionOffsetRange(mem, offset) ? RangeVector2f.toObject(mem, offset + 15) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 32)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 36)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 40)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 44)),
            hasCombatTextColor(mem, offset) ? Color.toObject(mem, offset + 48) : null,
            combatTextAnimationEvents
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.combatTextRandomPositionOffsetRange != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.combatTextColor != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.combatTextAnimationEvents != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      PacketIO.writeVector2f(buf, this.hitboxOffset);
      buf.writeByte(this.unknown ? 1 : 0);
      buf.writeIntLE(this.entityStatIndex);
      if (this.combatTextRandomPositionOffsetRange != null) {
         this.combatTextRandomPositionOffsetRange.serialize(buf);
      } else {
         buf.writeZero(17);
      }

      buf.writeFloatLE(this.combatTextViewportMargin);
      buf.writeFloatLE(this.combatTextDuration);
      buf.writeFloatLE(this.combatTextHitAngleModifierStrength);
      buf.writeFloatLE(this.combatTextFontSize);
      if (this.combatTextColor != null) {
         this.combatTextColor.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      if (this.combatTextAnimationEvents != null) {
         if (this.combatTextAnimationEvents.length > 4096000) {
            throw ProtocolException.arrayTooLong("CombatTextAnimationEvents", this.combatTextAnimationEvents.length, 4096000);
         }

         VarInt.write(buf, this.combatTextAnimationEvents.length);

         for (CombatTextEntityUIComponentAnimationEvent item : this.combatTextAnimationEvents) {
            item.serialize(buf);
         }
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.combatTextRandomPositionOffsetRange != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.combatTextColor != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.combatTextAnimationEvents != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      PacketIO.writeVector2f(mem, offset + 2, this.hitboxOffset);
      mem.set(PacketIO.PROTO_BOOL, offset + 10, this.unknown);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 11), this.entityStatIndex);
      if (this.combatTextRandomPositionOffsetRange != null) {
         this.combatTextRandomPositionOffsetRange.serialize(mem, offset + 15);
      } else {
         mem.asSlice(offset + 15, 17L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 32), this.combatTextViewportMargin);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 36), this.combatTextDuration);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 40), this.combatTextHitAngleModifierStrength);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 44), this.combatTextFontSize);
      if (this.combatTextColor != null) {
         this.combatTextColor.serialize(mem, offset + 48);
      } else {
         mem.asSlice(offset + 48, 3L).fill((byte)0);
      }

      int varOffset = offset + 51;
      if (this.combatTextAnimationEvents != null) {
         if (this.combatTextAnimationEvents.length > 4096000) {
            throw ProtocolException.arrayTooLong("CombatTextAnimationEvents", this.combatTextAnimationEvents.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.combatTextAnimationEvents.length);
         int combatTextAnimationEventsValueOffset = 0;

         for (int i = 0; i < this.combatTextAnimationEvents.length; i++) {
            combatTextAnimationEventsValueOffset += this.combatTextAnimationEvents[i].serialize(mem, varOffset + combatTextAnimationEventsValueOffset);
         }

         varOffset += combatTextAnimationEventsValueOffset;
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 51;
      if (this.combatTextAnimationEvents != null) {
         size += VarInt.size(this.combatTextAnimationEvents.length) + this.combatTextAnimationEvents.length * 33;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 51) {
         return ValidationResult.error("Buffer too small: expected at least 51 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 2) {
            return ValidationResult.error("Invalid EntityUIType value for Type");
         } else {
            v = offset + 51;
            if ((nullBits & 4) != 0) {
               int combatTextAnimationEventsCount = VarInt.peek(buffer, v);
               if (combatTextAnimationEventsCount < 0) {
                  return ValidationResult.error("Invalid array count for CombatTextAnimationEvents");
               }

               if (combatTextAnimationEventsCount > 4096000) {
                  return ValidationResult.error("CombatTextAnimationEvents exceeds max length 4096000");
               }

               v += VarInt.size(combatTextAnimationEventsCount);
               v += combatTextAnimationEventsCount * 33;
               if (v > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading CombatTextAnimationEvents");
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public EntityUIComponent clone() {
      EntityUIComponent copy = new EntityUIComponent();
      copy.type = this.type;
      copy.hitboxOffset = this.hitboxOffset;
      copy.unknown = this.unknown;
      copy.entityStatIndex = this.entityStatIndex;
      copy.combatTextRandomPositionOffsetRange = this.combatTextRandomPositionOffsetRange != null ? this.combatTextRandomPositionOffsetRange.clone() : null;
      copy.combatTextViewportMargin = this.combatTextViewportMargin;
      copy.combatTextDuration = this.combatTextDuration;
      copy.combatTextHitAngleModifierStrength = this.combatTextHitAngleModifierStrength;
      copy.combatTextFontSize = this.combatTextFontSize;
      copy.combatTextColor = this.combatTextColor != null ? this.combatTextColor.clone() : null;
      copy.combatTextAnimationEvents = this.combatTextAnimationEvents != null
         ? Arrays.stream(this.combatTextAnimationEvents).map(e -> e.clone()).toArray(CombatTextEntityUIComponentAnimationEvent[]::new)
         : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof EntityUIComponent other)
            ? false
            : Objects.equals(this.type, other.type)
               && Objects.equals(this.hitboxOffset, other.hitboxOffset)
               && this.unknown == other.unknown
               && this.entityStatIndex == other.entityStatIndex
               && Objects.equals(this.combatTextRandomPositionOffsetRange, other.combatTextRandomPositionOffsetRange)
               && this.combatTextViewportMargin == other.combatTextViewportMargin
               && this.combatTextDuration == other.combatTextDuration
               && this.combatTextHitAngleModifierStrength == other.combatTextHitAngleModifierStrength
               && this.combatTextFontSize == other.combatTextFontSize
               && Objects.equals(this.combatTextColor, other.combatTextColor)
               && Arrays.equals((Object[])this.combatTextAnimationEvents, (Object[])other.combatTextAnimationEvents);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Objects.hashCode(this.hitboxOffset);
      result = 31 * result + Boolean.hashCode(this.unknown);
      result = 31 * result + Integer.hashCode(this.entityStatIndex);
      result = 31 * result + Objects.hashCode(this.combatTextRandomPositionOffsetRange);
      result = 31 * result + Float.hashCode(this.combatTextViewportMargin);
      result = 31 * result + Float.hashCode(this.combatTextDuration);
      result = 31 * result + Float.hashCode(this.combatTextHitAngleModifierStrength);
      result = 31 * result + Float.hashCode(this.combatTextFontSize);
      result = 31 * result + Objects.hashCode(this.combatTextColor);
      return 31 * result + Arrays.hashCode((Object[])this.combatTextAnimationEvents);
   }
}
