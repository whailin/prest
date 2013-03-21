
int Foo::Bar()
{
	int a;
	int b;
	
	int c = a + b;
}

int Foo::Bar2()
{
	int a = 1, b = 2, c = 3;
	
	for(int i = 0; i < 10; ++i)
	{
		a++;
		b--;
		--c;
	}
}

void Foo::Operators()
{
	// Basic comparison operators
	if(a1 < b1) {}				// OK
	if(a2 <= b2) {}				// OK
	if(a3 > b3) {}				// OK
	if(a4 >= b4) {}				// OK
	if(a5 == b5) {}				// OK

	// Bit shift operators
	a6 << b6; // Single				// OK
	a7 << b7 << c7; // Multiple		// OK
	a8 >> b8; // Single				// OK
	a9 >> b9 >> c9; // Multiple		// OK
	
	// Arithmetic operators
	a10 = b10 + c10;			// OK
	a11 = b11 - c11;			// OK
	a12 = b12 * c12;			// OK
	a13 = b13 / c13;			// OK
	a14 = b14 % c14;			// OK
	
	// Unary plus and minus
	a15 = +b15;
	a16 = -b16;
	a17 = b17 - +c17;
	a18 = b18 + -c18;
	
	// Pre- and post-increment / -decrement
	a19 = b19++;
	a20 = b20--;
	a21 = ++b21;
	a22 = --b22;
	
	// Logical operators (default cases)
	if(a23) {} // unary true-false
	if(!a24) {} // NOT
	if(a25 && b25) {} // AND						// OK
	if(a26 || b26) {} // OR							// OK
	if(!a27 && !b27) {} // NOT and AND
	
	// Logical operators (non-symbol cases)
	if(not a28) {} // NOT
	if(a29 and b29) {} // AND
	if(a30 or b30) {} // OR
	
	// Bitwise logical operators
	if(~a31) {} // Bitwise NOT
	if(a32 & b32) {} // Bitwise AND					// OK
	if(a33 | b33) {} // Bitwise OR					// OK
	if(a34 ^ b34) {} // Bitwise XOR					// OK
	
	// Compound assignment operators
	a35 += b35;
	a36 -= b36;
	a37 *= b37;
	a38 /= b38;
	a39 %= b39;
	a40 &= b40;
	a41 |= b41;
	a42 ^= b42;
	a43 <<= b43;
	a44 >>= b44;
	
	// Array subscript
	a45 = b45[0];
	a46 = b46[c46];
	a47[0] = b47;
	a48[b48] = c48;
	a49[b49] = c49[d49];
	
	// Function call and comma
	doSomething(a50, b50);
	
	// Scope resolution
	SomeScope::doSomething();
	
	// Sizeof
	a51 = sizeof(b51);
}

/*
TextureUnitState::TextureAddressingMode convTexAddressMode(const String& params, MaterialScriptContext& context)
{
	if (params=="wrap")
		return TextureUnitState::TAM_WRAP;
	else if (params=="mirror")
		return TextureUnitState::TAM_MIRROR;
	else if (params=="clamp")
		return TextureUnitState::TAM_CLAMP;
	else if (params=="border")
		return TextureUnitState::TAM_BORDER;
	else
		logParseError("Bad tex_address_mode attribute, valid parameters are "
			"'wrap', 'mirror', 'clamp' or 'border'.", context);
	// default
	return TextureUnitState::TAM_WRAP;
}
*/

/*
void Foo::processAutoProgramParam(bool isNamed, const String& commandname,
        StringVector& vecparams, MaterialScriptContext& context,
		size_t index = 0, const String& paramName = StringUtil::BLANK)
    {
        // NB we assume that the first element of vecparams is taken up with either
        // the index or the parameter name, which we ignore

        // make sure param is in lower case
        StringUtil::toLowerCase(vecparams[1]);

        // lookup the param to see if its a valid auto constant
        const GpuProgramParameters::AutoConstantDefinition* autoConstantDef =
            context.programParams->getAutoConstantDefinition(vecparams[1]);

        // exit with error msg if the auto constant definition wasn't found
        if (!autoConstantDef)
		{
			logParseError("Invalid " + commandname + " attribute - "
				+ vecparams[1], context);
			return;
		}

        // add AutoConstant based on the type of data it uses
        switch (autoConstantDef->dataType)
        {
        case GpuProgramParameters::ACDT_NONE:
			if (isNamed)
				context.programParams->setNamedAutoConstant(paramName, autoConstantDef->acType, 0);
			else
	            context.programParams->setAutoConstant(index, autoConstantDef->acType, 0);
            break;

        case GpuProgramParameters::ACDT_INT:
            {
				// Special case animation_parametric, we need to keep track of number of times used
				if (autoConstantDef->acType == GpuProgramParameters::ACT_ANIMATION_PARAMETRIC)
				{
					if (isNamed)
						context.programParams->setNamedAutoConstant(
							paramName, autoConstantDef->acType, context.numAnimationParametrics++);
					else
						context.programParams->setAutoConstant(
							index, autoConstantDef->acType, context.numAnimationParametrics++);
				}
				// Special case texture projector - assume 0 if data not specified
				else if ((autoConstantDef->acType == GpuProgramParameters::ACT_TEXTURE_VIEWPROJ_MATRIX ||
						autoConstantDef->acType == GpuProgramParameters::ACT_TEXTURE_WORLDVIEWPROJ_MATRIX ||
						autoConstantDef->acType == GpuProgramParameters::ACT_SPOTLIGHT_VIEWPROJ_MATRIX ||
						autoConstantDef->acType == GpuProgramParameters::ACT_SPOTLIGHT_WORLDVIEWPROJ_MATRIX)
					&& vecparams.size() == 2)
				{
					if (isNamed)
						context.programParams->setNamedAutoConstant(
							paramName, autoConstantDef->acType, 0);
					else
						context.programParams->setAutoConstant(
							index, autoConstantDef->acType, 0);

				}
				else
				{

					if (vecparams.size() != 3)
					{
						logParseError("Invalid " + commandname + " attribute - "
							"expected 3 parameters.", context);
						return;
					}

					size_t extraParam = StringConverter::parseInt(vecparams[2]);
					if (isNamed)
						context.programParams->setNamedAutoConstant(
							paramName, autoConstantDef->acType, extraParam);
					else
						context.programParams->setAutoConstant(
							index, autoConstantDef->acType, extraParam);
				}
            }
            break;

        case GpuProgramParameters::ACDT_REAL:
            {
                // special handling for time
                if (autoConstantDef->acType == GpuProgramParameters::ACT_TIME ||
                    autoConstantDef->acType == GpuProgramParameters::ACT_FRAME_TIME)
                {
                    Real factor = 1.0f;
                    if (vecparams.size() == 3)
                    {
                        factor = StringConverter::parseReal(vecparams[2]);
                    }

					if (isNamed)
						context.programParams->setNamedAutoConstantReal(paramName, 
							autoConstantDef->acType, factor);
					else
	                    context.programParams->setAutoConstantReal(index, 
							autoConstantDef->acType, factor);
                }
                else // normal processing for auto constants that take an extra real value
                {
                    if (vecparams.size() != 3)
                    {
                        logParseError("Invalid " + commandname + " attribute - "
                            "expected 3 parameters.", context);
                        return;
                    }

			        Real rData = StringConverter::parseReal(vecparams[2]);
					if (isNamed)
						context.programParams->setNamedAutoConstantReal(paramName, 
							autoConstantDef->acType, rData);
					else
						context.programParams->setAutoConstantReal(index, 
							autoConstantDef->acType, rData);
                }
            }
            break;

        } // end switch


    }
	*/