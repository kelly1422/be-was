package model;

import java.util.HashMap;
import java.util.Map;

public class RequestDto {
    private String method;
    private String path;
    private String methodAndPath;
    private Map<String, String> params;
    private Map<String, String> headers;

    public RequestDto(String method, String path) {
        this.method = method;
        this.path = path;
        this.methodAndPath = method + " " + path;
        this.params = new HashMap<>();
        this.headers = new HashMap<>();
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getMethodAndPath() {
        return methodAndPath;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getReferer() {
        return headers.get("Referer");
    }

    public void addParam(String paramKey, String paramValue) {
        this.params.put(paramKey, paramValue);
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public String headersToString() {
        return  "\n-------------------------------------\n" +
                ("http method: " + this.method + "\n") +
                ("path: " + this.path + "\n") +
                ("Host: " + headers.get("Host") + "\n") +
                "-------------------------------------\n";
    }
}
