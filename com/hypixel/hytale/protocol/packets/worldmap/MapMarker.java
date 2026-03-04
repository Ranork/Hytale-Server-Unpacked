package com.hypixel.hytale.protocol.packets.worldmap;

import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.Transform;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MapMarker {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 38;
   public static final int VARIABLE_FIELD_COUNT = 6;
   public static final int VARIABLE_BLOCK_START = 62;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public String id = "";
   @Nullable
   public FormattedMessage name;
   @Nullable
   public String customName;
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
      @Nullable String customName,
      @Nonnull String markerImage,
      @Nonnull Transform transform,
      @Nullable ContextMenuItem[] contextMenuItems,
      @Nullable MapMarkerComponent[] components
   ) {
      this.id = id;
      this.name = name;
      this.customName = customName;
      this.markerImage = markerImage;
      this.transform = transform;
      this.contextMenuItems = contextMenuItems;
      this.components = components;
   }

   public MapMarker(@Nonnull MapMarker other) {
      this.id = other.id;
      this.name = other.name;
      this.customName = other.customName;
      this.markerImage = other.markerImage;
      this.transform = other.transform;
      this.contextMenuItems = other.contextMenuItems;
      this.components = other.components;
   }

   @Nonnull
   public static MapMarker deserialize(@Nonnull ByteBuf buf, int offset) {
      MapMarker obj = new MapMarker();
      byte nullBits = buf.getByte(offset);
      obj.transform = Transform.deserialize(buf, offset + 1);
      int varPos0 = offset + 62 + buf.getIntLE(offset + 38);
      int idLen = VarInt.peek(buf, varPos0);
      if (idLen < 0) {
         throw ProtocolException.negativeLength("Id", idLen);
      } else if (idLen > 4096000) {
         throw ProtocolException.stringTooLong("Id", idLen, 4096000);
      } else {
         obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         if ((nullBits & 1) != 0) {
            varPos0 = offset + 62 + buf.getIntLE(offset + 42);
            obj.name = FormattedMessage.deserialize(buf, varPos0);
         }

         if ((nullBits & 2) != 0) {
            varPos0 = offset + 62 + buf.getIntLE(offset + 46);
            idLen = VarInt.peek(buf, varPos0);
            if (idLen < 0) {
               throw ProtocolException.negativeLength("CustomName", idLen);
            }

            if (idLen > 4096000) {
               throw ProtocolException.stringTooLong("CustomName", idLen, 4096000);
            }

            obj.customName = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         varPos0 = offset + 62 + buf.getIntLE(offset + 50);
         idLen = VarInt.peek(buf, varPos0);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("MarkerImage", idLen);
         } else if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("MarkerImage", idLen, 4096000);
         } else {
            obj.markerImage = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
            if ((nullBits & 4) != 0) {
               varPos0 = offset + 62 + buf.getIntLE(offset + 54);
               idLen = VarInt.peek(buf, varPos0);
               if (idLen < 0) {
                  throw ProtocolException.negativeLength("ContextMenuItems", idLen);
               }

               if (idLen > 4096000) {
                  throw ProtocolException.arrayTooLong("ContextMenuItems", idLen, 4096000);
               }

               int varIntLen = VarInt.length(buf, varPos0);
               if (varPos0 + varIntLen + idLen * 0L > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("ContextMenuItems", varPos0 + varIntLen + idLen * 0, buf.readableBytes());
               }

               obj.contextMenuItems = new ContextMenuItem[idLen];
               int elemPos = varPos0 + varIntLen;

               for (int i = 0; i < idLen; i++) {
                  obj.contextMenuItems[i] = ContextMenuItem.deserialize(buf, elemPos);
                  elemPos += ContextMenuItem.computeBytesConsumed(buf, elemPos);
               }
            }

            if ((nullBits & 8) != 0) {
               varPos0 = offset + 62 + buf.getIntLE(offset + 58);
               idLen = VarInt.peek(buf, varPos0);
               if (idLen < 0) {
                  throw ProtocolException.negativeLength("Components", idLen);
               }

               if (idLen > 4096000) {
                  throw ProtocolException.arrayTooLong("Components", idLen, 4096000);
               }

               int varIntLen = VarInt.length(buf, varPos0);
               if (varPos0 + varIntLen + idLen * 1L > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("Components", varPos0 + varIntLen + idLen * 1, buf.readableBytes());
               }

               obj.components = new MapMarkerComponent[idLen];
               int elemPos = varPos0 + varIntLen;

               for (int i = 0; i < idLen; i++) {
                  obj.components[i] = MapMarkerComponent.deserialize(buf, elemPos);
                  elemPos += MapMarkerComponent.computeBytesConsumed(buf, elemPos);
               }
            }

            return obj;
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 62;
      int fieldOffset0 = buf.getIntLE(offset + 38);
      int pos0 = offset + 62 + fieldOffset0;
      int sl = VarInt.peek(buf, pos0);
      pos0 += VarInt.length(buf, pos0) + sl;
      if (pos0 - offset > maxEnd) {
         maxEnd = pos0 - offset;
      }

      if ((nullBits & 1) != 0) {
         fieldOffset0 = buf.getIntLE(offset + 42);
         pos0 = offset + 62 + fieldOffset0;
         pos0 += FormattedMessage.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         fieldOffset0 = buf.getIntLE(offset + 46);
         pos0 = offset + 62 + fieldOffset0;
         sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      fieldOffset0 = buf.getIntLE(offset + 50);
      pos0 = offset + 62 + fieldOffset0;
      sl = VarInt.peek(buf, pos0);
      pos0 += VarInt.length(buf, pos0) + sl;
      if (pos0 - offset > maxEnd) {
         maxEnd = pos0 - offset;
      }

      if ((nullBits & 4) != 0) {
         fieldOffset0 = buf.getIntLE(offset + 54);
         pos0 = offset + 62 + fieldOffset0;
         sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0);

         for (int i = 0; i < sl; i++) {
            pos0 += ContextMenuItem.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         fieldOffset0 = buf.getIntLE(offset + 58);
         pos0 = offset + 62 + fieldOffset0;
         sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0);

         for (int i = 0; i < sl; i++) {
            pos0 += MapMarkerComponent.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.customName != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.contextMenuItems != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.components != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      this.transform.serialize(buf);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int customNameOffsetSlot = buf.writerIndex();
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

      if (this.customName != null) {
         buf.setIntLE(customNameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.customName, 4096000);
      } else {
         buf.setIntLE(customNameOffsetSlot, -1);
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

   public int computeSize() {
      int size = 62;
      size += PacketIO.stringSize(this.id);
      if (this.name != null) {
         size += this.name.computeSize();
      }

      if (this.customName != null) {
         size += PacketIO.stringSize(this.customName);
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
      if (buffer.readableBytes() - offset < 62) {
         return ValidationResult.error("Buffer too small: expected at least 62 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int idOffset = buffer.getIntLE(offset + 38);
         if (idOffset < 0) {
            return ValidationResult.error("Invalid offset for Id");
         } else {
            int pos = offset + 62 + idOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Id");
            } else {
               int idLen = VarInt.peek(buffer, pos);
               if (idLen < 0) {
                  return ValidationResult.error("Invalid string length for Id");
               } else if (idLen > 4096000) {
                  return ValidationResult.error("Id exceeds max length 4096000");
               } else {
                  pos += VarInt.length(buffer, pos);
                  pos += idLen;
                  if (pos > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading Id");
                  } else {
                     if ((nullBits & 1) != 0) {
                        idOffset = buffer.getIntLE(offset + 42);
                        if (idOffset < 0) {
                           return ValidationResult.error("Invalid offset for Name");
                        }

                        pos = offset + 62 + idOffset;
                        if (pos >= buffer.writerIndex()) {
                           return ValidationResult.error("Offset out of bounds for Name");
                        }

                        ValidationResult nameResult = FormattedMessage.validateStructure(buffer, pos);
                        if (!nameResult.isValid()) {
                           return ValidationResult.error("Invalid Name: " + nameResult.error());
                        }

                        pos += FormattedMessage.computeBytesConsumed(buffer, pos);
                     }

                     if ((nullBits & 2) != 0) {
                        idOffset = buffer.getIntLE(offset + 46);
                        if (idOffset < 0) {
                           return ValidationResult.error("Invalid offset for CustomName");
                        }

                        pos = offset + 62 + idOffset;
                        if (pos >= buffer.writerIndex()) {
                           return ValidationResult.error("Offset out of bounds for CustomName");
                        }

                        idLen = VarInt.peek(buffer, pos);
                        if (idLen < 0) {
                           return ValidationResult.error("Invalid string length for CustomName");
                        }

                        if (idLen > 4096000) {
                           return ValidationResult.error("CustomName exceeds max length 4096000");
                        }

                        pos += VarInt.length(buffer, pos);
                        pos += idLen;
                        if (pos > buffer.writerIndex()) {
                           return ValidationResult.error("Buffer overflow reading CustomName");
                        }
                     }

                     idOffset = buffer.getIntLE(offset + 50);
                     if (idOffset < 0) {
                        return ValidationResult.error("Invalid offset for MarkerImage");
                     } else {
                        pos = offset + 62 + idOffset;
                        if (pos >= buffer.writerIndex()) {
                           return ValidationResult.error("Offset out of bounds for MarkerImage");
                        } else {
                           idLen = VarInt.peek(buffer, pos);
                           if (idLen < 0) {
                              return ValidationResult.error("Invalid string length for MarkerImage");
                           } else if (idLen > 4096000) {
                              return ValidationResult.error("MarkerImage exceeds max length 4096000");
                           } else {
                              pos += VarInt.length(buffer, pos);
                              pos += idLen;
                              if (pos > buffer.writerIndex()) {
                                 return ValidationResult.error("Buffer overflow reading MarkerImage");
                              } else {
                                 if ((nullBits & 4) != 0) {
                                    idOffset = buffer.getIntLE(offset + 54);
                                    if (idOffset < 0) {
                                       return ValidationResult.error("Invalid offset for ContextMenuItems");
                                    }

                                    pos = offset + 62 + idOffset;
                                    if (pos >= buffer.writerIndex()) {
                                       return ValidationResult.error("Offset out of bounds for ContextMenuItems");
                                    }

                                    idLen = VarInt.peek(buffer, pos);
                                    if (idLen < 0) {
                                       return ValidationResult.error("Invalid array count for ContextMenuItems");
                                    }

                                    if (idLen > 4096000) {
                                       return ValidationResult.error("ContextMenuItems exceeds max length 4096000");
                                    }

                                    pos += VarInt.length(buffer, pos);

                                    for (int i = 0; i < idLen; i++) {
                                       ValidationResult structResult = ContextMenuItem.validateStructure(buffer, pos);
                                       if (!structResult.isValid()) {
                                          return ValidationResult.error("Invalid ContextMenuItem in ContextMenuItems[" + i + "]: " + structResult.error());
                                       }

                                       pos += ContextMenuItem.computeBytesConsumed(buffer, pos);
                                    }
                                 }

                                 if ((nullBits & 8) != 0) {
                                    idOffset = buffer.getIntLE(offset + 58);
                                    if (idOffset < 0) {
                                       return ValidationResult.error("Invalid offset for Components");
                                    }

                                    pos = offset + 62 + idOffset;
                                    if (pos >= buffer.writerIndex()) {
                                       return ValidationResult.error("Offset out of bounds for Components");
                                    }

                                    idLen = VarInt.peek(buffer, pos);
                                    if (idLen < 0) {
                                       return ValidationResult.error("Invalid array count for Components");
                                    }

                                    if (idLen > 4096000) {
                                       return ValidationResult.error("Components exceeds max length 4096000");
                                    }

                                    pos += VarInt.length(buffer, pos);

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
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public MapMarker clone() {
      MapMarker copy = new MapMarker();
      copy.id = this.id;
      copy.name = this.name != null ? this.name.clone() : null;
      copy.customName = this.customName;
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
               && Objects.equals(this.customName, other.customName)
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
      result = 31 * result + Objects.hashCode(this.customName);
      result = 31 * result + Objects.hashCode(this.markerImage);
      result = 31 * result + Objects.hashCode(this.transform);
      result = 31 * result + Arrays.hashCode((Object[])this.contextMenuItems);
      return 31 * result + Arrays.hashCode((Object[])this.components);
   }
}
