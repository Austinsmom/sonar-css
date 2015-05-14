/*
 * SonarQube CSS Plugin
 * Copyright (C) 2013 Tamas Kende
 * kende.tamas@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.css.checks;

import com.sonar.sslr.api.AstNode;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.css.parser.CssGrammar;

import java.util.List;

import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import org.sonar.check.BelongsToProfile;
import org.sonar.check.Cardinality;
import org.sonar.check.Priority;
import org.sonar.check.Rule;

/**
 * https://github.com/stubbornella/csslint/wiki/Bulletproof-font-face
 * @author tkende
 *
 */
@Rule(
  key = "bulletproof-font-face",
  name = "Bulletproof font face",
  priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.ARCHITECTURE_RELIABILITY)
@SqaleConstantRemediation("10min")
@ActivatedByDefault
public class BulletproofFontFace extends SquidCheck<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(CssGrammar.AT_RULE);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if ("font-face".equals(astNode.getFirstChild(CssGrammar.AT_KEYWORD).getFirstChild(CssGrammar.IDENT).getTokenValue())) {
      List<AstNode> declarations = astNode.getFirstDescendant(CssGrammar.atRuleBlock).getFirstChild(CssGrammar.SUP_DECLARATION).getChildren(CssGrammar.DECLARATION);
      for (AstNode declaration : declarations) {
        if ("src".equals(declaration.getFirstChild(CssGrammar.PROPERTY).getTokenValue())) {
          String firstAnyFunciontValue = CssChecksUtil.getStringValue(
              declaration.getFirstChild(CssGrammar.VALUE)
                  .getFirstChild(CssGrammar.FUNCTION)
                  .getFirstChild(CssGrammar.parameters)
                  .getFirstDescendant(CssGrammar.parameter));
          if (!firstAnyFunciontValue.matches(".*\\.eot\\?.*?['\"]?$")) {
            getContext().createLineViolation(this, "First web font has missing query string or it is not eot", astNode);
          }
          // We only care about the first function
          return;
        }
      }
    }
  }

}
