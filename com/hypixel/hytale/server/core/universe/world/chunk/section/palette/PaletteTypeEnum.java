package com.hypixel.hytale.server.core.universe.world.chunk.section.palette;

import com.hypixel.hytale.protocol.packets.world.PaletteType;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public enum PaletteTypeEnum {
   EMPTY(PaletteType.Empty, () -> EmptySectionPalette.INSTANCE),
   HALF_BYTE(PaletteType.HalfByte, HalfByteSectionPalette::new),
   BYTE(PaletteType.Byte, ByteSectionPalette::new),
   SHORT(PaletteType.Short, ShortSectionPalette::new);

   private static final PaletteTypeEnum[] values = values();
   @Nonnull
   private final PaletteType paletteType;
   private final Supplier<? extends ISectionPalette> constructor;
   private final byte paletteId;

   public static PaletteTypeEnum get(byte paletteId) {
      return values[paletteId];
   }

   private <T extends ISectionPalette> PaletteTypeEnum(@Nonnull PaletteType paletteType, Supplier<T> constructor) {
      this.paletteType = paletteType;
      this.constructor = constructor;
      this.paletteId = (byte)paletteType.ordinal();
   }

   @Nonnull
   public PaletteType getPaletteType() {
      return this.paletteType;
   }

   public Supplier<? extends ISectionPalette> getConstructor() {
      return this.constructor;
   }

   public byte getPaletteId() {
      return this.paletteId;
   }
}
