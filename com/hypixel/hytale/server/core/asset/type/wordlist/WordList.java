package com.hypixel.hytale.server.core.asset.type.wordlist;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WordList implements JsonAssetWithMap<String, DefaultAssetMap<String, WordList>> {
   private static final String WORDLISTS_TRANSLATION_FILE = "wordlists";
   public static final AssetBuilderCodec<String, WordList> CODEC = AssetBuilderCodec.builder(
         WordList.class,
         WordList::new,
         Codec.STRING,
         (wordList, s) -> wordList.id = s,
         wordList -> wordList.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .appendInherited(
         new KeyedCodec<>("TranslationKeys", Codec.STRING_ARRAY),
         (wordList, o) -> wordList.translationKeys = o,
         wordList -> wordList.translationKeys,
         (wordList, parent) -> wordList.translationKeys = parent.translationKeys
      )
      .documentation(
         "The list of word message keys. Need to be added in Assets/Server/Languages/wordlists.lang. For example if the WordList asset file is 'animals' and you write 'cow' here, it will refer to 'animals.cow' (full path is 'wordlists.animals.cow')"
      )
      .add()
      .afterDecode(WordList::processConfig)
      .build();
   private static AssetStore<String, WordList, DefaultAssetMap<String, WordList>> ASSET_STORE;
   @Nonnull
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(WordList::getAssetStore));
   private static final WordList EMPTY = new WordList();
   protected AssetExtraInfo.Data data;
   protected String id;
   protected String[] translationKeys;

   @Nonnull
   public static AssetStore<String, WordList, DefaultAssetMap<String, WordList>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(WordList.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, WordList> getAssetMap() {
      return (DefaultAssetMap<String, WordList>)getAssetStore().getAssetMap();
   }

   public static WordList getWordList(@Nullable String assetKey) {
      if (assetKey != null && !assetKey.isEmpty()) {
         WordList wordList = getAssetMap().getAsset(assetKey);
         return wordList == null ? EMPTY : wordList;
      } else {
         return EMPTY;
      }
   }

   protected WordList() {
   }

   public String getId() {
      return this.id;
   }

   protected void processConfig() {
      if (this.translationKeys != null) {
         String idLower = this.id.toLowerCase();
         String[] remappedTranslationKeys = new String[this.translationKeys.length];

         for (int i = 0; i < this.translationKeys.length; i++) {
            remappedTranslationKeys[i] = "wordlists." + idLower + "." + this.translationKeys[i];
         }

         this.translationKeys = remappedTranslationKeys;
      }
   }

   @Nullable
   public String pickDefaultLanguage(@Nonnull Random random, @Nonnull Set<String> alreadyUsedTranslated) {
      String translationKey = this.pickTranslationKey(random, alreadyUsedTranslated, "en-US");
      return translationKey == null ? null : I18nModule.get().getMessage("en-US", translationKey);
   }

   @Nullable
   public String pickTranslationKey(@Nonnull Random random, @Nonnull Set<String> alreadyUsedTranslated, String languageForAlreadyUsed) {
      List<String> available = toKeysListMinusTranslated(this.translationKeys, alreadyUsedTranslated, languageForAlreadyUsed);
      return available.isEmpty() ? null : available.get(random.nextInt(available.size()));
   }

   @Nonnull
   private static <T> List<T> toListMinusSet(@Nullable T[] array, @Nonnull Set<T> set) {
      if (array != null && array.length != 0) {
         List<T> result = new ObjectArrayList(array.length);

         for (T elem : array) {
            if (!set.contains(elem)) {
               result.add(elem);
            }
         }

         return result;
      } else {
         return Collections.emptyList();
      }
   }

   @Nonnull
   private static List<String> toKeysListMinusTranslated(@Nullable String[] translationKeys, @Nonnull Set<String> alreadyUsedTranslated, String language) {
      if (translationKeys != null && translationKeys.length != 0) {
         List<String> result = new ObjectArrayList(translationKeys.length);

         for (String translationKey : translationKeys) {
            String translated = I18nModule.get().getMessage(language, translationKey);
            if (translated != null && !alreadyUsedTranslated.contains(translated)) {
               result.add(translationKey);
            }
         }

         return result;
      } else {
         return Collections.emptyList();
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "WordList{id='" + this.id + "', translationKeys=" + this.translationKeys + "}";
   }
}
