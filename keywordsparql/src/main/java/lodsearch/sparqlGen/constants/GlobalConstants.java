package lodsearch.sparqlGen.constants;

import java.util.HashMap;
import java.util.Map;

public class GlobalConstants {
	public static final String LOD_SPARQL_ENDPOINT = "http://lod.openlinksw.com/sparql/";	
	public static final String DBPEDIA_SPARQL_ENDPOINT = "http://dbpedia.org/sparql/";
	public static final String SPARQL_ENDPOINT = DBPEDIA_SPARQL_ENDPOINT;
	public static final String DBPEDIA_GRAPH_IRI = "http://dbpedia.org";
	public static final Map<String,String> prefixes = Map.of(
		    "rdfs", "http://www.w3.org/2000/01/rdf-schema#",
		    "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
		    "bif","bif:"
		);
	

}
