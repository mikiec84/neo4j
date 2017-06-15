/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.cypher.internal.frontend.v3_3.ast

import org.neo4j.cypher.internal.frontend.v3_3.symbols._
import org.neo4j.cypher.internal.frontend.v3_3.test_helpers.CypherFunSuite
import org.neo4j.cypher.internal.frontend.v3_3.{DummyPosition, SemanticState}

class CaseExpressionTest extends CypherFunSuite {

  test("Simple: Should combine types of alternatives") {
    val caseExpression = CaseExpression(
      expression = Some(DummyExpression(CTString)),
      alternatives = IndexedSeq(
        (
          DummyExpression(CTString),
          DummyExpression(CTFloat)
        ), (
          DummyExpression(CTString),
          DummyExpression(CTInteger)
        )
      ),
      default = Some(DummyExpression(CTFloat))
    )(DummyPosition(2))

    val result = caseExpression.semanticCheck(Expression.SemanticContext.Simple)(SemanticState.clean)
    result.errors shouldBe empty
    caseExpression.types(result.state) should equal(CTInteger | CTFloat)
  }

  test("Generic: Should combine types of alternatives") {
    val caseExpression = CaseExpression(
      None,
      IndexedSeq(
        (
          DummyExpression(CTBoolean),
          DummyExpression(CTFloat | CTString)
        ), (
          DummyExpression(CTBoolean),
          DummyExpression(CTInteger)
        )
      ),
      Some(DummyExpression(CTFloat | CTNode))
    )(DummyPosition(2))

    val result = caseExpression.semanticCheck(Expression.SemanticContext.Simple)(SemanticState.clean)
    result.errors shouldBe empty
    caseExpression.types(result.state) should equal(CTInteger | CTFloat | CTString | CTNode)
  }

  test("Generic: should type check predicates") {
    val caseExpression = CaseExpression(
      None,
      IndexedSeq(
        (
          DummyExpression(CTBoolean),
          DummyExpression(CTFloat)
        ), (
          DummyExpression(CTString, DummyPosition(12)),
          DummyExpression(CTInteger)
        )
      ),
      Some(DummyExpression(CTFloat))
    )(DummyPosition(2))

    val result = caseExpression.semanticCheck(Expression.SemanticContext.Simple)(SemanticState.clean)
    result.errors should have size 1
    result.errors.head.msg should equal("Type mismatch: expected Boolean but was String")
    result.errors.head.position should equal(DummyPosition(12))
  }
}
