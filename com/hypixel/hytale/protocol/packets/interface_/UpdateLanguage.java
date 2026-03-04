package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateLanguage implements Packet, ToServerPacket {
   public static final int PACKET_ID = 232;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 16384006;
   @Nullable
   public String language;

   @Override
   public int getId() {
      return 232;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateLanguage() {
   }

   public UpdateLanguage(@Nullable String language) {
      this.language = language;
   }

   public UpdateLanguage(@Nonnull UpdateLanguage other) {
      this.language = other.language;
   }

   @Nonnull
   public static UpdateLanguage deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateLanguage obj = new UpdateLanguage();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int languageLen = VarInt.peek(buf, pos);
         if (languageLen < 0) {
            throw ProtocolException.negativeLength("Language", languageLen);
         }

         if (languageLen > 4096000) {
            throw ProtocolException.stringTooLong("Language", languageLen, 4096000);
         }

         int languageVarLen = VarInt.length(buf, pos);
         obj.language = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += languageVarLen + languageLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.language != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.language != null) {
         PacketIO.writeVarString(buf, this.language, 4096000);
      }
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.language != null) {
         size += PacketIO.stringSize(this.language);
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
            int languageLen = VarInt.peek(buffer, pos);
            if (languageLen < 0) {
               return ValidationResult.error("Invalid string length for Language");
            }

            if (languageLen > 4096000) {
               return ValidationResult.error("Language exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += languageLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Language");
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateLanguage clone() {
      UpdateLanguage copy = new UpdateLanguage();
      copy.language = this.language;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof UpdateLanguage other ? Objects.equals(this.language, other.language) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.language);
   }
}
