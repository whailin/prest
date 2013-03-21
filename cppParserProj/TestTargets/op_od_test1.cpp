void Foo::Operators()
{
	// Basic comparison operators
	if(a1 < b1) {}				// OP OK, OD OK
	if(a2 <= b2) {}				// OP OK, OD OK
	if(a3 > b3) {}				// OP OK, OD OK
	if(a4 >= b4) {}				// OP OK, OD OK
	if(a5 == b5) {}				// OP OK, OD OK

	// Bit shift operators
	a6 << b6; // Single				// OP OK, OD OK
	a7 << b7 << c7; // Multiple		// OP OK, OD OK
	a8 >> b8; // Single				// OP OK, OD OK
	a9 >> b9 >> c9; // Multiple		// OP OK, OD OK
	
	// Arithmetic operators
	a10 = b10 + c10;			// OP OK, OD OK
	a11 = b11 - c11;			// OP OK, OD OK
	a12 = b12 * c12;			// OP OK, OD OK
	a13 = b13 / c13;			// OP OK, OD OK
	a14 = b14 % c14;			// OP OK, OD OK
	
	// Unary plus and minus
	a15 = +b15;	// OP OK, OD OK
	a16 = -b16;// OP OK, OD OK
	a17 = b17 - +c17; // OP OK, OD OK
	a18 = b18 + -c18; // OP OK, OD OK
	
	// Pre- and post-increment / -decrement
	a19 = b19++; // OP OK, OD OK
	a20 = b20--; // OP OK, OD OK
	a21 = ++b21; // OD OK
	a22 = --b22; // OD OK
	
	// Logical operators (default cases)
	if(a23) {} // unary true-false					// OP OK, OD OK
	if(!a24) {} // NOT								// OP OK, OD OK
	if(a25 && b25) {} // AND						// OP OK, OD OK
	if(a26 || b26) {} // OR							// OP OK, OD OK
	if(!a27 && !b27) {} // NOT and AND				// OP OK, OD OK
	
	// Logical operators (non-symbol cases)
	if(not a28) {} // NOT  							// OD OK
	if(a29 and b29) {} // AND						
	if(a30 or b30) {} // OR							
	
	// Bitwise logical operators
	if(~a31) {} // Bitwise NOT						// OD OK
	if(a32 & b32) {} // Bitwise AND					// OP OK
	if(a33 | b33) {} // Bitwise OR					// OP OK, OD OK
	if(a34 ^ b34) {} // Bitwise XOR					// OP OK, OD OK
	
	// Compound assignment operators
	a35 += b35;		// OP OK, OD OK
	a36 -= b36;		// OP OK, OD OK
	a37 *= b37;		// OP OK
	a38 /= b38;		// OP OK, OD OK
	a39 %= b39;		// OP OK, OD OK
	a40 &= b40;		// OP OK
	a41 |= b41;		// OP OK, OD OK
	a42 ^= b42;		// OP OK, OD OK
	a43 <<= b43;	// OP OK, OD OK
	a44 >>= b44;	// OP OK, OD OK
	
	// Array subscript
	a45 = b45[0];
	a46 = b46[c46];
	a47[0] = b47;
	a48[b48] = c48;
	a49[b49] = c49[d49];
	
	// Function call and comma
	doSomething(a50, b50); // OP OK
	
	// Scope resolution
	SomeScope::doSomething();
	
	// Sizeof
	a51 = sizeof(b51);
}