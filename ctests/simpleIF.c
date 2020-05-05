asm("addi sp, sp, 0x100"); // SP set to 256
asm("jal main");        // call main
asm("mv a1, a0");       // save return value in a1

int main() {
  int a = 5;
  int b = 8;
  
  if (a > b) {
    return b-a;
  }
  return b*a;
}