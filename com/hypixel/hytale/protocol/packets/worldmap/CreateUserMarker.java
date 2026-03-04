package com.hypixel.hytale.protocol.packets.worldmap;

import com.hypixel.hytale.protocol.Color;
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

public class CreateUserMarker implements Packet, ToServerPacket {
   public static final int PACKET_ID = 246;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 13;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 21;
   public static final int MAX_SIZE = 32768031;
   public float x;
   public float z;
   @Nullable
   public String name;
   @Nullable
   public String markerImage;
   @Nullable
   public Color tintColor;
   public boolean shared;

   @Override
   public int getId() {
      return 246;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public CreateUserMarker() {
   }

   public CreateUserMarker(float x, float z, @Nullable String name, @Nullable String markerImage, @Nullable Color tintColor, boolean shared) {
      this.x = x;
      this.z = z;
      this.name = name;
      this.markerImage = markerImage;
      this.tintColor = tintColor;
      this.shared = shared;
   }

   public CreateUserMarker(@Nonnull CreateUserMarker other) {
      this.x = other.x;
      this.z = other.z;
      this.name = other.name;
      this.markerImage = other.markerImage;
      this.tintColor = other.tintColor;
      this.shared = other.shared;
   }

   @Nonnull
   public static CreateUserMarker deserialize(@Nonnull ByteBuf buf, int offset) {
      CreateUserMarker obj = new CreateUserMarker();
      byte nullBits = buf.getByte(offset);
      obj.x = buf.getFloatLE(offset + 1);
      obj.z = buf.getFloatLE(offset + 5);
      if ((nullBits & 1) != 0) {
         obj.tintColor = Color.deserialize(buf, offset + 9);
      }

      obj.shared = buf.getByte(offset + 12) != 0;
      if ((nullBits & 2) != 0) {
         int varPos0 = offset + 21 + buf.getIntLE(offset + 13);
         int nameLen = VarInt.peek(buf, varPos0);
         if (nameLen < 0) {
            throw ProtocolException.negativeLength("Name", nameLen);
         }

         if (nameLen > 4096000) {
            throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
         }

         obj.name = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 4) != 0) {
         int varPos1 = offset + 21 + buf.getIntLE(offset + 17);
         int markerImageLen = VarInt.peek(buf, varPos1);
         if (markerImageLen < 0) {
            throw ProtocolException.negativeLength("MarkerImage", markerImageLen);
         }

         if (markerImageLen > 4096000) {
            throw ProtocolException.stringTooLong("MarkerImage", markerImageLen, 4096000);
         }

         obj.markerImage = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 21;
      if ((nullBits & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 13);
         int pos0 = offset + 21 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 17);
         int pos1 = offset + 21 + fieldOffset1;
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
      if (this.tintColor != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.name != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.markerImage != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.x);
      buf.writeFloatLE(this.z);
      if (this.tintColor != null) {
         this.tintColor.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      buf.writeByte(this.shared ? 1 : 0);
      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int markerImageOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.name != null) {
         buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.name, 4096000);
      } else {
         buf.setIntLE(nameOffsetSlot, -1);
      }

      if (this.markerImage != null) {
         buf.setIntLE(markerImageOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.markerImage, 4096000);
      } else {
         buf.setIntLE(markerImageOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 21;
      if (this.name != null) {
         size += PacketIO.stringSize(this.name);
      }

      if (this.markerImage != null) {
         size += PacketIO.stringSize(this.markerImage);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 21) {
         return ValidationResult.error("Buffer too small: expected at least 21 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 2) != 0) {
            int nameOffset = buffer.getIntLE(offset + 13);
            if (nameOffset < 0) {
               return ValidationResult.error("Invalid offset for Name");
            }

            int pos = offset + 21 + nameOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Name");
            }

            int nameLen = VarInt.peek(buffer, pos);
            if (nameLen < 0) {
               return ValidationResult.error("Invalid string length for Name");
            }

            if (nameLen > 4096000) {
               return ValidationResult.error("Name exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += nameLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Name");
            }
         }

         if ((nullBits & 4) != 0) {
            int markerImageOffset = buffer.getIntLE(offset + 17);
            if (markerImageOffset < 0) {
               return ValidationResult.error("Invalid offset for MarkerImage");
            }

            int posx = offset + 21 + markerImageOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for MarkerImage");
            }

            int markerImageLen = VarInt.peek(buffer, posx);
            if (markerImageLen < 0) {
               return ValidationResult.error("Invalid string length for MarkerImage");
            }

            if (markerImageLen > 4096000) {
               return ValidationResult.error("MarkerImage exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += markerImageLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading MarkerImage");
            }
         }

         return ValidationResult.OK;
      }
   }

   public CreateUserMarker clone() {
      CreateUserMarker copy = new CreateUserMarker();
      copy.x = this.x;
      copy.z = this.z;
      copy.name = this.name;
      copy.markerImage = this.markerImage;
      copy.tintColor = this.tintColor != null ? this.tintColor.clone() : null;
      copy.shared = this.shared;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CreateUserMarker other)
            ? false
            : this.x == other.x
               && this.z == other.z
               && Objects.equals(this.name, other.name)
               && Objects.equals(this.markerImage, other.markerImage)
               && Objects.equals(this.tintColor, other.tintColor)
               && this.shared == other.shared;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.z, this.name, this.markerImage, this.tintColor, this.shared);
   }
}
