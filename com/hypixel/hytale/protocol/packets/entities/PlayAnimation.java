package com.hypixel.hytale.protocol.packets.entities;

import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayAnimation implements Packet, ToClientPacket {
   public static final int PACKET_ID = 162;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 14;
   public static final int MAX_SIZE = 32768024;
   public int entityId;
   @Nullable
   public String itemAnimationsId;
   @Nullable
   public String animationId;
   @Nonnull
   public AnimationSlot slot = AnimationSlot.Movement;

   @Override
   public int getId() {
      return 162;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public PlayAnimation() {
   }

   public PlayAnimation(int entityId, @Nullable String itemAnimationsId, @Nullable String animationId, @Nonnull AnimationSlot slot) {
      this.entityId = entityId;
      this.itemAnimationsId = itemAnimationsId;
      this.animationId = animationId;
      this.slot = slot;
   }

   public PlayAnimation(@Nonnull PlayAnimation other) {
      this.entityId = other.entityId;
      this.itemAnimationsId = other.itemAnimationsId;
      this.animationId = other.animationId;
      this.slot = other.slot;
   }

   @Nonnull
   public static PlayAnimation deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 14) {
         throw ProtocolException.bufferTooSmall("PlayAnimation", 14, buf.readableBytes() - offset);
      } else {
         PlayAnimation obj = new PlayAnimation();
         byte nullBits = buf.getByte(offset);
         obj.entityId = buf.getIntLE(offset + 1);
         obj.slot = AnimationSlot.fromValue(buf.getByte(offset + 5));
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 6);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 14) {
               throw ProtocolException.invalidOffset("ItemAnimationsId", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 14 + varPosBase0;
            int itemAnimationsIdLen = VarInt.peek(buf, varPos0);
            if (itemAnimationsIdLen < 0) {
               throw ProtocolException.invalidVarInt("ItemAnimationsId");
            }

            int itemAnimationsIdVarIntLen = VarInt.size(itemAnimationsIdLen);
            if (itemAnimationsIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ItemAnimationsId", itemAnimationsIdLen, 4096000);
            }

            if (varPos0 + itemAnimationsIdVarIntLen + itemAnimationsIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ItemAnimationsId", varPos0 + itemAnimationsIdVarIntLen + itemAnimationsIdLen, buf.readableBytes());
            }

            obj.itemAnimationsId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 10);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 14) {
               throw ProtocolException.invalidOffset("AnimationId", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 14 + varPosBase1;
            int animationIdLen = VarInt.peek(buf, varPos1);
            if (animationIdLen < 0) {
               throw ProtocolException.invalidVarInt("AnimationId");
            }

            int animationIdVarIntLen = VarInt.size(animationIdLen);
            if (animationIdLen > 4096000) {
               throw ProtocolException.stringTooLong("AnimationId", animationIdLen, 4096000);
            }

            if (varPos1 + animationIdVarIntLen + animationIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("AnimationId", varPos1 + animationIdVarIntLen + animationIdLen, buf.readableBytes());
            }

            obj.animationId = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 14;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 6);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 14) {
            throw ProtocolException.invalidOffset("ItemAnimationsId", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 14 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 10);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 14) {
            throw ProtocolException.invalidOffset("AnimationId", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 14 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 14L;
   }

   public static int getEntityId(MemorySegment mem) {
      return getEntityId(mem, 0);
   }

   public static int getEntityId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static String getItemAnimationsId(MemorySegment mem) {
      return getItemAnimationsId(mem, 0);
   }

   @Nullable
   public static String getItemAnimationsId(MemorySegment mem, int offset) {
      return hasItemAnimationsId(mem, offset)
         ? PacketIO.readVarString("ItemAnimationsId", mem, offset + getValidatedOffset(mem, offset, 6, 14, "ItemAnimationsId"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getAnimationId(MemorySegment mem) {
      return getAnimationId(mem, 0);
   }

   @Nullable
   public static String getAnimationId(MemorySegment mem, int offset) {
      return hasAnimationId(mem, offset)
         ? PacketIO.readVarString("AnimationId", mem, offset + getValidatedOffset(mem, offset, 10, 14, "AnimationId"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static AnimationSlot getSlot(MemorySegment mem) {
      return getSlot(mem, 0);
   }

   public static AnimationSlot getSlot(MemorySegment mem, int offset) {
      return AnimationSlot.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 5)));
   }

   public static boolean hasItemAnimationsId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasAnimationId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static PlayAnimation toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static PlayAnimation toObject(MemorySegment mem, int offset) {
      if (offset + 14 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("PlayAnimation", offset + 14, (int)mem.byteSize());
      } else {
         return new PlayAnimation(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            hasItemAnimationsId(mem, offset)
               ? PacketIO.readVarString("ItemAnimationsId", mem, offset + getValidatedOffset(mem, offset, 6, 14, "ItemAnimationsId"), 4096000, PacketIO.UTF8)
               : null,
            hasAnimationId(mem, offset)
               ? PacketIO.readVarString("AnimationId", mem, offset + getValidatedOffset(mem, offset, 10, 14, "AnimationId"), 4096000, PacketIO.UTF8)
               : null,
            AnimationSlot.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 5)))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.itemAnimationsId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.animationId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.entityId);
      buf.writeByte(this.slot.getValue());
      int itemAnimationsIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int animationIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.itemAnimationsId != null) {
         buf.setIntLE(itemAnimationsIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.itemAnimationsId, 4096000);
      } else {
         buf.setIntLE(itemAnimationsIdOffsetSlot, -1);
      }

      if (this.animationId != null) {
         buf.setIntLE(animationIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.animationId, 4096000);
      } else {
         buf.setIntLE(animationIdOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.itemAnimationsId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.animationId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.entityId);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 5), (byte)this.slot.getValue());
      int varOffset = offset + 14;
      if (this.itemAnimationsId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 14);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.itemAnimationsId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), -1);
      }

      if (this.animationId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), varOffset - offset - 14);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.animationId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 14;
      if (this.itemAnimationsId != null) {
         size += PacketIO.stringSize(this.itemAnimationsId);
      }

      if (this.animationId != null) {
         size += PacketIO.stringSize(this.animationId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 14) {
         return ValidationResult.error("Buffer too small: expected at least 14 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 5) & 255;
         if (v >= 5) {
            return ValidationResult.error("Invalid AnimationSlot value for Slot");
         } else {
            if ((nullBits & 1) != 0) {
               v = buffer.getIntLE(offset + 6);
               if (v < 0 || v > buffer.writerIndex() - offset - 14) {
                  return ValidationResult.error("Invalid offset for ItemAnimationsId");
               }

               int pos = offset + 14 + v;
               int itemAnimationsIdLen = VarInt.peek(buffer, pos);
               if (itemAnimationsIdLen < 0) {
                  return ValidationResult.error("Invalid string length for ItemAnimationsId");
               }

               if (itemAnimationsIdLen > 4096000) {
                  return ValidationResult.error("ItemAnimationsId exceeds max length 4096000");
               }

               pos += VarInt.size(itemAnimationsIdLen);
               pos += itemAnimationsIdLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading ItemAnimationsId");
               }
            }

            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 10);
               if (v < 0 || v > buffer.writerIndex() - offset - 14) {
                  return ValidationResult.error("Invalid offset for AnimationId");
               }

               int posx = offset + 14 + v;
               int animationIdLen = VarInt.peek(buffer, posx);
               if (animationIdLen < 0) {
                  return ValidationResult.error("Invalid string length for AnimationId");
               }

               if (animationIdLen > 4096000) {
                  return ValidationResult.error("AnimationId exceeds max length 4096000");
               }

               posx += VarInt.size(animationIdLen);
               posx += animationIdLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading AnimationId");
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public PlayAnimation clone() {
      PlayAnimation copy = new PlayAnimation();
      copy.entityId = this.entityId;
      copy.itemAnimationsId = this.itemAnimationsId;
      copy.animationId = this.animationId;
      copy.slot = this.slot;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof PlayAnimation other)
            ? false
            : this.entityId == other.entityId
               && Objects.equals(this.itemAnimationsId, other.itemAnimationsId)
               && Objects.equals(this.animationId, other.animationId)
               && Objects.equals(this.slot, other.slot);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.entityId, this.itemAnimationsId, this.animationId, this.slot);
   }
}
