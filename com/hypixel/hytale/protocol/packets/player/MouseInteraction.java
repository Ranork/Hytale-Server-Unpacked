package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.MouseButtonEvent;
import com.hypixel.hytale.protocol.MouseMotionEvent;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.WorldInteraction;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector2fc;

public class MouseInteraction implements Packet, ToServerPacket {
   public static final int PACKET_ID = 111;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 44;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 52;
   public static final int MAX_SIZE = 20480071;
   public long clientTimestamp;
   public int activeSlot;
   @Nullable
   public String itemInHandId;
   @Nonnull
   public Vector2fc screenPoint = PacketIO.ZERO_VECTOR2;
   @Nullable
   public MouseButtonEvent mouseButton;
   @Nullable
   public MouseMotionEvent mouseMotion;
   @Nullable
   public WorldInteraction worldInteraction;

   @Override
   public int getId() {
      return 111;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public MouseInteraction() {
   }

   public MouseInteraction(
      long clientTimestamp,
      int activeSlot,
      @Nullable String itemInHandId,
      @Nonnull Vector2fc screenPoint,
      @Nullable MouseButtonEvent mouseButton,
      @Nullable MouseMotionEvent mouseMotion,
      @Nullable WorldInteraction worldInteraction
   ) {
      this.clientTimestamp = clientTimestamp;
      this.activeSlot = activeSlot;
      this.itemInHandId = itemInHandId;
      this.screenPoint = screenPoint;
      this.mouseButton = mouseButton;
      this.mouseMotion = mouseMotion;
      this.worldInteraction = worldInteraction;
   }

   public MouseInteraction(@Nonnull MouseInteraction other) {
      this.clientTimestamp = other.clientTimestamp;
      this.activeSlot = other.activeSlot;
      this.itemInHandId = other.itemInHandId;
      this.screenPoint = other.screenPoint;
      this.mouseButton = other.mouseButton;
      this.mouseMotion = other.mouseMotion;
      this.worldInteraction = other.worldInteraction;
   }

   @Nonnull
   public static MouseInteraction deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 52) {
         throw ProtocolException.bufferTooSmall("MouseInteraction", 52, buf.readableBytes() - offset);
      } else {
         MouseInteraction obj = new MouseInteraction();
         byte nullBits = buf.getByte(offset);
         obj.clientTimestamp = buf.getLongLE(offset + 1);
         obj.activeSlot = buf.getIntLE(offset + 9);
         obj.screenPoint = PacketIO.readVector2f(buf, offset + 13);
         if ((nullBits & 1) != 0) {
            obj.mouseButton = MouseButtonEvent.deserialize(buf, offset + 21);
         }

         if ((nullBits & 2) != 0) {
            obj.worldInteraction = WorldInteraction.deserialize(buf, offset + 24);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 44);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 52) {
               throw ProtocolException.invalidOffset("ItemInHandId", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 52 + varPosBase0;
            int itemInHandIdLen = VarInt.peek(buf, varPos0);
            if (itemInHandIdLen < 0) {
               throw ProtocolException.invalidVarInt("ItemInHandId");
            }

            int itemInHandIdVarIntLen = VarInt.size(itemInHandIdLen);
            if (itemInHandIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ItemInHandId", itemInHandIdLen, 4096000);
            }

            if (varPos0 + itemInHandIdVarIntLen + itemInHandIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ItemInHandId", varPos0 + itemInHandIdVarIntLen + itemInHandIdLen, buf.readableBytes());
            }

            obj.itemInHandId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 48);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 52) {
               throw ProtocolException.invalidOffset("MouseMotion", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 52 + varPosBase1;
            obj.mouseMotion = MouseMotionEvent.deserialize(buf, varPos1);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 52;
      if ((nullBits & 4) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 44);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 52) {
            throw ProtocolException.invalidOffset("ItemInHandId", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 52 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 48);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 52) {
            throw ProtocolException.invalidOffset("MouseMotion", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 52 + fieldOffset1;
         pos1 += MouseMotionEvent.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 52L;
   }

   public static long getClientTimestamp(MemorySegment mem) {
      return getClientTimestamp(mem, 0);
   }

   public static long getClientTimestamp(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_LONG, (long)(offset + 1));
   }

   public static int getActiveSlot(MemorySegment mem) {
      return getActiveSlot(mem, 0);
   }

   public static int getActiveSlot(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 9));
   }

   @Nullable
   public static String getItemInHandId(MemorySegment mem) {
      return getItemInHandId(mem, 0);
   }

   @Nullable
   public static String getItemInHandId(MemorySegment mem, int offset) {
      return hasItemInHandId(mem, offset)
         ? PacketIO.readVarString("ItemInHandId", mem, offset + getValidatedOffset(mem, offset, 44, 52, "ItemInHandId"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static Vector2fc getScreenPoint(MemorySegment mem) {
      return getScreenPoint(mem, 0);
   }

   public static Vector2fc getScreenPoint(MemorySegment mem, int offset) {
      return PacketIO.readVector2f(mem, offset + 13);
   }

   @Nullable
   public static MouseButtonEvent getMouseButton(MemorySegment mem) {
      return getMouseButton(mem, 0);
   }

   @Nullable
   public static MouseButtonEvent getMouseButton(MemorySegment mem, int offset) {
      return hasMouseButton(mem, offset) ? MouseButtonEvent.toObject(mem, offset + 21) : null;
   }

   @Nullable
   public static MouseMotionEvent getMouseMotion(MemorySegment mem) {
      return getMouseMotion(mem, 0);
   }

   @Nullable
   public static MouseMotionEvent getMouseMotion(MemorySegment mem, int offset) {
      return hasMouseMotion(mem, offset) ? MouseMotionEvent.toObject(mem, offset + getValidatedOffset(mem, offset, 48, 52, "MouseMotion")) : null;
   }

   @Nullable
   public static WorldInteraction getWorldInteraction(MemorySegment mem) {
      return getWorldInteraction(mem, 0);
   }

   @Nullable
   public static WorldInteraction getWorldInteraction(MemorySegment mem, int offset) {
      return hasWorldInteraction(mem, offset) ? WorldInteraction.toObject(mem, offset + 24) : null;
   }

   public static boolean hasMouseButton(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasWorldInteraction(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasItemInHandId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasMouseMotion(MemorySegment mem, int offset) {
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

   public static MouseInteraction toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static MouseInteraction toObject(MemorySegment mem, int offset) {
      if (offset + 52 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("MouseInteraction", offset + 52, (int)mem.byteSize());
      } else {
         return new MouseInteraction(
            mem.get(PacketIO.PROTO_LONG, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 9)),
            hasItemInHandId(mem, offset)
               ? PacketIO.readVarString("ItemInHandId", mem, offset + getValidatedOffset(mem, offset, 44, 52, "ItemInHandId"), 4096000, PacketIO.UTF8)
               : null,
            PacketIO.readVector2f(mem, offset + 13),
            hasMouseButton(mem, offset) ? MouseButtonEvent.toObject(mem, offset + 21) : null,
            hasMouseMotion(mem, offset) ? MouseMotionEvent.toObject(mem, offset + getValidatedOffset(mem, offset, 48, 52, "MouseMotion")) : null,
            hasWorldInteraction(mem, offset) ? WorldInteraction.toObject(mem, offset + 24) : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.mouseButton != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.worldInteraction != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.itemInHandId != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.mouseMotion != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      buf.writeLongLE(this.clientTimestamp);
      buf.writeIntLE(this.activeSlot);
      PacketIO.writeVector2f(buf, this.screenPoint);
      if (this.mouseButton != null) {
         this.mouseButton.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      if (this.worldInteraction != null) {
         this.worldInteraction.serialize(buf);
      } else {
         buf.writeZero(20);
      }

      int itemInHandIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int mouseMotionOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.itemInHandId != null) {
         buf.setIntLE(itemInHandIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.itemInHandId, 4096000);
      } else {
         buf.setIntLE(itemInHandIdOffsetSlot, -1);
      }

      if (this.mouseMotion != null) {
         buf.setIntLE(mouseMotionOffsetSlot, buf.writerIndex() - varBlockStart);
         this.mouseMotion.serialize(buf);
      } else {
         buf.setIntLE(mouseMotionOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.mouseButton != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.worldInteraction != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.itemInHandId != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.mouseMotion != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_LONG, (long)(offset + 1), this.clientTimestamp);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 9), this.activeSlot);
      PacketIO.writeVector2f(mem, offset + 13, this.screenPoint);
      if (this.mouseButton != null) {
         this.mouseButton.serialize(mem, offset + 21);
      } else {
         mem.asSlice(offset + 21, 3L).fill((byte)0);
      }

      if (this.worldInteraction != null) {
         this.worldInteraction.serialize(mem, offset + 24);
      } else {
         mem.asSlice(offset + 24, 20L).fill((byte)0);
      }

      int varOffset = offset + 52;
      if (this.itemInHandId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 44), varOffset - offset - 52);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.itemInHandId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 44), -1);
      }

      if (this.mouseMotion != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 48), varOffset - offset - 52);
         varOffset += this.mouseMotion.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 48), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 52;
      if (this.itemInHandId != null) {
         size += PacketIO.stringSize(this.itemInHandId);
      }

      if (this.mouseMotion != null) {
         size += this.mouseMotion.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 52) {
         return ValidationResult.error("Buffer too small: expected at least 52 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 4) != 0) {
            int itemInHandIdOffset = buffer.getIntLE(offset + 44);
            if (itemInHandIdOffset < 0 || itemInHandIdOffset > buffer.writerIndex() - offset - 52) {
               return ValidationResult.error("Invalid offset for ItemInHandId");
            }

            int pos = offset + 52 + itemInHandIdOffset;
            int itemInHandIdLen = VarInt.peek(buffer, pos);
            if (itemInHandIdLen < 0) {
               return ValidationResult.error("Invalid string length for ItemInHandId");
            }

            if (itemInHandIdLen > 4096000) {
               return ValidationResult.error("ItemInHandId exceeds max length 4096000");
            }

            pos += VarInt.size(itemInHandIdLen);
            pos += itemInHandIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ItemInHandId");
            }
         }

         if ((nullBits & 8) != 0) {
            int mouseMotionOffset = buffer.getIntLE(offset + 48);
            if (mouseMotionOffset < 0 || mouseMotionOffset > buffer.writerIndex() - offset - 52) {
               return ValidationResult.error("Invalid offset for MouseMotion");
            }

            int posx = offset + 52 + mouseMotionOffset;
            ValidationResult mouseMotionResult = MouseMotionEvent.validateStructure(buffer, posx);
            if (!mouseMotionResult.isValid()) {
               return ValidationResult.error("Invalid MouseMotion: " + mouseMotionResult.error());
            }

            posx += MouseMotionEvent.computeBytesConsumed(buffer, posx);
         }

         return ValidationResult.OK;
      }
   }

   public MouseInteraction clone() {
      MouseInteraction copy = new MouseInteraction();
      copy.clientTimestamp = this.clientTimestamp;
      copy.activeSlot = this.activeSlot;
      copy.itemInHandId = this.itemInHandId;
      copy.screenPoint = this.screenPoint;
      copy.mouseButton = this.mouseButton != null ? this.mouseButton.clone() : null;
      copy.mouseMotion = this.mouseMotion != null ? this.mouseMotion.clone() : null;
      copy.worldInteraction = this.worldInteraction != null ? this.worldInteraction.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof MouseInteraction other)
            ? false
            : this.clientTimestamp == other.clientTimestamp
               && this.activeSlot == other.activeSlot
               && Objects.equals(this.itemInHandId, other.itemInHandId)
               && Objects.equals(this.screenPoint, other.screenPoint)
               && Objects.equals(this.mouseButton, other.mouseButton)
               && Objects.equals(this.mouseMotion, other.mouseMotion)
               && Objects.equals(this.worldInteraction, other.worldInteraction);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.clientTimestamp, this.activeSlot, this.itemInHandId, this.screenPoint, this.mouseButton, this.mouseMotion, this.worldInteraction);
   }
}
