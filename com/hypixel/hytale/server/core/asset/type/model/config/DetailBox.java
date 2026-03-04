package com.hypixel.hytale.server.core.asset.type.model.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.io.NetworkSerializers;
import javax.annotation.Nonnull;

public class DetailBox implements NetworkSerializable<com.hypixel.hytale.protocol.DetailBox> {
   public static final BuilderCodec<DetailBox> CODEC = BuilderCodec.builder(DetailBox.class, DetailBox::new)
      .appendInherited(new KeyedCodec<>("Offset", Vector3d.CODEC), (o, i) -> o.offset = i, o -> o.offset, (o, p) -> o.offset = p.offset)
      .addValidator(Validators.nonNull())
      .add()
      .<Box>appendInherited(new KeyedCodec<>("Box", Box.CODEC), (o, i) -> o.box = i, o -> o.box, (o, p) -> o.box = p.box)
      .addValidator(Validators.nonNull())
      .add()
      .build();
   protected Vector3d offset = Vector3d.ZERO;
   protected Box box = Box.UNIT;

   public DetailBox() {
   }

   public DetailBox(Vector3d offset, Box box) {
      this.offset = offset;
      this.box = box;
   }

   public DetailBox(DetailBox other) {
      this.offset.assign(other.offset);
      this.box.assign(other.box);
   }

   public Vector3d getOffset() {
      return this.offset;
   }

   public Box getBox() {
      return this.box;
   }

   public DetailBox scaled(float scale) {
      return new DetailBox(this.offset.clone().scale(scale), this.box.clone().scale(scale));
   }

   @Nonnull
   public com.hypixel.hytale.protocol.DetailBox toPacket() {
      return new com.hypixel.hytale.protocol.DetailBox(
         new Vector3f((float)this.offset.x, (float)this.offset.y, (float)this.offset.z), NetworkSerializers.BOX.toPacket(this.box)
      );
   }
}
