// SPDX-License-Identifier: Apache-2.0

package chiseltest.tests

import chisel3.experimental.BundleLiterals.AddBundleLiteralConstructor
import chisel3.experimental.OpaqueType
import chisel3._
import chiseltest._
import chiseltest.iotesters.PeekPokeTester
import org.scalatest.flatspec.AnyFlatSpec

import scala.collection.immutable.SeqMap

class OpaqueTypeTest extends AnyFlatSpec with ChiselScalatestTester {

  class OpaqueRecord[T <: Data](val data: T) extends Record with OpaqueType {
    val elements: SeqMap[String, T] = SeqMap("" -> data)
  }

  class OpaquePassthrough[T <: Data](data: T) extends Module {
    val in: OpaqueRecord[T] = IO(Input(new OpaqueRecord(data)))
    val out: OpaqueRecord[T] = IO(Output(new OpaqueRecord(data)))
    out := in
  }

  def rec[T <: Data](_val: => T): OpaqueRecord[T] = new OpaqueRecord(_val.cloneType: T).Lit(_.data -> _val)

  def testPokeExpect[T <: Data](_val: => T): TestResult =
    test(new OpaquePassthrough(_val.cloneType)) { dut =>
      dut.in.poke(rec(_val))
      dut.out.expect(rec(_val))
    }

  class PokeExpectTester[T <: Data](dut: OpaquePassthrough[T], _val: => T) extends PeekPokeTester(dut) {
    poke(dut.in, IndexedSeq(_val.litValue))
    expect(dut.out, IndexedSeq(_val.litValue))
  }

  def testPokeExpectTester[T <: Data](_val: => T): Unit =
    test(new OpaquePassthrough(_val.cloneType))
      .runPeekPoke(new PokeExpectTester(_, _val))

  behavior of "OpaqueType"

  it should "poke and expect successfully" in {
    testPokeExpect(4.U(6.W))
    testPokeExpect(-4.S(8.W))
    testPokeExpect(rec(5.U(3.W)))
  }

  it should "poke and expect successfully using PeekPokeTester" in {
    testPokeExpectTester(4.U(6.W))
    testPokeExpectTester(-4.S(8.W))
    testPokeExpectTester(rec(5.U(3.W)))
  }

}
