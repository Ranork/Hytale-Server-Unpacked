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
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 18) {
         throw ProtocolException.bufferTooSmall("UpdateMachinimaScene", 18, buf.readableBytes() - offset);
      } else {
         UpdateMachinimaScene obj = new UpdateMachinimaScene();
         byte nullBits = buf.getByte(offset);
         obj.frame = buf.getFloatLE(offset + 1);
         obj.updateType = SceneUpdateType.fromValue(buf.getByte(offset + 5));
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 6);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 18) {
               throw ProtocolException.invalidOffset("Player", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 18 + varPosBase0;
            int playerLen = VarInt.peek(buf, varPos0);
            if (playerLen < 0) {
               throw ProtocolException.invalidVarInt("Player");
            }

            int playerVarIntLen = VarInt.size(playerLen);
            if (playerLen > 4096000) {
               throw ProtocolException.stringTooLong("Player", playerLen, 4096000);
            }

            if (varPos0 + playerVarIntLen + playerLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Player", varPos0 + playerVarIntLen + playerLen, buf.readableBytes());
            }

            obj.player = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 10);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 18) {
               throw ProtocolException.invalidOffset("SceneName", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 18 + varPosBase1;
            int sceneNameLen = VarInt.peek(buf, varPos1);
            if (sceneNameLen < 0) {
               throw ProtocolException.invalidVarInt("SceneName");
            }

            int sceneNameVarIntLen = VarInt.size(sceneNameLen);
            if (sceneNameLen > 4096000) {
               throw ProtocolException.stringTooLong("SceneName", sceneNameLen, 4096000);
            }

            if (varPos1 + sceneNameVarIntLen + sceneNameLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("SceneName", varPos1 + sceneNameVarIntLen + sceneNameLen, buf.readableBytes());
            }

            obj.sceneName = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 14);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 18) {
               throw ProtocolException.invalidOffset("Scene", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 18 + varPosBase2;
            int sceneCount = VarInt.peek(buf, varPos2);
            if (sceneCount < 0) {
               throw ProtocolException.invalidVarInt("Scene");
            }

            int varIntLen = VarInt.size(sceneCount);
            if (sceneCount > 4096000) {
               throw ProtocolException.arrayTooLong("Scene", sceneCount, 4096000);
            }

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
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 18;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 6);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 18) {
            throw ProtocolException.invalidOffset("Player", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 18 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 10);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 18) {
            throw ProtocolException.invalidOffset("SceneName", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 18 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 14);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 18) {
            throw ProtocolException.invalidOffset("Scene", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 18 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(arrLen) + arrLen * 1;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 18L;
   }

   @Nullable
   public static String getPlayer(MemorySegment mem) {
      return getPlayer(mem, 0);
   }

   @Nullable
   public static String getPlayer(MemorySegment mem, int offset) {
      return hasPlayer(mem, offset)
         ? PacketIO.readVarString("Player", mem, offset + getValidatedOffset(mem, offset, 6, 18, "Player"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getSceneName(MemorySegment mem) {
      return getSceneName(mem, 0);
   }

   @Nullable
   public static String getSceneName(MemorySegment mem, int offset) {
      return hasSceneName(mem, offset)
         ? PacketIO.readVarString("SceneName", mem, offset + getValidatedOffset(mem, offset, 10, 18, "SceneName"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static float getFrame(MemorySegment mem) {
      return getFrame(mem, 0);
   }

   public static float getFrame(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static SceneUpdateType getUpdateType(MemorySegment mem) {
      return getUpdateType(mem, 0);
   }

   public static SceneUpdateType getUpdateType(MemorySegment mem, int offset) {
      return SceneUpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 5)));
   }

   @Nullable
   public static byte[] getScene(MemorySegment mem) {
      return getScene(mem, 0);
   }

   @Nullable
   public static byte[] getScene(MemorySegment mem, int offset) {
      if (!hasScene(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 14, 18, "Scene");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Scene", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Scene", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Scene", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               byte[] data = new byte[len];
               MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static boolean hasPlayer(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasSceneName(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasScene(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static UpdateMachinimaScene toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateMachinimaScene toObject(MemorySegment mem, int offset) {
      if (offset + 18 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateMachinimaScene", offset + 18, (int)mem.byteSize());
      } else {
         byte[] scene = null;
         if (hasScene(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 14, 18, "Scene");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Scene", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Scene", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Scene", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            scene = new byte[len];
            MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, scene, 0, len);
         }

         return new UpdateMachinimaScene(
            hasPlayer(mem, offset)
               ? PacketIO.readVarString("Player", mem, offset + getValidatedOffset(mem, offset, 6, 18, "Player"), 4096000, PacketIO.UTF8)
               : null,
            hasSceneName(mem, offset)
               ? PacketIO.readVarString("SceneName", mem, offset + getValidatedOffset(mem, offset, 10, 18, "SceneName"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            SceneUpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 5))),
            scene
         );
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.frame);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 5), (byte)this.updateType.getValue());
      int varOffset = offset + 18;
      if (this.player != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 18);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.player, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), -1);
      }

      if (this.sceneName != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), varOffset - offset - 18);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.sceneName, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), -1);
      }

      if (this.scene != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), varOffset - offset - 18);
         if (this.scene.length > 4096000) {
            throw ProtocolException.arrayTooLong("Scene", this.scene.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.scene.length);
         MemorySegment.copy(this.scene, 0, mem, PacketIO.PROTO_BYTE, varOffset, this.scene.length);
         varOffset += this.scene.length * 1;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), -1);
      }

      return varOffset - offset;
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
         int v = buffer.getByte(offset + 5) & 255;
         if (v >= 5) {
            return ValidationResult.error("Invalid SceneUpdateType value for UpdateType");
         } else {
            if ((nullBits & 1) != 0) {
               v = buffer.getIntLE(offset + 6);
               if (v < 0 || v > buffer.writerIndex() - offset - 18) {
                  return ValidationResult.error("Invalid offset for Player");
               }

               int pos = offset + 18 + v;
               int playerLen = VarInt.peek(buffer, pos);
               if (playerLen < 0) {
                  return ValidationResult.error("Invalid string length for Player");
               }

               if (playerLen > 4096000) {
                  return ValidationResult.error("Player exceeds max length 4096000");
               }

               pos += VarInt.size(playerLen);
               pos += playerLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Player");
               }
            }

            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 10);
               if (v < 0 || v > buffer.writerIndex() - offset - 18) {
                  return ValidationResult.error("Invalid offset for SceneName");
               }

               int posx = offset + 18 + v;
               int sceneNameLen = VarInt.peek(buffer, posx);
               if (sceneNameLen < 0) {
                  return ValidationResult.error("Invalid string length for SceneName");
               }

               if (sceneNameLen > 4096000) {
                  return ValidationResult.error("SceneName exceeds max length 4096000");
               }

               posx += VarInt.size(sceneNameLen);
               posx += sceneNameLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading SceneName");
               }
            }

            if ((nullBits & 4) != 0) {
               v = buffer.getIntLE(offset + 14);
               if (v < 0 || v > buffer.writerIndex() - offset - 18) {
                  return ValidationResult.error("Invalid offset for Scene");
               }

               int posxx = offset + 18 + v;
               int sceneCount = VarInt.peek(buffer, posxx);
               if (sceneCount < 0) {
                  return ValidationResult.error("Invalid array count for Scene");
               }

               if (sceneCount > 4096000) {
                  return ValidationResult.error("Scene exceeds max length 4096000");
               }

               posxx += VarInt.size(sceneCount);
               posxx += sceneCount * 1;
               if (posxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Scene");
               }
            }

            return ValidationResult.OK;
         }
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
