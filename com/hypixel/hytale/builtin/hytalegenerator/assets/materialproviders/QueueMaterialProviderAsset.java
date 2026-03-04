package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.QueueMaterialProvider;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class QueueMaterialProviderAsset extends MaterialProviderAsset {
   @Nonnull
   public static final BuilderCodec<QueueMaterialProviderAsset> CODEC = BuilderCodec.builder(
         QueueMaterialProviderAsset.class, QueueMaterialProviderAsset::new, MaterialProviderAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Queue", new ArrayCodec<>(MaterialProviderAsset.CODEC, MaterialProviderAsset[]::new), true), (t, k) -> t.queue = k, k -> k.queue)
      .add()
      .build();
   private MaterialProviderAsset[] queue = new MaterialProviderAsset[0];

   @Nonnull
   @Override
   public MaterialProvider<Material> build(@Nonnull MaterialProviderAsset.Argument argument) {
      if (super.skip()) {
         return MaterialProvider.noMaterialProvider();
      } else {
         ArrayList<MaterialProvider<Material>> queueList = new ArrayList<>(this.queue.length);

         for (MaterialProviderAsset m : this.queue) {
            if (m == null) {
               LoggerUtil.getLogger().warning("Null element in queue provided.");
            } else {
               queueList.add(m.build(argument));
            }
         }

         return new QueueMaterialProvider<>(queueList);
      }
   }

   @Override
   public void cleanUp() {
      for (MaterialProviderAsset materialProviderAsset : this.queue) {
         materialProviderAsset.cleanUp();
      }
   }
}
