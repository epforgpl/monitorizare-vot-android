package ro.code4.monitorizarevot.net.model;

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

    @SuppressWarnings("unused")
    public District() {}

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

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        District district = (District) o;
        return id.equals(district.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
