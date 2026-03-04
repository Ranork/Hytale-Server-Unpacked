package com.hypixel.hytale.builtin.asseteditor.event;

import com.hypixel.hytale.builtin.asseteditor.EditorClient;
import com.hypixel.hytale.event.IEvent;
import javax.annotation.Nonnull;

public abstract class EditorClientEvent<KeyType> implements IEvent<KeyType> {
   private final EditorClient editorClient;

   public EditorClientEvent(EditorClient editorClient) {
      this.editorClient = editorClient;
   }

   public EditorClient getEditorClient() {
      return this.editorClient;
   }

   @Nonnull
   @Override
   public String toString() {
      return "EditorClientEvent{editorClient=" + this.editorClient + "}";
   }
}
