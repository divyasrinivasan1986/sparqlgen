package lodsearch.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import lodsearch.index.HDTIndex;

@Component
public class InitializeHDT  implements ApplicationListener<ContextRefreshedEvent>{
	
	private static final String dataSource = "/home/divya/lod/dbpedia2016-04en.hdt";
		
	
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// TODO Auto-generated method stub
//		HDTIndex.setHDTSparql(new HDTSparql(dataSource));
	}


}