package com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.directionality;

import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.ConstantPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.PatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.Directionality;
import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.OrthogonalDirection;
import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.PatternDirectionality;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class PatternDirectionalityAsset extends DirectionalityAsset {
   @Nonnull
   public static final BuilderCodec<PatternDirectionalityAsset> CODEC = BuilderCodec.builder(
         PatternDirectionalityAsset.class, PatternDirectionalityAsset::new, DirectionalityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("InitialDirection", OrthogonalDirection.CODEC, true), (asset, v) -> asset.prefabDirection = v, asset -> asset.prefabDirection)
      .add()
      .append(new KeyedCodec<>("Seed", Codec.STRING, true), (asset, v) -> asset.seed = v, asset -> asset.seed)
      .add()
      .append(new KeyedCodec<>("NorthPattern", PatternAsset.CODEC, true), (asset, v) -> asset.northPatternAsset = v, asset -> asset.northPatternAsset)
      .add()
      .append(new KeyedCodec<>("SouthPattern", PatternAsset.CODEC, true), (asset, v) -> asset.southPatternAsset = v, asset -> asset.southPatternAsset)
      .add()
      .append(new KeyedCodec<>("EastPattern", PatternAsset.CODEC, true), (asset, v) -> asset.eastPatternAsset = v, asset -> asset.eastPatternAsset)
      .add()
      .append(new KeyedCodec<>("WestPattern", PatternAsset.CODEC, true), (asset, v) -> asset.westPatternAsset = v, asset -> asset.westPatternAsset)
      .add()
      .build();
   private String seed = "A";
   private OrthogonalDirection prefabDirection = OrthogonalDirection.N;
   private PatternAsset northPatternAsset = new ConstantPatternAsset();
   private PatternAsset southPatternAsset = new ConstantPatternAsset();
   private PatternAsset eastPatternAsset = new ConstantPatternAsset();
   private PatternAsset westPatternAsset = new ConstantPatternAsset();

   @Nonnull
   @Override
   public Directionality build(@Nonnull DirectionalityAsset.Argument argument) {
      int intSeed = argument.parentSeed.child(this.seed).createSupplier().get();
      OrthogonalDirection direction = this.prefabDirection;
      Pattern northPattern = this.northPatternAsset == null ? Pattern.noPattern() : this.northPatternAsset.build(PatternAsset.argumentFrom(argument));
      Pattern southPattern = this.southPatternAsset == null ? Pattern.noPattern() : this.southPatternAsset.build(PatternAsset.argumentFrom(argument));
      Pattern eastPattern = this.eastPatternAsset == null ? Pattern.noPattern() : this.eastPatternAsset.build(PatternAsset.argumentFrom(argument));
      Pattern westPattern = this.westPatternAsset == null ? Pattern.noPattern() : this.westPatternAsset.build(PatternAsset.argumentFrom(argument));
      return new PatternDirectionality(direction, southPattern, northPattern, eastPattern, westPattern, intSeed);
   }

   @Override
   public void cleanUp() {
      this.northPatternAsset.cleanUp();
      this.southPatternAsset.cleanUp();
      this.eastPatternAsset.cleanUp();
      this.westPatternAsset.cleanUp();
   }
}
