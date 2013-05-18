/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cppParser.utils.parameter;

/**
 * 
 * @author Tomi
 */
public interface ParameterToken
{
	@Override
	public String toString();

	public boolean isFunctionCall();
}
