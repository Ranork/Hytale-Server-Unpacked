package com.hypixel.hytale.protocol.packets.serveraccess;

import com.hypixel.hytale.protocol.HostAddress;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
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

public class UpdateServerAccess implements Packet, ToServerPacket {
   public static final int PACKET_ID = 251;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public Access access = Access.Private;
   @Nullable
   public HostAddress[] hosts;

   @Override
   public int getId() {
      return 251;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateServerAccess() {
   }

   public UpdateServerAccess(@Nonnull Access access, @Nullable HostAddress[] hosts) {
      this.access = access;
      this.hosts = hosts;
   }

   public UpdateServerAccess(@Nonnull UpdateServerAccess other) {
      this.access = other.access;
      this.hosts = other.hosts;
   }

   @Nonnull
   public static UpdateServerAccess deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 2) {
         throw ProtocolException.bufferTooSmall("UpdateServerAccess", 2, buf.readableBytes() - offset);
      } else {
         UpdateServerAccess obj = new UpdateServerAccess();
         byte nullBits = buf.getByte(offset);
         obj.access = Access.fromValue(buf.getByte(offset + 1));
         int pos = offset + 2;
         if ((nullBits & 1) != 0) {
            int hostsCount = VarInt.peek(buf, pos);
            if (hostsCount < 0) {
               throw ProtocolException.invalidVarInt("Hosts");
            }

            int hostsVarLen = VarInt.size(hostsCount);
            if (hostsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Hosts", hostsCount, 4096000);
            }

            if (pos + hostsVarLen + hostsCount * 2L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Hosts", pos + hostsVarLen + hostsCount * 2, buf.readableBytes());
            }

            pos += hostsVarLen;
            obj.hosts = new HostAddress[hostsCount];

            for (int i = 0; i < hostsCount; i++) {
               obj.hosts[i] = HostAddress.deserialize(buf, pos);
               pos += HostAddress.computeBytesConsumed(buf, pos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos += HostAddress.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 2L;
   }

   public static Access getAccess(MemorySegment mem) {
      return getAccess(mem, 0);
   }

   public static Access getAccess(MemorySegment mem, int offset) {
      return Access.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   @Nullable
   public static HostAddress[] getHosts(MemorySegment mem) {
      return getHosts(mem, 0);
   }

   @Nullable
   public static HostAddress[] getHosts(MemorySegment mem, int offset) {
      if (!hasHosts(mem, offset)) {
         return null;
      } else {
         int off = offset + 2;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Hosts", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Hosts", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Hosts", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               HostAddress[] data = new HostAddress[len];

               for (int i = 0; i < len; i++) {
                  data[i] = HostAddress.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasHosts(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateServerAccess toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateServerAccess toObject(MemorySegment mem, int offset) {
      if (offset + 2 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateServerAccess", offset + 2, (int)mem.byteSize());
      } else {
         HostAddress[] hosts = null;
         if (hasHosts(mem, offset)) {
            int off = offset + 2;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Hosts", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Hosts", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Hosts", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            hosts = new HostAddress[len];

            for (int i = 0; i < len; i++) {
               hosts[i] = HostAddress.toObject(mem, off);
               off += hosts[i].computeSize();
            }
         }

         return new UpdateServerAccess(Access.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), hosts);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.hosts != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.access.getValue());
      if (this.hosts != null) {
         if (this.hosts.length > 4096000) {
            throw ProtocolException.arrayTooLong("Hosts", this.hosts.length, 4096000);
         }

         VarInt.write(buf, this.hosts.length);

         for (HostAddress item : this.hosts) {
            item.serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.hosts != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.access.getValue());
      int varOffset = offset + 2;
      if (this.hosts != null) {
         if (this.hosts.length > 4096000) {
            throw ProtocolException.arrayTooLong("Hosts", this.hosts.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.hosts.length);
         int hostsValueOffset = 0;

         for (int i = 0; i < this.hosts.length; i++) {
            hostsValueOffset += this.hosts[i].serialize(mem, varOffset + hostsValueOffset);
         }

         varOffset += hostsValueOffset;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 2;
      if (this.hosts != null) {
         int hostsSize = 0;

         for (HostAddress elem : this.hosts) {
            hostsSize += elem.computeSize();
         }

         size += VarInt.size(this.hosts.length) + hostsSize;
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
            return ValidationResult.error("Invalid Access value for Access");
         } else {
            v = offset + 2;
            if ((nullBits & 1) != 0) {
               int hostsCount = VarInt.peek(buffer, v);
               if (hostsCount < 0) {
                  return ValidationResult.error("Invalid array count for Hosts");
               }

               if (hostsCount > 4096000) {
                  return ValidationResult.error("Hosts exceeds max length 4096000");
               }

               v += VarInt.size(hostsCount);

               for (int i = 0; i < hostsCount; i++) {
                  ValidationResult structResult = HostAddress.validateStructure(buffer, v);
                  if (!structResult.isValid()) {
                     return ValidationResult.error("Invalid HostAddress in Hosts[" + i + "]: " + structResult.error());
                  }

                  v += HostAddress.computeBytesConsumed(buffer, v);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateServerAccess clone() {
      UpdateServerAccess copy = new UpdateServerAccess();
      copy.access = this.access;
      copy.hosts = this.hosts != null ? Arrays.stream(this.hosts).map(e -> e.clone()).toArray(HostAddress[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateServerAccess other)
            ? false
            : Objects.equals(this.access, other.access) && Arrays.equals((Object[])this.hosts, (Object[])other.hosts);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.access);
      return 31 * result + Arrays.hashCode((Object[])this.hosts);
   }
}
