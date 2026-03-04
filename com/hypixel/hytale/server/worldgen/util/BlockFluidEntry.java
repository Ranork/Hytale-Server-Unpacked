package com.hypixel.hytale.server.worldgen.util;

@Deprecated
public record BlockFluidEntry(int blockId, int rotation, int fluidId) {
   public static final BlockFluidEntry[] EMPTY_ARRAY = new BlockFluidEntry[0];
   public static final BlockFluidEntry EMPTY = new BlockFluidEntry(0, 0, 0);
}
