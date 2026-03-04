package com.hypixel.hytale.server.core.asset.type.model.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.cosmetics.CosmeticAssetValidator;
import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class ModelAttachment implements NetworkSerializable<com.hypixel.hytale.protocol.ModelAttachment> {
   public static final BuilderCodec<ModelAttachment> CODEC = BuilderCodec.builder(ModelAttachment.class, ModelAttachment::new)
      .append(new KeyedCodec<>("Model", Codec.STRING), (modelAttachment, s) -> modelAttachment.model = s, modelAttachment -> modelAttachment.model)
      .addValidator(CommonAssetValidator.MODEL_CHARACTER_ATTACHMENT)
      .add()
      .<String>append(
         new KeyedCodec<>("Texture", Codec.STRING), (modelAttachment, s) -> modelAttachment.texture = s, modelAttachment -> modelAttachment.texture
      )
      .addValidator(CommonAssetValidator.TEXTURE_CHARACTER_ATTACHMENT)
      .add()
      .<String>append(
         new KeyedCodec<>("GradientSet", Codec.STRING), (modelAttachment, s) -> modelAttachment.gradientSet = s, modelAttachment -> modelAttachment.gradientSet
      )
      .metadata(new UIEditor(new UIEditor.Dropdown("GradientSets")))
      .addValidator(new CosmeticAssetValidator(CosmeticType.GRADIENT_SETS))
      .add()
      .<String>append(
         new KeyedCodec<>("GradientId", Codec.STRING), (modelAttachment, s) -> modelAttachment.gradientId = s, modelAttachment -> modelAttachment.gradientId
      )
      .metadata(new UIEditor(new UIEditor.Dropdown("GradientIds")))
      .add()
      .addField(
         new KeyedCodec<>("Weight", Codec.DOUBLE), (modelAttachment, aDouble) -> modelAttachment.weight = aDouble, modelAttachment -> modelAttachment.weight
      )
      .build();
   protected String model;
   protected String texture;
   protected String gradientSet;
   protected String gradientId;
   protected double weight = 1.0;

   public ModelAttachment(String model, String texture, String gradientSet, String gradientId, double weight) {
      this.model = model;
      this.texture = texture;
      this.gradientSet = gradientSet;
      this.gradientId = gradientId;
      this.weight = weight;
   }

   protected ModelAttachment() {
   }

   public String getModel() {
      return this.model;
   }

   public String getTexture() {
      return this.texture;
   }

   public String getGradientId() {
      return this.gradientId;
   }

   public String getGradientSet() {
      return this.gradientSet;
   }

   public double getWeight() {
      return this.weight;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ModelAttachment toPacket() {
      com.hypixel.hytale.protocol.ModelAttachment packet = new com.hypixel.hytale.protocol.ModelAttachment();
      packet.model = this.model;
      packet.texture = this.texture;
      packet.gradientSet = this.gradientSet;
      packet.gradientId = this.gradientId;
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ModelAttachment{model='"
         + this.model
         + "', texture='"
         + this.texture
         + "', gradientSet='"
         + this.gradientSet
         + "', gradientId='"
         + this.gradientId
         + "', weight="
         + this.weight
         + "}";
   }
}
