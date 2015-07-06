package io.github.lazyluck.easywiki.util;

import java.io.Serializable;
import java.util.List;

/***************************************
 * Serializable class for passing list objects between Activities
 ***************************************/

public class SerializableList implements Serializable {

    private List<?> title_list;

    public SerializableList(List<?> title_list){
        this.title_list = title_list;
    }

    public List<?> getList(){
        return title_list;
    }
}