package lodsearch.sparqlGen.termRdfMapper;


import java.util.*;
import java.util.stream.Collectors;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

import lodsearch.sparqlGen.constants.GlobalConstants;
import lodsearch.sparqlGen.queryRes.QueryResult;
import lodsearch.spotlightner.SpotlightClient;
import lodsearch.utils.GeneralUtils;

public class TermMapper {

	public static LinkedHashMap<String, String[]> queryKeywordVecsMap;
	public LinkedHashMap<String, List<QueryResult>> termToRdfMapping = new LinkedHashMap<String, List<QueryResult>>();
	
	public LinkedHashMap<String, List<QueryResult>> obtainRdfMappings(String query,LinkedHashMap<String, String[]> keywordVecs) {
		List<String> namedKeysList = new ArrayList<String>();
		LinkedHashMap<String, List<QueryResult>> tempMap = new LinkedHashMap<String, List<QueryResult>>();
		
		/** spotlight part **/
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
			if(termCountInUri > 0) {
				QueryResult qRes = new QueryResult();
				qRes.setType("resource");
				qRes.setMappingURI(namedEntityUri);
				List<QueryResult> mPairs = new ArrayList<QueryResult>();
				mPairs.add(qRes);
				namedKeysList.add(concatTerm.toString().trim());
				tempMap.put(concatTerm.toString().trim(), mPairs);
			}
		}
		/** end of spotlight part **/
		
		List<QueryResult> queryTermMappings = new ArrayList<QueryResult>();
		
		List<String> queryTerms = new ArrayList<String>(keywordVecs.keySet());
		boolean added =  false;
		for (int i = 1; i <= queryTerms.size(); i++) {
			for(String namedKey:namedKeysList) {
				if (namedKey.toLowerCase().contains(queryTerms.get(i - 1).toLowerCase())) {
					if(!termToRdfMapping.containsKey(namedKey))
						termToRdfMapping.put(namedKey, tempMap.get(namedKey));
					added = true;
			    }
			}
			if(added) {
				added = false;
				continue;
			}else {
				for (String word : keywordVecs.get(queryTerms.get(i - 1))) {
					if(word != null) {
						List<QueryResult> queryMappings = fetchTermMappings(word);
						Set<String> mappingUriSet = new HashSet<String>();
						List<QueryResult> noDuplicateMappings = queryMappings.stream().filter(e -> mappingUriSet.add(e.getMappingURI()))
					            .collect(Collectors.toList());
						queryTermMappings.addAll(noDuplicateMappings);
					}
					
				}
				
				termToRdfMapping.put(queryTerms.get(i - 1), queryTermMappings);
				queryTermMappings = new ArrayList<QueryResult>();
			}
		
		}

		return termToRdfMapping;
	}

	public static List<QueryResult> fetchTermMappings(String term) {
		List<QueryResult> queryMappings = new ArrayList<QueryResult>();
		String queryPart1 = null;
		String queryPart2 = null;
		String queryPart3 = null;
	/*	String[] naiveTermSplit = naiveTermSplitter(term);
		String queryTerm = null;
		if(naiveTermSplit!=null) {
			queryTerm = naiveTermSplit[0] + "' and '" + naiveTermSplit[1];
		}else {
			queryTerm = term;
		}*/
		try {
				/**
				 * here do separate small queries for res,ont and prop. append to the list
				 */
			
				queryPart1 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "PREFIX bif:<bif:> \n"
						+ "select ?s ?t "
						+ "where {\n" + "{ \n" + "?s rdf:type ?t . \n" + "?s rdfs:label ?lbl .\n"
						+ "?lbl bif:contains " + "\"" + term + "\" .\n" 
//						+ "FILTER (CONTAINS(?lbl, \""+ term + "\")) .\n"
						+ "filter not exists{ \n"
						+ "?s rdf:type rdf:Property . \n"
						+ "?s rdf:type <http://www.w3.org/2002/07/owl#Class> }" 
						+ "FILTER (lang(?lbl) = 'en') . \n" + "}\n"
						+ "} ORDER BY strlen(str(?s)) limit 10";
				queryPart2 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "PREFIX bif:<bif:> \n"
						+ "select ?s ?t ?lbl "
						+ "where {\n" + "{ \n" + "?s rdf:type ?t . \n"
						+ "?s rdf:type <http://www.w3.org/2002/07/owl#Class> . \n" + "?s rdfs:label ?lbl . \n"
//						+ "?lbl bif:contains " + "\"'" + term + "'\" . \n" 
						+ "FILTER (CONTAINS(?lbl, \""+ term + "\")) .\n"
						+ "FILTER (lang(?lbl) = 'en') . \n" + "}\n"
						+ "} ORDER BY strlen(str(?s)) limit 10";
				queryPart3 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "PREFIX bif:<bif:> \n"
						+ "select ?s ?t ?lbl "
						+ "where {\n" + "{ \n" + "?s rdf:type ?t . \n"
						+ "?s rdf:type rdf:Property . \n" + "?s rdfs:label ?lbl . \n" 
						+ "FILTER (CONTAINS(?lbl, \""+ term + "\")) .\n"
//						+ "?lbl bif:contains " + "\"'" + term + "'\" . \n" 
						+ "FILTER (lang(?lbl) = 'en') . \n" + "}\n" + "} ORDER BY strlen(str(?s)) limit 10";

				QueryExecution queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT,
						queryPart1, GlobalConstants.DBPEDIA_GRAPH_IRI);
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
				queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT, queryPart2,
						GlobalConstants.DBPEDIA_GRAPH_IRI);
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

				queryRes = QueryExecutionFactory.sparqlService(GlobalConstants.SPARQL_ENDPOINT, queryPart3,
						GlobalConstants.DBPEDIA_GRAPH_IRI);
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
		}catch(Exception e) {
			e.getStackTrace();
		}
		finally {
		}
	
		return queryMappings;
	}

	public static String[] naiveTermSplitter(String s) {
		String naiveTermSplit[]=new String[2];
		int splitLen = s.length()/3;
		if(splitLen>0) {
			naiveTermSplit[0] = s.substring(0, splitLen);
			naiveTermSplit[1] = s.substring(s.length()-splitLen, s.length());
			return naiveTermSplit;
		}
		else {
			return null;
		}
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
		System.out.println("TermMapper.main()");
	}

}
