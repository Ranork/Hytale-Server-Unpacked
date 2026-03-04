package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class ServerSetFluids implements Packet, ToClientPacket {
   public static final int PACKET_ID = 143;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 28672017;
   public int x;
   public int y;
   public int z;
   @Nonnull
   public SetFluidCmd[] cmds = new SetFluidCmd[0];

   @Override
   public int getId() {
      return 143;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Chunks;
   }

   public ServerSetFluids() {
   }

   public ServerSetFluids(int x, int y, int z, @Nonnull SetFluidCmd[] cmds) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.cmds = cmds;
   }

   public ServerSetFluids(@Nonnull ServerSetFluids other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
      this.cmds = other.cmds;
   }

   @Nonnull
   public static ServerSetFluids deserialize(@Nonnull ByteBuf buf, int offset) {
      ServerSetFluids obj = new ServerSetFluids();
      obj.x = buf.getIntLE(offset + 0);
      obj.y = buf.getIntLE(offset + 4);
      obj.z = buf.getIntLE(offset + 8);
      int pos = offset + 12;
      int cmdsCount = VarInt.peek(buf, pos);
      if (cmdsCount < 0) {
         throw ProtocolException.negativeLength("Cmds", cmdsCount);
      } else if (cmdsCount > 4096000) {
         throw ProtocolException.arrayTooLong("Cmds", cmdsCount, 4096000);
      } else {
         int cmdsVarLen = VarInt.size(cmdsCount);
         if (pos + cmdsVarLen + cmdsCount * 7L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Cmds", pos + cmdsVarLen + cmdsCount * 7, buf.readableBytes());
         } else {
            pos += cmdsVarLen;
            obj.cmds = new SetFluidCmd[cmdsCount];

            for (int i = 0; i < cmdsCount; i++) {
               obj.cmds[i] = SetFluidCmd.deserialize(buf, pos);
               pos += SetFluidCmd.computeBytesConsumed(buf, pos);
            }

            return obj;
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 12;
      int arrLen = VarInt.peek(buf, pos);
      pos += VarInt.length(buf, pos);

      for (int i = 0; i < arrLen; i++) {
         pos += SetFluidCmd.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.x);
      buf.writeIntLE(this.y);
      buf.writeIntLE(this.z);
      if (this.cmds.length > 4096000) {
         throw ProtocolException.arrayTooLong("Cmds", this.cmds.length, 4096000);
      } else {
         VarInt.write(buf, this.cmds.length);

         for (SetFluidCmd item : this.cmds) {
            item.serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 12;
      return size + VarInt.size(this.cmds.length) + this.cmds.length * 7;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 12) {
         return ValidationResult.error("Buffer too small: expected at least 12 bytes");
      } else {
         int pos = offset + 12;
         int cmdsCount = VarInt.peek(buffer, pos);
         if (cmdsCount < 0) {
            return ValidationResult.error("Invalid array count for Cmds");
         } else if (cmdsCount > 4096000) {
            return ValidationResult.error("Cmds exceeds max length 4096000");
         } else {
            pos += VarInt.length(buffer, pos);
            pos += cmdsCount * 7;
            return pos > buffer.writerIndex() ? ValidationResult.error("Buffer overflow reading Cmds") : ValidationResult.OK;
         }
      }
   }

   public ServerSetFluids clone() {
      ServerSetFluids copy = new ServerSetFluids();
      copy.x = this.x;
      copy.y = this.y;
      copy.z = this.z;
      copy.cmds = Arrays.stream(this.cmds).map(e -> e.clone()).toArray(SetFluidCmd[]::new);
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ServerSetFluids other)
            ? false
            : this.x == other.x && this.y == other.y && this.z == other.z && Arrays.equals((Object[])this.cmds, (Object[])other.cmds);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.x);
      result = 31 * result + Integer.hashCode(this.y);
      result = 31 * result + Integer.hashCode(this.z);
      return 31 * result + Arrays.hashCode((Object[])this.cmds);
   }
}
