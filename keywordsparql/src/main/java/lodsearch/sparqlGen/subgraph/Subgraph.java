package lodsearch.sparqlGen.subgraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.lang.sparql_11.ParseException;

import lodsearch.response.Response;
import lodsearch.sparqlGen.constants.GlobalConstants;
import lodsearch.sparqlGen.queryRes.QueryResult;

public class Subgraph {
	private static List<SelectBuilder> spanningSubgraphs = new ArrayList<SelectBuilder>();
	
	public static Response formSubgraphs(LinkedHashMap<String, List<QueryResult>> termToRdfMapping) {
		
		List<String> queryTerms = new ArrayList<String>(termToRdfMapping.keySet());
		List<SelectBuilder> subgraphsList = new ArrayList<SelectBuilder>();
		for (int i = 0; i < queryTerms.size()-1; i++) {
			List<QueryResult> firstTermUris = termToRdfMapping.get(queryTerms.get(i));
			List<QueryResult> secondTermUris = termToRdfMapping.get(queryTerms.get(i+1));
			List<SelectBuilder> newSubgraphsList = new ArrayList<SelectBuilder>();
			for(int j=0;j<firstTermUris.size();j++) {
				QueryResult firsttermUri = firstTermUris.get(j);
				for(int k=0;k<secondTermUris.size();k++) {
					QueryResult secondTermUri = secondTermUris.get(k);
					
					int index = Integer.parseInt(String.valueOf(j>10?j*2+1:j) + String.valueOf(k>10?k*2+1:k));
					if(!subgraphsList.isEmpty()) {
						for(SelectBuilder sb:subgraphsList) {
							SelectBuilder subgraph = checkUriTypeAndFormSubgraph(firsttermUri,secondTermUri,index,sb);
							if(!(subgraph.equals(sb)))
								newSubgraphsList.add(subgraph);
						}
					}
					else {
						SelectBuilder subgraphParam = new SelectBuilder();
						subgraphParam.addPrefixes(GlobalConstants.prefixes);
						SelectBuilder subgraph = checkUriTypeAndFormSubgraph(firsttermUri,secondTermUri,index,subgraphParam);
						if(!(subgraph.equals(subgraphParam)))
							newSubgraphsList.add(subgraph);
							//subgraphsList.add(subgraph);
					}
					
				}
			}
			if(!newSubgraphsList.isEmpty())
				subgraphsList = newSubgraphsList;
		}
		Set<String> answers = new HashSet<String>();
		spanningSubgraphs = subgraphsList;
		//subgraph will be spanning subgraph here
		for(SelectBuilder sb:spanningSubgraphs) {
			QueryExecution queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT,
					sb.buildString(), GlobalConstants.DBPEDIA_GRAPH_IRI);
			ResultSet res1 = queryRes.execSelect();
			while (res1.hasNext()) {
				QuerySolution nextSolution = res1.nextSolution();
				Iterator<String> it = nextSolution.varNames();
				while(it.hasNext()) {
					String var = it.next();
					if(nextSolution.get(var).isLiteral())
						answers.add(nextSolution.get(var).asLiteral().getString());
					if(nextSolution.get(var).isResource())
						answers.add(nextSolution.get(var).asNode().getURI());
				}
				
			}
		}
		//send to display 
		Response response = new Response();
		response.setAnswers(answers);
		
		//send list of results to gerbil
		return response;
	}
	private static SelectBuilder checkUriTypeAndFormSubgraph(QueryResult firsttermUri, QueryResult secondTermUri,int index, SelectBuilder subgraph) {
		SelectBuilder sb = subgraph;
		try {
			if(firsttermUri.getMappingURI().toLowerCase().contains("resource") && secondTermUri.getMappingURI().toLowerCase().contains("resource")) {
				sb = formResResSubgraph(firsttermUri,secondTermUri,index,subgraph);
			}
			else if(firsttermUri.getMappingURI().toLowerCase().contains("ontology") && secondTermUri.getMappingURI().toLowerCase().contains("ontology")) {
				if(firsttermUri.getType().toLowerCase().contains("property") && secondTermUri.getType().toLowerCase().contains("ontology")) {
					sb = formOntPropSubgraph(secondTermUri,firsttermUri,index,subgraph);
				}
				else if(firsttermUri.getType().toLowerCase().contains("ontology") && secondTermUri.getType().toLowerCase().contains("property")) {
					sb = formOntPropSubgraph(firsttermUri,secondTermUri,index,subgraph);
				}
				else if(firsttermUri.getType().toLowerCase().contains("property") && secondTermUri.getType().toLowerCase().contains("property")) {
					sb = formPropPropSubgraph(firsttermUri,secondTermUri,index,subgraph);
				}else {
					sb = formOntOntSubgraph(firsttermUri,secondTermUri,index,subgraph);
				}
			}
			else if(firsttermUri.getMappingURI().toLowerCase().contains("property") && secondTermUri.getMappingURI().toLowerCase().contains("property")) {
				sb = formPropPropSubgraph(firsttermUri,secondTermUri,index,subgraph);
			}
			else if((firsttermUri.getMappingURI().toLowerCase().contains("resource") && secondTermUri.getMappingURI().toLowerCase().contains("ontology")) ) {
				if(secondTermUri.getType().toLowerCase().contains("property")) {
					sb = formResPropSubgraph(firsttermUri,secondTermUri,index,subgraph);
				}
				else {
					sb = formResOntSubgraph(firsttermUri,secondTermUri,index,subgraph);
				}
				
			}
			else if((firsttermUri.getMappingURI().toLowerCase().contains("ontology") && secondTermUri.getMappingURI().toLowerCase().contains("resource"))) {
				if(firsttermUri.getType().toLowerCase().contains("property")) {
					sb = formResPropSubgraph(secondTermUri,firsttermUri,index,subgraph);
				}
				else {
					sb = formResOntSubgraph(secondTermUri,firsttermUri,index,subgraph);
				}
				
			}
			else if((firsttermUri.getMappingURI().toLowerCase().contains("resource") && secondTermUri.getMappingURI().toLowerCase().contains("property"))) {
				sb = formResPropSubgraph(firsttermUri,secondTermUri,index,subgraph);
			}
			else if((firsttermUri.getMappingURI().toLowerCase().contains("property") && secondTermUri.getMappingURI().toLowerCase().contains("resource"))) {
				sb = formResPropSubgraph(secondTermUri,firsttermUri,index,subgraph);
			}
			else if((firsttermUri.getMappingURI().toLowerCase().contains("ontology") && secondTermUri.getMappingURI().toLowerCase().contains("property"))) {
				if(firsttermUri.getType().toLowerCase().contains("property")) {
					sb = formPropPropSubgraph(firsttermUri,secondTermUri,index,subgraph);
				}
				else {
					sb = formOntPropSubgraph(firsttermUri,secondTermUri,index,subgraph);
				}			
			}
			else if((firsttermUri.getMappingURI().toLowerCase().contains("property") && secondTermUri.getMappingURI().toLowerCase().contains("ontology"))) {
				if(secondTermUri.getType().toLowerCase().contains("property")) {
					sb = formPropPropSubgraph(firsttermUri,secondTermUri,index,subgraph);
				}
				else {
					sb = formOntPropSubgraph(secondTermUri,firsttermUri,index,subgraph);
				}	
			}
		}catch(Exception e) {
			e.getStackTrace();
		}finally {
			return sb;
		}	
	}
	private static SelectBuilder formOntPropSubgraph(QueryResult firsttermUri, QueryResult secondTermUri,int index, SelectBuilder subgraph) {
		SelectBuilder sb1 = subgraph.clone();
		SelectBuilder sb2 = subgraph.clone();
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
		
	
		int count2 = res2.hasNext()?res2.nextSolution().get("c"+(index + "a")).asLiteral().getInt():0;
		//compare res1 res2 results. keep higher one
		int count1 = res1.hasNext()?res1.nextSolution().get("c"+index).asLiteral().getInt():0;
		if(count1 > count2) {
			sb1 = subgraph.clone();
			sb1.addWhere("?res"+index, "rdf:type", "<"+firsttermUri.getMappingURI()+">");
			sb1.addWhere("?res"+index, "<"+secondTermUri.getMappingURI()+">","?res"+(index + "a") );
			finalBuilder = sb1;
		}
		else if(count2 > count1) {
			sb2 = subgraph.clone();
			sb2.addWhere("?res"+index, "rdf:type", "<"+firsttermUri.getMappingURI()+">");
			sb2.addWhere("?res"+(index + "a"), "<"+secondTermUri.getMappingURI()+">","?res"+index );
			finalBuilder = sb2;
		}
		if(count1==0 && count2==0) {
			return subgraph;
		}
		else {
			finalBuilder.addVar("?res"+index).addGroupBy("?res"+index);/*.addVar("?res"+(index + "a"))*/;
			/*finalBuilder.addVar("?lbl"+index).addVar("?lbl"+(index + "a"));
			finalBuilder.addWhere("?res"+index,"rdfs:label","?lbl"+index);
			finalBuilder.addWhere("?res"+(index + "a"),"rdfs:label","?lbl"+(index + "a"));*/
		}
		
		return finalBuilder;

	}
	private static SelectBuilder formResPropSubgraph(QueryResult firsttermUri, QueryResult secondTermUri,int index, SelectBuilder subgraph) {
		SelectBuilder sb1 = subgraph.clone();
		SelectBuilder sb2 = subgraph.clone();
		SelectBuilder finalBuilder = new SelectBuilder();
		try {
			sb1.addVar("count(*)", "?p"+index).addVar("?res"+index).addWhere("<"+firsttermUri.getMappingURI()+">", "<"+secondTermUri.getMappingURI()+">","?res"+index ).addGroupBy("?res"+index);
			sb2.addVar("count(*)", "?p"+(index + "a")).addVar("?res"+index).addWhere("?res"+index, "<"+secondTermUri.getMappingURI()+">","<"+firsttermUri.getMappingURI()+">").addGroupBy("?res"+index);

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
		int count1 = res1.hasNext()?res1.nextSolution().get("p"+index).asLiteral().getInt():0;
		int count2 = res2.hasNext()?res2.nextSolution().get("p"+(index + "a")).asLiteral().getInt():0;
		if(count1 > count2) {
			sb1 = subgraph.clone();
			sb1.addVar("?res"+index).addWhere("<"+firsttermUri.getMappingURI()+">", "<"+secondTermUri.getMappingURI()+">","?res"+index ).addGroupBy("?res"+index);
			finalBuilder = sb1;
		}
		else if(count2 > count1){
			sb2 = subgraph.clone();
			sb2.addVar("?res"+index).addWhere("?res"+index, "<"+secondTermUri.getMappingURI()+">","<"+firsttermUri.getMappingURI()+">").addGroupBy("?res"+index);

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
		SelectBuilder sb1 = subgraph.clone();
		SelectBuilder sb2 = subgraph.clone();
		SelectBuilder sb3 = subgraph.clone();
		SelectBuilder finalBuilder = new SelectBuilder();
		try {
			sb1.addWhere("<"+firsttermUri.getMappingURI()+">", "rdf:type", "<"+secondTermUri.getMappingURI()+">");			
			sb1.addVar("count(*)", "?c"+index);
			
			sb2.addVar("?res"+index).addWhere("?res"+index, "rdf:type", "<"+secondTermUri.getMappingURI()+">");
			sb2.addWhere("?res"+index, "?p", "<"+firsttermUri.getMappingURI()+">");
			sb2.addVar("count(*)", "?c"+(index + "a")).addGroupBy("?res"+index);
			
			sb3.addVar("?res"+index).addWhere("?res"+index, "rdf:type", "<"+secondTermUri.getMappingURI()+">");
			sb3.addWhere("<"+firsttermUri.getMappingURI()+">", "?p", "?res"+index);
			sb3.addVar("count(*)", "?c"+(index + "a")+"a").addGroupBy("?res"+index);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		QueryExecution queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT,
				sb1.buildString(), GlobalConstants.DBPEDIA_GRAPH_IRI);
		ResultSet res1 = queryRes.execSelect();
		
		//compare res1 res2 results. keep higher one
		int count1 = res1.hasNext()?res1.nextSolution().get("c"+index).asLiteral().getInt():0;
		int count2=0;int count3=0;
		if(count1 > 0) {
			sb1 = subgraph.clone();
			sb1.addWhere("<"+firsttermUri.getMappingURI()+">", "rdf:type", "<"+secondTermUri.getMappingURI()+">");			
			finalBuilder = sb1;
		}
		else {
			queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT,
					sb2.buildString(), GlobalConstants.DBPEDIA_GRAPH_IRI);
			ResultSet res2 = queryRes.execSelect();
			
			queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT,
					sb3.buildString(), GlobalConstants.DBPEDIA_GRAPH_IRI);
			ResultSet res3 = queryRes.execSelect();
			count2 = res2.hasNext()?res2.nextSolution().get("c"+(index + "a")).asLiteral().getInt():0;
			count3 = res3.hasNext()?res3.nextSolution().get("c"+(index + "a")+"a").asLiteral().getInt():0;
			if(count1==0 && count2==0 && count3==0) {
				return subgraph;
			}
			else if(count2>count3 || count2==count3) {
				sb2 = subgraph.clone();
				sb2.addVar("?res"+index).addWhere("?res"+index, "rdf:type", "<"+secondTermUri.getMappingURI()+">");
				sb2.addWhere("?res"+index, "?p", "<"+firsttermUri.getMappingURI()+">");
				sb2.addGroupBy("?res"+index);
				finalBuilder = sb2;
				/*finalBuilder.addVar("?lbl1").addVar("?lbl2");
				finalBuilder.addWhere("?res1","rdfs:label","?lbl1");
				finalBuilder.addWhere("?p","rdfs:label","?lbl2");*/
			}
			else if(count3>count2) {
				sb3 = subgraph.clone();
				sb3.addVar("?res"+index).addWhere("?res"+index, "rdf:type", "<"+secondTermUri.getMappingURI()+">");
				sb3.addWhere("<"+firsttermUri.getMappingURI()+">", "?p", "?res"+index);
				sb3.addGroupBy("?res"+index);
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
		SelectBuilder sb1 = subgraph.clone();
		SelectBuilder sb2 = subgraph.clone();
		SelectBuilder finalBuilder = new SelectBuilder();
		try {
			sb1.addVar("?res"+index).addWhere("?res"+index, "rdf:type", "<"+firsttermUri.getMappingURI()+">").addGroupBy("?res"+index);
			sb1./*addVar("?res"+(index + "a")).*/addWhere("?res"+(index + "a"), "rdf:type", "<"+secondTermUri.getMappingURI()+">");
			sb1.addVar("count(*)", "?p"+index).addWhere("?res"+index, "?p", "?res"+(index + "a"));
			/*addWhere("<"+firsttermUri.getMappingURI()+">", "?p", "<"+secondTermUri.getMappingURI()+">")
			 * addWhere("<"+secondTermUri.getMappingURI()+">", "?p", "<"+firsttermUri.getMappingURI()+">"
			 */
			sb2.addVar("?res"+index).addWhere("?res"+index, "rdf:type", "<"+firsttermUri.getMappingURI()+">").addGroupBy("?res"+index);
			sb2/*.addVar("?res"+(index + "a"))*/.addWhere("?res"+(index + "a"), "rdf:type", "<"+secondTermUri.getMappingURI()+">");
			sb2.addVar("count(*)", "?p"+(index + "a")).addWhere("?res"+(index + "a"), "?p", "?res"+index);

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
		int count1 = res1.hasNext()?res1.nextSolution().get("p"+index).asLiteral().getInt():0;
		int count2 = res2.hasNext()?res2.nextSolution().get("p"+(index + "a")).asLiteral().getInt():0;
		if(count1 > count2) {
			sb1 = subgraph.clone();
			sb1.addVar("?res"+index).addWhere("?res"+index, "rdf:type", "<"+firsttermUri.getMappingURI()+">").addGroupBy("?res"+index);
			sb1.addWhere("?res"+(index + "a"), "rdf:type", "<"+secondTermUri.getMappingURI()+">");
			sb1.addWhere("?res"+index, "?p", "?res"+(index + "a"));
			finalBuilder = sb1;
		}
		else if(count2 > count1){
			sb2 = subgraph.clone();
			sb2.addVar("?res"+index).addWhere("?res"+index, "rdf:type", "<"+firsttermUri.getMappingURI()+">").addGroupBy("?res"+index);
			sb2.addWhere("?res"+(index + "a"), "rdf:type", "<"+secondTermUri.getMappingURI()+">");
			sb2.addWhere("?res"+(index + "a"), "?p", "?res"+index);
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
		SelectBuilder sb1 = subgraph.clone();
		SelectBuilder sb2 = subgraph.clone();
		SelectBuilder finalBuilder = new SelectBuilder();
		try {
			sb1.addVar("?p"+index).addGroupBy("?p"+index).addVar("count(*)", "?c"+index).addWhere("<"+firsttermUri.getMappingURI()+">", "?p"+index, "<"+secondTermUri.getMappingURI()+">");
			sb2.addVar("?p"+(index + "a")).addGroupBy("?p"+(index+"a")).addVar("count(*)", "?c"+(index + "a")).addWhere("<"+secondTermUri.getMappingURI()+">", "?p"+(index + "a"), "<"+firsttermUri.getMappingURI()+">");

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
		int count1 = res1.hasNext()?res1.nextSolution().get("c"+index).asLiteral().getInt():0;
		int count2 = res2.hasNext()?res2.nextSolution().get("c"+(index + "a")).asLiteral().getInt():0;
		if(count1 > count2) {
			sb1 = subgraph.clone();
			sb1.addVar("?p"+index).addGroupBy("?p"+index).addWhere("<"+firsttermUri.getMappingURI()+">", "?p"+index, "<"+secondTermUri.getMappingURI()+">");
			finalBuilder = sb1;
		}
		else if(count2 > count1){
			sb2 = subgraph.clone();
			sb2.addVar("?p"+(index + "a")).addGroupBy("?p"+(index+"a")).addWhere("<"+secondTermUri.getMappingURI()+">", "?p"+(index + "a"), "<"+firsttermUri.getMappingURI()+">");
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
