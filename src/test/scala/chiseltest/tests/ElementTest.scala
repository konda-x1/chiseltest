// SPDX-License-Identifier: Apache-2.0

package chiseltest.tests

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ElementTest extends AnyFlatSpec with ChiselScalatestTester with Matchers {
  behavior of "Testers2 with Element types"

  // TODO: automatically detect overflow conditions and error out

  it should "work with UInt" in {
    test(new Module {
      val io = IO(new Bundle {
        val in1 = Input(UInt(8.W))
        val in2 = Input(UInt(8.W))
        val out = Output(UInt(8.W))

        def expect(in1Val: UInt, in2Val: UInt, outVal: UInt): Unit = {
          in1.poke(in1Val)
          in2.poke(in2Val)
          out.expect(outVal)
        }
      })
      io.out := io.in1 + io.in2
    }) { c =>
      c.io.expect(0.U, 0.U, 0.U)
      c.io.expect(1.U, 0.U, 1.U)
      c.io.expect(0.U, 1.U, 1.U)
      c.io.expect(1.U, 1.U, 2.U)
      c.io.expect(254.U, 1.U, 255.U)
      c.io.expect(255.U, 1.U, 0.U)  // overflow behavior
      c.io.expect(255.U, 255.U, 254.U)  // overflow behavior
    }
  }

  it should "work with SInt" in {
    test(new Module {
      val io = IO(new Bundle {
        val in1 = Input(SInt(8.W))
        val in2 = Input(SInt(8.W))
        val out = Output(SInt(8.W))

        def expect(in1Val: SInt, in2Val: SInt, outVal: SInt): Unit = {
          in1.poke(in1Val)
          in2.poke(in2Val)
          out.expect(outVal)
        }
      })
      io.out := io.in1 + io.in2
    }) { c =>
      c.io.expect(0.S, 0.S, 0.S)
      c.io.expect(1.S, 0.S, 1.S)
      c.io.expect(0.S, 1.S, 1.S)
      c.io.expect(1.S, 1.S, 2.S)

      c.io.expect(127.S, -1.S, 126.S)
      c.io.expect(127.S, -127.S, 0.S)
      c.io.expect(-128.S, 127.S, -1.S)
      c.io.expect(-126.S, 127.S, 1.S)

      c.io.expect(127.S, 1.S, -128.S)
      c.io.expect(-128.S, -1.S, 127.S)
      c.io.expect(127.S, 127.S, -2.S)
      c.io.expect(-128.S, -128.S, 0.S)
    }
  }

  it should "work with Bool" in {
    test(new Module {
      val io = IO(new Bundle {
        val in1 = Input(Bool())
        val in2 = Input(Bool())
        val outAnd = Output(Bool())
        val outOr = Output(Bool())

        def expect(in1Val: Bool, in2Val: Bool, andVal: Bool, orVal: Bool): Unit = {
          in1.poke(in1Val)
          in2.poke(in2Val)
          outAnd.expect(andVal)
          outOr.expect(orVal)
        }
      })
      io.outAnd := io.in1 && io.in2
      io.outOr := io.in1 || io.in2
    }) { c =>
      c.io.expect(true.B, true.B, true.B, true.B)
      c.io.expect(false.B, false.B, false.B, false.B)
      c.io.expect(true.B, false.B, false.B, true.B)
      c.io.expect(false.B, true.B, false.B, true.B)
    }
  }
}
