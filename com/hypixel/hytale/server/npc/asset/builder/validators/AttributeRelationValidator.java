package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;

public class AttributeRelationValidator extends Validator {
   private final String firstAttribute;
   private final RelationalOperator relation;
   private final String secondAttribute;

   private AttributeRelationValidator(String firstAttribute, RelationalOperator relation, String secondAttribute) {
      this.firstAttribute = firstAttribute;
      this.relation = relation;
      this.secondAttribute = secondAttribute;
   }

   @Nonnull
   public static AttributeRelationValidator withAttributes(String firstAttribute, RelationalOperator relation, String secondAttribute) {
      return new AttributeRelationValidator(firstAttribute, relation, secondAttribute);
   }
}
