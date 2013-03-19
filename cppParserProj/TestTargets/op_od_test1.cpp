/*
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

int Foo::Bar3()
{
	int a = 0, b = 1, c = 2;
	if(a < b)
	{
	
	}
	
	if(a <= b)
	{
	
	}
	
	if(a > b)
	{
	
	}
	
	if(a >= b)
	{
	
	}
	
	if(a == b)
	{
	
	}
	
	a << b << c;
	a >> b;
}


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