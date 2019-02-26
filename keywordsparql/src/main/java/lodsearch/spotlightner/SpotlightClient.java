package lodsearch.spotlightner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lodsearch.spotlightner.common.AnnotationUnit;

public class SpotlightClient {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		  Spotlight spotlight = new Spotlight();
	        AnnotationUnit annotationUnit;


	        // By TEXT

	        String text = "wife of bill gates";

	        try {
				annotationUnit = spotlight.get(text);
				  print(annotationUnit);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      
	}
	private static void print(AnnotationUnit annotationUnit) {

        if (annotationUnit != null && annotationUnit.getResources() != null) {
        	
            annotationUnit.getResources().stream().forEach(r -> System.out.println(r.getUri()));
        }
}
	public List<String> getNamedEntities(String textQuery){
		List<String> namedEntities = new ArrayList<String>();
		AnnotationUnit annotationUnit;
		Spotlight spotlight = new Spotlight();

        try {
			annotationUnit = spotlight.get(textQuery);
			  if (annotationUnit != null && annotationUnit.getResources() != null) {
		            annotationUnit.getResources().stream().forEach(r -> namedEntities.add(r.getUri()));
		        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return namedEntities;
	}
}
