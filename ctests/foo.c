// This is our minimal startup code (usually in _start)
asm("lui sp, 0x100000"); // SP set to 1 MB
asm("jal main");        // call main
asm("mv a1, a0");       // save return value in a1
asm("lui a0, 10");       // prepare ecall exit
asm("ecall");           // now your simulator should stop
int main() {

  int a = 1;
  int b = 2;

  return a+b;
}