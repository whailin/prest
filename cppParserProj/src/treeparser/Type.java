

package treeparser;

/**
 * Different types for recognizing different types of parsed objects
 * @author Tomi
 */
public enum Type {
    STRING, BRACKET, PREPROCESSOR, OTHER, ROOT, COMMENT, 
    CHARLITERAL, NEWLINE, KEYWORD, SENTENCE, SIMPLESENTENCE, SEMICOLON, 
    WORDTOKEN, UNKNOWN, SPECIAL;

}
