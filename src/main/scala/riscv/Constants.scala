package riscv
import chisel3._
import chisel3.util._
/**
 * Constants used in EX stage.
 */
object Constants
{
  /**
   * End positions of bits in CTL register.
   */
  def END_WB = 6;
  def END_MEM = 4;
  def ALU_OP = 2;
  def ALU_SRC = 0;

  /**
   * End positions of bits in ID/EX register.
   */
  def ID_EX_D1 = 120;
  def ID_EX_D2 = 88;
  def ID_EX_IMM = 56;
  def ID_EX_F = 24;
  def ID_EX_RS2 = 14;
  def ID_EX_RS1 = 9; 
  def ID_EX_RD = 4;

  /**
   * End positions of bits in EX/MEM register.
   */
  def EX_MEM_WB = 72;
  def EX_MEM_MEM = 70;
  def EX_MEM_ADDR = 68;
  def EX_MEM_WD = 36;
  def EX_MEM_RD = 4;

  /**
   * Sizes of signals.
   */
  def SZ_ID_EX_REG = 121.W;
  def SZ_CTL_REG = 7.W;
  def SZ_EX_MEM_REG = 73.W;

  def SZ_RD = 5.W;
  def SZ_RS1 = 5.W
  def SZ_RS2 = 5.W
  def SZ_ALU_OP = 2.W
  def SZ_ALU_SRC = 1.W
  def SZ_MUX_CTRL = 2.W
  def SZ_ALU_FN = 4.W // size of the ALU operation signal
  def SZ_32_BIT = 32.W // size of an input
  //def SZ_OUTPUT = 32.W// size of an output
  //def SZ_IMMEDIATE = 32.W // size of an immediate


  // Supported ALU functions
  def FN_ADD  = 2.U
  def FN_SUB  = 6.U
  def FN_OR = 1.U
  def FN_AND = 0.U

  
  def FN_SLT  = 7.U
  //def FN_NOR = 1
}
