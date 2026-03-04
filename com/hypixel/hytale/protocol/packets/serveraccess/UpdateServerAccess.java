package com.hypixel.hytale.protocol.packets.serveraccess;

import com.hypixel.hytale.protocol.HostAddress;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
      UpdateServerAccess obj = new UpdateServerAccess();
      byte nullBits = buf.getByte(offset);
      obj.access = Access.fromValue(buf.getByte(offset + 1));
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int hostsCount = VarInt.peek(buf, pos);
         if (hostsCount < 0) {
            throw ProtocolException.negativeLength("Hosts", hostsCount);
         }

         if (hostsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Hosts", hostsCount, 4096000);
         }

         int hostsVarLen = VarInt.size(hostsCount);
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < arrLen; i++) {
            pos += HostAddress.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
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
         int pos = offset + 2;
         if ((nullBits & 1) != 0) {
            int hostsCount = VarInt.peek(buffer, pos);
            if (hostsCount < 0) {
               return ValidationResult.error("Invalid array count for Hosts");
            }

            if (hostsCount > 4096000) {
               return ValidationResult.error("Hosts exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < hostsCount; i++) {
               ValidationResult structResult = HostAddress.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid HostAddress in Hosts[" + i + "]: " + structResult.error());
               }

               pos += HostAddress.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
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
