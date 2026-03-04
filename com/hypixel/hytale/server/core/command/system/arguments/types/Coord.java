package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nonnull;

public class Coord {
   private final double value;
   private final boolean height;
   private final boolean relative;
   private final boolean chunk;

   public Coord(double value, boolean height, boolean relative, boolean chunk) {
      this.value = value;
      this.height = height;
      this.relative = relative;
      this.chunk = chunk;
   }

   public double getValue() {
      return this.value;
   }

   public boolean isNotBase() {
      return !this.height && !this.relative && !this.chunk;
   }

   public boolean isHeight() {
      return this.height;
   }

   public boolean isRelative() {
      return this.relative;
   }

   public boolean isChunk() {
      return this.chunk;
   }

   public double resolveXZ(double base) {
      return this.resolve(base);
   }

   public double resolveYAtWorldCoords(double base, @Nonnull World world, double x, double z) throws GeneralCommandException {
      if (this.height) {
         WorldChunk worldCoords = world.getNonTickingChunk(ChunkUtil.indexChunkFromBlock(x, z));
         if (worldCoords == null) {
            throw new GeneralCommandException(Message.raw("Failed to load chunk at (" + x + ", " + z + ")"));
         } else {
            return worldCoords.getHeight(MathUtil.floor(x), MathUtil.floor(z)) + 1 + this.resolve(0.0);
         }
      } else {
         return this.resolve(base);
      }
   }

   protected double resolve(double base) {
      double val = this.chunk ? this.value * 32.0 : this.value;
      return this.relative ? val + base : val;
   }

   @Nonnull
   public static Coord parse(@Nonnull String str) {
      boolean height = false;
      boolean relative = false;
      boolean chunk = false;
      int index = 0;

      label20:
      while (true) {
         switch (str.charAt(index)) {
            case '_':
               index++;
               height = true;
               if (str.length() == index) {
                  return new Coord(0.0, true, relative, chunk);
               }
               break;
            case 'c':
               index++;
               chunk = true;
               if (str.length() == index) {
                  return new Coord(0.0, height, relative, true);
               }
               break label20;
            case '~':
               index++;
               relative = true;
               if (str.length() == index) {
                  return new Coord(0.0, height, true, chunk);
               }
               break;
            default:
               break label20;
         }
      }

      String rest = str.substring(index);
      return new Coord(Double.parseDouble(rest), height, relative, chunk);
   }
}
