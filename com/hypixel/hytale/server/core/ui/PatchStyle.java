package com.hypixel.hytale.server.core.ui;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class PatchStyle {
   public static final BuilderCodec<PatchStyle> CODEC = BuilderCodec.builder(PatchStyle.class, PatchStyle::new)
      .addField(new KeyedCodec<>("TexturePath", ValueCodec.STRING), (p, t) -> p.texturePath = t, p -> p.texturePath)
      .addField(new KeyedCodec<>("Border", ValueCodec.INTEGER), (p, t) -> p.border = t, p -> p.border)
      .addField(new KeyedCodec<>("HorizonzalBorder", ValueCodec.INTEGER), (p, t) -> p.horizontalBorder = t, p -> p.horizontalBorder)
      .addField(new KeyedCodec<>("VerticalBorder", ValueCodec.INTEGER), (p, t) -> p.verticalBorder = t, p -> p.verticalBorder)
      .addField(new KeyedCodec<>("Color", ValueCodec.STRING), (p, t) -> p.color = t, p -> p.color)
      .addField(new KeyedCodec<>("Area", new ValueCodec<>(Area.CODEC)), (p, t) -> p.area = t, p -> p.area)
      .build();
   private Value<String> texturePath;
   private Value<Integer> border;
   private Value<Integer> horizontalBorder;
   private Value<Integer> verticalBorder;
   private Value<String> color;
   private Value<Area> area;

   public PatchStyle() {
   }

   public PatchStyle(Value<String> texturePath) {
      this.texturePath = texturePath;
   }

   public PatchStyle(Value<String> texturePath, Value<Integer> border) {
      this.texturePath = texturePath;
      this.border = border;
   }

   @Nonnull
   public PatchStyle setTexturePath(Value<String> texturePath) {
      this.texturePath = texturePath;
      return this;
   }

   @Nonnull
   public PatchStyle setBorder(Value<Integer> border) {
      this.border = border;
      return this;
   }

   @Nonnull
   public PatchStyle setHorizontalBorder(Value<Integer> horizontalBorder) {
      this.horizontalBorder = horizontalBorder;
      return this;
   }

   @Nonnull
   public PatchStyle setVerticalBorder(Value<Integer> verticalBorder) {
      this.verticalBorder = verticalBorder;
      return this;
   }

   @Nonnull
   public PatchStyle setColor(Value<String> color) {
      this.color = color;
      return this;
   }

   @Nonnull
   public PatchStyle setArea(Value<Area> area) {
      this.area = area;
      return this;
   }
}
