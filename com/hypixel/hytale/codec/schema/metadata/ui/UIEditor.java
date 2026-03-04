package com.hypixel.hytale.codec.schema.metadata.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.metadata.Metadata;
import javax.annotation.Nonnull;

public class UIEditor implements Metadata {
   public static final CodecMapCodec<UIEditor.EditorComponent> CODEC = new CodecMapCodec<>("component");
   public static final UIEditor.Timeline TIMELINE = new UIEditor.Timeline();
   public static final UIEditor.WeightedTimeline WEIGHTED_TIMELINE = new UIEditor.WeightedTimeline();
   private final UIEditor.EditorComponent component;

   public UIEditor(UIEditor.EditorComponent component) {
      this.component = component;
   }

   @Override
   public void modify(@Nonnull Schema schema) {
      schema.getHytale().setUiEditorComponent(this.component);
   }

   public static void init() {
      CODEC.register("Timeline", UIEditor.Timeline.class, UIEditor.Timeline.CODEC);
      CODEC.register("WeightedTimeline", UIEditor.WeightedTimeline.class, UIEditor.WeightedTimeline.CODEC);
      CODEC.register("Number", UIEditor.FormattedNumber.class, UIEditor.FormattedNumber.CODEC);
      CODEC.register("Text", UIEditor.TextField.class, UIEditor.TextField.CODEC);
      CODEC.register("MultilineText", UIEditor.MultilineTextField.class, UIEditor.MultilineTextField.CODEC);
      CODEC.register("Dropdown", UIEditor.Dropdown.class, UIEditor.Dropdown.CODEC);
      CODEC.register("Icon", UIEditor.Icon.class, UIEditor.Icon.CODEC);
      CODEC.register("LocalizationKey", UIEditor.LocalizationKeyField.class, UIEditor.LocalizationKeyField.CODEC);
   }

   public static class Dropdown implements UIEditor.EditorComponent {
      public static final BuilderCodec<UIEditor.Dropdown> CODEC = BuilderCodec.builder(UIEditor.Dropdown.class, UIEditor.Dropdown::new)
         .addField(new KeyedCodec<>("dataSet", Codec.STRING, false, true), (o, i) -> o.dataSet = i, o -> o.dataSet)
         .build();
      private String dataSet;

      public Dropdown(String dataSet) {
         this.dataSet = dataSet;
      }

      protected Dropdown() {
      }
   }

   public interface EditorComponent {
   }

   public static class FormattedNumber implements UIEditor.EditorComponent {
      public static final BuilderCodec<UIEditor.FormattedNumber> CODEC = BuilderCodec.builder(UIEditor.FormattedNumber.class, UIEditor.FormattedNumber::new)
         .addField(new KeyedCodec<>("step", Codec.DOUBLE, false, true), (o, i) -> o.step = i, o -> o.step)
         .addField(new KeyedCodec<>("suffix", Codec.STRING, false, true), (o, i) -> o.suffix = i, o -> o.suffix)
         .addField(new KeyedCodec<>("maxDecimalPlaces", Codec.INTEGER, false, true), (o, i) -> o.maxDecimalPlaces = i, o -> o.maxDecimalPlaces)
         .build();
      private Double step;
      private String suffix;
      private Integer maxDecimalPlaces;

      public FormattedNumber(Double step, String suffix, Integer maxDecimalPlaces) {
         this.step = step;
         this.suffix = suffix;
         this.maxDecimalPlaces = maxDecimalPlaces;
      }

      public FormattedNumber() {
      }

      @Nonnull
      public UIEditor.FormattedNumber setStep(Double step) {
         this.step = step;
         return this;
      }

      @Nonnull
      public UIEditor.FormattedNumber setSuffix(String suffix) {
         this.suffix = suffix;
         return this;
      }

      @Nonnull
      public UIEditor.FormattedNumber setMaxDecimalPlaces(Integer maxDecimalPlaces) {
         this.maxDecimalPlaces = maxDecimalPlaces;
         return this;
      }
   }

   public static class Icon implements UIEditor.EditorComponent {
      public static final BuilderCodec<UIEditor.Icon> CODEC = BuilderCodec.builder(UIEditor.Icon.class, UIEditor.Icon::new)
         .addField(new KeyedCodec<>("defaultPathTemplate", Codec.STRING, true, true), (o, i) -> o.defaultPathTemplate = i, o -> o.defaultPathTemplate)
         .addField(new KeyedCodec<>("width", Codec.INTEGER, true, true), (o, i) -> o.width = i, o -> o.width)
         .addField(new KeyedCodec<>("height", Codec.INTEGER, true, true), (o, i) -> o.height = i, o -> o.height)
         .build();
      private String defaultPathTemplate;
      private int width;
      private int height;

      public Icon(String defaultPathTemplate, int width, int height) {
         this.defaultPathTemplate = defaultPathTemplate;
         this.width = width;
         this.height = height;
      }

      public Icon() {
      }
   }

   public static class LocalizationKeyField implements UIEditor.EditorComponent {
      public static final BuilderCodec<UIEditor.LocalizationKeyField> CODEC = BuilderCodec.builder(
            UIEditor.LocalizationKeyField.class, UIEditor.LocalizationKeyField::new
         )
         .addField(new KeyedCodec<>("keyTemplate", Codec.STRING, false, true), (o, i) -> o.keyTemplate = i, o -> o.keyTemplate)
         .addField(new KeyedCodec<>("generateDefaultKey", Codec.BOOLEAN, false, true), (o, i) -> o.generateDefaultKey = i, o -> o.generateDefaultKey)
         .build();
      private String keyTemplate;
      private boolean generateDefaultKey;

      public LocalizationKeyField(String keyTemplate) {
         this(keyTemplate, false);
      }

      public LocalizationKeyField(String keyTemplate, boolean generateDefaultKey) {
         this.keyTemplate = keyTemplate;
         this.generateDefaultKey = generateDefaultKey;
      }

      public LocalizationKeyField() {
      }
   }

   public static class MultilineTextField implements UIEditor.EditorComponent {
      public static final BuilderCodec<UIEditor.MultilineTextField> CODEC = BuilderCodec.builder(
            UIEditor.MultilineTextField.class, UIEditor.MultilineTextField::new
         )
         .build();
   }

   public static class TextField implements UIEditor.EditorComponent {
      public static final BuilderCodec<UIEditor.TextField> CODEC = BuilderCodec.builder(UIEditor.TextField.class, UIEditor.TextField::new)
         .addField(new KeyedCodec<>("dataSet", Codec.STRING, false, true), (o, i) -> o.dataSet = i, o -> o.dataSet)
         .build();
      private String dataSet;

      public TextField(String dataSet) {
         this.dataSet = dataSet;
      }

      protected TextField() {
      }
   }

   public static class Timeline implements UIEditor.EditorComponent {
      public static final BuilderCodec<UIEditor.Timeline> CODEC = BuilderCodec.builder(UIEditor.Timeline.class, UIEditor.Timeline::new).build();
   }

   public static class WeightedTimeline implements UIEditor.EditorComponent {
      public static final BuilderCodec<UIEditor.WeightedTimeline> CODEC = BuilderCodec.builder(UIEditor.WeightedTimeline.class, UIEditor.WeightedTimeline::new)
         .build();
   }
}
