//When testing these functions starting with w should not be recognized
Test::doSomething()
{
	//Variable
	int w1();
	
	//typecasts
	int(w2);
	(int)w3;
	
	a1();
	a2(2);
	a3(2,2);
	
	//Strings as parameter
	a4("hello");
	a5(")");
	a6("w2()");
	
	//Function call as parameter
	a6(b6());
	a7(b7(c7()),d7());
	
	// ::, ->, and . tokens
	
	Namespace1::a7();
	Namespace1::Namespace2::a8();
	
	Struct1.a9();
	myObj->a10();
	myObj->a11()->a12();
	
	//Other
	if(a13());
	if(0+a14());
	if(2*a15();
	for(int w4=a16();w4<b16();w4++);
	
	//assignment
	int w5=a17();
	
	
	
	
}