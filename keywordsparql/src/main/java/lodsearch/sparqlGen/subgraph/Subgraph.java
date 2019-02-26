package lodsearch.sparqlGen.subgraph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import lodsearch.sparqlGen.queryRes.QueryResult;

public class Subgraph {
	
	public static void formSubgraphs(LinkedHashMap<String, List<QueryResult>> termToRdfMapping) {
		List<String> queryTerms = new ArrayList<String>(termToRdfMapping.keySet());
		StringBuilder subgraph = new StringBuilder();
		for (int i = 0; i < queryTerms.size()-1; i++) {
			List<QueryResult> firstTermUris = termToRdfMapping.get(queryTerms.get(i));
			List<QueryResult> secondTermUris = termToRdfMapping.get(queryTerms.get(i+1));
			for(QueryResult firsttermUri : firstTermUris) {
				for(QueryResult secondTermUri : secondTermUris) {
					subgraph = subgraph.append(checkUriTypeAndFormSubgraph(firsttermUri,secondTermUri));
				}
			}

		}
		//subgraph will be spanning subgraph here
	}
	private static String checkUriTypeAndFormSubgraph(QueryResult firsttermUri, QueryResult secondTermUri) {
		String subgraph = null;
		//need a mechanism to backtrack if results not found
		if(firsttermUri.getType().contains("resource") && secondTermUri.getType().contains("resource")) {
			subgraph = formResResSubgraph(firsttermUri,secondTermUri);
		}
		else if(firsttermUri.getType().contains("ontology") && secondTermUri.getType().contains("ontology")) {
			subgraph = formOntOntSubgraph(firsttermUri,secondTermUri);
		}
		else if(firsttermUri.getType().contains("property") && secondTermUri.getType().contains("property")) {
			subgraph = formPropPropSubgraph(firsttermUri,secondTermUri);
		}
		else if((firsttermUri.getType().contains("resource") && secondTermUri.getType().contains("ontology")) ||
				(firsttermUri.getType().contains("ontology") && secondTermUri.getType().contains("resource")) ) {
			subgraph = formResOntSubgraph(firsttermUri,secondTermUri);
		}
		else if((firsttermUri.getType().contains("resource") && secondTermUri.getType().contains("property")) ||
				(firsttermUri.getType().contains("property") && secondTermUri.getType().contains("resource")) ) {
			subgraph = formResPropSubgraph(firsttermUri,secondTermUri);
		}
		else if((firsttermUri.getType().contains("ontology") && secondTermUri.getType().contains("property")) ||
				(firsttermUri.getType().contains("property") && secondTermUri.getType().contains("ontology")) ) {
			subgraph = formOntPropSubgraph(firsttermUri,secondTermUri);
		}
		return subgraph;
	}
	private static String formOntPropSubgraph(QueryResult firsttermUri, QueryResult secondTermUri) {
		// TODO Auto-generated method stub
		return null;
	}
	private static String formResPropSubgraph(QueryResult firsttermUri, QueryResult secondTermUri) {
		// TODO Auto-generated method stub
		return null;
	}
	private static String formResOntSubgraph(QueryResult firsttermUri, QueryResult secondTermUri) {
		// TODO Auto-generated method stub
		return null;
	}
	private static String formPropPropSubgraph(QueryResult firsttermUri, QueryResult secondTermUri) {
		// TODO Auto-generated method stub
		return null;
	}
	private static String formOntOntSubgraph(QueryResult firsttermUri, QueryResult secondTermUri) {
		// TODO Auto-generated method stub
		return null;
	}
	private static String formResResSubgraph(QueryResult firsttermUri, QueryResult secondTermUri) {
		StringBuilder subgraph = new StringBuilder();
		String query1 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "PREFIX bif:<bif:> \n"
				+ "select count(?p) where {\n" 
				+ "<"+firsttermUri.getMappingURI()+"> ?p <"+secondTermUri.getMappingURI()+">  \n"
				+ "} ";
		String query2 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "PREFIX bif:<bif:> \n"
				+ "select count(?p) where {\n" 
				+ "<"+secondTermUri.getMappingURI()+"> ?p <"+firsttermUri.getMappingURI()+">  \n"
				+ "} ";
		QueryExecution queryRes = QueryExecutionFactory.sparqlService("http://lod.openlinksw.com/sparql/",
				query1, "http://dbpedia.org");
		ResultSet res1 = queryRes.execSelect();
		while (res1.hasNext()) {
			QuerySolution nextSolution = res1.nextSolution();
		}
		queryRes = QueryExecutionFactory.sparqlService("http://lod.openlinksw.com/sparql/",
				query1, "http://dbpedia.org");
		ResultSet res2 = queryRes.execSelect();
		while (res2.hasNext()) {
			QuerySolution nextSolution = res2.nextSolution();
		}
		//compare res1 res2 results. keep higher one
		if(1>2) {
			subgraph=subgraph.append(query1);
		}
		else if(2>1){
			subgraph=subgraph.append(query2);
		}
		else {//if both zero
			subgraph=subgraph.append("");
		}
		return subgraph.toString();
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
