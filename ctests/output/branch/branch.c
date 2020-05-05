asm("addi sp, sp, 0x100"); // SP set to 256
asm("jal main");        // call main
asm("mv a1, a0");       // save return value in a1

int main() 
{
    int x = 1;
    int y = 2;
    int z, q = 0;
    int j = 3;
    int k = 3;
    if ((x==y) || (j>k))
        z=1;
    else
        q=10;
    return z;
}