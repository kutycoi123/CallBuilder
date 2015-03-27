/*
 * Copyright (C) 2015 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.callbuilder;

import com.google.callbuilder.Unification.Atom;
import com.google.callbuilder.Unification.Result;
import com.google.callbuilder.Unification.Sequence;
import com.google.callbuilder.Unification.Unifiable;
import com.google.callbuilder.Unification.Variable;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class UnificationTest {
  private Atom a = new Atom();
  private Atom b = new Atom();
  private Atom c = new Atom();
  private Variable x = new Variable();
  private Variable y = new Variable();
  private Variable z = new Variable();

  @Test
  public void testTwoAtomsNotEqual() {
    Assert.assertEquals(Optional.absent(),
        Unification.unify(new Unification.Atom(), new Unification.Atom()));
  }

  private Sequence seq(Unifiable... items) {
    return new AutoValue_Unification_Sequence(ImmutableList.copyOf(items));
  }

  @Test
  public void testVariableResolvesTransitivelyToAtom() {
    // ? Y = [a, X], X = c.
    Result result = Unification.unify(
        seq(seq(a, x), b),
        seq(y, x)).get();
    // Y = [a, b],
    // X = b.
    Assert.assertEquals(seq(a, b), result.resolve(y));
    Assert.assertEquals(b, result.resolve(x));
  }

  @Test
  public void testSimpleAssignmentAcrossSequence() {
    Assert.assertEquals(
        ImmutableMap.of(x, b, y, a),
        Unification.unify(
            seq(a, x),
            seq(y, b)).get().rawResult());
  }

  @Test
  public void testGenericTypeLikeCase() {
    // a<X> == a<b<Y>>
    // Y = c
    // Result:
    // Y = c
    // X = b<c>
    Result result = Unification.unify(
        seq(seq(a, x        ), y),
        seq(seq(a, seq(b, y)), c)).get();
    Assert.assertEquals(c, result.resolve(y));
    Assert.assertEquals(seq(b, c), result.resolve(x));
  }

  @Test
  public void testTrivialFailure() {
    Assert.assertEquals(
        Optional.absent(),
        Unification.unify(seq(a), seq(b)));
  }

  @Test
  public void testSlighlyTrickyFailure() {
    Assert.assertEquals(
        Optional.absent(),
        Unification.unify(seq(a, x, x, z), seq(y, b, z, y)));
  }
}