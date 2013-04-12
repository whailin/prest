#define MULTIPLY(x, y) (x) * (y)

// Test for simple macro expansion
void Foo::Bar()
{
	int a = 3;
	int b = 5;
	int c = MULTIPLY(a, b);
}

#define SIZE 1024
#define SIZE_W 128
#define SIZE_H 256

#define SIZE_X ((SIZE_W) + (SIZE_H))

void Foo::Bar2()
{
	int s = SIZE;
	int w = SIZE_W * SIZE_H;
	int result = MULTIPLY(SIZE_W, SIZE_H);
	
	int x = SIZE_X;
}