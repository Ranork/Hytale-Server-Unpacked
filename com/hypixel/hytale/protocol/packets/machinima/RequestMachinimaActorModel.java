package com.hypixel.hytale.protocol.packets.machinima;

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

public class RequestMachinimaActorModel implements Packet, ToServerPacket {
   public static final int PACKET_ID = 260;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 49152028;
   @Nullable
   public String modelId;
   @Nullable
   public String sceneName;
   @Nullable
   public String actorName;

   @Override
   public int getId() {
      return 260;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public RequestMachinimaActorModel() {
   }

   public RequestMachinimaActorModel(@Nullable String modelId, @Nullable String sceneName, @Nullable String actorName) {
      this.modelId = modelId;
      this.sceneName = sceneName;
      this.actorName = actorName;
   }

   public RequestMachinimaActorModel(@Nonnull RequestMachinimaActorModel other) {
      this.modelId = other.modelId;
      this.sceneName = other.sceneName;
      this.actorName = other.actorName;
   }

   @Nonnull
   public static RequestMachinimaActorModel deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("RequestMachinimaActorModel", 13, buf.readableBytes() - offset);
      } else {
         RequestMachinimaActorModel obj = new RequestMachinimaActorModel();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("ModelId", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 13 + varPosBase0;
            int modelIdLen = VarInt.peek(buf, varPos0);
            if (modelIdLen < 0) {
               throw ProtocolException.invalidVarInt("ModelId");
            }

            int modelIdVarIntLen = VarInt.size(modelIdLen);
            if (modelIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ModelId", modelIdLen, 4096000);
            }

            if (varPos0 + modelIdVarIntLen + modelIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ModelId", varPos0 + modelIdVarIntLen + modelIdLen, buf.readableBytes());
            }

            obj.modelId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("SceneName", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 13 + varPosBase1;
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
            int varPosBase2 = buf.getIntLE(offset + 9);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("ActorName", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 13 + varPosBase2;
            int actorNameLen = VarInt.peek(buf, varPos2);
            if (actorNameLen < 0) {
               throw ProtocolException.invalidVarInt("ActorName");
            }

            int actorNameVarIntLen = VarInt.size(actorNameLen);
            if (actorNameLen > 4096000) {
               throw ProtocolException.stringTooLong("ActorName", actorNameLen, 4096000);
            }

            if (varPos2 + actorNameVarIntLen + actorNameLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ActorName", varPos2 + actorNameVarIntLen + actorNameLen, buf.readableBytes());
            }

            obj.actorName = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("ModelId", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 13 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("SceneName", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 13 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("ActorName", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 13 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   @Nullable
   public static String getModelId(MemorySegment mem) {
      return getModelId(mem, 0);
   }

   @Nullable
   public static String getModelId(MemorySegment mem, int offset) {
      return hasModelId(mem, offset)
         ? PacketIO.readVarString("ModelId", mem, offset + getValidatedOffset(mem, offset, 1, 13, "ModelId"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getSceneName(MemorySegment mem) {
      return getSceneName(mem, 0);
   }

   @Nullable
   public static String getSceneName(MemorySegment mem, int offset) {
      return hasSceneName(mem, offset)
         ? PacketIO.readVarString("SceneName", mem, offset + getValidatedOffset(mem, offset, 5, 13, "SceneName"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getActorName(MemorySegment mem) {
      return getActorName(mem, 0);
   }

   @Nullable
   public static String getActorName(MemorySegment mem, int offset) {
      return hasActorName(mem, offset)
         ? PacketIO.readVarString("ActorName", mem, offset + getValidatedOffset(mem, offset, 9, 13, "ActorName"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean hasModelId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasSceneName(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasActorName(MemorySegment mem, int offset) {
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

   public static RequestMachinimaActorModel toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static RequestMachinimaActorModel toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("RequestMachinimaActorModel", offset + 13, (int)mem.byteSize());
      } else {
         return new RequestMachinimaActorModel(
            hasModelId(mem, offset)
               ? PacketIO.readVarString("ModelId", mem, offset + getValidatedOffset(mem, offset, 1, 13, "ModelId"), 4096000, PacketIO.UTF8)
               : null,
            hasSceneName(mem, offset)
               ? PacketIO.readVarString("SceneName", mem, offset + getValidatedOffset(mem, offset, 5, 13, "SceneName"), 4096000, PacketIO.UTF8)
               : null,
            hasActorName(mem, offset)
               ? PacketIO.readVarString("ActorName", mem, offset + getValidatedOffset(mem, offset, 9, 13, "ActorName"), 4096000, PacketIO.UTF8)
               : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.modelId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.sceneName != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.actorName != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      int modelIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int sceneNameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int actorNameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.modelId != null) {
         buf.setIntLE(modelIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.modelId, 4096000);
      } else {
         buf.setIntLE(modelIdOffsetSlot, -1);
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.modelId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.sceneName != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.actorName != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 13;
      if (this.modelId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 13);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.modelId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.sceneName != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 13);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.sceneName, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.actorName != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 13);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.actorName, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 13;
      if (this.modelId != null) {
         size += PacketIO.stringSize(this.modelId);
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
            int modelIdOffset = buffer.getIntLE(offset + 1);
            if (modelIdOffset < 0 || modelIdOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for ModelId");
            }

            int pos = offset + 13 + modelIdOffset;
            int modelIdLen = VarInt.peek(buffer, pos);
            if (modelIdLen < 0) {
               return ValidationResult.error("Invalid string length for ModelId");
            }

            if (modelIdLen > 4096000) {
               return ValidationResult.error("ModelId exceeds max length 4096000");
            }

            pos += VarInt.size(modelIdLen);
            pos += modelIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ModelId");
            }
         }

         if ((nullBits & 2) != 0) {
            int sceneNameOffset = buffer.getIntLE(offset + 5);
            if (sceneNameOffset < 0 || sceneNameOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for SceneName");
            }

            int posx = offset + 13 + sceneNameOffset;
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
            int actorNameOffset = buffer.getIntLE(offset + 9);
            if (actorNameOffset < 0 || actorNameOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for ActorName");
            }

            int posxx = offset + 13 + actorNameOffset;
            int actorNameLen = VarInt.peek(buffer, posxx);
            if (actorNameLen < 0) {
               return ValidationResult.error("Invalid string length for ActorName");
            }

            if (actorNameLen > 4096000) {
               return ValidationResult.error("ActorName exceeds max length 4096000");
            }

            posxx += VarInt.size(actorNameLen);
            posxx += actorNameLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ActorName");
            }
         }

         return ValidationResult.OK;
      }
   }

   public RequestMachinimaActorModel clone() {
      RequestMachinimaActorModel copy = new RequestMachinimaActorModel();
      copy.modelId = this.modelId;
      copy.sceneName = this.sceneName;
      copy.actorName = this.actorName;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof RequestMachinimaActorModel other)
            ? false
            : Objects.equals(this.modelId, other.modelId) && Objects.equals(this.sceneName, other.sceneName) && Objects.equals(this.actorName, other.actorName);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.modelId, this.sceneName, this.actorName);
   }
}
