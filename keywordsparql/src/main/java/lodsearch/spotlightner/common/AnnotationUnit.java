package lodsearch.spotlightner.common;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static lodsearch.spotlightner.common.Constants.DBPEDIA;
import static lodsearch.spotlightner.common.Constants.HTTP;
import static lodsearch.spotlightner.common.Constants.SCHEMA;
import static lodsearch.spotlightner.common.Prefixes.DBPEDIA_ONTOLOGY;
import static lodsearch.spotlightner.common.Prefixes.SCHEMA_ONTOLOGY;

@Getter
@Setter
@NoArgsConstructor
public class AnnotationUnit {

    @SerializedName("@text")
    private String text;

    @SerializedName("@confidence")
    private String confidence;

    @SerializedName("@support")
    private String support;

    @SerializedName("@types")
    private String types;

    @SerializedName("@sparql")
    private String sparql;

    @SerializedName("@policy")
    private String policy;

    @SerializedName("Resources")
    private List<ResourceItem> resources;

    public Integer endIndex() {
        if (text != null) {
            return text.length();
        }
        return 0;
    }

    public String getTypes() {
        if (types != null && !types.isEmpty()) {
            return types.replace("Http", HTTP).
                    replace(DBPEDIA, DBPEDIA_ONTOLOGY).
                    replace(SCHEMA, SCHEMA_ONTOLOGY);
        }
        return types;
    }

    public Integer beginIndex() {
        return 1;
    }
}