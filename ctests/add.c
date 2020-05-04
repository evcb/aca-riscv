asm("addi sp, sp, 0x200"); // SP set to 512
asm("jal main");        // call main
asm("mv a1, a0");       // save return value in a1
int main() {

  int a = 1;
  int b = 2;

  return a+b;
}