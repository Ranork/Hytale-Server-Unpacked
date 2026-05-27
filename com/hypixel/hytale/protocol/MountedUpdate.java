package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3fc;

public class MountedUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 47;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 47;
   public static final int MAX_SIZE = 47;
   public int mountedToEntity;
   @Nonnull
   public Vector3fc attachmentOffset = PacketIO.ZERO_VECTOR3;
   @Nonnull
   public MountController controller = MountController.Minecart;
   @Nullable
   public BlockMount block;

   public MountedUpdate() {
   }

   public MountedUpdate(int mountedToEntity, @Nonnull Vector3fc attachmentOffset, @Nonnull MountController controller, @Nullable BlockMount block) {
      this.mountedToEntity = mountedToEntity;
      this.attachmentOffset = attachmentOffset;
      this.controller = controller;
      this.block = block;
   }

   public MountedUpdate(@Nonnull MountedUpdate other) {
      this.mountedToEntity = other.mountedToEntity;
      this.attachmentOffset = other.attachmentOffset;
      this.controller = other.controller;
      this.block = other.block;
   }

   @Nonnull
   public static MountedUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 47) {
         throw ProtocolException.bufferTooSmall("MountedUpdate", 47, buf.readableBytes() - offset);
      } else {
         MountedUpdate obj = new MountedUpdate();
         byte nullBits = buf.getByte(offset);
         obj.mountedToEntity = buf.getIntLE(offset + 1);
         obj.attachmentOffset = PacketIO.readVector3f(buf, offset + 5);
         obj.controller = MountController.fromValue(buf.getByte(offset + 17));
         if ((nullBits & 1) != 0) {
            obj.block = BlockMount.deserialize(buf, offset + 18);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 47;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 47L;
   }

   public static int getMountedToEntity(MemorySegment mem) {
      return getMountedToEntity(mem, 0);
   }

   public static int getMountedToEntity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static Vector3fc getAttachmentOffset(MemorySegment mem) {
      return getAttachmentOffset(mem, 0);
   }

   public static Vector3fc getAttachmentOffset(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 5);
   }

   public static MountController getController(MemorySegment mem) {
      return getController(mem, 0);
   }

   public static MountController getController(MemorySegment mem, int offset) {
      return MountController.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 17)));
   }

   @Nullable
   public static BlockMount getBlock(MemorySegment mem) {
      return getBlock(mem, 0);
   }

   @Nullable
   public static BlockMount getBlock(MemorySegment mem, int offset) {
      return hasBlock(mem, offset) ? BlockMount.toObject(mem, offset + 18) : null;
   }

   public static boolean hasBlock(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static MountedUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static MountedUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 47 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("MountedUpdate", offset + 47, (int)mem.byteSize());
      } else {
         return new MountedUpdate(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            PacketIO.readVector3f(mem, offset + 5),
            MountController.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 17))),
            hasBlock(mem, offset) ? BlockMount.toObject(mem, offset + 18) : null
         );
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.block != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.mountedToEntity);
      PacketIO.writeVector3f(buf, this.attachmentOffset);
      buf.writeByte(this.controller.getValue());
      if (this.block != null) {
         this.block.serialize(buf);
      } else {
         buf.writeZero(29);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.block != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.mountedToEntity);
      PacketIO.writeVector3f(mem, offset + 5, this.attachmentOffset);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 17), (byte)this.controller.getValue());
      if (this.block != null) {
         this.block.serialize(mem, offset + 18);
      } else {
         mem.asSlice(offset + 18, 29L).fill((byte)0);
      }

      return 47;
   }

   @Override
   public int computeSize() {
      return 47;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 47) {
         return ValidationResult.error("Buffer too small: expected at least 47 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 17) & 255;
         return v >= 2 ? ValidationResult.error("Invalid MountController value for Controller") : ValidationResult.OK;
      }
   }

   public MountedUpdate clone() {
      MountedUpdate copy = new MountedUpdate();
      copy.mountedToEntity = this.mountedToEntity;
      copy.attachmentOffset = this.attachmentOffset;
      copy.controller = this.controller;
      copy.block = this.block != null ? this.block.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof MountedUpdate other)
            ? false
            : this.mountedToEntity == other.mountedToEntity
               && Objects.equals(this.attachmentOffset, other.attachmentOffset)
               && Objects.equals(this.controller, other.controller)
               && Objects.equals(this.block, other.block);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.mountedToEntity, this.attachmentOffset, this.controller, this.block);
   }
}
