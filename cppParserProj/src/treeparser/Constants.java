

package treeparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Tomi
 */
public class Constants {
    public static final String specialChars="/*-+";
    public static final String[] keywords={
        "alignas", // C++11
        "alignof", // C++11
        "and",
        "and_eq",
        "asm",
        "auto",
        "bitand",
        "bitor",
        "bool",
        "break",
        "case",
        "catch",
        "char",
        "char16_t", // C++11
        "char32_t",
        "class",
        "compl",
        "const",
        "constexpr", //C++11
        "const_cast",
        "continue",
        "decltype", //C++11
        "default",
        "delete",
        "do",
        "double",
        "dynamic_cast",
        "else",
        "enum",
        "explicit",
        "export",
        "extern",
        "false",
        "float",
        "for",
        "friend",
        "goto",
        "if",
        "inline",
        "int",
        "long",
        "mutable",
        "namespace",
        "new",
        "noexcept", //C++11
        "not",
        "not_eq",
        "nullptr", //C++11
        "operator",
        "or",
        "or_eq",
        "private",
        "protected",
        "public",
        "register",
        "reinterpret_cast",
        "return",
        "short",
        "signed",
        "sizeof",
        "static",
        "static_assert", //C++11
        "static_cast",
        "struct",
        "switch",
        "template",
        "this",
        "thread_local", //C++11
        "throw",
        "true",
        "try",
        "typedef",
        "typeid",
        "typename",
        "union",
        "unsigned",
        "using",
        "virtual",
        "void",
        "volatile",
        "wchar_t",
        "while",
        "xor",
        "xor_eq"
    };
    
     public static final String[] twoCharOperators={
         "::",
         "+=", 
         "-=", 
         "*=",
         "/=",
         "%=",
         "^=",
         "&=",
         "|=",
         "<<",
         ">>",
         "==",
         "!=",
         "<=",
         ">=",
         "&&",
         "||",
         "++",
         "--",
         "->"
     };
     
     public static final String[] threeCharOperator={};
    
    public static final List<String> l=new ArrayList<String>(Arrays.asList(keywords));
    
    public static boolean isTwoCharOperator(String str){
        int l=twoCharOperators.length;
        for(int i=0;i<l;i++){
            if(twoCharOperators[i].contentEquals(str))
                return true;
        }
        return false;
    }
    public static boolean isKeyword(String str){
        int i=Collections.binarySearch(Constants.l, str);
        if(i>=0)
            return true;
        else
            return false;
    }
    /**
     * Can the given character be used in C++ variable name?
     * Returns true for letters a-z, A-Z, 0-9 and _
     * @param c
     * @return 
     */
    public static boolean isValidNameChar(char c){
        if(c>='a' && c<='z')
            return true;
        else if(c>='A' && c<='Z')
            return true;
        else if(c>='0'&&c<='9')
            return true;
        else if(c=='_')
            return true;
        return false;
    }
    
    public static boolean isSpecialChar(char c){
        if(((int)c)>=33 &&((int)c)<=47) //these are:!"#$%&'()*+,-./
            return true;
        else if(((int)c)>=58 &&((int)c)<=64) //:;<=>?
            return true;
        else
            return false;
        
    }
}
