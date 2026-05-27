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

public class ProtocolEmote {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 3;
   public static final int VARIABLE_FIELD_COUNT = 4;
   public static final int VARIABLE_BLOCK_START = 19;
   public static final int MAX_SIZE = 65536039;
   @Nullable
   public String id;
   @Nullable
   public String name;
   @Nullable
   public String animation;
   @Nullable
   public String icon;
   public boolean isLooping;
   public boolean hideItemInHand;

   public ProtocolEmote() {
   }

   public ProtocolEmote(
      @Nullable String id, @Nullable String name, @Nullable String animation, @Nullable String icon, boolean isLooping, boolean hideItemInHand
   ) {
      this.id = id;
      this.name = name;
      this.animation = animation;
      this.icon = icon;
      this.isLooping = isLooping;
      this.hideItemInHand = hideItemInHand;
   }

   public ProtocolEmote(@Nonnull ProtocolEmote other) {
      this.id = other.id;
      this.name = other.name;
      this.animation = other.animation;
      this.icon = other.icon;
      this.isLooping = other.isLooping;
      this.hideItemInHand = other.hideItemInHand;
   }

   @Nonnull
   public static ProtocolEmote deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 19) {
         throw ProtocolException.bufferTooSmall("ProtocolEmote", 19, buf.readableBytes() - offset);
      } else {
         ProtocolEmote obj = new ProtocolEmote();
         byte nullBits = buf.getByte(offset);
         obj.isLooping = buf.getByte(offset + 1) != 0;
         obj.hideItemInHand = buf.getByte(offset + 2) != 0;
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 3);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 19) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 19 + varPosBase0;
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

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 7);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 19) {
               throw ProtocolException.invalidOffset("Name", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 19 + varPosBase1;
            int nameLen = VarInt.peek(buf, varPos1);
            if (nameLen < 0) {
               throw ProtocolException.invalidVarInt("Name");
            }

            int nameVarIntLen = VarInt.size(nameLen);
            if (nameLen > 4096000) {
               throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
            }

            if (varPos1 + nameVarIntLen + nameLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Name", varPos1 + nameVarIntLen + nameLen, buf.readableBytes());
            }

            obj.name = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 11);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 19) {
               throw ProtocolException.invalidOffset("Animation", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 19 + varPosBase2;
            int animationLen = VarInt.peek(buf, varPos2);
            if (animationLen < 0) {
               throw ProtocolException.invalidVarInt("Animation");
            }

            int animationVarIntLen = VarInt.size(animationLen);
            if (animationLen > 4096000) {
               throw ProtocolException.stringTooLong("Animation", animationLen, 4096000);
            }

            if (varPos2 + animationVarIntLen + animationLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Animation", varPos2 + animationVarIntLen + animationLen, buf.readableBytes());
            }

            obj.animation = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 15);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 19) {
               throw ProtocolException.invalidOffset("Icon", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 19 + varPosBase3;
            int iconLen = VarInt.peek(buf, varPos3);
            if (iconLen < 0) {
               throw ProtocolException.invalidVarInt("Icon");
            }

            int iconVarIntLen = VarInt.size(iconLen);
            if (iconLen > 4096000) {
               throw ProtocolException.stringTooLong("Icon", iconLen, 4096000);
            }

            if (varPos3 + iconVarIntLen + iconLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Icon", varPos3 + iconVarIntLen + iconLen, buf.readableBytes());
            }

            obj.icon = PacketIO.readVarString(buf, varPos3, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 19;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 3);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 19) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 19 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 7);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 19) {
            throw ProtocolException.invalidOffset("Name", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 19 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 11);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 19) {
            throw ProtocolException.invalidOffset("Animation", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 19 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 15);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 19) {
            throw ProtocolException.invalidOffset("Icon", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 19 + fieldOffset3;
         int sl = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(sl) + sl;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 19L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 3, 19, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static String getName(MemorySegment mem) {
      return getName(mem, 0);
   }

   @Nullable
   public static String getName(MemorySegment mem, int offset) {
      return hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 7, 19, "Name"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static String getAnimation(MemorySegment mem) {
      return getAnimation(mem, 0);
   }

   @Nullable
   public static String getAnimation(MemorySegment mem, int offset) {
      return hasAnimation(mem, offset)
         ? PacketIO.readVarString("Animation", mem, offset + getValidatedOffset(mem, offset, 11, 19, "Animation"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getIcon(MemorySegment mem) {
      return getIcon(mem, 0);
   }

   @Nullable
   public static String getIcon(MemorySegment mem, int offset) {
      return hasIcon(mem, offset)
         ? PacketIO.readVarString("Icon", mem, offset + getValidatedOffset(mem, offset, 15, 19, "Icon"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean getIsLooping(MemorySegment mem) {
      return getIsLooping(mem, 0);
   }

   public static boolean getIsLooping(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static boolean getHideItemInHand(MemorySegment mem) {
      return getHideItemInHand(mem, 0);
   }

   public static boolean getHideItemInHand(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2));
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasName(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasAnimation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasIcon(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static ProtocolEmote toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ProtocolEmote toObject(MemorySegment mem, int offset) {
      if (offset + 19 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ProtocolEmote", offset + 19, (int)mem.byteSize());
      } else {
         return new ProtocolEmote(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 3, 19, "Id"), 4096000, PacketIO.UTF8) : null,
            hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 7, 19, "Name"), 4096000, PacketIO.UTF8) : null,
            hasAnimation(mem, offset)
               ? PacketIO.readVarString("Animation", mem, offset + getValidatedOffset(mem, offset, 11, 19, "Animation"), 4096000, PacketIO.UTF8)
               : null,
            hasIcon(mem, offset) ? PacketIO.readVarString("Icon", mem, offset + getValidatedOffset(mem, offset, 15, 19, "Icon"), 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.name != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.animation != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.icon != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.isLooping ? 1 : 0);
      buf.writeByte(this.hideItemInHand ? 1 : 0);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int animationOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int iconOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.name != null) {
         buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.name, 4096000);
      } else {
         buf.setIntLE(nameOffsetSlot, -1);
      }

      if (this.animation != null) {
         buf.setIntLE(animationOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.animation, 4096000);
      } else {
         buf.setIntLE(animationOffsetSlot, -1);
      }

      if (this.icon != null) {
         buf.setIntLE(iconOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.icon, 4096000);
      } else {
         buf.setIntLE(iconOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.name != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.animation != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.icon != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.isLooping);
      mem.set(PacketIO.PROTO_BOOL, offset + 2, this.hideItemInHand);
      int varOffset = offset + 19;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 3), varOffset - offset - 19);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 3), -1);
      }

      if (this.name != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 7), varOffset - offset - 19);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.name, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 7), -1);
      }

      if (this.animation != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 11), varOffset - offset - 19);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.animation, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 11), -1);
      }

      if (this.icon != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 15), varOffset - offset - 19);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.icon, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 15), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 19;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.name != null) {
         size += PacketIO.stringSize(this.name);
      }

      if (this.animation != null) {
         size += PacketIO.stringSize(this.animation);
      }

      if (this.icon != null) {
         size += PacketIO.stringSize(this.icon);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 19) {
         return ValidationResult.error("Buffer too small: expected at least 19 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int idOffset = buffer.getIntLE(offset + 3);
            if (idOffset < 0 || idOffset > buffer.writerIndex() - offset - 19) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 19 + idOffset;
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

         if ((nullBits & 2) != 0) {
            int nameOffset = buffer.getIntLE(offset + 7);
            if (nameOffset < 0 || nameOffset > buffer.writerIndex() - offset - 19) {
               return ValidationResult.error("Invalid offset for Name");
            }

            int posx = offset + 19 + nameOffset;
            int nameLen = VarInt.peek(buffer, posx);
            if (nameLen < 0) {
               return ValidationResult.error("Invalid string length for Name");
            }

            if (nameLen > 4096000) {
               return ValidationResult.error("Name exceeds max length 4096000");
            }

            posx += VarInt.size(nameLen);
            posx += nameLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Name");
            }
         }

         if ((nullBits & 4) != 0) {
            int animationOffset = buffer.getIntLE(offset + 11);
            if (animationOffset < 0 || animationOffset > buffer.writerIndex() - offset - 19) {
               return ValidationResult.error("Invalid offset for Animation");
            }

            int posxx = offset + 19 + animationOffset;
            int animationLen = VarInt.peek(buffer, posxx);
            if (animationLen < 0) {
               return ValidationResult.error("Invalid string length for Animation");
            }

            if (animationLen > 4096000) {
               return ValidationResult.error("Animation exceeds max length 4096000");
            }

            posxx += VarInt.size(animationLen);
            posxx += animationLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Animation");
            }
         }

         if ((nullBits & 8) != 0) {
            int iconOffset = buffer.getIntLE(offset + 15);
            if (iconOffset < 0 || iconOffset > buffer.writerIndex() - offset - 19) {
               return ValidationResult.error("Invalid offset for Icon");
            }

            int posxxx = offset + 19 + iconOffset;
            int iconLen = VarInt.peek(buffer, posxxx);
            if (iconLen < 0) {
               return ValidationResult.error("Invalid string length for Icon");
            }

            if (iconLen > 4096000) {
               return ValidationResult.error("Icon exceeds max length 4096000");
            }

            posxxx += VarInt.size(iconLen);
            posxxx += iconLen;
            if (posxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Icon");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ProtocolEmote clone() {
      ProtocolEmote copy = new ProtocolEmote();
      copy.id = this.id;
      copy.name = this.name;
      copy.animation = this.animation;
      copy.icon = this.icon;
      copy.isLooping = this.isLooping;
      copy.hideItemInHand = this.hideItemInHand;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ProtocolEmote other)
            ? false
            : Objects.equals(this.id, other.id)
               && Objects.equals(this.name, other.name)
               && Objects.equals(this.animation, other.animation)
               && Objects.equals(this.icon, other.icon)
               && this.isLooping == other.isLooping
               && this.hideItemInHand == other.hideItemInHand;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.name, this.animation, this.icon, this.isLooping, this.hideItemInHand);
   }
}
