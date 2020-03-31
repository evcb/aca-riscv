package riscv
import chisel3._
import chisel3.util._
/**
 * Constants used in EX stage.
 */
object Constants
{
  /**
   * Positions of bits in ID/EX register.
   */
  def CONTROL_WB = 255;
  def CONTROL_MEM = 254;
  def CONTROL_EX = 253;

  /**
   * Sizes of signals.
   */
  def SZ_RD = 5.W;
  def SZ_ID_EX_REG = 256.W;
  def SZ_EX_MEM_REG = 68.W;
  def SZ_RS1 = 32.W
  def SZ_RS2 = 32.W
  def SZ_MUX_CTRL = 1.W
  def SZ_ID_EX_F = 10.W
  def SZ_ALU_OP = 2.W
  def SZ_ALU_SRC = 1.W
  def SZ_ALU_FN = 4.W // size of the ALU operation signal
  def SZ_INPUT = 32.W // size of an input
  def SZ_OUTPUT = 32.W// size of an output
  def SZ_FLAG = 1.W

  // Supported ALU functions
  def FN_ADD  = 2.U
  def FN_SUB  = 6.U
  def FN_OR = 1.U
  def FN_AND = 0.U
  def FN_SLT  = 7.U
  def FN_NOR = 1
}
