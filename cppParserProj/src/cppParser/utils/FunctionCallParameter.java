

package cppParser.utils;

/**
 *
 * @author Tomi
 */
public class FunctionCallParameter {
    public static final int VARIABLE=1, FUNCTIONCALL=1;
    
    public int parameterType;
    public String name;
    public String type;
    public boolean primitive;

}
