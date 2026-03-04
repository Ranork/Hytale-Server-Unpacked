package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.InstantData;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorSetGameTime implements Packet, ToServerPacket {
   public static final int PACKET_ID = 352;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 14;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 14;
   public static final int MAX_SIZE = 14;
   @Nullable
   public InstantData gameTime;
   public boolean paused;

   @Override
   public int getId() {
      return 352;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorSetGameTime() {
   }

   public AssetEditorSetGameTime(@Nullable InstantData gameTime, boolean paused) {
      this.gameTime = gameTime;
      this.paused = paused;
   }

   public AssetEditorSetGameTime(@Nonnull AssetEditorSetGameTime other) {
      this.gameTime = other.gameTime;
      this.paused = other.paused;
   }

   @Nonnull
   public static AssetEditorSetGameTime deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorSetGameTime obj = new AssetEditorSetGameTime();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.gameTime = InstantData.deserialize(buf, offset + 1);
      }

      obj.paused = buf.getByte(offset + 13) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 14;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.gameTime != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.gameTime != null) {
         this.gameTime.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeByte(this.paused ? 1 : 0);
   }

   @Override
   public int computeSize() {
      return 14;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 14 ? ValidationResult.error("Buffer too small: expected at least 14 bytes") : ValidationResult.OK;
   }

   public AssetEditorSetGameTime clone() {
      AssetEditorSetGameTime copy = new AssetEditorSetGameTime();
      copy.gameTime = this.gameTime != null ? this.gameTime.clone() : null;
      copy.paused = this.paused;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorSetGameTime other) ? false : Objects.equals(this.gameTime, other.gameTime) && this.paused == other.paused;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.gameTime, this.paused);
   }
}
