package com.hypixel.hytale.protocol;

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

public class ModelDisplay {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 37;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 45;
   public static final int MAX_SIZE = 32768055;
   @Nullable
   public String node;
   @Nullable
   public String attachTo;
   @Nullable
   public Vector3fc translation;
   @Nullable
   public Vector3fc rotation;
   @Nullable
   public Vector3fc scale;

   public ModelDisplay() {
   }

   public ModelDisplay(
      @Nullable String node, @Nullable String attachTo, @Nullable Vector3fc translation, @Nullable Vector3fc rotation, @Nullable Vector3fc scale
   ) {
      this.node = node;
      this.attachTo = attachTo;
      this.translation = translation;
      this.rotation = rotation;
      this.scale = scale;
   }

   public ModelDisplay(@Nonnull ModelDisplay other) {
      this.node = other.node;
      this.attachTo = other.attachTo;
      this.translation = other.translation;
      this.rotation = other.rotation;
      this.scale = other.scale;
   }

   @Nonnull
   public static ModelDisplay deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 45) {
         throw ProtocolException.bufferTooSmall("ModelDisplay", 45, buf.readableBytes() - offset);
      } else {
         ModelDisplay obj = new ModelDisplay();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.translation = PacketIO.readVector3f(buf, offset + 1);
         }

         if ((nullBits & 2) != 0) {
            obj.rotation = PacketIO.readVector3f(buf, offset + 13);
         }

         if ((nullBits & 4) != 0) {
            obj.scale = PacketIO.readVector3f(buf, offset + 25);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 37);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 45) {
               throw ProtocolException.invalidOffset("Node", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 45 + varPosBase0;
            int nodeLen = VarInt.peek(buf, varPos0);
            if (nodeLen < 0) {
               throw ProtocolException.invalidVarInt("Node");
            }

            int nodeVarIntLen = VarInt.size(nodeLen);
            if (nodeLen > 4096000) {
               throw ProtocolException.stringTooLong("Node", nodeLen, 4096000);
            }

            if (varPos0 + nodeVarIntLen + nodeLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Node", varPos0 + nodeVarIntLen + nodeLen, buf.readableBytes());
            }

            obj.node = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 16) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 41);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 45) {
               throw ProtocolException.invalidOffset("AttachTo", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 45 + varPosBase1;
            int attachToLen = VarInt.peek(buf, varPos1);
            if (attachToLen < 0) {
               throw ProtocolException.invalidVarInt("AttachTo");
            }

            int attachToVarIntLen = VarInt.size(attachToLen);
            if (attachToLen > 4096000) {
               throw ProtocolException.stringTooLong("AttachTo", attachToLen, 4096000);
            }

            if (varPos1 + attachToVarIntLen + attachToLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("AttachTo", varPos1 + attachToVarIntLen + attachToLen, buf.readableBytes());
            }

            obj.attachTo = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 45;
      if ((nullBits & 8) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 37);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 45) {
            throw ProtocolException.invalidOffset("Node", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 45 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 41);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 45) {
            throw ProtocolException.invalidOffset("AttachTo", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 45 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 45L;
   }

   @Nullable
   public static String getNode(MemorySegment mem) {
      return getNode(mem, 0);
   }

   @Nullable
   public static String getNode(MemorySegment mem, int offset) {
      return hasNode(mem, offset)
         ? PacketIO.readVarString("Node", mem, offset + getValidatedOffset(mem, offset, 37, 45, "Node"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getAttachTo(MemorySegment mem) {
      return getAttachTo(mem, 0);
   }

   @Nullable
   public static String getAttachTo(MemorySegment mem, int offset) {
      return hasAttachTo(mem, offset)
         ? PacketIO.readVarString("AttachTo", mem, offset + getValidatedOffset(mem, offset, 41, 45, "AttachTo"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static Vector3fc getTranslation(MemorySegment mem) {
      return getTranslation(mem, 0);
   }

   @Nullable
   public static Vector3fc getTranslation(MemorySegment mem, int offset) {
      return hasTranslation(mem, offset) ? PacketIO.readVector3f(mem, offset + 1) : null;
   }

   @Nullable
   public static Vector3fc getRotation(MemorySegment mem) {
      return getRotation(mem, 0);
   }

   @Nullable
   public static Vector3fc getRotation(MemorySegment mem, int offset) {
      return hasRotation(mem, offset) ? PacketIO.readVector3f(mem, offset + 13) : null;
   }

   @Nullable
   public static Vector3fc getScale(MemorySegment mem) {
      return getScale(mem, 0);
   }

   @Nullable
   public static Vector3fc getScale(MemorySegment mem, int offset) {
      return hasScale(mem, offset) ? PacketIO.readVector3f(mem, offset + 25) : null;
   }

   public static boolean hasTranslation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasRotation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasScale(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasNode(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasAttachTo(MemorySegment mem, int offset) {
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

   public static ModelDisplay toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ModelDisplay toObject(MemorySegment mem, int offset) {
      if (offset + 45 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ModelDisplay", offset + 45, (int)mem.byteSize());
      } else {
         return new ModelDisplay(
            hasNode(mem, offset) ? PacketIO.readVarString("Node", mem, offset + getValidatedOffset(mem, offset, 37, 45, "Node"), 4096000, PacketIO.UTF8) : null,
            hasAttachTo(mem, offset)
               ? PacketIO.readVarString("AttachTo", mem, offset + getValidatedOffset(mem, offset, 41, 45, "AttachTo"), 4096000, PacketIO.UTF8)
               : null,
            hasTranslation(mem, offset) ? PacketIO.readVector3f(mem, offset + 1) : null,
            hasRotation(mem, offset) ? PacketIO.readVector3f(mem, offset + 13) : null,
            hasScale(mem, offset) ? PacketIO.readVector3f(mem, offset + 25) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.translation != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.scale != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.node != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.attachTo != null) {
         nullBits = (byte)(nullBits | 16);
      }

      buf.writeByte(nullBits);
      if (this.translation != null) {
         PacketIO.writeVector3f(buf, this.translation);
      } else {
         buf.writeZero(12);
      }

      if (this.rotation != null) {
         PacketIO.writeVector3f(buf, this.rotation);
      } else {
         buf.writeZero(12);
      }

      if (this.scale != null) {
         PacketIO.writeVector3f(buf, this.scale);
      } else {
         buf.writeZero(12);
      }

      int nodeOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int attachToOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.node != null) {
         buf.setIntLE(nodeOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.node, 4096000);
      } else {
         buf.setIntLE(nodeOffsetSlot, -1);
      }

      if (this.attachTo != null) {
         buf.setIntLE(attachToOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.attachTo, 4096000);
      } else {
         buf.setIntLE(attachToOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.translation != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.scale != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.node != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.attachTo != null) {
         nullBits = (byte)(nullBits | 16);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.translation != null) {
         PacketIO.writeVector3f(mem, offset + 1, this.translation);
      } else {
         mem.asSlice(offset + 1, 12L).fill((byte)0);
      }

      if (this.rotation != null) {
         PacketIO.writeVector3f(mem, offset + 13, this.rotation);
      } else {
         mem.asSlice(offset + 13, 12L).fill((byte)0);
      }

      if (this.scale != null) {
         PacketIO.writeVector3f(mem, offset + 25, this.scale);
      } else {
         mem.asSlice(offset + 25, 12L).fill((byte)0);
      }

      int varOffset = offset + 45;
      if (this.node != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 37), varOffset - offset - 45);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.node, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 37), -1);
      }

      if (this.attachTo != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 41), varOffset - offset - 45);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.attachTo, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 41), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 45;
      if (this.node != null) {
         size += PacketIO.stringSize(this.node);
      }

      if (this.attachTo != null) {
         size += PacketIO.stringSize(this.attachTo);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 45) {
         return ValidationResult.error("Buffer too small: expected at least 45 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 8) != 0) {
            int nodeOffset = buffer.getIntLE(offset + 37);
            if (nodeOffset < 0 || nodeOffset > buffer.writerIndex() - offset - 45) {
               return ValidationResult.error("Invalid offset for Node");
            }

            int pos = offset + 45 + nodeOffset;
            int nodeLen = VarInt.peek(buffer, pos);
            if (nodeLen < 0) {
               return ValidationResult.error("Invalid string length for Node");
            }

            if (nodeLen > 4096000) {
               return ValidationResult.error("Node exceeds max length 4096000");
            }

            pos += VarInt.size(nodeLen);
            pos += nodeLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Node");
            }
         }

         if ((nullBits & 16) != 0) {
            int attachToOffset = buffer.getIntLE(offset + 41);
            if (attachToOffset < 0 || attachToOffset > buffer.writerIndex() - offset - 45) {
               return ValidationResult.error("Invalid offset for AttachTo");
            }

            int posx = offset + 45 + attachToOffset;
            int attachToLen = VarInt.peek(buffer, posx);
            if (attachToLen < 0) {
               return ValidationResult.error("Invalid string length for AttachTo");
            }

            if (attachToLen > 4096000) {
               return ValidationResult.error("AttachTo exceeds max length 4096000");
            }

            posx += VarInt.size(attachToLen);
            posx += attachToLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading AttachTo");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ModelDisplay clone() {
      ModelDisplay copy = new ModelDisplay();
      copy.node = this.node;
      copy.attachTo = this.attachTo;
      copy.translation = this.translation;
      copy.rotation = this.rotation;
      copy.scale = this.scale;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ModelDisplay other)
            ? false
            : Objects.equals(this.node, other.node)
               && Objects.equals(this.attachTo, other.attachTo)
               && Objects.equals(this.translation, other.translation)
               && Objects.equals(this.rotation, other.rotation)
               && Objects.equals(this.scale, other.scale);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.node, this.attachTo, this.translation, this.rotation, this.scale);
   }
}
