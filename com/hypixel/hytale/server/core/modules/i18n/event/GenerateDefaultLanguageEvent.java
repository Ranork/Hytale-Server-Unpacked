package com.hypixel.hytale.server.core.modules.i18n.event;

import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.modules.i18n.generator.TranslationMap;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

public class GenerateDefaultLanguageEvent implements IEvent<Void> {
   private final ConcurrentHashMap<String, TranslationMap> translationFiles;

   public GenerateDefaultLanguageEvent(ConcurrentHashMap<String, TranslationMap> translationFiles) {
      this.translationFiles = translationFiles;
   }

   public void putTranslationFile(@Nonnull String filename, @Nonnull TranslationMap translations) {
      this.translationFiles.put(filename, translations);
   }
}
