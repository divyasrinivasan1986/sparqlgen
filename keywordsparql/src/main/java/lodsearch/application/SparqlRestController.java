package lodsearch.application;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lodsearch.index.HDTIndex;
import lodsearch.request.IncomingRequest;
import lodsearch.response.Response;
import lodsearch.sparqlGen.SparqlRunner;
import lodsearch.utils.GeneralUtils;

@RestController
@RequestMapping(method = RequestMethod.POST , produces="application/json")
public class SparqlRestController {
	
    @RequestMapping("/sparqlgen")
    public Response doKeywordSearch(@RequestBody final IncomingRequest request) {
    	String query = request.getRequestContent().get(0)==null?"":request.getRequestContent().get(0).getText();
    	LinkedHashMap<String,Object[][]> wordMap = request.getRequestContent().get(0)==null?null:request.getRequestContent().get(0).getWordMap();
    	LinkedHashMap<String,String[]> cleanedWordMap = GeneralUtils.cleanUIInputMap(wordMap);
    	SparqlRunner rdfhdt = new SparqlRunner();
//    	SparqlRunner rdfhdt = new SparqlRunner(new HDTSparql("/home/divya/lod/dbpedia2016-04en.hdt"));
    	Response response = rdfhdt.search(query,cleanedWordMap);
    	return response;
    }
}
