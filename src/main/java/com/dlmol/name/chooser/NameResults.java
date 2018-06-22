package com.dlmol.name.chooser;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmolineu on 8/7/16.
 */
public class NameResults {
    private List<Name> names;
    private List<String> resultList;

    public NameResults() {
        names = new ArrayList<>();
        resultList = new ArrayList<>();
    }

    public String getResultDetails(){
        return StringUtils.join(resultList, "\n");
    }

    public void addResult(String result) {
        this.resultList.add(result);
    }

    public List<Name> getNames() { return names; }

    public String getChosenName() {
        if (names == null || names.size() == 0)
            return "";
        else
            return names.get(names.size() - 1).getName();
    }

    public void setNames(List<Name> names) {
        this.names = names;
    }
}
