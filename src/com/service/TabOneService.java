package com.service;

import org.codehaus.jackson.map.ObjectMapper;

public class TabOneService {
    public TabOneService() {

    }

    public String parseJson(String text) {
        String json = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object obj = mapper.readValue(text, Object.class);
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            json = e.getMessage();
        }
        if (json.contains("")) {
//            json = json.substring(json.indexOf("Source:"));
        }
        return json;
    }

    public String simplifyException(String exc) {
        String ret = "";

        return ret;
    }
}
