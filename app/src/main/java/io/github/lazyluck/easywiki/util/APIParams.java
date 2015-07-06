package io.github.lazyluck.easywiki.util;

import java.util.HashMap;
import java.util.Map;

/***************************************
 * Wikipedia API parameters to be used in POST.
 ***************************************/

public class APIParams {

    public static Map<String,String> defaultListParams;

    // - Must be called/initialized in main Activity
    public static void initParams() {
        if (defaultListParams == null) {
            defaultListParams = new HashMap<String, String>();
            defaultListParams.put("format", "json");
            defaultListParams.put("action", "query");
            defaultListParams.put("list", "search");
            defaultListParams.put("rawcontinue", "");
            defaultListParams.put("srprop", "");
            defaultListParams.put("srsearch", "");
        }
    }

    // - Update all parameters with the entered string, should be called before search
    public static void updateSearchTerm(String searchterm) {
        defaultListParams.put("srsearch", searchterm);
    }

}