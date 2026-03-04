package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ServerInfo implements Packet, ToClientPacket {
   public static final int PACKET_ID = 223;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 32768023;
   @Nullable
   public String serverName;
   @Nullable
   public String motd;
   public int maxPlayers;

   @Override
   public int getId() {
      return 223;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ServerInfo() {
   }

   public ServerInfo(@Nullable String serverName, @Nullable String motd, int maxPlayers) {
      this.serverName = serverName;
      this.motd = motd;
      this.maxPlayers = maxPlayers;
   }

   public ServerInfo(@Nonnull ServerInfo other) {
      this.serverName = other.serverName;
      this.motd = other.motd;
      this.maxPlayers = other.maxPlayers;
   }

   @Nonnull
   public static ServerInfo deserialize(@Nonnull ByteBuf buf, int offset) {
      ServerInfo obj = new ServerInfo();
      byte nullBits = buf.getByte(offset);
      obj.maxPlayers = buf.getIntLE(offset + 1);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 13 + buf.getIntLE(offset + 5);
         int serverNameLen = VarInt.peek(buf, varPos0);
         if (serverNameLen < 0) {
            throw ProtocolException.negativeLength("ServerName", serverNameLen);
         }

         if (serverNameLen > 4096000) {
            throw ProtocolException.stringTooLong("ServerName", serverNameLen, 4096000);
         }

         obj.serverName = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 13 + buf.getIntLE(offset + 9);
         int motdLen = VarInt.peek(buf, varPos1);
         if (motdLen < 0) {
            throw ProtocolException.negativeLength("Motd", motdLen);
         }

         if (motdLen > 4096000) {
            throw ProtocolException.stringTooLong("Motd", motdLen, 4096000);
         }

         obj.motd = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 5);
         int pos0 = offset + 13 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 9);
         int pos1 = offset + 13 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.serverName != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.motd != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.maxPlayers);
      int serverNameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int motdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.serverName != null) {
         buf.setIntLE(serverNameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.serverName, 4096000);
      } else {
         buf.setIntLE(serverNameOffsetSlot, -1);
      }

      if (this.motd != null) {
         buf.setIntLE(motdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.motd, 4096000);
      } else {
         buf.setIntLE(motdOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 13;
      if (this.serverName != null) {
         size += PacketIO.stringSize(this.serverName);
      }

      if (this.motd != null) {
         size += PacketIO.stringSize(this.motd);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int serverNameOffset = buffer.getIntLE(offset + 5);
            if (serverNameOffset < 0) {
               return ValidationResult.error("Invalid offset for ServerName");
            }

            int pos = offset + 13 + serverNameOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ServerName");
            }

            int serverNameLen = VarInt.peek(buffer, pos);
            if (serverNameLen < 0) {
               return ValidationResult.error("Invalid string length for ServerName");
            }

            if (serverNameLen > 4096000) {
               return ValidationResult.error("ServerName exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += serverNameLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ServerName");
            }
         }

         if ((nullBits & 2) != 0) {
            int motdOffset = buffer.getIntLE(offset + 9);
            if (motdOffset < 0) {
               return ValidationResult.error("Invalid offset for Motd");
            }

            int posx = offset + 13 + motdOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Motd");
            }

            int motdLen = VarInt.peek(buffer, posx);
            if (motdLen < 0) {
               return ValidationResult.error("Invalid string length for Motd");
            }

            if (motdLen > 4096000) {
               return ValidationResult.error("Motd exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += motdLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Motd");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ServerInfo clone() {
      ServerInfo copy = new ServerInfo();
      copy.serverName = this.serverName;
      copy.motd = this.motd;
      copy.maxPlayers = this.maxPlayers;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ServerInfo other)
            ? false
            : Objects.equals(this.serverName, other.serverName) && Objects.equals(this.motd, other.motd) && this.maxPlayers == other.maxPlayers;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.serverName, this.motd, this.maxPlayers);
   }
}
