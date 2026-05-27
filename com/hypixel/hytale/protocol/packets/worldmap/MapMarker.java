package com.hypixel.hytale.protocol.packets.worldmap;

import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.Transform;
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

public class MapMarker {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 38;
   public static final int VARIABLE_FIELD_COUNT = 5;
   public static final int VARIABLE_BLOCK_START = 58;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public String id = "";
   @Nullable
   public FormattedMessage name;
   @Nonnull
   public String markerImage = "";
   @Nonnull
   public Transform transform = new Transform();
   @Nullable
   public ContextMenuItem[] contextMenuItems;
   @Nullable
   public MapMarkerComponent[] components;

   public MapMarker() {
   }

   public MapMarker(
      @Nonnull String id,
      @Nullable FormattedMessage name,
      @Nonnull String markerImage,
      @Nonnull Transform transform,
      @Nullable ContextMenuItem[] contextMenuItems,
      @Nullable MapMarkerComponent[] components
   ) {
      this.id = id;
      this.name = name;
      this.markerImage = markerImage;
      this.transform = transform;
      this.contextMenuItems = contextMenuItems;
      this.components = components;
   }

   public MapMarker(@Nonnull MapMarker other) {
      this.id = other.id;
      this.name = other.name;
      this.markerImage = other.markerImage;
      this.transform = other.transform;
      this.contextMenuItems = other.contextMenuItems;
      this.components = other.components;
   }

   @Nonnull
   public static MapMarker deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 58) {
         throw ProtocolException.bufferTooSmall("MapMarker", 58, buf.readableBytes() - offset);
      } else {
         MapMarker obj = new MapMarker();
         byte nullBits = buf.getByte(offset);
         obj.transform = Transform.deserialize(buf, offset + 1);
         int varPosBase0 = buf.getIntLE(offset + 38);
         if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 58) {
            int varPos0 = offset + 58 + varPosBase0;
            int idLen = VarInt.peek(buf, varPos0);
            if (idLen < 0) {
               throw ProtocolException.invalidVarInt("Id");
            } else {
               int idVarIntLen = VarInt.size(idLen);
               if (idLen > 4096000) {
                  throw ProtocolException.stringTooLong("Id", idLen, 4096000);
               } else if (varPos0 + idVarIntLen + idLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("Id", varPos0 + idVarIntLen + idLen, buf.readableBytes());
               } else {
                  obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
                  if ((nullBits & 1) != 0) {
                     varPosBase0 = buf.getIntLE(offset + 42);
                     if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 58) {
                        throw ProtocolException.invalidOffset("Name", varPosBase0, buf.readableBytes());
                     }

                     varPos0 = offset + 58 + varPosBase0;
                     obj.name = FormattedMessage.deserialize(buf, varPos0);
                  }

                  varPosBase0 = buf.getIntLE(offset + 46);
                  if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 58) {
                     varPos0 = offset + 58 + varPosBase0;
                     idLen = VarInt.peek(buf, varPos0);
                     if (idLen < 0) {
                        throw ProtocolException.invalidVarInt("MarkerImage");
                     } else {
                        idVarIntLen = VarInt.size(idLen);
                        if (idLen > 4096000) {
                           throw ProtocolException.stringTooLong("MarkerImage", idLen, 4096000);
                        } else if (varPos0 + idVarIntLen + idLen > buf.readableBytes()) {
                           throw ProtocolException.bufferTooSmall("MarkerImage", varPos0 + idVarIntLen + idLen, buf.readableBytes());
                        } else {
                           obj.markerImage = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
                           if ((nullBits & 2) != 0) {
                              varPosBase0 = buf.getIntLE(offset + 50);
                              if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 58) {
                                 throw ProtocolException.invalidOffset("ContextMenuItems", varPosBase0, buf.readableBytes());
                              }

                              varPos0 = offset + 58 + varPosBase0;
                              idLen = VarInt.peek(buf, varPos0);
                              if (idLen < 0) {
                                 throw ProtocolException.invalidVarInt("ContextMenuItems");
                              }

                              idVarIntLen = VarInt.size(idLen);
                              if (idLen > 4096000) {
                                 throw ProtocolException.arrayTooLong("ContextMenuItems", idLen, 4096000);
                              }

                              if (varPos0 + idVarIntLen + idLen * 0L > buf.readableBytes()) {
                                 throw ProtocolException.bufferTooSmall("ContextMenuItems", varPos0 + idVarIntLen + idLen * 0, buf.readableBytes());
                              }

                              obj.contextMenuItems = new ContextMenuItem[idLen];
                              int elemPos = varPos0 + idVarIntLen;

                              for (int i = 0; i < idLen; i++) {
                                 obj.contextMenuItems[i] = ContextMenuItem.deserialize(buf, elemPos);
                                 elemPos += ContextMenuItem.computeBytesConsumed(buf, elemPos);
                              }
                           }

                           if ((nullBits & 4) != 0) {
                              varPosBase0 = buf.getIntLE(offset + 54);
                              if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 58) {
                                 throw ProtocolException.invalidOffset("Components", varPosBase0, buf.readableBytes());
                              }

                              varPos0 = offset + 58 + varPosBase0;
                              idLen = VarInt.peek(buf, varPos0);
                              if (idLen < 0) {
                                 throw ProtocolException.invalidVarInt("Components");
                              }

                              idVarIntLen = VarInt.size(idLen);
                              if (idLen > 4096000) {
                                 throw ProtocolException.arrayTooLong("Components", idLen, 4096000);
                              }

                              if (varPos0 + idVarIntLen + idLen * 1L > buf.readableBytes()) {
                                 throw ProtocolException.bufferTooSmall("Components", varPos0 + idVarIntLen + idLen * 1, buf.readableBytes());
                              }

                              obj.components = new MapMarkerComponent[idLen];
                              int elemPos = varPos0 + idVarIntLen;

                              for (int i = 0; i < idLen; i++) {
                                 obj.components[i] = MapMarkerComponent.deserialize(buf, elemPos);
                                 elemPos += MapMarkerComponent.computeBytesConsumed(buf, elemPos);
                              }
                           }

                           return obj;
                        }
                     }
                  } else {
                     throw ProtocolException.invalidOffset("MarkerImage", varPosBase0, buf.readableBytes());
                  }
               }
            }
         } else {
            throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 58;
      int fieldOffset0 = buf.getIntLE(offset + 38);
      if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 58) {
         int pos0 = offset + 58 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }

         if ((nullBits & 1) != 0) {
            fieldOffset0 = buf.getIntLE(offset + 42);
            if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 58) {
               throw ProtocolException.invalidOffset("Name", fieldOffset0, maxEnd);
            }

            pos0 = offset + 58 + fieldOffset0;
            pos0 += FormattedMessage.computeBytesConsumed(buf, pos0);
            if (pos0 - offset > maxEnd) {
               maxEnd = pos0 - offset;
            }
         }

         fieldOffset0 = buf.getIntLE(offset + 46);
         if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 58) {
            pos0 = offset + 58 + fieldOffset0;
            sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.size(sl) + sl;
            if (pos0 - offset > maxEnd) {
               maxEnd = pos0 - offset;
            }

            if ((nullBits & 2) != 0) {
               fieldOffset0 = buf.getIntLE(offset + 50);
               if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 58) {
                  throw ProtocolException.invalidOffset("ContextMenuItems", fieldOffset0, maxEnd);
               }

               pos0 = offset + 58 + fieldOffset0;
               sl = VarInt.peek(buf, pos0);
               pos0 += VarInt.size(sl);

               for (int i = 0; i < sl; i++) {
                  pos0 += ContextMenuItem.computeBytesConsumed(buf, pos0);
               }

               if (pos0 - offset > maxEnd) {
                  maxEnd = pos0 - offset;
               }
            }

            if ((nullBits & 4) != 0) {
               fieldOffset0 = buf.getIntLE(offset + 54);
               if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 58) {
                  throw ProtocolException.invalidOffset("Components", fieldOffset0, maxEnd);
               }

               pos0 = offset + 58 + fieldOffset0;
               sl = VarInt.peek(buf, pos0);
               pos0 += VarInt.size(sl);

               for (int i = 0; i < sl; i++) {
                  pos0 += MapMarkerComponent.computeBytesConsumed(buf, pos0);
               }

               if (pos0 - offset > maxEnd) {
                  maxEnd = pos0 - offset;
               }
            }

            return maxEnd;
         } else {
            throw ProtocolException.invalidOffset("MarkerImage", fieldOffset0, maxEnd);
         }
      } else {
         throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
      }
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 58L;
   }

   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   public static String getId(MemorySegment mem, int offset) {
      return PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 38, 58, "Id"), 4096000, PacketIO.UTF8);
   }

   @Nullable
   public static FormattedMessage getName(MemorySegment mem) {
      return getName(mem, 0);
   }

   @Nullable
   public static FormattedMessage getName(MemorySegment mem, int offset) {
      return hasName(mem, offset) ? FormattedMessage.toObject(mem, offset + getValidatedOffset(mem, offset, 42, 58, "Name")) : null;
   }

   public static String getMarkerImage(MemorySegment mem) {
      return getMarkerImage(mem, 0);
   }

   public static String getMarkerImage(MemorySegment mem, int offset) {
      return PacketIO.readVarString("MarkerImage", mem, offset + getValidatedOffset(mem, offset, 46, 58, "MarkerImage"), 4096000, PacketIO.UTF8);
   }

   public static Transform getTransform(MemorySegment mem) {
      return getTransform(mem, 0);
   }

   public static Transform getTransform(MemorySegment mem, int offset) {
      return Transform.toObject(mem, offset + 1);
   }

   @Nullable
   public static ContextMenuItem[] getContextMenuItems(MemorySegment mem) {
      return getContextMenuItems(mem, 0);
   }

   @Nullable
   public static ContextMenuItem[] getContextMenuItems(MemorySegment mem, int offset) {
      if (!hasContextMenuItems(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 50, 58, "ContextMenuItems");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ContextMenuItems", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("ContextMenuItems", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ContextMenuItems", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ContextMenuItem[] data = new ContextMenuItem[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ContextMenuItem.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static MapMarkerComponent[] getComponents(MemorySegment mem) {
      return getComponents(mem, 0);
   }

   @Nullable
   public static MapMarkerComponent[] getComponents(MemorySegment mem, int offset) {
      if (!hasComponents(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 54, 58, "Components");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Components", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Components", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Components", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               MapMarkerComponent[] data = new MapMarkerComponent[len];

               for (int i = 0; i < len; i++) {
                  data[i] = MapMarkerComponent.toObject(mem, off);
                  off += data[i].computeSizeWithTypeId();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasName(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasContextMenuItems(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasComponents(MemorySegment mem, int offset) {
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

   public static MapMarker toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static MapMarker toObject(MemorySegment mem, int offset) {
      if (offset + 58 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("MapMarker", offset + 58, (int)mem.byteSize());
      } else {
         ContextMenuItem[] contextMenuItems = null;
         if (hasContextMenuItems(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 50, 58, "ContextMenuItems");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("ContextMenuItems", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("ContextMenuItems", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ContextMenuItems", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            contextMenuItems = new ContextMenuItem[len];

            for (int i = 0; i < len; i++) {
               contextMenuItems[i] = ContextMenuItem.toObject(mem, off);
               off += contextMenuItems[i].computeSize();
            }
         }

         MapMarkerComponent[] components = null;
         if (hasComponents(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 54, 58, "Components");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("Components", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("Components", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Components", offx + lenOffset + lenx, (int)mem.byteSize());
            }

            offx += lenOffset;
            components = new MapMarkerComponent[lenx];

            for (int i = 0; i < lenx; i++) {
               components[i] = MapMarkerComponent.toObject(mem, offx);
               offx += components[i].computeSizeWithTypeId();
            }
         }

         return new MapMarker(
            PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 38, 58, "Id"), 4096000, PacketIO.UTF8),
            hasName(mem, offset) ? FormattedMessage.toObject(mem, offset + getValidatedOffset(mem, offset, 42, 58, "Name")) : null,
            PacketIO.readVarString("MarkerImage", mem, offset + getValidatedOffset(mem, offset, 46, 58, "MarkerImage"), 4096000, PacketIO.UTF8),
            Transform.toObject(mem, offset + 1),
            contextMenuItems,
            components
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.contextMenuItems != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.components != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      this.transform.serialize(buf);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int markerImageOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int contextMenuItemsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int componentsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarString(buf, this.id, 4096000);
      if (this.name != null) {
         buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
         this.name.serialize(buf);
      } else {
         buf.setIntLE(nameOffsetSlot, -1);
      }

      buf.setIntLE(markerImageOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarString(buf, this.markerImage, 4096000);
      if (this.contextMenuItems != null) {
         buf.setIntLE(contextMenuItemsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.contextMenuItems.length > 4096000) {
            throw ProtocolException.arrayTooLong("ContextMenuItems", this.contextMenuItems.length, 4096000);
         }

         VarInt.write(buf, this.contextMenuItems.length);

         for (ContextMenuItem item : this.contextMenuItems) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(contextMenuItemsOffsetSlot, -1);
      }

      if (this.components != null) {
         buf.setIntLE(componentsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.components.length > 4096000) {
            throw ProtocolException.arrayTooLong("Components", this.components.length, 4096000);
         }

         VarInt.write(buf, this.components.length);

         for (MapMarkerComponent item : this.components) {
            item.serializeWithTypeId(buf);
         }
      } else {
         buf.setIntLE(componentsOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.contextMenuItems != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.components != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      this.transform.serialize(mem, offset + 1);
      int varOffset = offset + 58;
      mem.set(PacketIO.PROTO_INT, (long)(offset + 38), varOffset - offset - 58);
      varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      if (this.name != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 42), varOffset - offset - 58);
         varOffset += this.name.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 42), -1);
      }

      mem.set(PacketIO.PROTO_INT, (long)(offset + 46), varOffset - offset - 58);
      varOffset += PacketIO.writeVarString(mem, varOffset, this.markerImage, 4096000);
      if (this.contextMenuItems != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 50), varOffset - offset - 58);
         if (this.contextMenuItems.length > 4096000) {
            throw ProtocolException.arrayTooLong("ContextMenuItems", this.contextMenuItems.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.contextMenuItems.length);
         int contextMenuItemsValueOffset = 0;

         for (int i = 0; i < this.contextMenuItems.length; i++) {
            contextMenuItemsValueOffset += this.contextMenuItems[i].serialize(mem, varOffset + contextMenuItemsValueOffset);
         }

         varOffset += contextMenuItemsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 50), -1);
      }

      if (this.components != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 54), varOffset - offset - 58);
         if (this.components.length > 4096000) {
            throw ProtocolException.arrayTooLong("Components", this.components.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.components.length);
         int componentsValueOffset = 0;

         for (int i = 0; i < this.components.length; i++) {
            componentsValueOffset += this.components[i].serializeWithTypeId(mem, varOffset + componentsValueOffset);
         }

         varOffset += componentsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 54), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 58;
      size += PacketIO.stringSize(this.id);
      if (this.name != null) {
         size += this.name.computeSize();
      }

      size += PacketIO.stringSize(this.markerImage);
      if (this.contextMenuItems != null) {
         int contextMenuItemsSize = 0;

         for (ContextMenuItem elem : this.contextMenuItems) {
            contextMenuItemsSize += elem.computeSize();
         }

         size += VarInt.size(this.contextMenuItems.length) + contextMenuItemsSize;
      }

      if (this.components != null) {
         int componentsSize = 0;

         for (MapMarkerComponent elem : this.components) {
            componentsSize += elem.computeSizeWithTypeId();
         }

         size += VarInt.size(this.components.length) + componentsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 58) {
         return ValidationResult.error("Buffer too small: expected at least 58 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int idOffset = buffer.getIntLE(offset + 38);
         if (idOffset >= 0 && idOffset <= buffer.writerIndex() - offset - 58) {
            int pos = offset + 58 + idOffset;
            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            } else if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            } else {
               pos += VarInt.size(idLen);
               pos += idLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Id");
               } else {
                  if ((nullBits & 1) != 0) {
                     idOffset = buffer.getIntLE(offset + 42);
                     if (idOffset < 0 || idOffset > buffer.writerIndex() - offset - 58) {
                        return ValidationResult.error("Invalid offset for Name");
                     }

                     pos = offset + 58 + idOffset;
                     ValidationResult nameResult = FormattedMessage.validateStructure(buffer, pos);
                     if (!nameResult.isValid()) {
                        return ValidationResult.error("Invalid Name: " + nameResult.error());
                     }

                     pos += FormattedMessage.computeBytesConsumed(buffer, pos);
                  }

                  idOffset = buffer.getIntLE(offset + 46);
                  if (idOffset >= 0 && idOffset <= buffer.writerIndex() - offset - 58) {
                     pos = offset + 58 + idOffset;
                     idLen = VarInt.peek(buffer, pos);
                     if (idLen < 0) {
                        return ValidationResult.error("Invalid string length for MarkerImage");
                     } else if (idLen > 4096000) {
                        return ValidationResult.error("MarkerImage exceeds max length 4096000");
                     } else {
                        pos += VarInt.size(idLen);
                        pos += idLen;
                        if (pos > buffer.writerIndex()) {
                           return ValidationResult.error("Buffer overflow reading MarkerImage");
                        } else {
                           if ((nullBits & 2) != 0) {
                              idOffset = buffer.getIntLE(offset + 50);
                              if (idOffset < 0 || idOffset > buffer.writerIndex() - offset - 58) {
                                 return ValidationResult.error("Invalid offset for ContextMenuItems");
                              }

                              pos = offset + 58 + idOffset;
                              idLen = VarInt.peek(buffer, pos);
                              if (idLen < 0) {
                                 return ValidationResult.error("Invalid array count for ContextMenuItems");
                              }

                              if (idLen > 4096000) {
                                 return ValidationResult.error("ContextMenuItems exceeds max length 4096000");
                              }

                              pos += VarInt.size(idLen);

                              for (int i = 0; i < idLen; i++) {
                                 ValidationResult structResult = ContextMenuItem.validateStructure(buffer, pos);
                                 if (!structResult.isValid()) {
                                    return ValidationResult.error("Invalid ContextMenuItem in ContextMenuItems[" + i + "]: " + structResult.error());
                                 }

                                 pos += ContextMenuItem.computeBytesConsumed(buffer, pos);
                              }
                           }

                           if ((nullBits & 4) != 0) {
                              idOffset = buffer.getIntLE(offset + 54);
                              if (idOffset < 0 || idOffset > buffer.writerIndex() - offset - 58) {
                                 return ValidationResult.error("Invalid offset for Components");
                              }

                              pos = offset + 58 + idOffset;
                              idLen = VarInt.peek(buffer, pos);
                              if (idLen < 0) {
                                 return ValidationResult.error("Invalid array count for Components");
                              }

                              if (idLen > 4096000) {
                                 return ValidationResult.error("Components exceeds max length 4096000");
                              }

                              pos += VarInt.size(idLen);

                              for (int i = 0; i < idLen; i++) {
                                 ValidationResult structResult = MapMarkerComponent.validateStructure(buffer, pos);
                                 if (!structResult.isValid()) {
                                    return ValidationResult.error("Invalid MapMarkerComponent in Components[" + i + "]: " + structResult.error());
                                 }

                                 pos += MapMarkerComponent.computeBytesConsumed(buffer, pos);
                              }
                           }

                           return ValidationResult.OK;
                        }
                     }
                  } else {
                     return ValidationResult.error("Invalid offset for MarkerImage");
                  }
               }
            }
         } else {
            return ValidationResult.error("Invalid offset for Id");
         }
      }
   }

   public MapMarker clone() {
      MapMarker copy = new MapMarker();
      copy.id = this.id;
      copy.name = this.name != null ? this.name.clone() : null;
      copy.markerImage = this.markerImage;
      copy.transform = this.transform.clone();
      copy.contextMenuItems = this.contextMenuItems != null ? Arrays.stream(this.contextMenuItems).map(e -> e.clone()).toArray(ContextMenuItem[]::new) : null;
      copy.components = this.components != null ? Arrays.copyOf(this.components, this.components.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof MapMarker other)
            ? false
            : Objects.equals(this.id, other.id)
               && Objects.equals(this.name, other.name)
               && Objects.equals(this.markerImage, other.markerImage)
               && Objects.equals(this.transform, other.transform)
               && Arrays.equals((Object[])this.contextMenuItems, (Object[])other.contextMenuItems)
               && Arrays.equals((Object[])this.components, (Object[])other.components);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.id);
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Objects.hashCode(this.markerImage);
      result = 31 * result + Objects.hashCode(this.transform);
      result = 31 * result + Arrays.hashCode((Object[])this.contextMenuItems);
      return 31 * result + Arrays.hashCode((Object[])this.components);
   }
}
