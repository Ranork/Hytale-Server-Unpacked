package com.hypixel.hytale.protocol.packets.machinima;

import com.hypixel.hytale.protocol.Model;
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

public class SetMachinimaActorModel implements Packet, ToClientPacket {
   public static final int PACKET_ID = 261;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public Model model;
   @Nullable
   public String sceneName;
   @Nullable
   public String actorName;

   @Override
   public int getId() {
      return 261;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SetMachinimaActorModel() {
   }

   public SetMachinimaActorModel(@Nullable Model model, @Nullable String sceneName, @Nullable String actorName) {
      this.model = model;
      this.sceneName = sceneName;
      this.actorName = actorName;
   }

   public SetMachinimaActorModel(@Nonnull SetMachinimaActorModel other) {
      this.model = other.model;
      this.sceneName = other.sceneName;
      this.actorName = other.actorName;
   }

   @Nonnull
   public static SetMachinimaActorModel deserialize(@Nonnull ByteBuf buf, int offset) {
      SetMachinimaActorModel obj = new SetMachinimaActorModel();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 13 + buf.getIntLE(offset + 1);
         obj.model = Model.deserialize(buf, varPos0);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 13 + buf.getIntLE(offset + 5);
         int sceneNameLen = VarInt.peek(buf, varPos1);
         if (sceneNameLen < 0) {
            throw ProtocolException.negativeLength("SceneName", sceneNameLen);
         }

         if (sceneNameLen > 4096000) {
            throw ProtocolException.stringTooLong("SceneName", sceneNameLen, 4096000);
         }

         obj.sceneName = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 13 + buf.getIntLE(offset + 9);
         int actorNameLen = VarInt.peek(buf, varPos2);
         if (actorNameLen < 0) {
            throw ProtocolException.negativeLength("ActorName", actorNameLen);
         }

         if (actorNameLen > 4096000) {
            throw ProtocolException.stringTooLong("ActorName", actorNameLen, 4096000);
         }

         obj.actorName = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 13 + fieldOffset0;
         pos0 += Model.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 13 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         int pos2 = offset + 13 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.model != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.sceneName != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.actorName != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      int modelOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int sceneNameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int actorNameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.model != null) {
         buf.setIntLE(modelOffsetSlot, buf.writerIndex() - varBlockStart);
         this.model.serialize(buf);
      } else {
         buf.setIntLE(modelOffsetSlot, -1);
      }

      if (this.sceneName != null) {
         buf.setIntLE(sceneNameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.sceneName, 4096000);
      } else {
         buf.setIntLE(sceneNameOffsetSlot, -1);
      }

      if (this.actorName != null) {
         buf.setIntLE(actorNameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.actorName, 4096000);
      } else {
         buf.setIntLE(actorNameOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 13;
      if (this.model != null) {
         size += this.model.computeSize();
      }

      if (this.sceneName != null) {
         size += PacketIO.stringSize(this.sceneName);
      }

      if (this.actorName != null) {
         size += PacketIO.stringSize(this.actorName);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int modelOffset = buffer.getIntLE(offset + 1);
            if (modelOffset < 0) {
               return ValidationResult.error("Invalid offset for Model");
            }

            int pos = offset + 13 + modelOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Model");
            }

            ValidationResult modelResult = Model.validateStructure(buffer, pos);
            if (!modelResult.isValid()) {
               return ValidationResult.error("Invalid Model: " + modelResult.error());
            }

            pos += Model.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int sceneNameOffset = buffer.getIntLE(offset + 5);
            if (sceneNameOffset < 0) {
               return ValidationResult.error("Invalid offset for SceneName");
            }

            int posx = offset + 13 + sceneNameOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for SceneName");
            }

            int sceneNameLen = VarInt.peek(buffer, posx);
            if (sceneNameLen < 0) {
               return ValidationResult.error("Invalid string length for SceneName");
            }

            if (sceneNameLen > 4096000) {
               return ValidationResult.error("SceneName exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += sceneNameLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading SceneName");
            }
         }

         if ((nullBits & 4) != 0) {
            int actorNameOffset = buffer.getIntLE(offset + 9);
            if (actorNameOffset < 0) {
               return ValidationResult.error("Invalid offset for ActorName");
            }

            int posxx = offset + 13 + actorNameOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ActorName");
            }

            int actorNameLen = VarInt.peek(buffer, posxx);
            if (actorNameLen < 0) {
               return ValidationResult.error("Invalid string length for ActorName");
            }

            if (actorNameLen > 4096000) {
               return ValidationResult.error("ActorName exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += actorNameLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ActorName");
            }
         }

         return ValidationResult.OK;
      }
   }

   public SetMachinimaActorModel clone() {
      SetMachinimaActorModel copy = new SetMachinimaActorModel();
      copy.model = this.model != null ? this.model.clone() : null;
      copy.sceneName = this.sceneName;
      copy.actorName = this.actorName;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SetMachinimaActorModel other)
            ? false
            : Objects.equals(this.model, other.model) && Objects.equals(this.sceneName, other.sceneName) && Objects.equals(this.actorName, other.actorName);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.model, this.sceneName, this.actorName);
   }
}
