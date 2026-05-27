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

public class MouseMotionEvent {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 4096014;
   @Nullable
   public MouseButtonType[] mouseButtonType;
   @Nullable
   public Vector2i relativeMotion;

   public MouseMotionEvent() {
   }

   public MouseMotionEvent(@Nullable MouseButtonType[] mouseButtonType, @Nullable Vector2i relativeMotion) {
      this.mouseButtonType = mouseButtonType;
      this.relativeMotion = relativeMotion;
   }

   public MouseMotionEvent(@Nonnull MouseMotionEvent other) {
      this.mouseButtonType = other.mouseButtonType;
      this.relativeMotion = other.relativeMotion;
   }

   @Nonnull
   public static MouseMotionEvent deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("MouseMotionEvent", 9, buf.readableBytes() - offset);
      } else {
         MouseMotionEvent obj = new MouseMotionEvent();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.relativeMotion = Vector2i.deserialize(buf, offset + 1);
         }

         int pos = offset + 9;
         if ((nullBits & 2) != 0) {
            int mouseButtonTypeCount = VarInt.peek(buf, pos);
            if (mouseButtonTypeCount < 0) {
               throw ProtocolException.invalidVarInt("MouseButtonType");
            }

            int mouseButtonTypeVarLen = VarInt.size(mouseButtonTypeCount);
            if (mouseButtonTypeCount > 4096000) {
               throw ProtocolException.arrayTooLong("MouseButtonType", mouseButtonTypeCount, 4096000);
            }

            if (pos + mouseButtonTypeVarLen + mouseButtonTypeCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("MouseButtonType", pos + mouseButtonTypeVarLen + mouseButtonTypeCount * 1, buf.readableBytes());
            }

            pos += mouseButtonTypeVarLen;
            obj.mouseButtonType = new MouseButtonType[mouseButtonTypeCount];

            for (int i = 0; i < mouseButtonTypeCount; i++) {
               obj.mouseButtonType[i] = MouseButtonType.fromValue(buf.getByte(pos));
               pos++;
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 2) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen) + arrLen * 1;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   @Nullable
   public static MouseButtonType[] getMouseButtonType(MemorySegment mem) {
      return getMouseButtonType(mem, 0);
   }

   @Nullable
   public static MouseButtonType[] getMouseButtonType(MemorySegment mem, int offset) {
      if (!hasMouseButtonType(mem, offset)) {
         return null;
      } else {
         int off = offset + 9;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("MouseButtonType", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("MouseButtonType", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("MouseButtonType", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               MouseButtonType[] data = new MouseButtonType[len];

               for (int i = 0; i < len; i++) {
                  data[i] = MouseButtonType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static Vector2i getRelativeMotion(MemorySegment mem) {
      return getRelativeMotion(mem, 0);
   }

   @Nullable
   public static Vector2i getRelativeMotion(MemorySegment mem, int offset) {
      return hasRelativeMotion(mem, offset) ? Vector2i.toObject(mem, offset + 1) : null;
   }

   public static boolean hasRelativeMotion(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasMouseButtonType(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static MouseMotionEvent toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static MouseMotionEvent toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("MouseMotionEvent", offset + 9, (int)mem.byteSize());
      } else {
         MouseButtonType[] mouseButtonType = null;
         if (hasMouseButtonType(mem, offset)) {
            int off = offset + 9;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("MouseButtonType", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("MouseButtonType", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("MouseButtonType", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            mouseButtonType = new MouseButtonType[len];

            for (int i = 0; i < len; i++) {
               mouseButtonType[i] = MouseButtonType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
            }
         }

         return new MouseMotionEvent(mouseButtonType, hasRelativeMotion(mem, offset) ? Vector2i.toObject(mem, offset + 1) : null);
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.relativeMotion != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.mouseButtonType != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      if (this.relativeMotion != null) {
         this.relativeMotion.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.mouseButtonType != null) {
         if (this.mouseButtonType.length > 4096000) {
            throw ProtocolException.arrayTooLong("MouseButtonType", this.mouseButtonType.length, 4096000);
         }

         VarInt.write(buf, this.mouseButtonType.length);

         for (MouseButtonType item : this.mouseButtonType) {
            buf.writeByte(item.getValue());
         }
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.relativeMotion != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.mouseButtonType != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.relativeMotion != null) {
         this.relativeMotion.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 8L).fill((byte)0);
      }

      int varOffset = offset + 9;
      if (this.mouseButtonType != null) {
         if (this.mouseButtonType.length > 4096000) {
            throw ProtocolException.arrayTooLong("MouseButtonType", this.mouseButtonType.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.mouseButtonType.length);

         for (int i = 0; i < this.mouseButtonType.length; i++) {
            mem.set(PacketIO.PROTO_BYTE, (long)(varOffset + i * 1), (byte)this.mouseButtonType[i].getValue());
         }

         varOffset += this.mouseButtonType.length * 1;
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 9;
      if (this.mouseButtonType != null) {
         size += VarInt.size(this.mouseButtonType.length) + this.mouseButtonType.length * 1;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 9;
         if ((nullBits & 2) != 0) {
            int mouseButtonTypeCount = VarInt.peek(buffer, pos);
            if (mouseButtonTypeCount < 0) {
               return ValidationResult.error("Invalid array count for MouseButtonType");
            }

            if (mouseButtonTypeCount > 4096000) {
               return ValidationResult.error("MouseButtonType exceeds max length 4096000");
            }

            pos += VarInt.size(mouseButtonTypeCount);
            if (pos + mouseButtonTypeCount * 1L > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading MouseButtonType");
            }

            for (int i = 0; i < mouseButtonTypeCount; i++) {
               int v = buffer.getByte(pos) & 255;
               if (v >= 5) {
                  return ValidationResult.error("Invalid MouseButtonType value for MouseButtonType[i]");
               }

               pos++;
            }
         }

         return ValidationResult.OK;
      }
   }

   public MouseMotionEvent clone() {
      MouseMotionEvent copy = new MouseMotionEvent();
      copy.mouseButtonType = this.mouseButtonType != null ? Arrays.copyOf(this.mouseButtonType, this.mouseButtonType.length) : null;
      copy.relativeMotion = this.relativeMotion != null ? this.relativeMotion.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof MouseMotionEvent other)
            ? false
            : Arrays.equals((Object[])this.mouseButtonType, (Object[])other.mouseButtonType) && Objects.equals(this.relativeMotion, other.relativeMotion);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode((Object[])this.mouseButtonType);
      return 31 * result + Objects.hashCode(this.relativeMotion);
   }
}
