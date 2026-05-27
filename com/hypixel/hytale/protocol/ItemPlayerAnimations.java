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

public class ItemPlayerAnimations {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 91;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 103;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   @Nullable
   public Map<String, ItemAnimation> animations;
   @Nullable
   public WiggleWeights wiggleWeights;
   @Nullable
   public CameraSettings camera;
   @Nullable
   public ItemPullbackConfiguration pullbackConfig;
   public boolean useFirstPersonOverride;

   public ItemPlayerAnimations() {
   }

   public ItemPlayerAnimations(
      @Nullable String id,
      @Nullable Map<String, ItemAnimation> animations,
      @Nullable WiggleWeights wiggleWeights,
      @Nullable CameraSettings camera,
      @Nullable ItemPullbackConfiguration pullbackConfig,
      boolean useFirstPersonOverride
   ) {
      this.id = id;
      this.animations = animations;
      this.wiggleWeights = wiggleWeights;
      this.camera = camera;
      this.pullbackConfig = pullbackConfig;
      this.useFirstPersonOverride = useFirstPersonOverride;
   }

   public ItemPlayerAnimations(@Nonnull ItemPlayerAnimations other) {
      this.id = other.id;
      this.animations = other.animations;
      this.wiggleWeights = other.wiggleWeights;
      this.camera = other.camera;
      this.pullbackConfig = other.pullbackConfig;
      this.useFirstPersonOverride = other.useFirstPersonOverride;
   }

   @Nonnull
   public static ItemPlayerAnimations deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 103) {
         throw ProtocolException.bufferTooSmall("ItemPlayerAnimations", 103, buf.readableBytes() - offset);
      } else {
         ItemPlayerAnimations obj = new ItemPlayerAnimations();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.wiggleWeights = WiggleWeights.deserialize(buf, offset + 1);
         }

         if ((nullBits & 2) != 0) {
            obj.pullbackConfig = ItemPullbackConfiguration.deserialize(buf, offset + 41);
         }

         obj.useFirstPersonOverride = buf.getByte(offset + 90) != 0;
         if ((nullBits & 4) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 91);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 103) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 103 + varPosBase0;
            int idLen = VarInt.peek(buf, varPos0);
            if (idLen < 0) {
               throw ProtocolException.invalidVarInt("Id");
            }

            int idVarIntLen = VarInt.size(idLen);
            if (idLen > 4096000) {
               throw ProtocolException.stringTooLong("Id", idLen, 4096000);
            }

            if (varPos0 + idVarIntLen + idLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Id", varPos0 + idVarIntLen + idLen, buf.readableBytes());
            }

            obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 95);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 103) {
               throw ProtocolException.invalidOffset("Animations", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 103 + varPosBase1;
            int animationsCount = VarInt.peek(buf, varPos1);
            if (animationsCount < 0) {
               throw ProtocolException.invalidVarInt("Animations");
            }

            int varIntLen = VarInt.size(animationsCount);
            if (animationsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Animations", animationsCount, 4096000);
            }

            obj.animations = new HashMap<>(animationsCount);
            int dictPos = varPos1 + varIntLen;

            for (int i = 0; i < animationsCount; i++) {
               int keyLen = VarInt.peek(buf, dictPos);
               if (keyLen < 0) {
                  throw ProtocolException.invalidVarInt("key");
               }

               int keyVarLen = VarInt.size(keyLen);
               if (keyLen > 4096000) {
                  throw ProtocolException.stringTooLong("key", keyLen, 4096000);
               }

               if (dictPos + keyVarLen + keyLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("key", dictPos + keyVarLen + keyLen, buf.readableBytes());
               }

               String key = PacketIO.readVarString(buf, dictPos);
               dictPos += keyVarLen + keyLen;
               ItemAnimation val = ItemAnimation.deserialize(buf, dictPos);
               dictPos += ItemAnimation.computeBytesConsumed(buf, dictPos);
               if (obj.animations.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("animations", key);
               }
            }
         }

         if ((nullBits & 16) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 99);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 103) {
               throw ProtocolException.invalidOffset("Camera", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 103 + varPosBase2;
            obj.camera = CameraSettings.deserialize(buf, varPos2);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 103;
      if ((nullBits & 4) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 91);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 103) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 103 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 95);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 103) {
            throw ProtocolException.invalidOffset("Animations", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 103 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos1);
            pos1 += VarInt.size(sl) + sl;
            pos1 += ItemAnimation.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 99);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 103) {
            throw ProtocolException.invalidOffset("Camera", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 103 + fieldOffset2;
         pos2 += CameraSettings.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 103L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 91, 103, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static Map<String, ItemAnimation> getAnimations(MemorySegment mem) {
      return getAnimations(mem, 0);
   }

   @Nullable
   public static Map<String, ItemAnimation> getAnimations(MemorySegment mem, int offset) {
      if (!hasAnimations(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 95, 103, "Animations");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Animations", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Animations", len, 4096000);
         } else {
            Map<String, ItemAnimation> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               ItemAnimation value = ItemAnimation.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Animations", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static WiggleWeights getWiggleWeights(MemorySegment mem) {
      return getWiggleWeights(mem, 0);
   }

   @Nullable
   public static WiggleWeights getWiggleWeights(MemorySegment mem, int offset) {
      return hasWiggleWeights(mem, offset) ? WiggleWeights.toObject(mem, offset + 1) : null;
   }

   @Nullable
   public static CameraSettings getCamera(MemorySegment mem) {
      return getCamera(mem, 0);
   }

   @Nullable
   public static CameraSettings getCamera(MemorySegment mem, int offset) {
      return hasCamera(mem, offset) ? CameraSettings.toObject(mem, offset + getValidatedOffset(mem, offset, 99, 103, "Camera")) : null;
   }

   @Nullable
   public static ItemPullbackConfiguration getPullbackConfig(MemorySegment mem) {
      return getPullbackConfig(mem, 0);
   }

   @Nullable
   public static ItemPullbackConfiguration getPullbackConfig(MemorySegment mem, int offset) {
      return hasPullbackConfig(mem, offset) ? ItemPullbackConfiguration.toObject(mem, offset + 41) : null;
   }

   public static boolean getUseFirstPersonOverride(MemorySegment mem) {
      return getUseFirstPersonOverride(mem, 0);
   }

   public static boolean getUseFirstPersonOverride(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 90));
   }

   public static boolean hasWiggleWeights(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasPullbackConfig(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasAnimations(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasCamera(MemorySegment mem, int offset) {
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

   public static ItemPlayerAnimations toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ItemPlayerAnimations toObject(MemorySegment mem, int offset) {
      if (offset + 103 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ItemPlayerAnimations", offset + 103, (int)mem.byteSize());
      } else {
         Map<String, ItemAnimation> animations = null;
         if (hasAnimations(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 95, 103, "Animations");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Animations", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Animations", len, 4096000);
            }

            animations = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               ItemAnimation value = ItemAnimation.toObject(mem, off);
               off += value.computeSize();
               if (animations.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Animations", key);
               }
            }
         }

         return new ItemPlayerAnimations(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 91, 103, "Id"), 4096000, PacketIO.UTF8) : null,
            animations,
            hasWiggleWeights(mem, offset) ? WiggleWeights.toObject(mem, offset + 1) : null,
            hasCamera(mem, offset) ? CameraSettings.toObject(mem, offset + getValidatedOffset(mem, offset, 99, 103, "Camera")) : null,
            hasPullbackConfig(mem, offset) ? ItemPullbackConfiguration.toObject(mem, offset + 41) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 90))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.wiggleWeights != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.pullbackConfig != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.id != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.animations != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.camera != null) {
         nullBits = (byte)(nullBits | 16);
      }

      buf.writeByte(nullBits);
      if (this.wiggleWeights != null) {
         this.wiggleWeights.serialize(buf);
      } else {
         buf.writeZero(40);
      }

      if (this.pullbackConfig != null) {
         this.pullbackConfig.serialize(buf);
      } else {
         buf.writeZero(49);
      }

      buf.writeByte(this.useFirstPersonOverride ? 1 : 0);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int animationsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int cameraOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.animations != null) {
         buf.setIntLE(animationsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.animations.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Animations", this.animations.size(), 4096000);
         }

         VarInt.write(buf, this.animations.size());

         for (Entry<String, ItemAnimation> e : this.animations.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(animationsOffsetSlot, -1);
      }

      if (this.camera != null) {
         buf.setIntLE(cameraOffsetSlot, buf.writerIndex() - varBlockStart);
         this.camera.serialize(buf);
      } else {
         buf.setIntLE(cameraOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.wiggleWeights != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.pullbackConfig != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.id != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.animations != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.camera != null) {
         nullBits = (byte)(nullBits | 16);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.wiggleWeights != null) {
         this.wiggleWeights.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 40L).fill((byte)0);
      }

      if (this.pullbackConfig != null) {
         this.pullbackConfig.serialize(mem, offset + 41);
      } else {
         mem.asSlice(offset + 41, 49L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BOOL, offset + 90, this.useFirstPersonOverride);
      int varOffset = offset + 103;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 91), varOffset - offset - 103);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 91), -1);
      }

      if (this.animations != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 95), varOffset - offset - 103);
         if (this.animations.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Animations", this.animations.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.animations.size());

         for (Entry<String, ItemAnimation> e : this.animations.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 95), -1);
      }

      if (this.camera != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 99), varOffset - offset - 103);
         varOffset += this.camera.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 99), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 103;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.animations != null) {
         int animationsSize = 0;

         for (Entry<String, ItemAnimation> kvp : this.animations.entrySet()) {
            animationsSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.animations.size()) + animationsSize;
      }

      if (this.camera != null) {
         size += this.camera.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 103) {
         return ValidationResult.error("Buffer too small: expected at least 103 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 4) != 0) {
            int idOffset = buffer.getIntLE(offset + 91);
            if (idOffset < 0 || idOffset > buffer.writerIndex() - offset - 103) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 103 + idOffset;
            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.size(idLen);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits & 8) != 0) {
            int animationsOffset = buffer.getIntLE(offset + 95);
            if (animationsOffset < 0 || animationsOffset > buffer.writerIndex() - offset - 103) {
               return ValidationResult.error("Invalid offset for Animations");
            }

            int posx = offset + 103 + animationsOffset;
            int animationsCount = VarInt.peek(buffer, posx);
            if (animationsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Animations");
            }

            if (animationsCount > 4096000) {
               return ValidationResult.error("Animations exceeds max length 4096000");
            }

            posx += VarInt.size(animationsCount);

            for (int i = 0; i < animationsCount; i++) {
               int keyLen = VarInt.peek(buffer, posx);
               if (keyLen < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLen > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               posx += VarInt.size(keyLen);
               posx += keyLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posx += ItemAnimation.computeBytesConsumed(buffer, posx);
            }
         }

         if ((nullBits & 16) != 0) {
            int cameraOffset = buffer.getIntLE(offset + 99);
            if (cameraOffset < 0 || cameraOffset > buffer.writerIndex() - offset - 103) {
               return ValidationResult.error("Invalid offset for Camera");
            }

            int posxx = offset + 103 + cameraOffset;
            ValidationResult cameraResult = CameraSettings.validateStructure(buffer, posxx);
            if (!cameraResult.isValid()) {
               return ValidationResult.error("Invalid Camera: " + cameraResult.error());
            }

            posxx += CameraSettings.computeBytesConsumed(buffer, posxx);
         }

         return ValidationResult.OK;
      }
   }

   public ItemPlayerAnimations clone() {
      ItemPlayerAnimations copy = new ItemPlayerAnimations();
      copy.id = this.id;
      if (this.animations != null) {
         Map<String, ItemAnimation> m = new HashMap<>();

         for (Entry<String, ItemAnimation> e : this.animations.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.animations = m;
      }

      copy.wiggleWeights = this.wiggleWeights != null ? this.wiggleWeights.clone() : null;
      copy.camera = this.camera != null ? this.camera.clone() : null;
      copy.pullbackConfig = this.pullbackConfig != null ? this.pullbackConfig.clone() : null;
      copy.useFirstPersonOverride = this.useFirstPersonOverride;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemPlayerAnimations other)
            ? false
            : Objects.equals(this.id, other.id)
               && Objects.equals(this.animations, other.animations)
               && Objects.equals(this.wiggleWeights, other.wiggleWeights)
               && Objects.equals(this.camera, other.camera)
               && Objects.equals(this.pullbackConfig, other.pullbackConfig)
               && this.useFirstPersonOverride == other.useFirstPersonOverride;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.animations, this.wiggleWeights, this.camera, this.pullbackConfig, this.useFirstPersonOverride);
   }
}
