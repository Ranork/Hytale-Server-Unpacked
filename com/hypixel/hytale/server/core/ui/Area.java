package com.hypixel.hytale.server.core.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class Area {
   public static final BuilderCodec<Area> CODEC = BuilderCodec.builder(Area.class, Area::new)
      .addField(new KeyedCodec<>("X", Codec.INTEGER), (p, t) -> p.x = t, p -> p.x)
      .addField(new KeyedCodec<>("Y", Codec.INTEGER), (p, t) -> p.y = t, p -> p.y)
      .addField(new KeyedCodec<>("Width", Codec.INTEGER), (p, t) -> p.width = t, p -> p.width)
      .addField(new KeyedCodec<>("Height", Codec.INTEGER), (p, t) -> p.height = t, p -> p.height)
      .build();
   private int x;
   private int y;
   private int width;
   private int height;

   @Nonnull
   public Area setX(int x) {
      this.x = x;
      return this;
   }

   @Nonnull
   public Area setY(int y) {
      this.y = y;
      return this;
   }

   @Nonnull
   public Area setWidth(int width) {
      this.width = width;
      return this;
   }

   @Nonnull
   public Area setHeight(int height) {
      this.height = height;
      return this;
   }
}
