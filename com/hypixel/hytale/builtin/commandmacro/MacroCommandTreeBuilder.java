package com.hypixel.hytale.builtin.commandmacro;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class MacroCommandTreeBuilder {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   private MacroCommandTreeBuilder() {
   }

   @Nullable
   static String rootTokenOf(@Nonnull MacroCommandBuilder builder) {
      String[] path = builder.getPathTokens();
      return path != null && path.length != 0 ? path[0].toLowerCase(Locale.ROOT) : null;
   }

   @Nullable
   static AbstractCommand build(@Nonnull String rootToken, @Nonnull Collection<MacroCommandBuilder> allMacros) {
      MacroCommandTreeBuilder.Node root = new MacroCommandTreeBuilder.Node(rootToken, rootToken);

      for (MacroCommandBuilder builder : allMacros) {
         String[] path = builder.getPathTokens();
         if (path == null) {
            if (builder.getName() != null) {
               LOGGER.at(Level.WARNING)
                  .log("Skipping macro command with invalid Name (must be non-blank, single-space-separated tokens): '%s'", builder.getName());
            }
         } else if (path[0].toLowerCase(Locale.ROOT).equals(rootToken)) {
            insert(root, builder, path);
         }
      }

      return root.macro == null && root.children.isEmpty() ? null : toCommand(root);
   }

   private static void insert(@Nonnull MacroCommandTreeBuilder.Node root, @Nonnull MacroCommandBuilder builder, @Nonnull String[] path) {
      MacroCommandTreeBuilder.Node current = root;
      StringBuilder pathBuilder = new StringBuilder(path[0].toLowerCase(Locale.ROOT));

      for (int i = 1; i < path.length; i++) {
         String token = path[i].toLowerCase(Locale.ROOT);
         pathBuilder.append(' ').append(token);
         current = current.children.computeIfAbsent(token, t -> new MacroCommandTreeBuilder.Node(t, pathBuilder.toString()));
      }

      if (current.macro != null) {
         LOGGER.at(Level.WARNING)
            .log("Duplicate macro command path '%s' (asset '%s'); ignoring duplicate, keeping the first definition.", current.fullPath, builder.getId());
      } else {
         current.macro = builder;
      }
   }

   @Nonnull
   private static AbstractCommand toCommand(@Nonnull MacroCommandTreeBuilder.Node node) {
      AbstractCommand command = null;
      if (node.macro != null) {
         try {
            command = node.macro.createBase(node.token, node.fullPath);
         } catch (RuntimeException var4) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var4))
               .log("Failed to construct macro command '%s' (asset '%s'); leaving path as a passthrough collection.", node.fullPath, node.macro.getId());
         }
      }

      if (command == null) {
         command = new MacroCommandCollection(node.token);
      }

      for (MacroCommandTreeBuilder.Node child : node.children.values()) {
         command.addSubCommand(toCommand(child));
      }

      return command;
   }

   private static final class Node {
      @Nonnull
      final String token;
      @Nonnull
      final String fullPath;
      @Nullable
      MacroCommandBuilder macro;
      @Nonnull
      final Map<String, MacroCommandTreeBuilder.Node> children = new LinkedHashMap<>();

      Node(@Nonnull String token, @Nonnull String fullPath) {
         this.token = token;
         this.fullPath = fullPath;
      }
   }
}
