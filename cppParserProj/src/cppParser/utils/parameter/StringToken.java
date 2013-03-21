

package cppParser.utils.parameter;

/**
 *
 * @author Tomi
 */
public class StringToken implements ParameterToken{
    public StringToken(String content){
        token=content;
    }
    public String token;
    
    public String toString(){
        return token;
    }
    
}
