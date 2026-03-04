package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.MouseButtonEvent;
import com.hypixel.hytale.protocol.MouseMotionEvent;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.Vector2f;
import com.hypixel.hytale.protocol.WorldInteraction;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MouseInteraction implements Packet, ToServerPacket {
   public static final int PACKET_ID = 111;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 44;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 52;
   public static final int MAX_SIZE = 20480071;
   public long clientTimestamp;
   public int activeSlot;
   @Nullable
   public String itemInHandId;
   @Nullable
   public Vector2f screenPoint;
   @Nullable
   public MouseButtonEvent mouseButton;
   @Nullable
   public MouseMotionEvent mouseMotion;
   @Nullable
   public WorldInteraction worldInteraction;

   @Override
   public int getId() {
      return 111;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public MouseInteraction() {
   }

   public MouseInteraction(
      long clientTimestamp,
      int activeSlot,
      @Nullable String itemInHandId,
      @Nullable Vector2f screenPoint,
      @Nullable MouseButtonEvent mouseButton,
      @Nullable MouseMotionEvent mouseMotion,
      @Nullable WorldInteraction worldInteraction
   ) {
      this.clientTimestamp = clientTimestamp;
      this.activeSlot = activeSlot;
      this.itemInHandId = itemInHandId;
      this.screenPoint = screenPoint;
      this.mouseButton = mouseButton;
      this.mouseMotion = mouseMotion;
      this.worldInteraction = worldInteraction;
   }

   public MouseInteraction(@Nonnull MouseInteraction other) {
      this.clientTimestamp = other.clientTimestamp;
      this.activeSlot = other.activeSlot;
      this.itemInHandId = other.itemInHandId;
      this.screenPoint = other.screenPoint;
      this.mouseButton = other.mouseButton;
      this.mouseMotion = other.mouseMotion;
      this.worldInteraction = other.worldInteraction;
   }

   @Nonnull
   public static MouseInteraction deserialize(@Nonnull ByteBuf buf, int offset) {
      MouseInteraction obj = new MouseInteraction();
      byte nullBits = buf.getByte(offset);
      obj.clientTimestamp = buf.getLongLE(offset + 1);
      obj.activeSlot = buf.getIntLE(offset + 9);
      if ((nullBits & 1) != 0) {
         obj.screenPoint = Vector2f.deserialize(buf, offset + 13);
      }

      if ((nullBits & 2) != 0) {
         obj.mouseButton = MouseButtonEvent.deserialize(buf, offset + 21);
      }

      if ((nullBits & 4) != 0) {
         obj.worldInteraction = WorldInteraction.deserialize(buf, offset + 24);
      }

      if ((nullBits & 8) != 0) {
         int varPos0 = offset + 52 + buf.getIntLE(offset + 44);
         int itemInHandIdLen = VarInt.peek(buf, varPos0);
         if (itemInHandIdLen < 0) {
            throw ProtocolException.negativeLength("ItemInHandId", itemInHandIdLen);
         }

         if (itemInHandIdLen > 4096000) {
            throw ProtocolException.stringTooLong("ItemInHandId", itemInHandIdLen, 4096000);
         }

         obj.itemInHandId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 16) != 0) {
         int varPos1 = offset + 52 + buf.getIntLE(offset + 48);
         obj.mouseMotion = MouseMotionEvent.deserialize(buf, varPos1);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 52;
      if ((nullBits & 8) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 44);
         int pos0 = offset + 52 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 48);
         int pos1 = offset + 52 + fieldOffset1;
         pos1 += MouseMotionEvent.computeBytesConsumed(buf, pos1);
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
      if (this.screenPoint != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.mouseButton != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.worldInteraction != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.itemInHandId != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.mouseMotion != null) {
         nullBits = (byte)(nullBits | 16);
      }

      buf.writeByte(nullBits);
      buf.writeLongLE(this.clientTimestamp);
      buf.writeIntLE(this.activeSlot);
      if (this.screenPoint != null) {
         this.screenPoint.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.mouseButton != null) {
         this.mouseButton.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      if (this.worldInteraction != null) {
         this.worldInteraction.serialize(buf);
      } else {
         buf.writeZero(20);
      }

      int itemInHandIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int mouseMotionOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.itemInHandId != null) {
         buf.setIntLE(itemInHandIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.itemInHandId, 4096000);
      } else {
         buf.setIntLE(itemInHandIdOffsetSlot, -1);
      }

      if (this.mouseMotion != null) {
         buf.setIntLE(mouseMotionOffsetSlot, buf.writerIndex() - varBlockStart);
         this.mouseMotion.serialize(buf);
      } else {
         buf.setIntLE(mouseMotionOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 52;
      if (this.itemInHandId != null) {
         size += PacketIO.stringSize(this.itemInHandId);
      }

      if (this.mouseMotion != null) {
         size += this.mouseMotion.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 52) {
         return ValidationResult.error("Buffer too small: expected at least 52 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 8) != 0) {
            int itemInHandIdOffset = buffer.getIntLE(offset + 44);
            if (itemInHandIdOffset < 0) {
               return ValidationResult.error("Invalid offset for ItemInHandId");
            }

            int pos = offset + 52 + itemInHandIdOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ItemInHandId");
            }

            int itemInHandIdLen = VarInt.peek(buffer, pos);
            if (itemInHandIdLen < 0) {
               return ValidationResult.error("Invalid string length for ItemInHandId");
            }

            if (itemInHandIdLen > 4096000) {
               return ValidationResult.error("ItemInHandId exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += itemInHandIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ItemInHandId");
            }
         }

         if ((nullBits & 16) != 0) {
            int mouseMotionOffset = buffer.getIntLE(offset + 48);
            if (mouseMotionOffset < 0) {
               return ValidationResult.error("Invalid offset for MouseMotion");
            }

            int posx = offset + 52 + mouseMotionOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for MouseMotion");
            }

            ValidationResult mouseMotionResult = MouseMotionEvent.validateStructure(buffer, posx);
            if (!mouseMotionResult.isValid()) {
               return ValidationResult.error("Invalid MouseMotion: " + mouseMotionResult.error());
            }

            posx += MouseMotionEvent.computeBytesConsumed(buffer, posx);
         }

         return ValidationResult.OK;
      }
   }

   public MouseInteraction clone() {
      MouseInteraction copy = new MouseInteraction();
      copy.clientTimestamp = this.clientTimestamp;
      copy.activeSlot = this.activeSlot;
      copy.itemInHandId = this.itemInHandId;
      copy.screenPoint = this.screenPoint != null ? this.screenPoint.clone() : null;
      copy.mouseButton = this.mouseButton != null ? this.mouseButton.clone() : null;
      copy.mouseMotion = this.mouseMotion != null ? this.mouseMotion.clone() : null;
      copy.worldInteraction = this.worldInteraction != null ? this.worldInteraction.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof MouseInteraction other)
            ? false
            : this.clientTimestamp == other.clientTimestamp
               && this.activeSlot == other.activeSlot
               && Objects.equals(this.itemInHandId, other.itemInHandId)
               && Objects.equals(this.screenPoint, other.screenPoint)
               && Objects.equals(this.mouseButton, other.mouseButton)
               && Objects.equals(this.mouseMotion, other.mouseMotion)
               && Objects.equals(this.worldInteraction, other.worldInteraction);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.clientTimestamp, this.activeSlot, this.itemInHandId, this.screenPoint, this.mouseButton, this.mouseMotion, this.worldInteraction);
   }
}
