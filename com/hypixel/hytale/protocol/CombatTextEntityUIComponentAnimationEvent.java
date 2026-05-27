package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.joml.Vector2fc;

public class CombatTextEntityUIComponentAnimationEvent {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 33;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 33;
   public static final int MAX_SIZE = 33;
   @Nonnull
   public CombatTextEntityUIAnimationEventType type = CombatTextEntityUIAnimationEventType.Scale;
   public float startAt;
   public float endAt;
   public float startScale;
   public float endScale;
   @Nonnull
   public Vector2fc positionOffset = PacketIO.ZERO_VECTOR2;
   public float startOpacity;
   public float endOpacity;

   public CombatTextEntityUIComponentAnimationEvent() {
   }

   public CombatTextEntityUIComponentAnimationEvent(
      @Nonnull CombatTextEntityUIAnimationEventType type,
      float startAt,
      float endAt,
      float startScale,
      float endScale,
      @Nonnull Vector2fc positionOffset,
      float startOpacity,
      float endOpacity
   ) {
      this.type = type;
      this.startAt = startAt;
      this.endAt = endAt;
      this.startScale = startScale;
      this.endScale = endScale;
      this.positionOffset = positionOffset;
      this.startOpacity = startOpacity;
      this.endOpacity = endOpacity;
   }

   public CombatTextEntityUIComponentAnimationEvent(@Nonnull CombatTextEntityUIComponentAnimationEvent other) {
      this.type = other.type;
      this.startAt = other.startAt;
      this.endAt = other.endAt;
      this.startScale = other.startScale;
      this.endScale = other.endScale;
      this.positionOffset = other.positionOffset;
      this.startOpacity = other.startOpacity;
      this.endOpacity = other.endOpacity;
   }

   @Nonnull
   public static CombatTextEntityUIComponentAnimationEvent deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 33) {
         throw ProtocolException.bufferTooSmall("CombatTextEntityUIComponentAnimationEvent", 33, buf.readableBytes() - offset);
      } else {
         CombatTextEntityUIComponentAnimationEvent obj = new CombatTextEntityUIComponentAnimationEvent();
         obj.type = CombatTextEntityUIAnimationEventType.fromValue(buf.getByte(offset + 0));
         obj.startAt = buf.getFloatLE(offset + 1);
         obj.endAt = buf.getFloatLE(offset + 5);
         obj.startScale = buf.getFloatLE(offset + 9);
         obj.endScale = buf.getFloatLE(offset + 13);
         obj.positionOffset = PacketIO.readVector2f(buf, offset + 17);
         obj.startOpacity = buf.getFloatLE(offset + 25);
         obj.endOpacity = buf.getFloatLE(offset + 29);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 33;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 33L;
   }

   public static CombatTextEntityUIAnimationEventType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static CombatTextEntityUIAnimationEventType getType(MemorySegment mem, int offset) {
      return CombatTextEntityUIAnimationEventType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static float getStartAt(MemorySegment mem) {
      return getStartAt(mem, 0);
   }

   public static float getStartAt(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static float getEndAt(MemorySegment mem) {
      return getEndAt(mem, 0);
   }

   public static float getEndAt(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5));
   }

   public static float getStartScale(MemorySegment mem) {
      return getStartScale(mem, 0);
   }

   public static float getStartScale(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9));
   }

   public static float getEndScale(MemorySegment mem) {
      return getEndScale(mem, 0);
   }

   public static float getEndScale(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 13));
   }

   public static Vector2fc getPositionOffset(MemorySegment mem) {
      return getPositionOffset(mem, 0);
   }

   public static Vector2fc getPositionOffset(MemorySegment mem, int offset) {
      return PacketIO.readVector2f(mem, offset + 17);
   }

   public static float getStartOpacity(MemorySegment mem) {
      return getStartOpacity(mem, 0);
   }

   public static float getStartOpacity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 25));
   }

   public static float getEndOpacity(MemorySegment mem) {
      return getEndOpacity(mem, 0);
   }

   public static float getEndOpacity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 29));
   }

   public static CombatTextEntityUIComponentAnimationEvent toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static CombatTextEntityUIComponentAnimationEvent toObject(MemorySegment mem, int offset) {
      if (offset + 33 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("CombatTextEntityUIComponentAnimationEvent", offset + 33, (int)mem.byteSize());
      } else {
         return new CombatTextEntityUIComponentAnimationEvent(
            CombatTextEntityUIAnimationEventType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0))),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 13)),
            PacketIO.readVector2f(mem, offset + 17),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 25)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 29))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.type.getValue());
      buf.writeFloatLE(this.startAt);
      buf.writeFloatLE(this.endAt);
      buf.writeFloatLE(this.startScale);
      buf.writeFloatLE(this.endScale);
      PacketIO.writeVector2f(buf, this.positionOffset);
      buf.writeFloatLE(this.startOpacity);
      buf.writeFloatLE(this.endOpacity);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.startAt);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 5), this.endAt);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 9), this.startScale);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 13), this.endScale);
      PacketIO.writeVector2f(mem, offset + 17, this.positionOffset);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 25), this.startOpacity);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 29), this.endOpacity);
      return 33;
   }

   public int computeSize() {
      return 33;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 33) {
         return ValidationResult.error("Buffer too small: expected at least 33 bytes");
      } else {
         int v = buffer.getByte(offset + 0) & 255;
         return v >= 3 ? ValidationResult.error("Invalid CombatTextEntityUIAnimationEventType value for Type") : ValidationResult.OK;
      }
   }

   public CombatTextEntityUIComponentAnimationEvent clone() {
      CombatTextEntityUIComponentAnimationEvent copy = new CombatTextEntityUIComponentAnimationEvent();
      copy.type = this.type;
      copy.startAt = this.startAt;
      copy.endAt = this.endAt;
      copy.startScale = this.startScale;
      copy.endScale = this.endScale;
      copy.positionOffset = this.positionOffset;
      copy.startOpacity = this.startOpacity;
      copy.endOpacity = this.endOpacity;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CombatTextEntityUIComponentAnimationEvent other)
            ? false
            : Objects.equals(this.type, other.type)
               && this.startAt == other.startAt
               && this.endAt == other.endAt
               && this.startScale == other.startScale
               && this.endScale == other.endScale
               && Objects.equals(this.positionOffset, other.positionOffset)
               && this.startOpacity == other.startOpacity
               && this.endOpacity == other.endOpacity;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.startAt, this.endAt, this.startScale, this.endScale, this.positionOffset, this.startOpacity, this.endOpacity);
   }
}
