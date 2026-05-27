package com.hypixel.hytale.protocol.packets.entities;

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

public class PlayEmote implements Packet, ToServerPacket {
   public static final int PACKET_ID = 167;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 16384006;
   @Nullable
   public String emoteId;

   @Override
   public int getId() {
      return 167;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public PlayEmote() {
   }

   public PlayEmote(@Nullable String emoteId) {
      this.emoteId = emoteId;
   }

   public PlayEmote(@Nonnull PlayEmote other) {
      this.emoteId = other.emoteId;
   }

   @Nonnull
   public static PlayEmote deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("PlayEmote", 1, buf.readableBytes() - offset);
      } else {
         PlayEmote obj = new PlayEmote();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int emoteIdLen = VarInt.peek(buf, pos);
            if (emoteIdLen < 0) {
               throw ProtocolException.invalidVarInt("EmoteId");
            }

            int emoteIdVarLen = VarInt.size(emoteIdLen);
            if (emoteIdLen > 4096000) {
               throw ProtocolException.stringTooLong("EmoteId", emoteIdLen, 4096000);
            }

            if (pos + emoteIdVarLen + emoteIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("EmoteId", pos + emoteIdVarLen + emoteIdLen, buf.readableBytes());
            }

            obj.emoteId = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += emoteIdVarLen + emoteIdLen;
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
   public static String getEmoteId(MemorySegment mem) {
      return getEmoteId(mem, 0);
   }

   @Nullable
   public static String getEmoteId(MemorySegment mem, int offset) {
      return hasEmoteId(mem, offset) ? PacketIO.readVarString("EmoteId", mem, offset + 1, 4096000, PacketIO.UTF8) : null;
   }

   public static boolean hasEmoteId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static PlayEmote toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static PlayEmote toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("PlayEmote", offset + 1, (int)mem.byteSize());
      } else {
         return new PlayEmote(hasEmoteId(mem, offset) ? PacketIO.readVarString("EmoteId", mem, offset + 1, 4096000, PacketIO.UTF8) : null);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.emoteId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.emoteId != null) {
         PacketIO.writeVarString(buf, this.emoteId, 4096000);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.emoteId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.emoteId != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.emoteId, 4096000);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.emoteId != null) {
         size += PacketIO.stringSize(this.emoteId);
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
            int emoteIdLen = VarInt.peek(buffer, pos);
            if (emoteIdLen < 0) {
               return ValidationResult.error("Invalid string length for EmoteId");
            }

            if (emoteIdLen > 4096000) {
               return ValidationResult.error("EmoteId exceeds max length 4096000");
            }

            pos += VarInt.size(emoteIdLen);
            pos += emoteIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading EmoteId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public PlayEmote clone() {
      PlayEmote copy = new PlayEmote();
      copy.emoteId = this.emoteId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof PlayEmote other ? Objects.equals(this.emoteId, other.emoteId) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.emoteId);
   }
}
