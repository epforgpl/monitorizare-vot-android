package ro.code4.monitorizarevot.net.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class District extends RealmObject {

    @PrimaryKey
    private String id;

    private int level;

    private String title;

    private String parentId;

    private int branchesCount = 0;

    public static List<String> extractTitles(Collection<District> c) {
        List<String> titles = new ArrayList<>();
        for(District d : c) {
            titles.add(d.getTitle());
        }
        return titles;
    }

    public District(String id, int level, String title, String parentId) {
        this.id = id;
        this.level = level;
        this.title = title;
        this.parentId = parentId;
    }

    public String getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public String getTitle() {
        return title;
    }

    public String getParentId() {
        return parentId;
    }

    public int getBranchesCount() {
        return branchesCount;
    }

    public void incrementBranchesCount() {
        this.branchesCount++;
    }
}
