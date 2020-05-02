asm("addi sp, sp, 0x100"); // SP set to 256
asm("jal main");        // call main
asm("mv a1, a0");       // save return value in a1
asm("lui a0, 10");       // prepare ecall exit
asm("ecall");           // now your simulator should stop

int fib(int n) 
{ 
    if (n <= 1) 
        return n; 
    return fib(n - 1) + fib(n - 2); 
} 

int main()    
{    
    int n = 4;
    fib(n);
    return 0;
}