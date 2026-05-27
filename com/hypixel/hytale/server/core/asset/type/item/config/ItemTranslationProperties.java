package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemTranslationProperties implements NetworkSerializable<com.hypixel.hytale.protocol.ItemTranslationProperties> {
   private static final Validator<String> SERVER_LANG_KEY_VALIDATOR = new Validator<String>() {
      public void accept(@Nullable String key, @Nonnull ValidationResults results) {
         if (key != null && key.startsWith("server.")) {
            I18nModule i18n = I18nModule.get();
            if (i18n != null) {
               if (!i18n.getMessages("en-US").containsKey(key)) {
                  results.warn("[LOC] Key '" + key + "' does not exist in server.lang!");
               }
            }
         }
      }

      @Override
      public void updateSchema(SchemaContext context, Schema target) {
      }
   };
   private static final Validator<Map<String, String>> ARGUMENT_LANG_KEYS_VALIDATOR = new Validator<Map<String, String>>() {
      public void accept(@Nullable Map<String, String> map, @Nonnull ValidationResults results) {
         if (map != null) {
            for (String value : map.values()) {
               ItemTranslationProperties.SERVER_LANG_KEY_VALIDATOR.accept(value, results);
            }
         }
      }

      @Override
      public void updateSchema(SchemaContext context, Schema target) {
      }
   };
   public static final BuilderCodec<ItemTranslationProperties> CODEC = BuilderCodec.builder(ItemTranslationProperties.class, ItemTranslationProperties::new)
      .appendInherited(new KeyedCodec<>("Name", Codec.STRING), (data, s) -> data.name = s, data -> data.name, (o, p) -> o.name = p.name)
      .addValidator(SERVER_LANG_KEY_VALIDATOR)
      .documentation("The translation key for the name of this item.")
      .metadata(new UIEditor(new UIEditor.LocalizationKeyField("server.items.{assetId}.name", true)))
      .add()
      .<Map>appendInherited(
         new KeyedCodec<>("NameArguments", new MapCodec<>(Codec.STRING, HashMap::new)),
         (data, m) -> data.nameArgumentKeys = m,
         data -> data.nameArgumentKeys,
         (o, p) -> o.nameArgumentKeys = p.nameArgumentKeys
      )
      .addValidator(ARGUMENT_LANG_KEYS_VALIDATOR)
      .documentation("The map of arguments used in the item name loc key, if any.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("Description", Codec.STRING), (data, s) -> data.description = s, data -> data.description, (o, p) -> o.description = p.description
      )
      .addValidator(SERVER_LANG_KEY_VALIDATOR)
      .documentation("The translation key for the description of this item.")
      .metadata(new UIEditor(new UIEditor.LocalizationKeyField("server.items.{assetId}.description")))
      .add()
      .<Map>appendInherited(
         new KeyedCodec<>("DescriptionArguments", new MapCodec<>(Codec.STRING, HashMap::new)),
         (data, m) -> data.descriptionArgumentKeys = m,
         data -> data.descriptionArgumentKeys,
         (o, p) -> o.descriptionArgumentKeys = p.descriptionArgumentKeys
      )
      .addValidator(ARGUMENT_LANG_KEYS_VALIDATOR)
      .documentation("The map of arguments used in the item description loc key, if any.")
      .add()
      .afterDecode(ItemTranslationProperties::resolveArguments)
      .build();
   @Nullable
   private String name;
   @Nullable
   private Map<String, String> nameArgumentKeys;
   @Nullable
   private Map<String, Message> nameArguments;
   @Nullable
   private String description;
   @Nullable
   private Map<String, String> descriptionArgumentKeys;
   @Nullable
   private Map<String, Message> descriptionArguments;

   ItemTranslationProperties() {
   }

   public ItemTranslationProperties(@Nonnull String name, @Nonnull String description) {
      this.name = name;
      this.description = description;
   }

   @Nullable
   public String getName() {
      return this.name;
   }

   @Nullable
   public Map<String, Message> getNameArguments() {
      return this.nameArguments;
   }

   @Nullable
   public String getDescription() {
      return this.description;
   }

   @Nullable
   public Map<String, Message> getDescriptionArguments() {
      return this.descriptionArguments;
   }

   private void resolveArguments() {
      this.nameArguments = wrapKeys(this.nameArgumentKeys);
      this.descriptionArguments = wrapKeys(this.descriptionArgumentKeys);
   }

   @Nullable
   private static Map<String, Message> wrapKeys(@Nullable Map<String, String> keys) {
      if (keys != null && !keys.isEmpty()) {
         HashMap<String, Message> result = new HashMap<>(keys.size());

         for (Entry<String, String> entry : keys.entrySet()) {
            result.put(entry.getKey(), Message.translation(entry.getValue()));
         }

         return result;
      } else {
         return null;
      }
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ItemTranslationProperties toPacket() {
      com.hypixel.hytale.protocol.ItemTranslationProperties packet = new com.hypixel.hytale.protocol.ItemTranslationProperties();
      packet.name = this.name;
      packet.description = this.description;
      if (this.nameArguments != null && !this.nameArguments.isEmpty()) {
         packet.nameArguments = new HashMap<>(this.nameArguments.size());

         for (Entry<String, Message> entry : this.nameArguments.entrySet()) {
            packet.nameArguments.put(entry.getKey(), entry.getValue().getFormattedMessage());
         }
      }

      if (this.descriptionArguments != null && !this.descriptionArguments.isEmpty()) {
         packet.descriptionArguments = new HashMap<>(this.descriptionArguments.size());

         for (Entry<String, Message> entry : this.descriptionArguments.entrySet()) {
            packet.descriptionArguments.put(entry.getKey(), entry.getValue().getFormattedMessage());
         }
      }

      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ItemTranslationProperties{name="
         + this.name
         + ", nameArguments="
         + this.nameArguments
         + ", description="
         + this.description
         + ", descriptionArguments="
         + this.descriptionArguments
         + "}";
   }
}
