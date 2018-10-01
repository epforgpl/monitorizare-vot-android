package ro.code4.monitorizarevot.net.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class FormVersion extends RealmObject implements Serializable {

    @PrimaryKey
    @Expose
    private String id;

    @Expose
    private Integer version;

    public FormVersion() {}

    public FormVersion(String formCode, Integer version) {
        this.id = formCode;
        this.version = version;
    }

    public Integer getVersion() {
        return version;
    }

    public String getId() { return id; }
}
