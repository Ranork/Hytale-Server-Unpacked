package com.hypixel.hytale.server.core.universe.world.worldmap.provider.chunk;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.ShaderType;
import com.hypixel.hytale.protocol.packets.worldmap.MapImage;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class ImageBuilder {
   private final long index;
   private final World world;
   @Nonnull
   private final MapImage image;
   private final int sampleWidth;
   private final int sampleHeight;
   private final int blockStepX;
   private final int blockStepZ;
   @Nonnull
   private final short[] heightSamples;
   @Nonnull
   private final int[] tintSamples;
   @Nonnull
   private final int[] blockSamples;
   @Nonnull
   private final short[] neighborHeightSamples;
   @Nonnull
   private final short[] fluidDepthSamples;
   @Nonnull
   private final int[] environmentSamples;
   @Nonnull
   private final int[] fluidSamples;
   private final ImageBuilder.Color outColor = new ImageBuilder.Color();
   @Nullable
   private WorldChunk worldChunk;
   private FluidSection[] fluidSections;

   public ImageBuilder(long index, int imageWidth, int imageHeight, World world) {
      this.index = index;
      this.world = world;
      this.image = new MapImage(imageWidth, imageHeight, new int[imageWidth * imageHeight]);
      this.sampleWidth = Math.min(32, this.image.width);
      this.sampleHeight = Math.min(32, this.image.height);
      this.blockStepX = Math.max(1, 32 / this.image.width);
      this.blockStepZ = Math.max(1, 32 / this.image.height);
      this.heightSamples = new short[this.sampleWidth * this.sampleHeight];
      this.tintSamples = new int[this.sampleWidth * this.sampleHeight];
      this.blockSamples = new int[this.sampleWidth * this.sampleHeight];
      this.neighborHeightSamples = new short[(this.sampleWidth + 2) * (this.sampleHeight + 2)];
      this.fluidDepthSamples = new short[this.sampleWidth * this.sampleHeight];
      this.environmentSamples = new int[this.sampleWidth * this.sampleHeight];
      this.fluidSamples = new int[this.sampleWidth * this.sampleHeight];
   }

   public long getIndex() {
      return this.index;
   }

   @Nonnull
   public MapImage getImage() {
      return this.image;
   }

   @Nonnull
   private CompletableFuture<ImageBuilder> fetchChunk() {
      return this.world.getChunkStore().getChunkReferenceAsync(this.index).thenApplyAsync(ref -> {
         if (ref != null && ref.isValid()) {
            this.worldChunk = ref.getStore().getComponent((Ref<ChunkStore>)ref, WorldChunk.getComponentType());
            ChunkColumn chunkColumn = ref.getStore().getComponent((Ref<ChunkStore>)ref, ChunkColumn.getComponentType());
            this.fluidSections = new FluidSection[10];

            for (int y = 0; y < 10; y++) {
               Ref<ChunkStore> sectionRef = chunkColumn.getSection(y);
               this.fluidSections[y] = this.world.getChunkStore().getStore().getComponent(sectionRef, FluidSection.getComponentType());
            }

            return this;
         } else {
            return null;
         }
      }, this.world);
   }

   @Nonnull
   private CompletableFuture<ImageBuilder> sampleNeighborsSync() {
      CompletableFuture<Void> north = this.world
         .getChunkStore()
         .getChunkReferenceAsync(ChunkUtil.indexChunk(this.worldChunk.getX(), this.worldChunk.getZ() - 1))
         .thenAcceptAsync(ref -> {
            if (ref != null && ref.isValid()) {
               WorldChunk worldChunk = ref.getStore().getComponent((Ref<ChunkStore>)ref, WorldChunk.getComponentType());
               int z = (this.sampleHeight - 1) * this.blockStepZ;

               for (int ix = 0; ix < this.sampleWidth; ix++) {
                  int x = ix * this.blockStepX;
                  this.neighborHeightSamples[1 + ix] = worldChunk.getHeight(x, z);
               }
            }
         }, this.world);
      CompletableFuture<Void> south = this.world
         .getChunkStore()
         .getChunkReferenceAsync(ChunkUtil.indexChunk(this.worldChunk.getX(), this.worldChunk.getZ() + 1))
         .thenAcceptAsync(ref -> {
            if (ref != null && ref.isValid()) {
               WorldChunk worldChunk = ref.getStore().getComponent((Ref<ChunkStore>)ref, WorldChunk.getComponentType());
               int z = 0;
               int neighbourStartIndex = (this.sampleHeight + 1) * (this.sampleWidth + 2) + 1;

               for (int ix = 0; ix < this.sampleWidth; ix++) {
                  int x = ix * this.blockStepX;
                  this.neighborHeightSamples[neighbourStartIndex + ix] = worldChunk.getHeight(x, z);
               }
            }
         }, this.world);
      CompletableFuture<Void> west = this.world
         .getChunkStore()
         .getChunkReferenceAsync(ChunkUtil.indexChunk(this.worldChunk.getX() - 1, this.worldChunk.getZ()))
         .thenAcceptAsync(ref -> {
            if (ref != null && ref.isValid()) {
               WorldChunk worldChunk = ref.getStore().getComponent((Ref<ChunkStore>)ref, WorldChunk.getComponentType());
               int x = (this.sampleWidth - 1) * this.blockStepX;

               for (int iz = 0; iz < this.sampleHeight; iz++) {
                  int z = iz * this.blockStepZ;
                  this.neighborHeightSamples[(iz + 1) * (this.sampleWidth + 2)] = worldChunk.getHeight(x, z);
               }
            }
         }, this.world);
      CompletableFuture<Void> east = this.world
         .getChunkStore()
         .getChunkReferenceAsync(ChunkUtil.indexChunk(this.worldChunk.getX() + 1, this.worldChunk.getZ()))
         .thenAcceptAsync(ref -> {
            if (ref != null && ref.isValid()) {
               WorldChunk worldChunk = ref.getStore().getComponent((Ref<ChunkStore>)ref, WorldChunk.getComponentType());
               int x = 0;

               for (int iz = 0; iz < this.sampleHeight; iz++) {
                  int z = iz * this.blockStepZ;
                  this.neighborHeightSamples[(iz + 1) * (this.sampleWidth + 2) + this.sampleWidth + 1] = worldChunk.getHeight(x, z);
               }
            }
         }, this.world);
      CompletableFuture<Void> northeast = this.world
         .getChunkStore()
         .getChunkReferenceAsync(ChunkUtil.indexChunk(this.worldChunk.getX() + 1, this.worldChunk.getZ() - 1))
         .thenAcceptAsync(ref -> {
            if (ref != null && ref.isValid()) {
               WorldChunk worldChunk = ref.getStore().getComponent((Ref<ChunkStore>)ref, WorldChunk.getComponentType());
               int x = 0;
               int z = (this.sampleHeight - 1) * this.blockStepZ;
               this.neighborHeightSamples[0] = worldChunk.getHeight(x, z);
            }
         }, this.world);
      CompletableFuture<Void> northwest = this.world
         .getChunkStore()
         .getChunkReferenceAsync(ChunkUtil.indexChunk(this.worldChunk.getX() - 1, this.worldChunk.getZ() - 1))
         .thenAcceptAsync(ref -> {
            if (ref != null && ref.isValid()) {
               WorldChunk worldChunk = ref.getStore().getComponent((Ref<ChunkStore>)ref, WorldChunk.getComponentType());
               int x = (this.sampleWidth - 1) * this.blockStepX;
               int z = (this.sampleHeight - 1) * this.blockStepZ;
               this.neighborHeightSamples[this.sampleWidth + 1] = worldChunk.getHeight(x, z);
            }
         }, this.world);
      CompletableFuture<Void> southeast = this.world
         .getChunkStore()
         .getChunkReferenceAsync(ChunkUtil.indexChunk(this.worldChunk.getX() + 1, this.worldChunk.getZ() + 1))
         .thenAcceptAsync(ref -> {
            if (ref != null && ref.isValid()) {
               WorldChunk worldChunk = ref.getStore().getComponent((Ref<ChunkStore>)ref, WorldChunk.getComponentType());
               int x = 0;
               int z = 0;
               this.neighborHeightSamples[(this.sampleHeight + 1) * (this.sampleWidth + 2) + this.sampleWidth + 1] = worldChunk.getHeight(x, z);
            }
         }, this.world);
      CompletableFuture<Void> southwest = this.world
         .getChunkStore()
         .getChunkReferenceAsync(ChunkUtil.indexChunk(this.worldChunk.getX() - 1, this.worldChunk.getZ() + 1))
         .thenAcceptAsync(ref -> {
            if (ref != null && ref.isValid()) {
               WorldChunk worldChunk = ref.getStore().getComponent((Ref<ChunkStore>)ref, WorldChunk.getComponentType());
               int x = (this.sampleWidth - 1) * this.blockStepX;
               int z = 0;
               this.neighborHeightSamples[(this.sampleHeight + 1) * (this.sampleWidth + 2)] = worldChunk.getHeight(x, z);
            }
         }, this.world);
      return CompletableFuture.allOf(north, south, west, east, northeast, northwest, southeast, southwest).thenApply(v -> this);
   }

   @Nonnull
   private ImageBuilder generateImageAsync() {
      for (int ix = 0; ix < this.sampleWidth; ix++) {
         for (int iz = 0; iz < this.sampleHeight; iz++) {
            int sampleIndex = iz * this.sampleWidth + ix;
            int x = ix * this.blockStepX;
            int z = iz * this.blockStepZ;
            short height = this.worldChunk.getHeight(x, z);
            int tint = this.worldChunk.getTint(x, z);
            this.heightSamples[sampleIndex] = height;
            this.tintSamples[sampleIndex] = tint;
            int blockId = this.worldChunk.getBlock(x, height, z);
            this.blockSamples[sampleIndex] = blockId;
            int fluidId = 0;
            int fluidTop = 320;
            Fluid fluid = null;
            int chunkYGround = ChunkUtil.chunkCoordinate((int)height);
            int chunkY = 9;

            label107:
            while (chunkY >= 0 && chunkY >= chunkYGround) {
               FluidSection fluidSection = this.fluidSections[chunkY];
               if (fluidSection != null && !fluidSection.isEmpty()) {
                  int minBlockY = Math.max(ChunkUtil.minBlock(chunkY), height);
                  int maxBlockY = ChunkUtil.maxBlock(chunkY);

                  for (int blockY = maxBlockY; blockY >= minBlockY; blockY--) {
                     fluidId = fluidSection.getFluidId(x, blockY, z);
                     if (fluidId != 0) {
                        fluid = Fluid.getAssetMap().getAsset(fluidId);
                        fluidTop = blockY;
                        break label107;
                     }
                  }

                  chunkY--;
               } else {
                  chunkY--;
               }
            }

            int fluidBottom;
            label128:
            for (fluidBottom = height; chunkY >= 0 && chunkY >= chunkYGround; chunkY--) {
               FluidSection fluidSection = this.fluidSections[chunkY];
               if (fluidSection == null || fluidSection.isEmpty()) {
                  fluidBottom = Math.min(ChunkUtil.maxBlock(chunkY) + 1, fluidTop);
                  break;
               }

               int minBlockY = Math.max(ChunkUtil.minBlock(chunkY), height);
               int maxBlockY = Math.min(ChunkUtil.maxBlock(chunkY), fluidTop - 1);

               for (int blockYx = maxBlockY; blockYx >= minBlockY; blockYx--) {
                  int nextFluidId = fluidSection.getFluidId(x, blockYx, z);
                  if (nextFluidId != fluidId) {
                     Fluid nextFluid = Fluid.getAssetMap().getAsset(nextFluidId);
                     if (!Objects.equals(fluid.getParticleColor(), nextFluid.getParticleColor())) {
                        fluidBottom = blockYx + 1;
                        break label128;
                     }
                  }
               }
            }

            short fluidDepth = fluidId != 0 ? (short)(fluidTop - fluidBottom + 1) : 0;
            int environmentId = this.worldChunk.getBlockChunk().getEnvironment(x, fluidTop, z);
            this.fluidDepthSamples[sampleIndex] = fluidDepth;
            this.environmentSamples[sampleIndex] = environmentId;
            this.fluidSamples[sampleIndex] = fluidId;
         }
      }

      float imageToSampleRatioWidth = (float)this.sampleWidth / this.image.width;
      float imageToSampleRatioHeight = (float)this.sampleHeight / this.image.height;
      int blockPixelWidth = Math.max(1, this.image.width / this.sampleWidth);
      int blockPixelHeight = Math.max(1, this.image.height / this.sampleHeight);

      for (int iz = 0; iz < this.sampleHeight; iz++) {
         System.arraycopy(this.heightSamples, iz * this.sampleWidth, this.neighborHeightSamples, (iz + 1) * (this.sampleWidth + 2) + 1, this.sampleWidth);
      }

      for (int ix = 0; ix < this.image.width; ix++) {
         for (int iz = 0; iz < this.image.height; iz++) {
            int sampleX = Math.min((int)(ix * imageToSampleRatioWidth), this.sampleWidth - 1);
            int sampleZ = Math.min((int)(iz * imageToSampleRatioHeight), this.sampleHeight - 1);
            int sampleIndex = sampleZ * this.sampleWidth + sampleX;
            int blockPixelX = ix % blockPixelWidth;
            int blockPixelZ = iz % blockPixelHeight;
            short height = this.heightSamples[sampleIndex];
            int tint = this.tintSamples[sampleIndex];
            int blockId = this.blockSamples[sampleIndex];
            if (height >= 0 && blockId != 0) {
               getBlockColor(blockId, tint, this.outColor);
               short north = this.neighborHeightSamples[sampleZ * (this.sampleWidth + 2) + sampleX + 1];
               short south = this.neighborHeightSamples[(sampleZ + 2) * (this.sampleWidth + 2) + sampleX + 1];
               short west = this.neighborHeightSamples[(sampleZ + 1) * (this.sampleWidth + 2) + sampleX];
               short east = this.neighborHeightSamples[(sampleZ + 1) * (this.sampleWidth + 2) + sampleX + 2];
               short northWest = this.neighborHeightSamples[sampleZ * (this.sampleWidth + 2) + sampleX];
               short northEast = this.neighborHeightSamples[sampleZ * (this.sampleWidth + 2) + sampleX + 2];
               short southWest = this.neighborHeightSamples[(sampleZ + 2) * (this.sampleWidth + 2) + sampleX];
               short southEast = this.neighborHeightSamples[(sampleZ + 2) * (this.sampleWidth + 2) + sampleX + 2];
               float shade = shadeFromHeights(
                  blockPixelX, blockPixelZ, blockPixelWidth, blockPixelHeight, height, north, south, west, east, northWest, northEast, southWest, southEast
               );
               this.outColor.multiply(shade);
               if (height < 320) {
                  int fluidId = this.fluidSamples[sampleIndex];
                  if (fluidId != 0) {
                     short fluidDepth = this.fluidDepthSamples[sampleIndex];
                     int environmentId = this.environmentSamples[sampleIndex];
                     getFluidColor(fluidId, environmentId, fluidDepth, this.outColor);
                  }
               }

               this.packImageData(ix, iz);
            } else {
               this.outColor.r = this.outColor.g = this.outColor.b = this.outColor.a = 0;
               this.packImageData(ix, iz);
            }
         }
      }

      return this;
   }

   private void packImageData(int ix, int iz) {
      this.image.data[iz * this.image.width + ix] = this.outColor.pack();
   }

   private static float shadeFromHeights(
      int blockPixelX,
      int blockPixelZ,
      int blockPixelWidth,
      int blockPixelHeight,
      short height,
      short north,
      short south,
      short west,
      short east,
      short northWest,
      short northEast,
      short southWest,
      short southEast
   ) {
      float u = (blockPixelX + 0.5F) / blockPixelWidth;
      float v = (blockPixelZ + 0.5F) / blockPixelHeight;
      float ud = (u + v) / 2.0F;
      float vd = (1.0F - u + v) / 2.0F;
      float dhdx1 = (height - west) * (1.0F - u) + (east - height) * u;
      float dhdz1 = (height - north) * (1.0F - v) + (south - height) * v;
      float dhdx2 = (height - northWest) * (1.0F - ud) + (southEast - height) * ud;
      float dhdz2 = (height - northEast) * (1.0F - vd) + (southWest - height) * vd;
      float dhdx = dhdx1 * 2.0F + dhdx2;
      float dhdz = dhdz1 * 2.0F + dhdz2;
      float dy = 3.0F;
      float invS = 1.0F / (float)Math.sqrt(dhdx * dhdx + dy * dy + dhdz * dhdz);
      float nx = dhdx * invS;
      float ny = dy * invS;
      float nz = dhdz * invS;
      float lx = -0.2F;
      float ly = 0.8F;
      float lz = 0.5F;
      float invL = 1.0F / (float)Math.sqrt(lx * lx + ly * ly + lz * lz);
      lx *= invL;
      ly *= invL;
      lz *= invL;
      float lambert = Math.max(0.0F, nx * lx + ny * ly + nz * lz);
      float ambient = 0.4F;
      float diffuse = 0.6F;
      return ambient + diffuse * lambert;
   }

   private static void getBlockColor(int blockId, int biomeTintColor, @Nonnull ImageBuilder.Color outColor) {
      BlockType block = BlockType.getAssetMap().getAsset(blockId);
      int biomeTintR = biomeTintColor >> 16 & 0xFF;
      int biomeTintG = biomeTintColor >> 8 & 0xFF;
      int biomeTintB = biomeTintColor >> 0 & 0xFF;
      com.hypixel.hytale.protocol.Color[] tintUp = block.getTintUp();
      boolean hasTint = tintUp != null && tintUp.length > 0;
      int selfTintR = hasTint ? tintUp[0].red & 255 : 255;
      int selfTintG = hasTint ? tintUp[0].green & 255 : 255;
      int selfTintB = hasTint ? tintUp[0].blue & 255 : 255;
      float biomeTintMultiplier = block.getBiomeTintUp() / 100.0F;
      int tintColorR = (int)(selfTintR + (biomeTintR - selfTintR) * biomeTintMultiplier);
      int tintColorG = (int)(selfTintG + (biomeTintG - selfTintG) * biomeTintMultiplier);
      int tintColorB = (int)(selfTintB + (biomeTintB - selfTintB) * biomeTintMultiplier);
      com.hypixel.hytale.protocol.Color particleColor = block.getParticleColor();
      if (particleColor != null && biomeTintMultiplier < 1.0F) {
         tintColorR = tintColorR * (particleColor.red & 255) / 255;
         tintColorG = tintColorG * (particleColor.green & 255) / 255;
         tintColorB = tintColorB * (particleColor.blue & 255) / 255;
      }

      outColor.r = tintColorR & 0xFF;
      outColor.g = tintColorG & 0xFF;
      outColor.b = tintColorB & 0xFF;
      outColor.a = 255;
   }

   private static void getFluidColor(int fluidId, int environmentId, int fluidDepth, @Nonnull ImageBuilder.Color outColor) {
      int tintColorR = 255;
      int tintColorG = 255;
      int tintColorB = 255;
      Fluid fluid = Fluid.getAssetMap().getAsset(fluidId);
      if (fluid != null) {
         if (fluid.hasEffect(ShaderType.Water)) {
            Environment environment = Environment.getAssetMap().getAsset(environmentId);
            com.hypixel.hytale.protocol.Color waterTint = environment.getWaterTint();
            if (waterTint != null) {
               tintColorR = tintColorR * (waterTint.red & 255) / 255;
               tintColorG = tintColorG * (waterTint.green & 255) / 255;
               tintColorB = tintColorB * (waterTint.blue & 255) / 255;
            }
         }

         com.hypixel.hytale.protocol.Color partcileColor = fluid.getParticleColor();
         if (partcileColor != null) {
            tintColorR = tintColorR * (partcileColor.red & 255) / 255;
            tintColorG = tintColorG * (partcileColor.green & 255) / 255;
            tintColorB = tintColorB * (partcileColor.blue & 255) / 255;
         }

         float depthMultiplier = Math.min(1.0F, 1.0F / fluidDepth);
         outColor.r = (int)(tintColorR + ((outColor.r & 0xFF) - tintColorR) * depthMultiplier) & 0xFF;
         outColor.g = (int)(tintColorG + ((outColor.g & 0xFF) - tintColorG) * depthMultiplier) & 0xFF;
         outColor.b = (int)(tintColorB + ((outColor.b & 0xFF) - tintColorB) * depthMultiplier) & 0xFF;
      }
   }

   @Nonnull
   public static CompletableFuture<ImageBuilder> build(long index, int imageWidth, int imageHeight, World world) {
      return CompletableFuture.completedFuture(new ImageBuilder(index, imageWidth, imageHeight, world))
         .thenCompose(ImageBuilder::fetchChunk)
         .thenCompose(builder -> builder != null ? builder.sampleNeighborsSync() : CompletableFuture.completedFuture(null))
         .thenApplyAsync(builder -> builder != null ? builder.generateImageAsync() : null);
   }

   private static class Color {
      public int r;
      public int g;
      public int b;
      public int a;

      public int pack() {
         return (this.r & 0xFF) << 24 | (this.g & 0xFF) << 16 | (this.b & 0xFF) << 8 | this.a & 0xFF;
      }

      public void multiply(float value) {
         this.r = Math.min(255, Math.max(0, (int)(this.r * value)));
         this.g = Math.min(255, Math.max(0, (int)(this.g * value)));
         this.b = Math.min(255, Math.max(0, (int)(this.b * value)));
      }
   }
}
