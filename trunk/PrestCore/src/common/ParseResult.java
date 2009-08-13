/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import categorizer.core.DataSet;
import parser.enumeration.Language;

/**
 * 
 * @author Gürhan
 */
public class ParseResult {

    private Language parserLanguage;
    private ParseMetricGroups parseMetricGroups;
    private ParseDataSets parseDataSets;

    public ParseResult() {
    }

    public ParseResult(Language language, DataContext collectedMetrics) {
        if (language != null && collectedMetrics != null) {
            parserLanguage = language;
            parseMetricGroups = null;
            parseDataSets = null;
            parseMetricGroups = new ParseMetricGroups(collectedMetrics);
            collectedMetrics = null;
            parseDataSets = new ParseDataSets(parseMetricGroups);
            parseMetricGroups = null;
        }
    }

    public ParseDataSets getParseDataSets() {
        return this.parseDataSets;
    }

    public void setDataSetByMetricType(int metricType, DataSet ds) {
        this.parseDataSets.setByMetricTypes(metricType, ds);
    }

    public void setParseDataSets(ParseDataSets parseDataSets) {
        this.parseDataSets = parseDataSets;
    }

    public ParseMetricGroups getParseMetricGroups() {
        return this.parseMetricGroups;
    }

    public void setParseMetricGroups(ParseMetricGroups parseMetricGroups) {
        this.parseMetricGroups = parseMetricGroups;
    }

    public Language getParserLanguage() {
        return this.parserLanguage;
    }

    public void setParserLanguage(Language parserLanguage) {
        this.parserLanguage = parserLanguage;
    }

    public DataSet getDataSetByMetricType(int metricType) {
        return this.parseDataSets.getByMetricType(metricType);
    }
}
