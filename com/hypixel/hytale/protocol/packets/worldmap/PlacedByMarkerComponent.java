package com.hypixel.hytale.protocol.packets.worldmap;

import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;

public class PlacedByMarkerComponent extends MapMarkerComponent {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 16;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 16;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public FormattedMessage name = new FormattedMessage();
   @Nonnull
   public UUID playerId = new UUID(0L, 0L);

   public PlacedByMarkerComponent() {
   }

   public PlacedByMarkerComponent(@Nonnull FormattedMessage name, @Nonnull UUID playerId) {
      this.name = name;
      this.playerId = playerId;
   }

   public PlacedByMarkerComponent(@Nonnull PlacedByMarkerComponent other) {
      this.name = other.name;
      this.playerId = other.playerId;
   }

   @Nonnull
   public static PlacedByMarkerComponent deserialize(@Nonnull ByteBuf buf, int offset) {
      PlacedByMarkerComponent obj = new PlacedByMarkerComponent();
      obj.playerId = PacketIO.readUUID(buf, offset + 0);
      int pos = offset + 16;
      obj.name = FormattedMessage.deserialize(buf, pos);
      pos += FormattedMessage.computeBytesConsumed(buf, pos);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 16;
      pos += FormattedMessage.computeBytesConsumed(buf, pos);
      return pos - offset;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      PacketIO.writeUUID(buf, this.playerId);
      this.name.serialize(buf);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      int size = 16;
      return size + this.name.computeSize();
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 16) {
         return ValidationResult.error("Buffer too small: expected at least 16 bytes");
      } else {
         int pos = offset + 16;
         ValidationResult nameResult = FormattedMessage.validateStructure(buffer, pos);
         if (!nameResult.isValid()) {
            return ValidationResult.error("Invalid Name: " + nameResult.error());
         } else {
            pos += FormattedMessage.computeBytesConsumed(buffer, pos);
            return ValidationResult.OK;
         }
      }
   }

   public PlacedByMarkerComponent clone() {
      PlacedByMarkerComponent copy = new PlacedByMarkerComponent();
      copy.name = this.name.clone();
      copy.playerId = this.playerId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof PlacedByMarkerComponent other)
            ? false
            : Objects.equals(this.name, other.name) && Objects.equals(this.playerId, other.playerId);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name, this.playerId);
   }
}
