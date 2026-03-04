package com.hypixel.hytale.protocol.packets.machinima;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateMachinimaScene implements Packet, ToServerPacket, ToClientPacket {
   public static final int PACKET_ID = 262;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 18;
   public static final int MAX_SIZE = 36864033;
   @Nullable
   public String player;
   @Nullable
   public String sceneName;
   public float frame;
   @Nonnull
   public SceneUpdateType updateType = SceneUpdateType.Update;
   @Nullable
   public byte[] scene;

   @Override
   public int getId() {
      return 262;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateMachinimaScene() {
   }

   public UpdateMachinimaScene(@Nullable String player, @Nullable String sceneName, float frame, @Nonnull SceneUpdateType updateType, @Nullable byte[] scene) {
      this.player = player;
      this.sceneName = sceneName;
      this.frame = frame;
      this.updateType = updateType;
      this.scene = scene;
   }

   public UpdateMachinimaScene(@Nonnull UpdateMachinimaScene other) {
      this.player = other.player;
      this.sceneName = other.sceneName;
      this.frame = other.frame;
      this.updateType = other.updateType;
      this.scene = other.scene;
   }

   @Nonnull
   public static UpdateMachinimaScene deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateMachinimaScene obj = new UpdateMachinimaScene();
      byte nullBits = buf.getByte(offset);
      obj.frame = buf.getFloatLE(offset + 1);
      obj.updateType = SceneUpdateType.fromValue(buf.getByte(offset + 5));
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 18 + buf.getIntLE(offset + 6);
         int playerLen = VarInt.peek(buf, varPos0);
         if (playerLen < 0) {
            throw ProtocolException.negativeLength("Player", playerLen);
         }

         if (playerLen > 4096000) {
            throw ProtocolException.stringTooLong("Player", playerLen, 4096000);
         }

         obj.player = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 18 + buf.getIntLE(offset + 10);
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
         int varPos2 = offset + 18 + buf.getIntLE(offset + 14);
         int sceneCount = VarInt.peek(buf, varPos2);
         if (sceneCount < 0) {
            throw ProtocolException.negativeLength("Scene", sceneCount);
         }

         if (sceneCount > 4096000) {
            throw ProtocolException.arrayTooLong("Scene", sceneCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos2);
         if (varPos2 + varIntLen + sceneCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Scene", varPos2 + varIntLen + sceneCount * 1, buf.readableBytes());
         }

         obj.scene = new byte[sceneCount];

         for (int i = 0; i < sceneCount; i++) {
            obj.scene[i] = buf.getByte(varPos2 + varIntLen + i * 1);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 18;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 6);
         int pos0 = offset + 18 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 10);
         int pos1 = offset + 18 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 14);
         int pos2 = offset + 18 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + arrLen * 1;
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
      if (this.player != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.sceneName != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.scene != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.frame);
      buf.writeByte(this.updateType.getValue());
      int playerOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int sceneNameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int sceneOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.player != null) {
         buf.setIntLE(playerOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.player, 4096000);
      } else {
         buf.setIntLE(playerOffsetSlot, -1);
      }

      if (this.sceneName != null) {
         buf.setIntLE(sceneNameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.sceneName, 4096000);
      } else {
         buf.setIntLE(sceneNameOffsetSlot, -1);
      }

      if (this.scene != null) {
         buf.setIntLE(sceneOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.scene.length > 4096000) {
            throw ProtocolException.arrayTooLong("Scene", this.scene.length, 4096000);
         }

         VarInt.write(buf, this.scene.length);

         for (byte item : this.scene) {
            buf.writeByte(item);
         }
      } else {
         buf.setIntLE(sceneOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 18;
      if (this.player != null) {
         size += PacketIO.stringSize(this.player);
      }

      if (this.sceneName != null) {
         size += PacketIO.stringSize(this.sceneName);
      }

      if (this.scene != null) {
         size += VarInt.size(this.scene.length) + this.scene.length * 1;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 18) {
         return ValidationResult.error("Buffer too small: expected at least 18 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int playerOffset = buffer.getIntLE(offset + 6);
            if (playerOffset < 0) {
               return ValidationResult.error("Invalid offset for Player");
            }

            int pos = offset + 18 + playerOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Player");
            }

            int playerLen = VarInt.peek(buffer, pos);
            if (playerLen < 0) {
               return ValidationResult.error("Invalid string length for Player");
            }

            if (playerLen > 4096000) {
               return ValidationResult.error("Player exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += playerLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Player");
            }
         }

         if ((nullBits & 2) != 0) {
            int sceneNameOffset = buffer.getIntLE(offset + 10);
            if (sceneNameOffset < 0) {
               return ValidationResult.error("Invalid offset for SceneName");
            }

            int posx = offset + 18 + sceneNameOffset;
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
            int sceneOffset = buffer.getIntLE(offset + 14);
            if (sceneOffset < 0) {
               return ValidationResult.error("Invalid offset for Scene");
            }

            int posxx = offset + 18 + sceneOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Scene");
            }

            int sceneCount = VarInt.peek(buffer, posxx);
            if (sceneCount < 0) {
               return ValidationResult.error("Invalid array count for Scene");
            }

            if (sceneCount > 4096000) {
               return ValidationResult.error("Scene exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += sceneCount * 1;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Scene");
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateMachinimaScene clone() {
      UpdateMachinimaScene copy = new UpdateMachinimaScene();
      copy.player = this.player;
      copy.sceneName = this.sceneName;
      copy.frame = this.frame;
      copy.updateType = this.updateType;
      copy.scene = this.scene != null ? Arrays.copyOf(this.scene, this.scene.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateMachinimaScene other)
            ? false
            : Objects.equals(this.player, other.player)
               && Objects.equals(this.sceneName, other.sceneName)
               && this.frame == other.frame
               && Objects.equals(this.updateType, other.updateType)
               && Arrays.equals(this.scene, other.scene);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.player);
      result = 31 * result + Objects.hashCode(this.sceneName);
      result = 31 * result + Float.hashCode(this.frame);
      result = 31 * result + Objects.hashCode(this.updateType);
      return 31 * result + Arrays.hashCode(this.scene);
   }
}
