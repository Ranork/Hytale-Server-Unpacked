package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderToolGMaskPresetLoadResponse implements Packet, ToServerPacket {
   public static final int PACKET_ID = 431;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 16384006;
   @Nullable
   public String maskData;

   @Override
   public int getId() {
      return 431;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolGMaskPresetLoadResponse() {
   }

   public BuilderToolGMaskPresetLoadResponse(@Nullable String maskData) {
      this.maskData = maskData;
   }

   public BuilderToolGMaskPresetLoadResponse(@Nonnull BuilderToolGMaskPresetLoadResponse other) {
      this.maskData = other.maskData;
   }

   @Nonnull
   public static BuilderToolGMaskPresetLoadResponse deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("BuilderToolGMaskPresetLoadResponse", 1, buf.readableBytes() - offset);
      } else {
         BuilderToolGMaskPresetLoadResponse obj = new BuilderToolGMaskPresetLoadResponse();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int maskDataLen = VarInt.peek(buf, pos);
            if (maskDataLen < 0) {
               throw ProtocolException.invalidVarInt("MaskData");
            }

            int maskDataVarLen = VarInt.size(maskDataLen);
            if (maskDataLen > 4096000) {
               throw ProtocolException.stringTooLong("MaskData", maskDataLen, 4096000);
            }

            if (pos + maskDataVarLen + maskDataLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("MaskData", pos + maskDataVarLen + maskDataLen, buf.readableBytes());
            }

            obj.maskData = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += maskDataVarLen + maskDataLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static String getMaskData(MemorySegment mem) {
      return getMaskData(mem, 0);
   }

   @Nullable
   public static String getMaskData(MemorySegment mem, int offset) {
      return hasMaskData(mem, offset) ? PacketIO.readVarString("MaskData", mem, offset + 1, 4096000, PacketIO.UTF8) : null;
   }

   public static boolean hasMaskData(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static BuilderToolGMaskPresetLoadResponse toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolGMaskPresetLoadResponse toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolGMaskPresetLoadResponse", offset + 1, (int)mem.byteSize());
      } else {
         return new BuilderToolGMaskPresetLoadResponse(
            hasMaskData(mem, offset) ? PacketIO.readVarString("MaskData", mem, offset + 1, 4096000, PacketIO.UTF8) : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.maskData != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.maskData != null) {
         PacketIO.writeVarString(buf, this.maskData, 4096000);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.maskData != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.maskData != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.maskData, 4096000);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.maskData != null) {
         size += PacketIO.stringSize(this.maskData);
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
            int maskDataLen = VarInt.peek(buffer, pos);
            if (maskDataLen < 0) {
               return ValidationResult.error("Invalid string length for MaskData");
            }

            if (maskDataLen > 4096000) {
               return ValidationResult.error("MaskData exceeds max length 4096000");
            }

            pos += VarInt.size(maskDataLen);
            pos += maskDataLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading MaskData");
            }
         }

         return ValidationResult.OK;
      }
   }

   public BuilderToolGMaskPresetLoadResponse clone() {
      BuilderToolGMaskPresetLoadResponse copy = new BuilderToolGMaskPresetLoadResponse();
      copy.maskData = this.maskData;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof BuilderToolGMaskPresetLoadResponse other ? Objects.equals(this.maskData, other.maskData) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.maskData);
   }
}
