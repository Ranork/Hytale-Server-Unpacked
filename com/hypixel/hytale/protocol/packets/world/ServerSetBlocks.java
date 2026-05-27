package com.hypixel.hytale.protocol.packets.world;

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

public class ServerSetBlocks implements Packet, ToClientPacket {
   public static final int PACKET_ID = 141;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 36864017;
   public int x;
   public int y;
   public int z;
   @Nonnull
   public SetBlockCmd[] cmds = new SetBlockCmd[0];

   @Override
   public int getId() {
      return 141;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Chunks;
   }

   public ServerSetBlocks() {
   }

   public ServerSetBlocks(int x, int y, int z, @Nonnull SetBlockCmd[] cmds) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.cmds = cmds;
   }

   public ServerSetBlocks(@Nonnull ServerSetBlocks other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
      this.cmds = other.cmds;
   }

   @Nonnull
   public static ServerSetBlocks deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 12) {
         throw ProtocolException.bufferTooSmall("ServerSetBlocks", 12, buf.readableBytes() - offset);
      } else {
         ServerSetBlocks obj = new ServerSetBlocks();
         obj.x = buf.getIntLE(offset + 0);
         obj.y = buf.getIntLE(offset + 4);
         obj.z = buf.getIntLE(offset + 8);
         int pos = offset + 12;
         int cmdsCount = VarInt.peek(buf, pos);
         if (cmdsCount < 0) {
            throw ProtocolException.invalidVarInt("Cmds");
         } else {
            int cmdsVarLen = VarInt.size(cmdsCount);
            if (cmdsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Cmds", cmdsCount, 4096000);
            } else if (pos + cmdsVarLen + cmdsCount * 9L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Cmds", pos + cmdsVarLen + cmdsCount * 9, buf.readableBytes());
            } else {
               pos += cmdsVarLen;
               obj.cmds = new SetBlockCmd[cmdsCount];

               for (int i = 0; i < cmdsCount; i++) {
                  obj.cmds[i] = SetBlockCmd.deserialize(buf, pos);
                  pos += SetBlockCmd.computeBytesConsumed(buf, pos);
               }

               return obj;
            }
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 12;
      int arrLen = VarInt.peek(buf, pos);
      pos += VarInt.size(arrLen);

      for (int i = 0; i < arrLen; i++) {
         pos += SetBlockCmd.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 12L;
   }

   public static int getX(MemorySegment mem) {
      return getX(mem, 0);
   }

   public static int getX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getY(MemorySegment mem) {
      return getY(mem, 0);
   }

   public static int getY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static int getZ(MemorySegment mem) {
      return getZ(mem, 0);
   }

   public static int getZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 8));
   }

   public static SetBlockCmd[] getCmds(MemorySegment mem) {
      return getCmds(mem, 0);
   }

   public static SetBlockCmd[] getCmds(MemorySegment mem, int offset) {
      int off = offset + 12;
      long packed = VarInt.getWithLength(mem, off);
      int len = (int)packed;
      if (len < 0) {
         throw ProtocolException.negativeLength("Cmds", len);
      } else if (len > 4096000) {
         throw ProtocolException.arrayTooLong("Cmds", len, 4096000);
      } else {
         int lenOffset = (int)(packed >>> 32);
         if (off + lenOffset + len * 9L > mem.byteSize()) {
            throw ProtocolException.bufferTooSmall("Cmds", off + lenOffset + len * 9, (int)mem.byteSize());
         } else {
            off += lenOffset;
            SetBlockCmd[] data = new SetBlockCmd[len];

            for (int i = 0; i < len; i++) {
               data[i] = SetBlockCmd.toObject(mem, off + i * 9);
            }

            return data;
         }
      }
   }

   public static ServerSetBlocks toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ServerSetBlocks toObject(MemorySegment mem, int offset) {
      if (offset + 12 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ServerSetBlocks", offset + 12, (int)mem.byteSize());
      } else {
         int off = offset + 12;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Cmds", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Cmds", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 9L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Cmds", off + lenOffset + len * 9, (int)mem.byteSize());
            } else {
               off += lenOffset;
               SetBlockCmd[] cmds = new SetBlockCmd[len];

               for (int i = 0; i < len; i++) {
                  cmds[i] = SetBlockCmd.toObject(mem, off + i * 9);
               }

               return new ServerSetBlocks(
                  mem.get(PacketIO.PROTO_INT, (long)(offset + 0)),
                  mem.get(PacketIO.PROTO_INT, (long)(offset + 4)),
                  mem.get(PacketIO.PROTO_INT, (long)(offset + 8)),
                  cmds
               );
            }
         }
      }
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

         for (SetBlockCmd item : this.cmds) {
            item.serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.x);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.y);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 8), this.z);
      int varOffset = offset + 12;
      if (this.cmds.length > 4096000) {
         throw ProtocolException.arrayTooLong("Cmds", this.cmds.length, 4096000);
      } else {
         varOffset += VarInt.set(mem, varOffset, this.cmds.length);
         int cmdsValueOffset = 0;

         for (int i = 0; i < this.cmds.length; i++) {
            cmdsValueOffset += this.cmds[i].serialize(mem, varOffset + cmdsValueOffset);
         }

         varOffset += cmdsValueOffset;
         return varOffset - offset;
      }
   }

   @Override
   public int computeSize() {
      int size = 12;
      return size + VarInt.size(this.cmds.length) + this.cmds.length * 9;
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
            pos += VarInt.size(cmdsCount);
            pos += cmdsCount * 9;
            return pos > buffer.writerIndex() ? ValidationResult.error("Buffer overflow reading Cmds") : ValidationResult.OK;
         }
      }
   }

   public ServerSetBlocks clone() {
      ServerSetBlocks copy = new ServerSetBlocks();
      copy.x = this.x;
      copy.y = this.y;
      copy.z = this.z;
      copy.cmds = Arrays.stream(this.cmds).map(e -> e.clone()).toArray(SetBlockCmd[]::new);
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ServerSetBlocks other)
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
