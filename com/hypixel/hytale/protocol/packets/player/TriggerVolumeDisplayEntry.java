package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3fc;

public class TriggerVolumeDisplayEntry {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 59;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 71;
   public static final int MAX_SIZE = 49152086;
   @Nonnull
   public TriggerVolumeShapeType shapeType = TriggerVolumeShapeType.Box;
   @Nonnull
   public Vector3fc position = PacketIO.ZERO_VECTOR3;
   @Nonnull
   public Vector3fc dimensions = PacketIO.ZERO_VECTOR3;
   @Nonnull
   public Vector3fc color = PacketIO.ZERO_VECTOR3;
   public float opacity;
   @Nullable
   public String name;
   @Nullable
   public String groupId;
   public int groupColor;
   @Nullable
   public String effectAssetRef;
   public byte targetTypes;
   public boolean keepLoaded;
   public boolean cancelDelayedOnExit;
   public float cooldown;
   public byte cooldownMode;
   public float activationDelay;
   @Nonnull
   public TriggerVolumeConditionTiming conditionTiming = TriggerVolumeConditionTiming.BeforeVolumeDelay;

   public TriggerVolumeDisplayEntry() {
   }

   public TriggerVolumeDisplayEntry(
      @Nonnull TriggerVolumeShapeType shapeType,
      @Nonnull Vector3fc position,
      @Nonnull Vector3fc dimensions,
      @Nonnull Vector3fc color,
      float opacity,
      @Nullable String name,
      @Nullable String groupId,
      int groupColor,
      @Nullable String effectAssetRef,
      byte targetTypes,
      boolean keepLoaded,
      boolean cancelDelayedOnExit,
      float cooldown,
      byte cooldownMode,
      float activationDelay,
      @Nonnull TriggerVolumeConditionTiming conditionTiming
   ) {
      this.shapeType = shapeType;
      this.position = position;
      this.dimensions = dimensions;
      this.color = color;
      this.opacity = opacity;
      this.name = name;
      this.groupId = groupId;
      this.groupColor = groupColor;
      this.effectAssetRef = effectAssetRef;
      this.targetTypes = targetTypes;
      this.keepLoaded = keepLoaded;
      this.cancelDelayedOnExit = cancelDelayedOnExit;
      this.cooldown = cooldown;
      this.cooldownMode = cooldownMode;
      this.activationDelay = activationDelay;
      this.conditionTiming = conditionTiming;
   }

   public TriggerVolumeDisplayEntry(@Nonnull TriggerVolumeDisplayEntry other) {
      this.shapeType = other.shapeType;
      this.position = other.position;
      this.dimensions = other.dimensions;
      this.color = other.color;
      this.opacity = other.opacity;
      this.name = other.name;
      this.groupId = other.groupId;
      this.groupColor = other.groupColor;
      this.effectAssetRef = other.effectAssetRef;
      this.targetTypes = other.targetTypes;
      this.keepLoaded = other.keepLoaded;
      this.cancelDelayedOnExit = other.cancelDelayedOnExit;
      this.cooldown = other.cooldown;
      this.cooldownMode = other.cooldownMode;
      this.activationDelay = other.activationDelay;
      this.conditionTiming = other.conditionTiming;
   }

   @Nonnull
   public static TriggerVolumeDisplayEntry deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 71) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeDisplayEntry", 71, buf.readableBytes() - offset);
      } else {
         TriggerVolumeDisplayEntry obj = new TriggerVolumeDisplayEntry();
         byte nullBits = buf.getByte(offset);
         obj.shapeType = TriggerVolumeShapeType.fromValue(buf.getByte(offset + 1));
         obj.position = PacketIO.readVector3f(buf, offset + 2);
         obj.dimensions = PacketIO.readVector3f(buf, offset + 14);
         obj.color = PacketIO.readVector3f(buf, offset + 26);
         obj.opacity = buf.getFloatLE(offset + 38);
         obj.groupColor = buf.getIntLE(offset + 42);
         obj.targetTypes = buf.getByte(offset + 46);
         obj.keepLoaded = buf.getByte(offset + 47) != 0;
         obj.cancelDelayedOnExit = buf.getByte(offset + 48) != 0;
         obj.cooldown = buf.getFloatLE(offset + 49);
         obj.cooldownMode = buf.getByte(offset + 53);
         obj.activationDelay = buf.getFloatLE(offset + 54);
         obj.conditionTiming = TriggerVolumeConditionTiming.fromValue(buf.getByte(offset + 58));
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 59);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 71) {
               throw ProtocolException.invalidOffset("Name", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 71 + varPosBase0;
            int nameLen = VarInt.peek(buf, varPos0);
            if (nameLen < 0) {
               throw ProtocolException.invalidVarInt("Name");
            }

            int nameVarIntLen = VarInt.size(nameLen);
            if (nameLen > 4096000) {
               throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
            }

            if (varPos0 + nameVarIntLen + nameLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Name", varPos0 + nameVarIntLen + nameLen, buf.readableBytes());
            }

            obj.name = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 63);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 71) {
               throw ProtocolException.invalidOffset("GroupId", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 71 + varPosBase1;
            int groupIdLen = VarInt.peek(buf, varPos1);
            if (groupIdLen < 0) {
               throw ProtocolException.invalidVarInt("GroupId");
            }

            int groupIdVarIntLen = VarInt.size(groupIdLen);
            if (groupIdLen > 4096000) {
               throw ProtocolException.stringTooLong("GroupId", groupIdLen, 4096000);
            }

            if (varPos1 + groupIdVarIntLen + groupIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("GroupId", varPos1 + groupIdVarIntLen + groupIdLen, buf.readableBytes());
            }

            obj.groupId = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 67);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 71) {
               throw ProtocolException.invalidOffset("EffectAssetRef", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 71 + varPosBase2;
            int effectAssetRefLen = VarInt.peek(buf, varPos2);
            if (effectAssetRefLen < 0) {
               throw ProtocolException.invalidVarInt("EffectAssetRef");
            }

            int effectAssetRefVarIntLen = VarInt.size(effectAssetRefLen);
            if (effectAssetRefLen > 4096000) {
               throw ProtocolException.stringTooLong("EffectAssetRef", effectAssetRefLen, 4096000);
            }

            if (varPos2 + effectAssetRefVarIntLen + effectAssetRefLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("EffectAssetRef", varPos2 + effectAssetRefVarIntLen + effectAssetRefLen, buf.readableBytes());
            }

            obj.effectAssetRef = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 71;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 59);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 71) {
            throw ProtocolException.invalidOffset("Name", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 71 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 63);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 71) {
            throw ProtocolException.invalidOffset("GroupId", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 71 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 67);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 71) {
            throw ProtocolException.invalidOffset("EffectAssetRef", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 71 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 71L;
   }

   public static TriggerVolumeShapeType getShapeType(MemorySegment mem) {
      return getShapeType(mem, 0);
   }

   public static TriggerVolumeShapeType getShapeType(MemorySegment mem, int offset) {
      return TriggerVolumeShapeType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static Vector3fc getPosition(MemorySegment mem) {
      return getPosition(mem, 0);
   }

   public static Vector3fc getPosition(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 2);
   }

   public static Vector3fc getDimensions(MemorySegment mem) {
      return getDimensions(mem, 0);
   }

   public static Vector3fc getDimensions(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 14);
   }

   public static Vector3fc getColor(MemorySegment mem) {
      return getColor(mem, 0);
   }

   public static Vector3fc getColor(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 26);
   }

   public static float getOpacity(MemorySegment mem) {
      return getOpacity(mem, 0);
   }

   public static float getOpacity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 38));
   }

   @Nullable
   public static String getName(MemorySegment mem) {
      return getName(mem, 0);
   }

   @Nullable
   public static String getName(MemorySegment mem, int offset) {
      return hasName(mem, offset)
         ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 59, 71, "Name"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getGroupId(MemorySegment mem) {
      return getGroupId(mem, 0);
   }

   @Nullable
   public static String getGroupId(MemorySegment mem, int offset) {
      return hasGroupId(mem, offset)
         ? PacketIO.readVarString("GroupId", mem, offset + getValidatedOffset(mem, offset, 63, 71, "GroupId"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static int getGroupColor(MemorySegment mem) {
      return getGroupColor(mem, 0);
   }

   public static int getGroupColor(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 42));
   }

   @Nullable
   public static String getEffectAssetRef(MemorySegment mem) {
      return getEffectAssetRef(mem, 0);
   }

   @Nullable
   public static String getEffectAssetRef(MemorySegment mem, int offset) {
      return hasEffectAssetRef(mem, offset)
         ? PacketIO.readVarString("EffectAssetRef", mem, offset + getValidatedOffset(mem, offset, 67, 71, "EffectAssetRef"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static byte getTargetTypes(MemorySegment mem) {
      return getTargetTypes(mem, 0);
   }

   public static byte getTargetTypes(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BYTE, (long)(offset + 46));
   }

   public static boolean getKeepLoaded(MemorySegment mem) {
      return getKeepLoaded(mem, 0);
   }

   public static boolean getKeepLoaded(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 47));
   }

   public static boolean getCancelDelayedOnExit(MemorySegment mem) {
      return getCancelDelayedOnExit(mem, 0);
   }

   public static boolean getCancelDelayedOnExit(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 48));
   }

   public static float getCooldown(MemorySegment mem) {
      return getCooldown(mem, 0);
   }

   public static float getCooldown(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 49));
   }

   public static byte getCooldownMode(MemorySegment mem) {
      return getCooldownMode(mem, 0);
   }

   public static byte getCooldownMode(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BYTE, (long)(offset + 53));
   }

   public static float getActivationDelay(MemorySegment mem) {
      return getActivationDelay(mem, 0);
   }

   public static float getActivationDelay(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 54));
   }

   public static TriggerVolumeConditionTiming getConditionTiming(MemorySegment mem) {
      return getConditionTiming(mem, 0);
   }

   public static TriggerVolumeConditionTiming getConditionTiming(MemorySegment mem, int offset) {
      return TriggerVolumeConditionTiming.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 58)));
   }

   public static boolean hasName(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasGroupId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasEffectAssetRef(MemorySegment mem, int offset) {
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

   public static TriggerVolumeDisplayEntry toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static TriggerVolumeDisplayEntry toObject(MemorySegment mem, int offset) {
      if (offset + 71 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeDisplayEntry", offset + 71, (int)mem.byteSize());
      } else {
         return new TriggerVolumeDisplayEntry(
            TriggerVolumeShapeType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            PacketIO.readVector3f(mem, offset + 2),
            PacketIO.readVector3f(mem, offset + 14),
            PacketIO.readVector3f(mem, offset + 26),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 38)),
            hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 59, 71, "Name"), 4096000, PacketIO.UTF8) : null,
            hasGroupId(mem, offset)
               ? PacketIO.readVarString("GroupId", mem, offset + getValidatedOffset(mem, offset, 63, 71, "GroupId"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 42)),
            hasEffectAssetRef(mem, offset)
               ? PacketIO.readVarString("EffectAssetRef", mem, offset + getValidatedOffset(mem, offset, 67, 71, "EffectAssetRef"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_BYTE, (long)(offset + 46)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 47)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 48)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 49)),
            mem.get(PacketIO.PROTO_BYTE, (long)(offset + 53)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 54)),
            TriggerVolumeConditionTiming.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 58)))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.groupId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.effectAssetRef != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.shapeType.getValue());
      PacketIO.writeVector3f(buf, this.position);
      PacketIO.writeVector3f(buf, this.dimensions);
      PacketIO.writeVector3f(buf, this.color);
      buf.writeFloatLE(this.opacity);
      buf.writeIntLE(this.groupColor);
      buf.writeByte(this.targetTypes);
      buf.writeByte(this.keepLoaded ? 1 : 0);
      buf.writeByte(this.cancelDelayedOnExit ? 1 : 0);
      buf.writeFloatLE(this.cooldown);
      buf.writeByte(this.cooldownMode);
      buf.writeFloatLE(this.activationDelay);
      buf.writeByte(this.conditionTiming.getValue());
      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int groupIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int effectAssetRefOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.name != null) {
         buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.name, 4096000);
      } else {
         buf.setIntLE(nameOffsetSlot, -1);
      }

      if (this.groupId != null) {
         buf.setIntLE(groupIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.groupId, 4096000);
      } else {
         buf.setIntLE(groupIdOffsetSlot, -1);
      }

      if (this.effectAssetRef != null) {
         buf.setIntLE(effectAssetRefOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.effectAssetRef, 4096000);
      } else {
         buf.setIntLE(effectAssetRefOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.groupId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.effectAssetRef != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.shapeType.getValue());
      PacketIO.writeVector3f(mem, offset + 2, this.position);
      PacketIO.writeVector3f(mem, offset + 14, this.dimensions);
      PacketIO.writeVector3f(mem, offset + 26, this.color);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 38), this.opacity);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 42), this.groupColor);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 46), this.targetTypes);
      mem.set(PacketIO.PROTO_BOOL, offset + 47, this.keepLoaded);
      mem.set(PacketIO.PROTO_BOOL, offset + 48, this.cancelDelayedOnExit);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 49), this.cooldown);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 53), this.cooldownMode);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 54), this.activationDelay);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 58), (byte)this.conditionTiming.getValue());
      int varOffset = offset + 71;
      if (this.name != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 59), varOffset - offset - 71);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.name, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 59), -1);
      }

      if (this.groupId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 63), varOffset - offset - 71);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.groupId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 63), -1);
      }

      if (this.effectAssetRef != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 67), varOffset - offset - 71);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.effectAssetRef, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 67), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 71;
      if (this.name != null) {
         size += PacketIO.stringSize(this.name);
      }

      if (this.groupId != null) {
         size += PacketIO.stringSize(this.groupId);
      }

      if (this.effectAssetRef != null) {
         size += PacketIO.stringSize(this.effectAssetRef);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 71) {
         return ValidationResult.error("Buffer too small: expected at least 71 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid TriggerVolumeShapeType value for ShapeType");
         } else {
            v = buffer.getByte(offset + 58) & 255;
            if (v >= 2) {
               return ValidationResult.error("Invalid TriggerVolumeConditionTiming value for ConditionTiming");
            } else {
               if ((nullBits & 1) != 0) {
                  v = buffer.getIntLE(offset + 59);
                  if (v < 0 || v > buffer.writerIndex() - offset - 71) {
                     return ValidationResult.error("Invalid offset for Name");
                  }

                  int pos = offset + 71 + v;
                  int nameLen = VarInt.peek(buffer, pos);
                  if (nameLen < 0) {
                     return ValidationResult.error("Invalid string length for Name");
                  }

                  if (nameLen > 4096000) {
                     return ValidationResult.error("Name exceeds max length 4096000");
                  }

                  pos += VarInt.size(nameLen);
                  pos += nameLen;
                  if (pos > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading Name");
                  }
               }

               if ((nullBits & 2) != 0) {
                  v = buffer.getIntLE(offset + 63);
                  if (v < 0 || v > buffer.writerIndex() - offset - 71) {
                     return ValidationResult.error("Invalid offset for GroupId");
                  }

                  int posx = offset + 71 + v;
                  int groupIdLen = VarInt.peek(buffer, posx);
                  if (groupIdLen < 0) {
                     return ValidationResult.error("Invalid string length for GroupId");
                  }

                  if (groupIdLen > 4096000) {
                     return ValidationResult.error("GroupId exceeds max length 4096000");
                  }

                  posx += VarInt.size(groupIdLen);
                  posx += groupIdLen;
                  if (posx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading GroupId");
                  }
               }

               if ((nullBits & 4) != 0) {
                  v = buffer.getIntLE(offset + 67);
                  if (v < 0 || v > buffer.writerIndex() - offset - 71) {
                     return ValidationResult.error("Invalid offset for EffectAssetRef");
                  }

                  int posxx = offset + 71 + v;
                  int effectAssetRefLen = VarInt.peek(buffer, posxx);
                  if (effectAssetRefLen < 0) {
                     return ValidationResult.error("Invalid string length for EffectAssetRef");
                  }

                  if (effectAssetRefLen > 4096000) {
                     return ValidationResult.error("EffectAssetRef exceeds max length 4096000");
                  }

                  posxx += VarInt.size(effectAssetRefLen);
                  posxx += effectAssetRefLen;
                  if (posxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading EffectAssetRef");
                  }
               }

               return ValidationResult.OK;
            }
         }
      }
   }

   public TriggerVolumeDisplayEntry clone() {
      TriggerVolumeDisplayEntry copy = new TriggerVolumeDisplayEntry();
      copy.shapeType = this.shapeType;
      copy.position = this.position;
      copy.dimensions = this.dimensions;
      copy.color = this.color;
      copy.opacity = this.opacity;
      copy.name = this.name;
      copy.groupId = this.groupId;
      copy.groupColor = this.groupColor;
      copy.effectAssetRef = this.effectAssetRef;
      copy.targetTypes = this.targetTypes;
      copy.keepLoaded = this.keepLoaded;
      copy.cancelDelayedOnExit = this.cancelDelayedOnExit;
      copy.cooldown = this.cooldown;
      copy.cooldownMode = this.cooldownMode;
      copy.activationDelay = this.activationDelay;
      copy.conditionTiming = this.conditionTiming;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof TriggerVolumeDisplayEntry other)
            ? false
            : Objects.equals(this.shapeType, other.shapeType)
               && Objects.equals(this.position, other.position)
               && Objects.equals(this.dimensions, other.dimensions)
               && Objects.equals(this.color, other.color)
               && this.opacity == other.opacity
               && Objects.equals(this.name, other.name)
               && Objects.equals(this.groupId, other.groupId)
               && this.groupColor == other.groupColor
               && Objects.equals(this.effectAssetRef, other.effectAssetRef)
               && this.targetTypes == other.targetTypes
               && this.keepLoaded == other.keepLoaded
               && this.cancelDelayedOnExit == other.cancelDelayedOnExit
               && this.cooldown == other.cooldown
               && this.cooldownMode == other.cooldownMode
               && this.activationDelay == other.activationDelay
               && Objects.equals(this.conditionTiming, other.conditionTiming);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.shapeType,
         this.position,
         this.dimensions,
         this.color,
         this.opacity,
         this.name,
         this.groupId,
         this.groupColor,
         this.effectAssetRef,
         this.targetTypes,
         this.keepLoaded,
         this.cancelDelayedOnExit,
         this.cooldown,
         this.cooldownMode,
         this.activationDelay,
         this.conditionTiming
      );
   }
}
