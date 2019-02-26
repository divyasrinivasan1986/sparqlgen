/*package lodsearch.sparqlGen;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.xmlbeans.soap.SOAPArrayType;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleString;
import org.rdfhdt.hdtjena.cmd.HDTSparql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.uttesh.exude.ExudeData;
import com.uttesh.exude.exception.InvalidDataException;

import lodsearch.utils.GeneralUtils;

public class SparqlRunner2 {

	public static LinkedHashMap<String, String[]> queryKeywordVecsMap;
	public HDTSparql hdtSparql;

	public SparqlRunner2(HDTSparql hdtSparql) {
		this.hdtSparql = hdtSparql;
	}

	public LinkedHashMap<String, List<QuerySolution>> obtainRdfMappings(LinkedHashMap<String, String[]> keywordVecs) {

//		hdtSparql = new HDTSparql("/home/divya/lod/dbpedia2016-04en.hdt");
		List<QuerySolution> queryTermMappings = new ArrayList<QuerySolution>();
		List<QuerySolution> queryConcatTermMappings = new ArrayList<QuerySolution>();
		LinkedHashMap<String, List<QuerySolution>> termToRdfMapping = new LinkedHashMap<String, List<QuerySolution>>();

		List<String> queryTerms = new ArrayList<String>(keywordVecs.keySet());
		String concatenatedTerm = "";
		// maintain order while doing this?
		for (int i = 1; i < queryTerms.size(); i++) {
			*//**** test code ******//*
			concatenatedTerm = queryTerms.get(i - 1) + " " + queryTerms.get(i);
			executeInitSparQL(concatenatedTerm);
			for (String word : keywordVecs.get(queryTerms.get(i - 1))) {
				executeInitSparQL(word);
			}
			*//********* end of test code ******//*

			*//******* real code uncomment later **********//*
			
			 * concatenatedTerm = queryTerms.get(i-1)+" "+queryTerms.get(i);
			 * queryConcatTermMappings = executeInitSparQL(concatenatedTerm);
			 * if(queryConcatTermMappings.size()>0) { termToRdfMapping.put(concatenatedTerm,
			 * queryConcatTermMappings); keywordVecs.remove(queryTerms.get(i-1));
			 * keywordVecs.remove(queryTerms.get(i)); }else { for(String
			 * word:keywordVecs.get(queryTerms.get(i-1))) { List<QuerySolution>
			 * queryMappings = executeInitSparQL(word);
			 * queryTermMappings.addAll(queryMappings); }
			 * termToRdfMapping.put(queryTerms.get(i-1), queryTermMappings);
			 * queryTermMappings.clear(); }
			 
		}
		// for last term
		*//******* real code uncomment later **********//*
		
		 * for(String word:keywordVecs.get(queryTerms.get(queryTerms.size()-1))) {
		 * List<QuerySolution> queryMappings = executeInitSparQL(word);
		 * queryTermMappings.addAll(queryMappings); } if(queryTermMappings.size()>0) {
		 * termToRdfMapping.put(queryTerms.get(queryTerms.size()-1), queryTermMappings);
		 * queryTermMappings.clear(); }
		 

		*//*** dont use below part *//*
		
		 * keywordVecs.forEach((k, v) -> {
		 * 
		 * for(String word:v) {
		 * 
		 * List<QuerySolution> queryMappings = executeInitSparQL(word);
		 * queryTermMappings.addAll(queryMappings); }
		 * 
		 * termToRdfMapping.put(k, queryTermMappings); queryTermMappings.clear(); });
		 
		return termToRdfMapping;
	}

	public List<QuerySolution> executeInitSparQL(String term) {
		List<QuerySolution> queryMappings = new ArrayList<QuerySolution>();
		String queryPart1 = null;
		String queryPart2 = null;
		String queryPart3 = null;
		try {
//			hdtSparql.hdt = HDTManager.mapIndexedHDT(hdtSparql.fileHDT, null);
			String[] termSplit = term.split(" ");
			if (termSplit.length == 2) {
				*//**
				 * this is just for named entity recognition
				 *//*
				
				  hdtSparql.sparqlQuery =
				  "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				  +"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
				  "select ?s ?t where {\n"
				  +"{ {?s rdfs:label ?lbl . Filter (lcase(str(?lbl)) = "+"\"" +term + "\"" +
				  ") . } \n" +"?s rdf:type ?t . }\n"
				  +"UNION {?s rdf:type <http://www.w3.org/2002/07/owl#Class> . ?s rdfs:label ?lbl . FILTER (lang(?lbl) = 'en') . FILTER regex(?lbl,"
				  +"\""+(termSplit[0]+".*?"+termSplit[1]) +"\""+",\"i\") . }"
				  +"UNION {?s rdf:type rdf:Property . ?s rdfs:label ?lbl . FILTER (lang(?lbl) = 'en') . FILTER regex(?lbl,"
				  +"\""+(termSplit[0]+".*?"+termSplit[1])+"\""+",\"i\") . }" +"} limit 10";
				 
				
				  hdtSparql.sparqlQuery =
				  "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				  +"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
				  +"PREFIX bif:<bif:> \n" + "select ?s ?t where {\n" +"{ ?s rdf:type ?t . }\n"
				  +"{?s rdfs:label ?lbl . Filter (lcase(str(?lbl)) = "+"\"" +term + "\"" +
				  "@en) . FILTER NOT EXISTS { ?s rdf:type <http://www.w3.org/2002/07/owl#Class> . ?s rdf:type rdf:Property . } . } \n"
				  +"UNION {?s rdf:type <http://www.w3.org/2002/07/owl#Class> . ?s rdfs:label ?lbl . ?lbl bif:contains "
				  +"\"" +termSplit[0] + "\" . ?lbl bif:contains "+"\"" +termSplit[1] +
				  "\" . FILTER (lang(?lbl) = 'en') . }"
				  +"UNION {?s rdf:type rdf:Property . ?s rdfs:label ?lbl . ?lbl bif:contains "
				  +"\"" +termSplit[0] + "\" . ?lbl bif:contains "+"\"" +termSplit[1] +
				  "\" . FILTER (lang(?lbl) = 'en') . }" +"} limit 10";
				 
				
				 * hdtSparql.sparqlQuery =
				 * "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				 * +"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
				 * +"PREFIX bif:<bif:> \n" + "select ?s ?t where {\n" +"{ ?s rdf:type ?t . }\n"
				 * +"{?s rdfs:label ?lbl . ?lbl bif:contains \"'" +termSplit[0] +
				 * "' and '"+termSplit[1]
				 * +"'\"  . FILTER NOT EXISTS { ?s rdf:type <http://www.w3.org/2002/07/owl#Class> . ?s rdf:type rdf:Property . } . } \n"
				 * +"} limit 10";
				 
				  
				  //latest query
				String sparqlQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "PREFIX bif:<bif:> \n"
						+ "select ?s ?t where { \n" + "{ ?s rdf:type ?t . } \n" + "{?s rdfs:label ?lbl . \n"
						+ "?lbl bif:contains \"'" + termSplit[0] + "' and '" + termSplit[1] + "'\" . \n" + " } \n"
						+ "} limit 10";
				 QueryExecution queryRes = QueryExecutionFactory.sparqlService("http://lod.openlinksw.com/sparql/", sparqlQuery, "http://dbpedia.org");

//				queryMappings.addAll(hdtSparql.executeSelect());
//				hdtSparql.execute();
				 queryRes.execSelect();
			} else {
				*//**
				 * here do separate small queries for res,ont and prop. append to the list
				 *//*
				
				  hdtSparql.sparqlQuery =
				  "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				  +"PREFIX foaf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
				  "select ?s ?t where {\n" +"{ ?s rdfs:label ?lbl . \n" +"?s foaf:type ?t . \n"
				  +"Filter (lcase(str(?lbl)) = "+"\"" +term + "\"" +") . }"
				  +"UNION {?s rdfs:label ?lbl . ?s foaf:type <http://www.w3.org/2002/07/owl#Class> . FILTER regex(?lbl,"
				  +"\""+term+"\""+",\"i\") . }"
				  +"UNION {?s rdfs:label ?lbl . ?s foaf:type foaf:Property . FILTER regex(?lbl,"
				  +"\""+term+"\""+",\"i\") . }" +"FILTER (lang(?lbl) = 'en')} limit 10"; 
				  hdtSparql.execute();
				 
				 hdtSparql.sparqlQuery =
				  "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				  +"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
				  +"PREFIX bif:<bif:> \n" + "select ?s ?t where {\n" +"{ ?s rdf:type ?t . }\n"
				  +"{?s rdfs:label ?lbl . Filter (lcase(str(?lbl)) = "+"\"" +term + "\"" +
				  "@en) . FILTER NOT EXISTS { ?s rdf:type <http://www.w3.org/2002/07/owl#Class> . ?s rdf:type rdf:Property . } . } \n"
				  +"UNION {?s rdf:type <http://www.w3.org/2002/07/owl#Class> . ?s rdfs:label ?lbl . ?lbl bif:contains "
				  +"\"" +term + "\" . FILTER (lang(?lbl) = 'en') . }"
				  +"UNION {?s rdf:type rdf:Property . ?s rdfs:label ?lbl . ?lbl bif:contains "
				  +"\"" +term + "\" . FILTER (lang(?lbl) = 'en') . }" +"} limit 10";
				 
				queryPart1 = "PREFIX llo: <http://lodlaundromat.org/ontology/>\n"
						+ "PREFIX ll: <http://lodlaundromat.org/resource/>\n"
						+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
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
 
				hdtSparql.sparqlQuery = queryPart1;
				hdtSparql.execute();
//				queryMappings.addAll(hdtSparql.executeSelect());
				hdtSparql.sparqlQuery = queryPart2;
				hdtSparql.execute();
//				queryMappings.addAll(hdtSparql.executeSelect());
				hdtSparql.sparqlQuery = queryPart3;
				hdtSparql.execute();
//				queryMappings.addAll(hdtSparql.executeSelect());
				*//*** uncomment next line later ***//*
				 queryMappings = hdtSparql.executeSelect(); 
				// hdtSparql.execute();
				
				 * queryMappings.get(0).get("?s").asNode().getURI();
				 * queryMappings.get(0).get("?t").asNode().getURI();
				 
				 QueryExecution queryRes = QueryExecutionFactory.sparqlService("http://lod.openlinksw.com/sparql/", queryPart1, "http://dbpedia.org");
				 ResultSet res = queryRes.execSelect();
				 res.getResultVars();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			
		}
		// instead of checking type, just check if contains ontology,resource or
		// property
		return queryMappings;
	}

	public void search(String query, LinkedHashMap<String, String[]> keywordVecs) {
		// Load HDT file NOTE: Use loadIndexedHDT() if you are doing ?P?, ?PO, ??O
		// queries
		HDT hdt;
		// stop words removal
		String output = GeneralUtils.removeStopWords(query);
		queryKeywordVecsMap = GeneralUtils.removestopWordEntriesFromMap(output, keywordVecs);
		@SuppressWarnings("unused")
		LinkedHashMap<String, List<QuerySolution>> termToNodesMap = obtainRdfMappings(queryKeywordVecsMap);

	}

	*//**
	 * Extra condition to add to SparQL in the end if this information is available.
	 * 
	 * @param query
	 * @return the extra condition
	 *//*
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
		// Load HDT file NOTE: Use loadIndexedHDT() if you are doing ?P?, ?PO, ??O
		// queries
		HDT hdt;
		try {
			
			 * SparqlRunner rdfsql = new SparqlRunner(); String whCondition =
			 * rdfsql.treatWhTerms(args[0]);
			 
			// stop words removal

			hdt = HDTManager.loadIndexedHDT("/home/divya/lod/dbpedia2016-04en.hdt", null);

			// Use mapHDT/mapIndexedHDT to save memory. // It will load the parts on
			// demand (possibly slower querying). // HDT hdt =
			// HDTManager.mapHDT("data/example.hdt", null);

			// Enumerate all triples. Empty string means "any" IteratorTripleString it;

			// Enumerate all triples. Empty string means "any"
			IteratorTripleString it = hdt.search("", "<http://www.w3.org/2000/01/rdf-schema#label>", "\"film\"@en");
			// IteratorTripleString it = hdt.search("","","");
			System.out.println("Estimated number of results: " + it.estimatedNumResults());
			while (it.hasNext()) {
				TripleString ts = it.next();
				System.out.println(ts);
			}
		}
		
		 * * // List all predicates System.out.println("Dataset contains " +
		 * hdt.getDictionary().getNpredicates() + " predicates:"); Iterator<? extends
		 * CharSequence> itPred =
		 * hdt.getDictionary().getPredicates().getSortedEntries(); while
		 * (itPred.hasNext()) { CharSequence str = itPred.next();
		 * System.out.println(str); }
		 * 
		 * 
		 * HDTSparql hdtSparql = new HDTSparql("/home/divya/lod/dbpedia2016-04en.hdt");
		 * String []words = {"actors","born","china"}; hdtSparql.fileHDT =
		 * "/home/divya/lod/dbpedia2016-04en.hdt"; for(String word:words) {
		 * hdtSparql.sparqlQuery =
		 * "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
		 * "select ?s where {\n" +"?s rdfs:label ?lbl . \n"
		 * +"FILTER (lang(?lbl) = 'en')" +"Filter (lcase(str(?lbl)) = "+"\"" +word +
		 * "\"" +")} limit 10";// "select ?s ?p ?o where {?s ?p ?o }";
		 * 
		 * hdtSparql.execute(); }
		 * 
		 * 
		 * } /* catch (NotFoundException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		  catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
*/