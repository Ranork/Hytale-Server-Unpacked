package com.hypixel.hytale.protocol.packets.window;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SendWindowAction implements Packet, ToServerPacket {
   public static final int PACKET_ID = 203;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 32768027;
   public int id;
   @Nonnull
   public WindowAction action;

   @Override
   public int getId() {
      return 203;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SendWindowAction() {
   }

   public SendWindowAction(int id, @Nonnull WindowAction action) {
      this.id = id;
      this.action = action;
   }

   public SendWindowAction(@Nonnull SendWindowAction other) {
      this.id = other.id;
      this.action = other.action;
   }

   @Nonnull
   public static SendWindowAction deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 4) {
         throw ProtocolException.bufferTooSmall("SendWindowAction", 4, buf.readableBytes() - offset);
      } else {
         SendWindowAction obj = new SendWindowAction();
         obj.id = buf.getIntLE(offset + 0);
         int pos = offset + 4;
         obj.action = WindowAction.deserialize(buf, pos);
         pos += WindowAction.computeBytesConsumed(buf, pos);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 4;
      pos += WindowAction.computeBytesConsumed(buf, pos);
      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 4L;
   }

   public static int getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   public static int getId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static WindowAction getAction(MemorySegment mem) {
      return getAction(mem, 0);
   }

   public static WindowAction getAction(MemorySegment mem, int offset) {
      return WindowAction.toObject(mem, offset + 4);
   }

   public static SendWindowAction toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SendWindowAction toObject(MemorySegment mem, int offset) {
      if (offset + 4 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SendWindowAction", offset + 4, (int)mem.byteSize());
      } else {
         return new SendWindowAction(mem.get(PacketIO.PROTO_INT, (long)(offset + 0)), WindowAction.toObject(mem, offset + 4));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.id);
      this.action.serializeWithTypeId(buf);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.id);
      int varOffset = offset + 4;
      varOffset += this.action.serializeWithTypeId(mem, varOffset);
      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 4;
      return size + this.action.computeSizeWithTypeId();
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 4) {
         return ValidationResult.error("Buffer too small: expected at least 4 bytes");
      } else {
         int pos = offset + 4;
         ValidationResult actionResult = WindowAction.validateStructure(buffer, pos);
         if (!actionResult.isValid()) {
            return ValidationResult.error("Invalid Action: " + actionResult.error());
         } else {
            pos += WindowAction.computeBytesConsumed(buffer, pos);
            return ValidationResult.OK;
         }
      }
   }

   public SendWindowAction clone() {
      SendWindowAction copy = new SendWindowAction();
      copy.id = this.id;
      copy.action = this.action;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SendWindowAction other) ? false : this.id == other.id && Objects.equals(this.action, other.action);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.action);
   }
}
