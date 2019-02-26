package lodsearch.spotlightner;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import lodsearch.spotlightner.common.AnnotationUnit;
import lodsearch.spotlightner.common.ResourceItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.apache.http.HttpHeaders.ACCEPT;
import static lodsearch.spotlightner.common.Constants.EMPTY;
import static lodsearch.spotlightner.common.Prefixes.DBPEDIA_ONTOLOGY;
import static lodsearch.spotlightner.common.Prefixes.SCHEMA_ONTOLOGY;

public class Spotlight {


    private static final String URL = "https://api.dbpedia-spotlight.org/annotate";
    private final HttpClient client;
    private final HttpPost request;

    public Spotlight() {

        client = new DefaultHttpClient();//HttpClientBuilder.create().build();
        request = new HttpPost(URL);

        init();

    }

    private void init() {

        request.addHeader(ACCEPT, "application/json");

    }

    private AnnotationUnit get() throws IOException {

        Gson gson = new Gson();

        AnnotationUnit annotationUnit = gson.fromJson(getContent(), AnnotationUnit.class);

        fixPrefixes(annotationUnit.getResources());

        return annotationUnit;
    }


    private String fixPrefixes(String value) {

        if (value != null && !value.isEmpty()) {
            return value.replace("Http", "http").
                    replace("DBpedia:", DBPEDIA_ONTOLOGY).
                    replace("Schema:", SCHEMA_ONTOLOGY);
        }
        return value;

    }

    private void fixPrefixes(ResourceItem resource) {
        resource.setTypes(fixPrefixes(resource.getTypes()));
    }


    private void fixPrefixes(List<ResourceItem> resources) {

        if (resources != null && !resources.isEmpty()) {

            resources.forEach(resourceItem -> fixPrefixes(resourceItem));

        }

    }

    private String getContent() throws IOException {

        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();

        String line = EMPTY;

        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }

    public AnnotationUnit get(URL url) throws IOException {

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("url", url.toString()));
        request.setEntity(new UrlEncodedFormEntity(params));

        return get();
    }

    public AnnotationUnit get(String text) throws IOException {

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("text", text));
        request.setEntity(new UrlEncodedFormEntity(params));


        return get();
    }
}