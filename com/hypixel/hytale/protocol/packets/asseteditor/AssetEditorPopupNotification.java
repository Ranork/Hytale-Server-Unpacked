package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorPopupNotification implements Packet, ToClientPacket {
   public static final int PACKET_ID = 337;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public AssetEditorPopupNotificationType type = AssetEditorPopupNotificationType.Info;
   @Nullable
   public FormattedMessage message;

   @Override
   public int getId() {
      return 337;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorPopupNotification() {
   }

   public AssetEditorPopupNotification(@Nonnull AssetEditorPopupNotificationType type, @Nullable FormattedMessage message) {
      this.type = type;
      this.message = message;
   }

   public AssetEditorPopupNotification(@Nonnull AssetEditorPopupNotification other) {
      this.type = other.type;
      this.message = other.message;
   }

   @Nonnull
   public static AssetEditorPopupNotification deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 2) {
         throw ProtocolException.bufferTooSmall("AssetEditorPopupNotification", 2, buf.readableBytes() - offset);
      } else {
         AssetEditorPopupNotification obj = new AssetEditorPopupNotification();
         byte nullBits = buf.getByte(offset);
         obj.type = AssetEditorPopupNotificationType.fromValue(buf.getByte(offset + 1));
         int pos = offset + 2;
         if ((nullBits & 1) != 0) {
            obj.message = FormattedMessage.deserialize(buf, pos);
            pos += FormattedMessage.computeBytesConsumed(buf, pos);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         pos += FormattedMessage.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 2L;
   }

   public static AssetEditorPopupNotificationType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static AssetEditorPopupNotificationType getType(MemorySegment mem, int offset) {
      return AssetEditorPopupNotificationType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   @Nullable
   public static FormattedMessage getMessage(MemorySegment mem) {
      return getMessage(mem, 0);
   }

   @Nullable
   public static FormattedMessage getMessage(MemorySegment mem, int offset) {
      return hasMessage(mem, offset) ? FormattedMessage.toObject(mem, offset + 2) : null;
   }

   public static boolean hasMessage(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static AssetEditorPopupNotification toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorPopupNotification toObject(MemorySegment mem, int offset) {
      if (offset + 2 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorPopupNotification", offset + 2, (int)mem.byteSize());
      } else {
         return new AssetEditorPopupNotification(
            AssetEditorPopupNotificationType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            hasMessage(mem, offset) ? FormattedMessage.toObject(mem, offset + 2) : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.message != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      if (this.message != null) {
         this.message.serialize(buf);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.message != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      int varOffset = offset + 2;
      if (this.message != null) {
         varOffset += this.message.serialize(mem, varOffset);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 2;
      if (this.message != null) {
         size += this.message.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 2) {
         return ValidationResult.error("Buffer too small: expected at least 2 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 4) {
            return ValidationResult.error("Invalid AssetEditorPopupNotificationType value for Type");
         } else {
            v = offset + 2;
            if ((nullBits & 1) != 0) {
               ValidationResult messageResult = FormattedMessage.validateStructure(buffer, v);
               if (!messageResult.isValid()) {
                  return ValidationResult.error("Invalid Message: " + messageResult.error());
               }

               v += FormattedMessage.computeBytesConsumed(buffer, v);
            }

            return ValidationResult.OK;
         }
      }
   }

   public AssetEditorPopupNotification clone() {
      AssetEditorPopupNotification copy = new AssetEditorPopupNotification();
      copy.type = this.type;
      copy.message = this.message != null ? this.message.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorPopupNotification other)
            ? false
            : Objects.equals(this.type, other.type) && Objects.equals(this.message, other.message);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.message);
   }
}
