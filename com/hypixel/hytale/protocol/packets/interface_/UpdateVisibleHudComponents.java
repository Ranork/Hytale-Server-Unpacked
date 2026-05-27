package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateVisibleHudComponents implements Packet, ToClientPacket {
   public static final int PACKET_ID = 230;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 4096006;
   @Nullable
   public HudComponent[] visibleComponents;

   @Override
   public int getId() {
      return 230;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateVisibleHudComponents() {
   }

   public UpdateVisibleHudComponents(@Nullable HudComponent[] visibleComponents) {
      this.visibleComponents = visibleComponents;
   }

   public UpdateVisibleHudComponents(@Nonnull UpdateVisibleHudComponents other) {
      this.visibleComponents = other.visibleComponents;
   }

   @Nonnull
   public static UpdateVisibleHudComponents deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("UpdateVisibleHudComponents", 1, buf.readableBytes() - offset);
      } else {
         UpdateVisibleHudComponents obj = new UpdateVisibleHudComponents();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int visibleComponentsCount = VarInt.peek(buf, pos);
            if (visibleComponentsCount < 0) {
               throw ProtocolException.invalidVarInt("VisibleComponents");
            }

            int visibleComponentsVarLen = VarInt.size(visibleComponentsCount);
            if (visibleComponentsCount > 4096000) {
               throw ProtocolException.arrayTooLong("VisibleComponents", visibleComponentsCount, 4096000);
            }

            if (pos + visibleComponentsVarLen + visibleComponentsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("VisibleComponents", pos + visibleComponentsVarLen + visibleComponentsCount * 1, buf.readableBytes());
            }

            pos += visibleComponentsVarLen;
            obj.visibleComponents = new HudComponent[visibleComponentsCount];

            for (int i = 0; i < visibleComponentsCount; i++) {
               obj.visibleComponents[i] = HudComponent.fromValue(buf.getByte(pos));
               pos++;
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen) + arrLen * 1;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static HudComponent[] getVisibleComponents(MemorySegment mem) {
      return getVisibleComponents(mem, 0);
   }

   @Nullable
   public static HudComponent[] getVisibleComponents(MemorySegment mem, int offset) {
      if (!hasVisibleComponents(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("VisibleComponents", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("VisibleComponents", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("VisibleComponents", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               HudComponent[] data = new HudComponent[len];

               for (int i = 0; i < len; i++) {
                  data[i] = HudComponent.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
               }

               return data;
            }
         }
      }
   }

   public static boolean hasVisibleComponents(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateVisibleHudComponents toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateVisibleHudComponents toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateVisibleHudComponents", offset + 1, (int)mem.byteSize());
      } else {
         HudComponent[] visibleComponents = null;
         if (hasVisibleComponents(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("VisibleComponents", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("VisibleComponents", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("VisibleComponents", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            visibleComponents = new HudComponent[len];

            for (int i = 0; i < len; i++) {
               visibleComponents[i] = HudComponent.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
            }
         }

         return new UpdateVisibleHudComponents(visibleComponents);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.visibleComponents != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.visibleComponents != null) {
         if (this.visibleComponents.length > 4096000) {
            throw ProtocolException.arrayTooLong("VisibleComponents", this.visibleComponents.length, 4096000);
         }

         VarInt.write(buf, this.visibleComponents.length);

         for (HudComponent item : this.visibleComponents) {
            buf.writeByte(item.getValue());
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.visibleComponents != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.visibleComponents != null) {
         if (this.visibleComponents.length > 4096000) {
            throw ProtocolException.arrayTooLong("VisibleComponents", this.visibleComponents.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.visibleComponents.length);

         for (int i = 0; i < this.visibleComponents.length; i++) {
            mem.set(PacketIO.PROTO_BYTE, (long)(varOffset + i * 1), (byte)this.visibleComponents[i].getValue());
         }

         varOffset += this.visibleComponents.length * 1;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.visibleComponents != null) {
         size += VarInt.size(this.visibleComponents.length) + this.visibleComponents.length * 1;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int visibleComponentsCount = VarInt.peek(buffer, pos);
            if (visibleComponentsCount < 0) {
               return ValidationResult.error("Invalid array count for VisibleComponents");
            }

            if (visibleComponentsCount > 4096000) {
               return ValidationResult.error("VisibleComponents exceeds max length 4096000");
            }

            pos += VarInt.size(visibleComponentsCount);
            if (pos + visibleComponentsCount * 1L > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading VisibleComponents");
            }

            for (int i = 0; i < visibleComponentsCount; i++) {
               int v = buffer.getByte(pos) & 255;
               if (v >= 24) {
                  return ValidationResult.error("Invalid HudComponent value for VisibleComponents[i]");
               }

               pos++;
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateVisibleHudComponents clone() {
      UpdateVisibleHudComponents copy = new UpdateVisibleHudComponents();
      copy.visibleComponents = this.visibleComponents != null ? Arrays.copyOf(this.visibleComponents, this.visibleComponents.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof UpdateVisibleHudComponents other ? Arrays.equals((Object[])this.visibleComponents, (Object[])other.visibleComponents) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.visibleComponents);
   }
}
