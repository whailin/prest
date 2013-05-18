package cppMetrics;

public class LOCMetrics implements Metrics
{
	public String file;
	public int commentLines = 0, commentedCodeLines = 0, codeOnlyLines = 0,
			emptyLines = 0, logicalLOC = 0;

	@Override
	public void calculateMetrics()
	{
		// TODO Auto-generated method stub
	}

	public LOCMetrics()
	{
	}

	public LOCMetrics(String file, int commentLines, int commentedCodeLines,
			int codeOnlyLines, int emptyLines, int logicalLOC)
	{
		this.file = file;
		this.commentLines = commentLines;
		this.commentedCodeLines = commentedCodeLines;
		this.codeOnlyLines = codeOnlyLines;
		this.emptyLines = emptyLines;
		this.logicalLOC = logicalLOC;
	}

	@Override
	public Result getResults()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
