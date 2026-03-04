package com.hypixel.hytale.server.core.asset.type.gameplay;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.server.core.asset.type.camera.CameraEffect;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class CameraEffectsConfig {
   @Nonnull
   public static final BuilderCodec<CameraEffectsConfig> CODEC = BuilderCodec.builder(CameraEffectsConfig.class, CameraEffectsConfig::new)
      .appendInherited(
         new KeyedCodec<>("DamageEffects", new MapCodec<>(CameraEffect.CHILD_ASSET_CODEC, HashMap::new)),
         (config, damageEffectIds) -> config.damageEffectIds = damageEffectIds,
         config -> config.damageEffectIds,
         (config, parent) -> config.damageEffectIds = parent.damageEffectIds
      )
      .addValidator(DamageCause.VALIDATOR_CACHE.getMapKeyValidator())
      .addValidator(CameraEffect.VALIDATOR_CACHE.getMapValueValidator())
      .documentation("The default damage camera effects")
      .add()
      .afterDecode(config -> {
         if (config.damageEffectIds != null) {
            config.damageEffectIndices = new Int2IntOpenHashMap();

            for (Entry<String, String> entry : config.damageEffectIds.entrySet()) {
               int key = DamageCause.getAssetMap().getIndex(entry.getKey());
               int effectIndex = CameraEffect.getAssetMap().getIndex(entry.getValue());
               config.damageEffectIndices.put(key, effectIndex);
            }
         }
      })
      .build();
   protected Map<String, String> damageEffectIds;
   @Nonnull
   protected transient Int2IntMap damageEffectIndices = Int2IntMaps.EMPTY_MAP;

   public int getCameraEffectIndex(int damageCauseIndex) {
      return this.damageEffectIndices.getOrDefault(damageCauseIndex, Integer.MIN_VALUE);
   }
}
