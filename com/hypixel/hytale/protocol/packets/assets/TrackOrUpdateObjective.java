package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Objective;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TrackOrUpdateObjective implements Packet, ToClientPacket {
   public static final int PACKET_ID = 69;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public Objective objective;

   @Override
   public int getId() {
      return 69;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public TrackOrUpdateObjective() {
   }

   public TrackOrUpdateObjective(@Nullable Objective objective) {
      this.objective = objective;
   }

   public TrackOrUpdateObjective(@Nonnull TrackOrUpdateObjective other) {
      this.objective = other.objective;
   }

   @Nonnull
   public static TrackOrUpdateObjective deserialize(@Nonnull ByteBuf buf, int offset) {
      TrackOrUpdateObjective obj = new TrackOrUpdateObjective();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         obj.objective = Objective.deserialize(buf, pos);
         pos += Objective.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         pos += Objective.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.objective != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.objective != null) {
         this.objective.serialize(buf);
      }
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.objective != null) {
         size += this.objective.computeSize();
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
            ValidationResult objectiveResult = Objective.validateStructure(buffer, pos);
            if (!objectiveResult.isValid()) {
               return ValidationResult.error("Invalid Objective: " + objectiveResult.error());
            }

            pos += Objective.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public TrackOrUpdateObjective clone() {
      TrackOrUpdateObjective copy = new TrackOrUpdateObjective();
      copy.objective = this.objective != null ? this.objective.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof TrackOrUpdateObjective other ? Objects.equals(this.objective, other.objective) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.objective);
   }
}
