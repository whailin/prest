

package cppParser.utils;

/**
 * This class is responsible for counting physical loc metrics. Logical lines of
 * code are counted in separate class
 * @author Tomi
 */
public class PLOCCounter {
    private static final int DEFAULT=0, COMMENT=1, LITERAL=2;
    private int mode=DEFAULT;
    public int codeLines=0;
	public int emptyLines = 0;
    public int commentedCodeLines = 0;
	public int commentOnlyLines = 0;		//comment lines
    public int preProcessorDirectives=0;

    public char last=' ';
    private String line="";
    private boolean inCommentBlock=false;
    private boolean codeFound=false, commentFound=false;
    private boolean foundSlash=false;
    
    public void push(char c){
        //System.out.print(c);
        
        switch(mode){
            case COMMENT:
                pushComment(c);
                last=c;
                return;
            case LITERAL:
                pushLiteral(c);
                last=c;
                return;
        }
        if(c!='\n' && c!='\r')
            line+=c;
        boolean justFoundComment=false;
        switch(c){
            case ' ':
            case '\r':
            case '\t':
                last=c;
                return;
            case '\n':
                if(foundSlash) codeFound=true;
                last=c;                
                countLine();
                return;
            case '*':
                if(last=='/'){
                    mode=COMMENT;
                    commentFound=true;
                    justFoundComment=true;
                    inCommentBlock=true;
                }
                break;
            case '/':
                if(last=='/'){
                    mode=COMMENT;
                    commentFound=true;
                    foundSlash=false;
                    justFoundComment=true;
                }else
                    foundSlash=true;
                break;
            case '"':
                if(last!='\'' && last!='\\')
                    mode=LITERAL;
        }
        if(c!='/'&& !justFoundComment) codeFound=true;
        last=c;
    }
    private void pushComment(char c) {
        switch(c){
            case ' ':
            case '\r':
            case '\t':
            case '\n':
                break;
            default:
                commentFound=true;
        }
        
        if(inCommentBlock){
            if(c=='\n'){
                countLine();
            }
            if(last=='*'&& c=='/'){
                inCommentBlock=false;
                mode=DEFAULT;
            }
        }else{
            if(c=='\n'){
                countLine();
                if(last!='\\') //Check if last letter is \ to see if //-comment goes to another line
                    mode=DEFAULT;
            }
        }
    }
    private void pushLiteral(char c) {
        codeFound=true;
        if(c=='"'){
            if(last!='\\')
                mode=DEFAULT;
        }else if (c=='\n'){
            if(last!='\\')
                mode=DEFAULT;
        }
    }
    
    
    private void countLine()
	{
            if(!line.isEmpty())
                if(line.charAt(0)=='#')
                    preProcessorDirectives++;
			if(codeFound)
			{
	            if(commentFound) commentedCodeLines++;
				else codeLines++;
			}
			else
			{
				if(commentFound) commentOnlyLines++;
				else emptyLines++;
			}
            
            commentFound=false;
            codeFound=false;
            foundSlash=false;
            line="";
	}

    

    
}
