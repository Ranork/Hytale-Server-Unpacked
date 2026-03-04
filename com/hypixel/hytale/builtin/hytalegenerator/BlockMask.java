package com.hypixel.hytale.builtin.hytalegenerator;

import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class BlockMask {
   private MaterialSet skippedBlocks = new MaterialSet();
   private MaterialSet defaultMask = new MaterialSet();
   @Nonnull
   private final List<MaterialSet> sourceBlocks = new ArrayList<>(0);
   @Nonnull
   private final List<MaterialSet> destinationBlocks = new ArrayList<>(0);

   public boolean canPlace(@Nonnull Material material) {
      return !this.skippedBlocks.test(material);
   }

   public boolean canPlace(int materialHash) {
      return !this.skippedBlocks.test(materialHash);
   }

   public boolean canReplace(@Nonnull Material source, @Nonnull Material destination) {
      return this.canReplace(source.hashMaterialIds(), destination.hashMaterialIds());
   }

   public boolean canReplace(int sourceHash, int destinationHash) {
      for (int i = 0; i < this.sourceBlocks.size(); i++) {
         if (this.sourceBlocks.get(i).test(sourceHash)) {
            return this.destinationBlocks.get(i).test(destinationHash);
         }
      }

      return !this.defaultMask.test(destinationHash);
   }

   public void setSkippedBlocks(@Nonnull MaterialSet materialSet) {
      this.skippedBlocks = materialSet;
   }

   public void putBlockMaskEntry(@Nonnull MaterialSet source, @Nonnull MaterialSet destination) {
      this.sourceBlocks.add(source);
      this.destinationBlocks.add(destination);
   }

   public void setDefaultMask(@Nonnull MaterialSet materialSet) {
      this.defaultMask = materialSet;
   }
}
