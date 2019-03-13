package lodsearch.sparqlGen;


import java.util.*;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

import lodsearch.response.Response;
import lodsearch.sparqlGen.queryRes.QueryResult;
import lodsearch.sparqlGen.subgraph.Subgraph;
import lodsearch.sparqlGen.termRdfMapper.TermMapper;
import lodsearch.spotlightner.SpotlightClient;
import lodsearch.utils.GeneralUtils;

public class SparqlRunner {

	public static LinkedHashMap<String, String[]> queryKeywordVecsMap;
	public LinkedHashMap<String, List<QueryResult>> termToRdfMapping = new LinkedHashMap<String, List<QueryResult>>();
	public String query;
	
	public Response search(String query, LinkedHashMap<String, String[]> keywordVecs) {
		this.query = query;
		// stop words removal
		String noStopWordsQuery = GeneralUtils.removeStopWords(query);
		queryKeywordVecsMap = GeneralUtils.removeStopWordEntriesFromMap(noStopWordsQuery, keywordVecs);
		TermMapper t = new TermMapper();
		termToRdfMapping = t.obtainRdfMappings(query,queryKeywordVecsMap);
		Response response = Subgraph.formSubgraphs(termToRdfMapping);
		return response;
	}

	public static void main(String[] args) {
		System.out.println("SparqlRunner.main()");
	}

}
