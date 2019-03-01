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
	private static List<SelectBuilder> spanningSubgraphs = new ArrayList<SelectBuilder>();
	
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
							if(!(subgraph == sb))
								newSubgraphsList.add(subgraph);
						}
					}
					else {
						SelectBuilder subgraphParam = new SelectBuilder();
						subgraphParam.addPrefixes(GlobalConstants.prefixes);
						SelectBuilder subgraph = checkUriTypeAndFormSubgraph(firsttermUri,secondTermUri,i,subgraphParam);
						if(!(subgraph == subgraphParam))
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
					sb.buildString(), GlobalConstants.DBPEDIA_GRAPH_IRI);
			ResultSet res1 = queryRes.execSelect();
			while (res1.hasNext()) {
				QuerySolution nextSolution = res1.nextSolution();
			}
		}
		
	}
	private static SelectBuilder checkUriTypeAndFormSubgraph(QueryResult firsttermUri, QueryResult secondTermUri,int index, SelectBuilder subgraph) {
		SelectBuilder sb = new SelectBuilder();
		//need a mechanism to backtrack if results not found
		if(firsttermUri.getType().contains("resource") && secondTermUri.getType().contains("resource")) {
			sb = formResResSubgraph(firsttermUri,secondTermUri,index,subgraph);
		}
		else if(firsttermUri.getType().contains("ontology") && secondTermUri.getType().contains("ontology")) {
			sb = formOntOntSubgraph(firsttermUri,secondTermUri,index,subgraph);
		}
		else if(firsttermUri.getType().contains("property") && secondTermUri.getType().contains("property")) {
			sb = formPropPropSubgraph(firsttermUri,secondTermUri,index,subgraph);
		}
		else if((firsttermUri.getType().contains("resource") && secondTermUri.getType().contains("ontology")) ) {
			sb = formResOntSubgraph(firsttermUri,secondTermUri,index,subgraph);
		}
		else if((firsttermUri.getType().contains("ontology") && secondTermUri.getType().contains("resource"))) {
			sb = formResOntSubgraph(secondTermUri,firsttermUri,index,subgraph);
		}
		else if((firsttermUri.getType().contains("resource") && secondTermUri.getType().contains("property"))) {
			sb = formResPropSubgraph(firsttermUri,secondTermUri,index,subgraph);
		}
		else if((firsttermUri.getType().contains("property") && secondTermUri.getType().contains("resource"))) {
			sb = formResPropSubgraph(secondTermUri,firsttermUri,index,subgraph);
		}
		else if((firsttermUri.getType().contains("ontology") && secondTermUri.getType().contains("property"))) {
			sb = formOntPropSubgraph(firsttermUri,secondTermUri,index,subgraph);
		}
		else if((firsttermUri.getType().contains("property") && secondTermUri.getType().contains("ontology"))) {
			sb = formOntPropSubgraph(secondTermUri,firsttermUri,index,subgraph);
		}
		
		return sb;
	}
	private static SelectBuilder formOntPropSubgraph(QueryResult firsttermUri, QueryResult secondTermUri,int index, SelectBuilder subgraph) {
		SelectBuilder sb1 = subgraph;
		SelectBuilder sb2 = subgraph;
		SelectBuilder finalBuilder = new SelectBuilder();
		try {
			sb1.addWhere("?res"+index, "rdf:type", "<"+firsttermUri.getMappingURI()+">");
			sb1.addWhere("?res"+index, "<"+secondTermUri.getMappingURI()+">","?res"+(index + "a") );
			sb1.addVar("count(*)", "?c"+index);
			
			sb2.addWhere("?res"+index, "rdf:type", "<"+firsttermUri.getMappingURI()+">");
			sb2.addWhere("?res"+(index + "a"), "<"+secondTermUri.getMappingURI()+">","?res"+index );
			sb2.addVar("count(*)", "?c"+(index + "a"));
		

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
		
	
		int count2 = res2.nextSolution().get("c"+(index + "a")).asLiteral().getInt();
		//compare res1 res2 results. keep higher one
		int count1 = res1.nextSolution().get("c"+index).asLiteral().getInt();
		if(count1 > count2) {
			finalBuilder = sb1;
		}
		else if(count2 > count1) {
			finalBuilder = sb2;
		}
		if(count1==0 && count2==0) {
			return subgraph;
		}
		else {
			finalBuilder.addVar("?res"+index)/*.addVar("?res"+(index + "a"))*/;
			/*finalBuilder.addVar("?lbl"+index).addVar("?lbl"+(index + "a"));
			finalBuilder.addWhere("?res"+index,"rdfs:label","?lbl"+index);
			finalBuilder.addWhere("?res"+(index + "a"),"rdfs:label","?lbl"+(index + "a"));*/
		}
		
		return finalBuilder;

	}
	private static SelectBuilder formResPropSubgraph(QueryResult firsttermUri, QueryResult secondTermUri,int index, SelectBuilder subgraph) {
		SelectBuilder sb1 = subgraph;
		SelectBuilder sb2 = subgraph;
		SelectBuilder finalBuilder = new SelectBuilder();
		try {
			sb1.addVar("count(*)", "?p"+index).addVar("?res"+index).addWhere("<"+firsttermUri.getMappingURI()+">", "<"+secondTermUri.getMappingURI()+">","?res"+index );
			sb2.addVar("count(*)", "?p"+(index + "a")).addVar("?res"+index).addWhere("?res"+index, "<"+secondTermUri.getMappingURI()+">","<"+firsttermUri.getMappingURI()+">");

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
		int count1 = res1.nextSolution().get("p"+index).asLiteral().getInt();
		int count2 = res2.nextSolution().get("p"+(index + "a")).asLiteral().getInt();
		if(count1 > count2) {
			finalBuilder = sb1;
		}
		else if(count2 > count1){
			finalBuilder = sb2;
		}
		if(count1 == 0 && count2==0) {
			return subgraph;
		}
		else {
			/*finalBuilder.addVar("?lbl1").addVar("?lbl3").addVar("?lbl2");
			finalBuilder.addWhere("<"+firsttermUri.getMappingURI()+">","rdfs:label","?lbl1");
			finalBuilder.addWhere("<"+secondTermUri.getMappingURI()+">","rdfs:label","?lbl2");
			finalBuilder.addWhere("?res1","rdfs:label","?lbl3");*/
			return finalBuilder;
		}
		
	}
	private static SelectBuilder formResOntSubgraph(QueryResult firsttermUri, QueryResult secondTermUri,int index, SelectBuilder subgraph) {
		SelectBuilder sb1 = subgraph;
		SelectBuilder sb2 = subgraph;
		SelectBuilder sb3 = subgraph;
		SelectBuilder finalBuilder = new SelectBuilder();
		try {
			sb1.addWhere("<"+firsttermUri.getMappingURI()+">", "rdf:type", "<"+secondTermUri.getMappingURI()+">");			
			sb1.addVar("count(*)", "?c"+index);
			
			sb2.addVar("?res"+index).addWhere("?res"+index, "rdf:type", "<"+secondTermUri.getMappingURI()+">");
			sb2.addWhere("?res"+index, "?p", "<"+firsttermUri.getMappingURI()+">");
			sb2.addVar("count(*)", "?c"+(index + "a"));
			
			sb3.addVar("?res"+index).addWhere("?res"+index, "rdf:type", "<"+secondTermUri.getMappingURI()+">");
			sb3.addWhere("<"+firsttermUri.getMappingURI()+">", "?p", "?res"+index);
			sb3.addVar("count(*)", "?c"+(index + "a")+"a");

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		QueryExecution queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT,
				sb1.buildString(), GlobalConstants.DBPEDIA_GRAPH_IRI);
		ResultSet res1 = queryRes.execSelect();
		
		//compare res1 res2 results. keep higher one
		int count1 = res1.nextSolution().get("c"+index).asLiteral().getInt();
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
			count2 = res2.nextSolution().get("c"+(index + "a")).asLiteral().getInt();
			count3 = res3.nextSolution().get("c"+(index + "a")+"a").asLiteral().getInt();
			if(count1==0 && count2==0 && count3==0) {
				return subgraph;
			}
			else if(count2>count3 || count2==count3) {
				finalBuilder = sb2;
				/*finalBuilder.addVar("?lbl1").addVar("?lbl2");
				finalBuilder.addWhere("?res1","rdfs:label","?lbl1");
				finalBuilder.addWhere("?p","rdfs:label","?lbl2");*/
			}
			else if(count3>count2) {
				finalBuilder = sb3;
				/*finalBuilder.addVar("?lbl1").addVar("?lbl2");
				finalBuilder.addWhere("?res1","rdfs:label","?lbl1");
				finalBuilder.addWhere("?p","rdfs:label","?lbl2");*/
			}
		}
		
		return finalBuilder;

	}
	private static SelectBuilder formPropPropSubgraph(QueryResult firsttermUri, QueryResult secondTermUri,int index, SelectBuilder subgraph) {
		return subgraph;
	}
	private static SelectBuilder formOntOntSubgraph(QueryResult firsttermUri, QueryResult secondTermUri,int index, SelectBuilder subgraph) {
		SelectBuilder sb1 = subgraph;
		SelectBuilder sb2 = subgraph;
		SelectBuilder finalBuilder = new SelectBuilder();
		try {
			sb1.addVar("?res"+index).addWhere("?res"+index, "rdf:type", "<"+firsttermUri.getMappingURI()+">");
			sb1./*addVar("?res"+(index + "a")).*/addWhere("?res"+(index + "a"), "rdf:type", "<"+secondTermUri.getMappingURI()+">");
			sb1.addVar("count(*)", "?p"+index).addWhere("<"+firsttermUri.getMappingURI()+">", "?p", "<"+secondTermUri.getMappingURI()+">");

			sb2.addVar("?res"+index).addWhere("?res"+index, "rdf:type", "<"+firsttermUri.getMappingURI()+">");
			sb2/*.addVar("?res"+(index + "a"))*/.addWhere("?res"+(index + "a"), "rdf:type", "<"+secondTermUri.getMappingURI()+">");
			sb2.addVar("count(*)", "?p"+(index + "a")).addWhere("<"+secondTermUri.getMappingURI()+">", "?p", "<"+firsttermUri.getMappingURI()+">");

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
		int count1 = res1.nextSolution().get("p"+index).asLiteral().getInt();
		int count2 = res2.nextSolution().get("p"+(index + "a")).asLiteral().getInt();
		if(count1 > count2) {
			
			finalBuilder = sb1;
		}
		else if(count2 > count1){
			finalBuilder = sb2;
		}
		if(count1 == 0 && count2==0) {
			return subgraph;
		}
		else {
			/*finalBuilder.addVar("?lbl1").addVar("?lbl3").addVar("?lbl2");
			finalBuilder.addWhere("?res1","rdfs:label","?lbl1");
			finalBuilder.addWhere("?p","rdfs:label","?lbl3");
			finalBuilder.addWhere("?res2","rdfs:label","?lbl2");*/
			return finalBuilder;
		}
	
	}
	private static SelectBuilder formResResSubgraph(QueryResult firsttermUri, QueryResult secondTermUri,int index, SelectBuilder subgraph) {
		SelectBuilder sb1 = subgraph;
		SelectBuilder sb2 = subgraph;
		SelectBuilder finalBuilder = new SelectBuilder();
		try {
			sb1.addVar("?p"+index).addVar("count(*)", "?p"+index).addWhere("<"+firsttermUri.getMappingURI()+">", "?p", "<"+secondTermUri.getMappingURI()+">");
			sb2.addVar("?p"+(index + "a")).addVar("count(*)", "?p"+(index + "a")).addWhere("<"+secondTermUri.getMappingURI()+">", "?p", "<"+firsttermUri.getMappingURI()+">");

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
		int count1 = res1.nextSolution().get("p"+index).asLiteral().getInt();
		int count2 = res2.nextSolution().get("p"+(index + "a")).asLiteral().getInt();
		if(count1 > count2) {
			
			finalBuilder = sb1;
		}
		else if(count2 > count1){
			finalBuilder = sb2;
		}
		if(count1 == 0 && count2==0) {
			return subgraph;
		}
		else {
			/*finalBuilder.addVar("?lbl1").addVar("?lbl3").addVar("?lbl2");
			finalBuilder.addWhere("<"+firsttermUri.getMappingURI()+">","rdfs:label","?lbl1");
			finalBuilder.addWhere("?p","rdfs:label","?lbl3");
			finalBuilder.addWhere("<"+secondTermUri.getMappingURI()+">","rdfs:label","?lbl2");*/
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
