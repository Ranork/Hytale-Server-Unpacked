package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3fc;

public class ProjectileConfig {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 167;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 175;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public PhysicsConfig physicsConfig;
   @Nullable
   public Model model;
   public double launchForce;
   @Nullable
   public Vector3fc spawnOffset;
   @Nullable
   public Direction rotationOffset;
   @Nullable
   public Map<InteractionType, Integer> interactions;
   public int launchLocalSoundEventIndex;
   public int launchWorldSoundEventIndex;
   public int projectileSoundEventIndex;

   public ProjectileConfig() {
   }

   public ProjectileConfig(
      @Nullable PhysicsConfig physicsConfig,
      @Nullable Model model,
      double launchForce,
      @Nullable Vector3fc spawnOffset,
      @Nullable Direction rotationOffset,
      @Nullable Map<InteractionType, Integer> interactions,
      int launchLocalSoundEventIndex,
      int launchWorldSoundEventIndex,
      int projectileSoundEventIndex
   ) {
      this.physicsConfig = physicsConfig;
      this.model = model;
      this.launchForce = launchForce;
      this.spawnOffset = spawnOffset;
      this.rotationOffset = rotationOffset;
      this.interactions = interactions;
      this.launchLocalSoundEventIndex = launchLocalSoundEventIndex;
      this.launchWorldSoundEventIndex = launchWorldSoundEventIndex;
      this.projectileSoundEventIndex = projectileSoundEventIndex;
   }

   public ProjectileConfig(@Nonnull ProjectileConfig other) {
      this.physicsConfig = other.physicsConfig;
      this.model = other.model;
      this.launchForce = other.launchForce;
      this.spawnOffset = other.spawnOffset;
      this.rotationOffset = other.rotationOffset;
      this.interactions = other.interactions;
      this.launchLocalSoundEventIndex = other.launchLocalSoundEventIndex;
      this.launchWorldSoundEventIndex = other.launchWorldSoundEventIndex;
      this.projectileSoundEventIndex = other.projectileSoundEventIndex;
   }

   @Nonnull
   public static ProjectileConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 175) {
         throw ProtocolException.bufferTooSmall("ProjectileConfig", 175, buf.readableBytes() - offset);
      } else {
         ProjectileConfig obj = new ProjectileConfig();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.physicsConfig = PhysicsConfig.deserialize(buf, offset + 1);
         }

         obj.launchForce = buf.getDoubleLE(offset + 123);
         if ((nullBits & 2) != 0) {
            obj.spawnOffset = PacketIO.readVector3f(buf, offset + 131);
         }

         if ((nullBits & 4) != 0) {
            obj.rotationOffset = Direction.deserialize(buf, offset + 143);
         }

         obj.launchLocalSoundEventIndex = buf.getIntLE(offset + 155);
         obj.launchWorldSoundEventIndex = buf.getIntLE(offset + 159);
         obj.projectileSoundEventIndex = buf.getIntLE(offset + 163);
         if ((nullBits & 8) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 167);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 175) {
               throw ProtocolException.invalidOffset("Model", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 175 + varPosBase0;
            obj.model = Model.deserialize(buf, varPos0);
         }

         if ((nullBits & 16) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 171);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 175) {
               throw ProtocolException.invalidOffset("Interactions", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 175 + varPosBase1;
            int interactionsCount = VarInt.peek(buf, varPos1);
            if (interactionsCount < 0) {
               throw ProtocolException.invalidVarInt("Interactions");
            }

            int varIntLen = VarInt.size(interactionsCount);
            if (interactionsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Interactions", interactionsCount, 4096000);
            }

            obj.interactions = new HashMap<>(interactionsCount);
            int dictPos = varPos1 + varIntLen;

            for (int i = 0; i < interactionsCount; i++) {
               InteractionType key = InteractionType.fromValue(buf.getByte(dictPos));
               int val = buf.getIntLE(++dictPos);
               dictPos += 4;
               if (obj.interactions.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("interactions", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 175;
      if ((nullBits & 8) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 167);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 175) {
            throw ProtocolException.invalidOffset("Model", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 175 + fieldOffset0;
         pos0 += Model.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 171);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 175) {
            throw ProtocolException.invalidOffset("Interactions", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 175 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos1 = ++pos1 + 4;
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 175L;
   }

   @Nullable
   public static PhysicsConfig getPhysicsConfig(MemorySegment mem) {
      return getPhysicsConfig(mem, 0);
   }

   @Nullable
   public static PhysicsConfig getPhysicsConfig(MemorySegment mem, int offset) {
      return hasPhysicsConfig(mem, offset) ? PhysicsConfig.toObject(mem, offset + 1) : null;
   }

   @Nullable
   public static Model getModel(MemorySegment mem) {
      return getModel(mem, 0);
   }

   @Nullable
   public static Model getModel(MemorySegment mem, int offset) {
      return hasModel(mem, offset) ? Model.toObject(mem, offset + getValidatedOffset(mem, offset, 167, 175, "Model")) : null;
   }

   public static double getLaunchForce(MemorySegment mem) {
      return getLaunchForce(mem, 0);
   }

   public static double getLaunchForce(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 123));
   }

   @Nullable
   public static Vector3fc getSpawnOffset(MemorySegment mem) {
      return getSpawnOffset(mem, 0);
   }

   @Nullable
   public static Vector3fc getSpawnOffset(MemorySegment mem, int offset) {
      return hasSpawnOffset(mem, offset) ? PacketIO.readVector3f(mem, offset + 131) : null;
   }

   @Nullable
   public static Direction getRotationOffset(MemorySegment mem) {
      return getRotationOffset(mem, 0);
   }

   @Nullable
   public static Direction getRotationOffset(MemorySegment mem, int offset) {
      return hasRotationOffset(mem, offset) ? Direction.toObject(mem, offset + 143) : null;
   }

   @Nullable
   public static Map<InteractionType, Integer> getInteractions(MemorySegment mem) {
      return getInteractions(mem, 0);
   }

   @Nullable
   public static Map<InteractionType, Integer> getInteractions(MemorySegment mem, int offset) {
      if (!hasInteractions(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 171, 175, "Interactions");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Interactions", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Interactions", len, 4096000);
         } else {
            Map<InteractionType, Integer> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               InteractionType key = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               int value = mem.get(PacketIO.PROTO_INT, (long)(++off));
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Interactions", key);
               }
            }

            return data;
         }
      }
   }

   public static int getLaunchLocalSoundEventIndex(MemorySegment mem) {
      return getLaunchLocalSoundEventIndex(mem, 0);
   }

   public static int getLaunchLocalSoundEventIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 155));
   }

   public static int getLaunchWorldSoundEventIndex(MemorySegment mem) {
      return getLaunchWorldSoundEventIndex(mem, 0);
   }

   public static int getLaunchWorldSoundEventIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 159));
   }

   public static int getProjectileSoundEventIndex(MemorySegment mem) {
      return getProjectileSoundEventIndex(mem, 0);
   }

   public static int getProjectileSoundEventIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 163));
   }

   public static boolean hasPhysicsConfig(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasSpawnOffset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasRotationOffset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasModel(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasInteractions(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static ProjectileConfig toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ProjectileConfig toObject(MemorySegment mem, int offset) {
      if (offset + 175 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ProjectileConfig", offset + 175, (int)mem.byteSize());
      } else {
         Map<InteractionType, Integer> interactions = null;
         if (hasInteractions(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 171, 175, "Interactions");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Interactions", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Interactions", len, 4096000);
            }

            interactions = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               InteractionType key = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               int value = mem.get(PacketIO.PROTO_INT, (long)(++off));
               off += 4;
               if (interactions.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Interactions", key);
               }
            }
         }

         return new ProjectileConfig(
            hasPhysicsConfig(mem, offset) ? PhysicsConfig.toObject(mem, offset + 1) : null,
            hasModel(mem, offset) ? Model.toObject(mem, offset + getValidatedOffset(mem, offset, 167, 175, "Model")) : null,
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 123)),
            hasSpawnOffset(mem, offset) ? PacketIO.readVector3f(mem, offset + 131) : null,
            hasRotationOffset(mem, offset) ? Direction.toObject(mem, offset + 143) : null,
            interactions,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 155)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 159)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 163))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.physicsConfig != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.spawnOffset != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.rotationOffset != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.model != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.interactions != null) {
         nullBits = (byte)(nullBits | 16);
      }

      buf.writeByte(nullBits);
      if (this.physicsConfig != null) {
         this.physicsConfig.serialize(buf);
      } else {
         buf.writeZero(122);
      }

      buf.writeDoubleLE(this.launchForce);
      if (this.spawnOffset != null) {
         PacketIO.writeVector3f(buf, this.spawnOffset);
      } else {
         buf.writeZero(12);
      }

      if (this.rotationOffset != null) {
         this.rotationOffset.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeIntLE(this.launchLocalSoundEventIndex);
      buf.writeIntLE(this.launchWorldSoundEventIndex);
      buf.writeIntLE(this.projectileSoundEventIndex);
      int modelOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int interactionsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.model != null) {
         buf.setIntLE(modelOffsetSlot, buf.writerIndex() - varBlockStart);
         this.model.serialize(buf);
      } else {
         buf.setIntLE(modelOffsetSlot, -1);
      }

      if (this.interactions != null) {
         buf.setIntLE(interactionsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.interactions.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Interactions", this.interactions.size(), 4096000);
         }

         VarInt.write(buf, this.interactions.size());

         for (Entry<InteractionType, Integer> e : this.interactions.entrySet()) {
            buf.writeByte(e.getKey().getValue());
            buf.writeIntLE(e.getValue());
         }
      } else {
         buf.setIntLE(interactionsOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.physicsConfig != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.spawnOffset != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.rotationOffset != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.model != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.interactions != null) {
         nullBits = (byte)(nullBits | 16);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.physicsConfig != null) {
         this.physicsConfig.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 122L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 123), this.launchForce);
      if (this.spawnOffset != null) {
         PacketIO.writeVector3f(mem, offset + 131, this.spawnOffset);
      } else {
         mem.asSlice(offset + 131, 12L).fill((byte)0);
      }

      if (this.rotationOffset != null) {
         this.rotationOffset.serialize(mem, offset + 143);
      } else {
         mem.asSlice(offset + 143, 12L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_INT, (long)(offset + 155), this.launchLocalSoundEventIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 159), this.launchWorldSoundEventIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 163), this.projectileSoundEventIndex);
      int varOffset = offset + 175;
      if (this.model != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 167), varOffset - offset - 175);
         varOffset += this.model.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 167), -1);
      }

      if (this.interactions != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 171), varOffset - offset - 175);
         if (this.interactions.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Interactions", this.interactions.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.interactions.size());

         for (Entry<InteractionType, Integer> e : this.interactions.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            mem.set(PacketIO.PROTO_INT, (long)(++varOffset), e.getValue());
            varOffset += 4;
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 171), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 175;
      if (this.model != null) {
         size += this.model.computeSize();
      }

      if (this.interactions != null) {
         size += VarInt.size(this.interactions.size()) + this.interactions.size() * 5;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 175) {
         return ValidationResult.error("Buffer too small: expected at least 175 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 8) != 0) {
            int modelOffset = buffer.getIntLE(offset + 167);
            if (modelOffset < 0 || modelOffset > buffer.writerIndex() - offset - 175) {
               return ValidationResult.error("Invalid offset for Model");
            }

            int pos = offset + 175 + modelOffset;
            ValidationResult modelResult = Model.validateStructure(buffer, pos);
            if (!modelResult.isValid()) {
               return ValidationResult.error("Invalid Model: " + modelResult.error());
            }

            pos += Model.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 16) != 0) {
            int interactionsOffset = buffer.getIntLE(offset + 171);
            if (interactionsOffset < 0 || interactionsOffset > buffer.writerIndex() - offset - 175) {
               return ValidationResult.error("Invalid offset for Interactions");
            }

            int pos = offset + 175 + interactionsOffset;
            int interactionsCount = VarInt.peek(buffer, pos);
            if (interactionsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Interactions");
            }

            if (interactionsCount > 4096000) {
               return ValidationResult.error("Interactions exceeds max length 4096000");
            }

            pos += VarInt.size(interactionsCount);

            for (int i = 0; i < interactionsCount; i++) {
               int v = buffer.getByte(pos) & 255;
               if (v >= 25) {
                  return ValidationResult.error("Invalid InteractionType value for key");
               }

               pos = ++pos + 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public ProjectileConfig clone() {
      ProjectileConfig copy = new ProjectileConfig();
      copy.physicsConfig = this.physicsConfig != null ? this.physicsConfig.clone() : null;
      copy.model = this.model != null ? this.model.clone() : null;
      copy.launchForce = this.launchForce;
      copy.spawnOffset = this.spawnOffset;
      copy.rotationOffset = this.rotationOffset != null ? this.rotationOffset.clone() : null;
      copy.interactions = this.interactions != null ? new HashMap<>(this.interactions) : null;
      copy.launchLocalSoundEventIndex = this.launchLocalSoundEventIndex;
      copy.launchWorldSoundEventIndex = this.launchWorldSoundEventIndex;
      copy.projectileSoundEventIndex = this.projectileSoundEventIndex;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ProjectileConfig other)
            ? false
            : Objects.equals(this.physicsConfig, other.physicsConfig)
               && Objects.equals(this.model, other.model)
               && this.launchForce == other.launchForce
               && Objects.equals(this.spawnOffset, other.spawnOffset)
               && Objects.equals(this.rotationOffset, other.rotationOffset)
               && Objects.equals(this.interactions, other.interactions)
               && this.launchLocalSoundEventIndex == other.launchLocalSoundEventIndex
               && this.launchWorldSoundEventIndex == other.launchWorldSoundEventIndex
               && this.projectileSoundEventIndex == other.projectileSoundEventIndex;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.physicsConfig,
         this.model,
         this.launchForce,
         this.spawnOffset,
         this.rotationOffset,
         this.interactions,
         this.launchLocalSoundEventIndex,
         this.launchWorldSoundEventIndex,
         this.projectileSoundEventIndex
      );
   }
}
