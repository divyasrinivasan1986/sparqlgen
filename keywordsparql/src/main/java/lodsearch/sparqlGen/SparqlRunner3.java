package lodsearch.sparqlGen;


import java.util.*;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

import lodsearch.sparqlGen.queryRes.QueryResult;
import lodsearch.spotlightner.SpotlightClient;
import lodsearch.utils.GeneralUtils;

public class SparqlRunner3 {

	public static LinkedHashMap<String, String[]> queryKeywordVecsMap;
	public LinkedHashMap<String, List<QueryResult>> termToRdfMapping = new LinkedHashMap<String, List<QueryResult>>();
	public String query;
	
	public LinkedHashMap<String, List<QueryResult>> obtainRdfMappings(LinkedHashMap<String, String[]> keywordVecs) {
		List<String> namedKeysList = new ArrayList<String>();
		LinkedHashMap<String, List<QueryResult>> tempMap = new LinkedHashMap<String, List<QueryResult>>();
		
		SpotlightClient sc = new SpotlightClient();
		List<String> namedEntities = sc.getNamedEntities(query);
		String[] termSplit = query.split(" ");
		int termCountInUri = 0;
		StringBuilder concatTerm = null;
		for(String namedEntityUri : namedEntities) {
			concatTerm = new StringBuilder();
			for(String term : termSplit) {
				if(namedEntityUri.toLowerCase().contains(term.toLowerCase())) {
					concatTerm.append(term + " ");
					termCountInUri++;
				}
			}
			if(termCountInUri > 1) {
				QueryResult qRes = new QueryResult();
				qRes.setType("resource");
				qRes.setMappingURI(namedEntityUri);
				List<QueryResult> mPairs = new ArrayList<QueryResult>();
				mPairs.add(qRes);
				namedKeysList.add(concatTerm.toString().trim());
				tempMap.put(concatTerm.toString().trim(), mPairs);
			}
		}
		
		List<QueryResult> queryTermMappings = new ArrayList<QueryResult>();
		
		List<String> queryTerms = new ArrayList<String>(keywordVecs.keySet());
		boolean added =  false;
		for (int i = 1; i <= queryTerms.size(); i++) {
			for(String namedKey:namedKeysList) {
				if (namedKey.toLowerCase().contains(queryTerms.get(i - 1).toLowerCase())) {
					termToRdfMapping.put(namedKey, tempMap.get(namedKey));
					added = true;
			    }
			}
			if(added) {
				added = false;
				continue;
			}else {
				for (String word : keywordVecs.get(queryTerms.get(i - 1))) {
					List<QueryResult> queryMappings = fetchTermMappings(word);
					queryTermMappings.addAll(queryMappings);
				}
				termToRdfMapping.put(queryTerms.get(i - 1), queryTermMappings);
				queryTermMappings.clear();
			}
			/*concatenatedTerm = queryTerms.get(i - 1) + " " + queryTerms.get(i);
			queryConcatTermMappings = fetchTermMappings(concatenatedTerm);
			if (queryConcatTermMappings.size() > 0) {
				termToRdfMapping.put(concatenatedTerm, queryConcatTermMappings);
				keywordVecs.remove(queryTerms.get(i - 1));
				keywordVecs.remove(queryTerms.get(i));
			} else {*/
				
//			}

		}
		// for last term
/*
		for (String word : keywordVecs.get(queryTerms.get(queryTerms.size() - 1))) {
			List<QueryResult> queryMappings = fetchTermMappings(word);
			queryTermMappings.addAll(queryMappings);
		}
		if (queryTermMappings.size() > 0) {
			termToRdfMapping.put(queryTerms.get(queryTerms.size() - 1), queryTermMappings);
			queryTermMappings.clear();
		}*/

		return termToRdfMapping;
	}

	public List<QueryResult> fetchTermMappings(String term) {
		List<QueryResult> queryMappings = new ArrayList<QueryResult>();
		String queryPart1 = null;
		String queryPart2 = null;
		String queryPart3 = null;
		try {
			/*String[] termSplit = term.split(" ");
			if (termSplit.length == 2) {
				*//**
				 * this is just for named entity recognition
				 *//*
				// latest query
				String sparqlQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "PREFIX bif:<bif:> \n"
						+ "select ?s ?t where { \n" + "{ ?s rdf:type ?t . } \n" + "{?s rdfs:label ?lbl . \n"
						+ "?lbl bif:contains \"'" + termSplit[0] + "' and '" + termSplit[1] + "'\" . \n" + " } \n"
						+ "} limit 10";
				QueryExecution queryRes = QueryExecutionFactory.sparqlService("http://lod.openlinksw.com/sparql/",
						sparqlQuery, "http://dbpedia.org");

				ResultSet res = queryRes.execSelect();
				while (res.hasNext()) {
					QuerySolution nextSolution = res.nextSolution();
					RDFNode tnode = nextSolution.get("t");
					RDFNode snode = nextSolution.get("s");
					QueryResult qRes = new QueryResult();
					qRes.setType(tnode.asNode().getURI());
					qRes.setMappingURI(snode.asNode().getURI());
					queryMappings.add(qRes);
				}
			} else {*/
				/**
				 * here do separate small queries for res,ont and prop. append to the list
				 */

				queryPart1 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "PREFIX bif:<bif:> \n"
						+ "select ?s ?t where {\n" + "{ \n" + "?s rdf:type ?t . \n" + "?s rdfs:label ?lbl .\n"
						+ "?lbl bif:contains " + "\"" + term + "\" .\n" + "FILTER (lang(?lbl) = 'en') . \n" + "}\n"
						+ "}ORDER BY strlen(str(?s)) limit 4";
				queryPart2 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "PREFIX bif:<bif:> \n"
						+ "select ?s ?t ?lbl where {\n" + "{ \n" + "?s rdf:type ?t . \n"
						+ "?s rdf:type <http://www.w3.org/2002/07/owl#Class> . \n" + "?s rdfs:label ?lbl . \n"
						+ "?lbl bif:contains " + "\"" + term + "\" . \n" + "FILTER (lang(?lbl) = 'en') . \n" + "}\n"
						+ "} limit 3";
				queryPart3 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "PREFIX bif:<bif:> \n"
						+ "select ?s ?t ?lbl where {\n" + "{ \n" + "?s rdf:type ?t . \n"
						+ "?s rdf:type rdf:Property . \n" + "?s rdfs:label ?lbl . \n" + "?lbl bif:contains " + "\""
						+ term + "\" . \n" + "FILTER (lang(?lbl) = 'en') . \n" + "}\n" + "} limit 3";

				
				  /*queryMappings.get(0).get("?s").asNode().getURI();
				  queryMappings.get(0).get("?t").asNode().getURI();
				  if (ynode.isResource()) {
					  results.add(Pair.of(nextSolution.getResource("r").getURI(),
					  nextSolution.getResource("y").getURI())); } else {
					  results.add(Pair.of(nextSolution.getResource("r").getURI(),
					  nextSolution.getLiteral("y").getString().replaceAll("\\n+", " "))); }*/
				  
				QueryExecution queryRes = QueryExecutionFactory.sparqlService("http://lod.openlinksw.com/sparql/",
						queryPart1, "http://dbpedia.org");
				ResultSet res = queryRes.execSelect();
				res.getResultVars();
				while (res.hasNext()) {
					QuerySolution nextSolution = res.nextSolution();
					RDFNode tnode = nextSolution.get("t");
					RDFNode snode = nextSolution.get("s");
					tnode.asNode().getURI();
					snode.asNode().getURI();
					QueryResult qRes = new QueryResult();
					qRes.setType(tnode.asNode().getURI());
					qRes.setMappingURI(snode.asNode().getURI());
					queryMappings.add(qRes);
				}
				queryRes = QueryExecutionFactory.sparqlService("http://lod.openlinksw.com/sparql/", queryPart2,
						"http://dbpedia.org");
				res = queryRes.execSelect();
				res.getResultVars();
				while (res.hasNext()) {
					QuerySolution nextSolution = res.nextSolution();
					RDFNode tnode = nextSolution.get("t");
					RDFNode snode = nextSolution.get("s");
					tnode.asNode().getURI();
					snode.asNode().getURI();
					QueryResult qRes = new QueryResult();
					qRes.setType(tnode.asNode().getURI());
					qRes.setMappingURI(snode.asNode().getURI());
					queryMappings.add(qRes);
				}

				queryRes = QueryExecutionFactory.sparqlService("http://lod.openlinksw.com/sparql/", queryPart3,
						"http://dbpedia.org");
				res = queryRes.execSelect();
				res.getResultVars();
				while (res.hasNext()) {
					QuerySolution nextSolution = res.nextSolution();
					RDFNode tnode = nextSolution.get("t");
					RDFNode snode = nextSolution.get("s");
					tnode.asNode().getURI();
					snode.asNode().getURI();
					QueryResult qRes = new QueryResult();
					qRes.setType(tnode.asNode().getURI());
					qRes.setMappingURI(snode.asNode().getURI());
					queryMappings.add(qRes);
				}
				queryRes.close();
//			}
		} finally {

		}
		// instead of checking type, just check if contains ontology,resource or
		// property
		return queryMappings;
	}

	public void search(String query, LinkedHashMap<String, String[]> keywordVecs) {
		this.query = query;
		// stop words removal
		String noStopWordsQuery = GeneralUtils.removeStopWords(query);
		queryKeywordVecsMap = GeneralUtils.removeStopWordEntriesFromMap(noStopWordsQuery, keywordVecs);
		//queryKeywordVecsMap= GeneralUtils.removeNamedEntityEntriesFromMap(termToRdfMapping.keySet(), keywordVecs);
		@SuppressWarnings("unused")
		LinkedHashMap<String, List<QueryResult>> termToNodesMap = obtainRdfMappings(queryKeywordVecsMap);
	}

	/**
	 * Extra condition to add to SparQL in the end if this information is available.
	 * 
	 * @param query
	 * @return the extra condition
	 */
	public String treatWhTerms(String query) {
		String output = null;
		if (query.toLowerCase().contains("where"))
			output = "?x http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://schema.org/Place";
		else if (query.toLowerCase().contains("when"))
			output = "?x http://www.w3.org/2000/01/rdf-schema#range http://www.w3.org/2001/XMLSchema#date";
		else if (query.toLowerCase().contains("who"))
			output = "?x http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://xmlns.com/foaf/0.1/Person";
		return output;
	}

	public static void main(String[] args) {
	}

}
