package ro.code4.monitorizarevot.net.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class VersionResponse implements Serializable {

    // TODO add serialized names of the forms to be translated when api is updated
    // https://github.com/code4romania/monitorizare-vot/issues/67
    @Expose
    @SerializedName("versiune")
    private Map<String, Integer> versions = new HashMap<>();

    public Map<String, Integer> getVersions() {
        return versions;
    }
}
