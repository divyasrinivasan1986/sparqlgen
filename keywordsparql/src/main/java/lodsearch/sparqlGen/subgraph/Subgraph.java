package lodsearch.sparqlGen.subgraph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.lang.sparql_11.ParseException;

import lodsearch.sparqlGen.constants.GlobalConstants;
import lodsearch.sparqlGen.queryRes.QueryResult;

public class Subgraph {
	private static SelectBuilder subgraph = new SelectBuilder();
	private static List<SelectBuilder> spanningSubgraphs = new ArrayList<SelectBuilder>();
	static {
		subgraph.addPrefixes(GlobalConstants.prefixes);
	}
	public static void formSubgraphs(LinkedHashMap<String, List<QueryResult>> termToRdfMapping) {
		List<String> queryTerms = new ArrayList<String>(termToRdfMapping.keySet());
		List<SelectBuilder> subgraphsList = new ArrayList<SelectBuilder>();
		for (int i = 0; i < queryTerms.size()-1; i++) {
			List<QueryResult> firstTermUris = termToRdfMapping.get(queryTerms.get(i));
			List<QueryResult> secondTermUris = termToRdfMapping.get(queryTerms.get(i+1));
			List<SelectBuilder> newSubgraphsList = new ArrayList<SelectBuilder>();
			for(QueryResult firsttermUri : firstTermUris) {
				for(QueryResult secondTermUri : secondTermUris) {
					if(!subgraphsList.isEmpty()) {
						for(SelectBuilder sb:subgraphsList) {
							SelectBuilder subgraph = checkUriTypeAndFormSubgraph(firsttermUri,secondTermUri,i,sb);
							newSubgraphsList.add(subgraph);
						}
					}
					else {
						SelectBuilder subgraph = checkUriTypeAndFormSubgraph(firsttermUri,secondTermUri,i,new SelectBuilder());
						subgraphsList.add(subgraph);
					}
					
				}
			}
			if(!newSubgraphsList.isEmpty())
				subgraphsList = newSubgraphsList;
		}
		spanningSubgraphs = subgraphsList;
		//subgraph will be spanning subgraph here
		for(SelectBuilder sb:spanningSubgraphs) {
			QueryExecution queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT,
					subgraph.buildString(), GlobalConstants.DBPEDIA_GRAPH_IRI);
			ResultSet res1 = queryRes.execSelect();
			while (res1.hasNext()) {
				QuerySolution nextSolution = res1.nextSolution();
			}
		}
		
	}
	private static SelectBuilder checkUriTypeAndFormSubgraph(QueryResult firsttermUri, QueryResult secondTermUri,int index, SelectBuilder sb2) {
		SelectBuilder sb = new SelectBuilder();
		//need a mechanism to backtrack if results not found
		if(firsttermUri.getType().contains("resource") && secondTermUri.getType().contains("resource")) {
			sb = formResResSubgraph(firsttermUri,secondTermUri,index);
		}
		else if(firsttermUri.getType().contains("ontology") && secondTermUri.getType().contains("ontology")) {
			sb = formOntOntSubgraph(firsttermUri,secondTermUri,index);
		}
		else if(firsttermUri.getType().contains("property") && secondTermUri.getType().contains("property")) {
			sb = formPropPropSubgraph(firsttermUri,secondTermUri,index);
		}
		else if((firsttermUri.getType().contains("resource") && secondTermUri.getType().contains("ontology")) ) {
			sb = formResOntSubgraph(firsttermUri,secondTermUri,index);
		}
		else if((firsttermUri.getType().contains("ontology") && secondTermUri.getType().contains("resource"))) {
			sb = formResOntSubgraph(secondTermUri,firsttermUri,index);
		}
		else if((firsttermUri.getType().contains("resource") && secondTermUri.getType().contains("property"))) {
			sb = formResPropSubgraph(firsttermUri,secondTermUri,index);
		}
		else if((firsttermUri.getType().contains("property") && secondTermUri.getType().contains("resource"))) {
			sb = formResPropSubgraph(secondTermUri,firsttermUri,index);
		}
		else if((firsttermUri.getType().contains("ontology") && secondTermUri.getType().contains("property"))) {
			sb = formOntPropSubgraph(firsttermUri,secondTermUri,index);
		}
		else if((firsttermUri.getType().contains("property") && secondTermUri.getType().contains("ontology"))) {
			sb = formOntPropSubgraph(secondTermUri,firsttermUri,index);
		}
		
		return sb;
	}
	private static SelectBuilder formOntPropSubgraph(QueryResult firsttermUri, QueryResult secondTermUri,int index) {
		SelectBuilder sb1 = new SelectBuilder();
		SelectBuilder sb2 = new SelectBuilder();
		SelectBuilder finalBuilder = new SelectBuilder();
		try {
			sb1.addPrefixes(GlobalConstants.prefixes);
			sb1.addWhere("?res1", "rdf:type", "<"+firsttermUri.getMappingURI()+">");
			sb1.addWhere("?res1", "<"+secondTermUri.getMappingURI()+">","?res2" );
			sb1.addVar("count(*)", "?c");
			
			sb2.addPrefixes(GlobalConstants.prefixes);
			sb2.addWhere("?res1", "rdf:type", "<"+firsttermUri.getMappingURI()+">");
			sb2.addWhere("?res2", "<"+secondTermUri.getMappingURI()+">","?res1" );
			sb2.addVar("count(*)", "?c");
		

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		QueryExecution queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT,
				sb1.buildString(), GlobalConstants.DBPEDIA_GRAPH_IRI);
		ResultSet res1 = queryRes.execSelect();
		
		queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT,
				sb2.buildString(), GlobalConstants.DBPEDIA_GRAPH_IRI);
		ResultSet res2 = queryRes.execSelect();
		
	
		int count2 = res2.nextSolution().get("c").asLiteral().getInt();
		//compare res1 res2 results. keep higher one
		int count1 = res1.nextSolution().get("c").asLiteral().getInt();
		if(count1 > count2) {
			finalBuilder = sb1;
		}
		else if(count2 > count1) {
			finalBuilder = sb2;
		}
		if(count1==0 && count2==0) {
			return finalBuilder;
		}
		else {
			finalBuilder.addVar("?lbl1").addVar("?lbl2");
			finalBuilder.addWhere("?res1","rdfs:label","?lbl1");
			finalBuilder.addWhere("?res2","rdfs:label","?lbl2");
		}
		
		return finalBuilder;

	}
	private static SelectBuilder formResPropSubgraph(QueryResult firsttermUri, QueryResult secondTermUri,int index) {
		SelectBuilder sb1 = new SelectBuilder();
		SelectBuilder sb2 = new SelectBuilder();
		SelectBuilder finalBuilder = new SelectBuilder();
		try {
			sb1.addVar("count(*)", "?p").addVar("?res1").addWhere("<"+firsttermUri.getMappingURI()+">", "<"+secondTermUri.getMappingURI()+">","?res1" );
			sb2.addVar("count(*)", "?p").addVar("?res1").addWhere("?res1", "<"+secondTermUri.getMappingURI()+">","<"+firsttermUri.getMappingURI()+">");

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		QueryExecution queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT,
				sb1.buildString(), GlobalConstants.DBPEDIA_GRAPH_IRI);
		ResultSet res1 = queryRes.execSelect();
		
		queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT,
				sb2.buildString(), GlobalConstants.DBPEDIA_GRAPH_IRI);
		ResultSet res2 = queryRes.execSelect();
		
		//compare res1 res2 results. keep higher one
		int count1 = res1.nextSolution().get("p").asLiteral().getInt();
		int count2 = res2.nextSolution().get("p").asLiteral().getInt();
		if(count1 > count2) {
			
			finalBuilder = sb1;
		}
		else if(count2 > count1){
			finalBuilder = sb2;
		}
		if(count1 == 0 && count2==0) {
			return finalBuilder;
		}
		else {
			finalBuilder.addVar("?lbl1").addVar("?lbl3").addVar("?lbl2");
			finalBuilder.addPrefixes(GlobalConstants.prefixes);
			finalBuilder.addWhere("<"+firsttermUri.getMappingURI()+">","rdfs:label","?lbl1");
			finalBuilder.addWhere("<"+secondTermUri.getMappingURI()+">","rdfs:label","?lbl2");
			finalBuilder.addWhere("?res1","rdfs:label","?lbl3");

			return finalBuilder;
		}
		
	}
	private static SelectBuilder formResOntSubgraph(QueryResult firsttermUri, QueryResult secondTermUri,int index) {
		SelectBuilder sb1 = new SelectBuilder();
		SelectBuilder sb2 = new SelectBuilder();
		SelectBuilder sb3 = new SelectBuilder();
		SelectBuilder finalBuilder = new SelectBuilder();
		try {
			sb1.addPrefixes(GlobalConstants.prefixes);
			sb1.addWhere("<"+firsttermUri.getMappingURI()+">", "rdf:type", "<"+secondTermUri.getMappingURI()+">");			
			sb1.addVar("count(*)", "?c");
			
			sb2.addPrefixes(GlobalConstants.prefixes);
			sb2.addVar("?res1").addWhere("?res1", "rdf:type", "<"+secondTermUri.getMappingURI()+">");
			sb2.addWhere("?res1", "?p", "<"+firsttermUri.getMappingURI()+">");
			sb2.addVar("count(*)", "?c");
			
			sb3.addPrefixes(GlobalConstants.prefixes);
			sb3.addVar("?res1").addWhere("?res1", "rdf:type", "<"+secondTermUri.getMappingURI()+">");
			sb3.addWhere("<"+firsttermUri.getMappingURI()+">", "?p", "?res1");
			sb3.addVar("count(*)", "?c");

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		QueryExecution queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT,
				sb1.buildString(), GlobalConstants.DBPEDIA_GRAPH_IRI);
		ResultSet res1 = queryRes.execSelect();
		
		//compare res1 res2 results. keep higher one
		int count1 = res1.nextSolution().get("c").asLiteral().getInt();
		int count2=0;int count3=0;
		if(count1 > 0) {
			finalBuilder = sb1;
		}
		else {
			queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT,
					sb2.buildString(), GlobalConstants.DBPEDIA_GRAPH_IRI);
			ResultSet res2 = queryRes.execSelect();
			
			queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT,
					sb3.buildString(), GlobalConstants.DBPEDIA_GRAPH_IRI);
			ResultSet res3 = queryRes.execSelect();
			count2 = res2.nextSolution().get("c").asLiteral().getInt();
			count3 = res3.nextSolution().get("c").asLiteral().getInt();
			if(count1==0 && count2==0 && count3==0) {
				return finalBuilder;
			}
			else if(count2>count3 || count2==count3) {
				finalBuilder = sb2;
				finalBuilder.addVar("?lbl1").addVar("?lbl2");
				finalBuilder.addWhere("?res1","rdfs:label","?lbl1");
				finalBuilder.addWhere("?p","rdfs:label","?lbl2");
			}
			else if(count3>count2) {
				finalBuilder = sb3;
				finalBuilder.addVar("?lbl1").addVar("?lbl2");
				finalBuilder.addWhere("?res1","rdfs:label","?lbl1");
				finalBuilder.addWhere("?p","rdfs:label","?lbl2");
			}
		}
		
		return finalBuilder;

	}
	private static SelectBuilder formPropPropSubgraph(QueryResult firsttermUri, QueryResult secondTermUri,int index) {
		SelectBuilder finalBuilder = new SelectBuilder();
		return finalBuilder;
	}
	private static SelectBuilder formOntOntSubgraph(QueryResult firsttermUri, QueryResult secondTermUri,int index) {
		SelectBuilder sb1 = new SelectBuilder();
		SelectBuilder sb2 = new SelectBuilder();
		SelectBuilder finalBuilder = new SelectBuilder();
		try {
			sb1.addPrefixes(GlobalConstants.prefixes);
			sb1.addVar("?res1").addWhere("?res1", "rdf:type", "<"+firsttermUri.getMappingURI()+">");
			sb1.addVar("?res2").addWhere("?res2", "rdf:type", "<"+secondTermUri.getMappingURI()+">");
			sb1.addVar("count(*)", "?p").addWhere("<"+firsttermUri.getMappingURI()+">", "?p", "<"+secondTermUri.getMappingURI()+">");

			sb2.addPrefixes(GlobalConstants.prefixes);
			sb2.addVar("?res1").addWhere("?res1", "rdf:type", "<"+firsttermUri.getMappingURI()+">");
			sb2.addVar("?res2").addWhere("?res2", "rdf:type", "<"+secondTermUri.getMappingURI()+">");
			sb2.addVar("count(*)", "?p").addWhere("<"+secondTermUri.getMappingURI()+">", "?p", "<"+firsttermUri.getMappingURI()+">");

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		QueryExecution queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT,
				sb1.buildString(), GlobalConstants.DBPEDIA_GRAPH_IRI);
		ResultSet res1 = queryRes.execSelect();
		
		queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT,
				sb2.buildString(), GlobalConstants.DBPEDIA_GRAPH_IRI);
		ResultSet res2 = queryRes.execSelect();
		
		//compare res1 res2 results. keep higher one
		int count1 = res1.nextSolution().get("p").asLiteral().getInt();
		int count2 = res2.nextSolution().get("p").asLiteral().getInt();
		if(count1 > count2) {
			
			finalBuilder = sb1;
		}
		else if(count2 > count1){
			finalBuilder = sb2;
		}
		if(count1 == 0 && count2==0) {
			return finalBuilder;
		}
		else {
			finalBuilder.addVar("?lbl1").addVar("?lbl3").addVar("?lbl2");
			finalBuilder.addWhere("?res1","rdfs:label","?lbl1");
			finalBuilder.addWhere("?p","rdfs:label","?lbl3");
			finalBuilder.addWhere("?res2","rdfs:label","?lbl2");
			return finalBuilder;
		}
	
	}
	private static SelectBuilder formResResSubgraph(QueryResult firsttermUri, QueryResult secondTermUri,int index) {
		SelectBuilder sb1 = new SelectBuilder();
		SelectBuilder sb2 = new SelectBuilder();
		SelectBuilder finalBuilder = new SelectBuilder();
		try {
			sb1.addVar("count(*)", "?p").addWhere("<"+firsttermUri.getMappingURI()+">", "?p", "<"+secondTermUri.getMappingURI()+">");
			sb2.addVar("count(*)", "?p").addWhere("<"+secondTermUri.getMappingURI()+">", "?p", "<"+firsttermUri.getMappingURI()+">");

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		QueryExecution queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT,
				sb1.buildString(), GlobalConstants.DBPEDIA_GRAPH_IRI);
		ResultSet res1 = queryRes.execSelect();
		
		queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT,
				sb2.buildString(), GlobalConstants.DBPEDIA_GRAPH_IRI);
		ResultSet res2 = queryRes.execSelect();
		
		//compare res1 res2 results. keep higher one
		int count1 = res1.nextSolution().get("p").asLiteral().getInt();
		int count2 = res2.nextSolution().get("p").asLiteral().getInt();
		if(count1 > count2) {
			
			finalBuilder = sb1;
		}
		else if(count2 > count1){
			finalBuilder = sb2;
		}
		if(count1 == 0 && count2==0) {
			return finalBuilder;
		}
		else {
			finalBuilder.addVar("?lbl1").addVar("?lbl3").addVar("?lbl2");
			finalBuilder.addPrefixes(GlobalConstants.prefixes);
			finalBuilder.addWhere("<"+firsttermUri.getMappingURI()+">","rdfs:label","?lbl1");
			finalBuilder.addWhere("?p","rdfs:label","?lbl3");
			finalBuilder.addWhere("<"+secondTermUri.getMappingURI()+">","rdfs:label","?lbl2");
			return finalBuilder;
		}
		
		/*String query1 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
		+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "PREFIX bif:<bif:> \n"
		+ "select count(?p) where {\n" 
		+ "<"+firsttermUri.getMappingURI()+"> ?p <"+secondTermUri.getMappingURI()+">  \n"
		+ "} ";
		String query2 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
		+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "PREFIX bif:<bif:> \n"
		+ "select count(?p) where {\n" 
		+ "<"+secondTermUri.getMappingURI()+"> ?p <"+firsttermUri.getMappingURI()+">  \n"
		+ "} ";*/
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
