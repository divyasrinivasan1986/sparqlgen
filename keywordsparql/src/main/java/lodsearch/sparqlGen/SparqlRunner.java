package lodsearch.sparqlGen;


import java.util.*;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

import lodsearch.sparqlGen.queryRes.QueryResult;
import lodsearch.sparqlGen.termRdfMapper.TermMapper;
import lodsearch.spotlightner.SpotlightClient;
import lodsearch.utils.GeneralUtils;

public class SparqlRunner {

	public static LinkedHashMap<String, String[]> queryKeywordVecsMap;
	public static LinkedHashMap<String, List<QueryResult>> termToRdfMapping = new LinkedHashMap<String, List<QueryResult>>();
	public String query;
	
	public void search(String query, LinkedHashMap<String, String[]> keywordVecs) {
		this.query = query;
		// stop words removal
		String noStopWordsQuery = GeneralUtils.removeStopWords(query);
		queryKeywordVecsMap = GeneralUtils.removeStopWordEntriesFromMap(noStopWordsQuery, keywordVecs);
		termToRdfMapping = TermMapper.obtainRdfMappings(query,queryKeywordVecsMap);
	}

	public static void main(String[] args) {
		System.out.println("SparqlRunner.main()");
	}

}
