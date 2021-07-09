package org.example;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class Source {
    private String code;
    private String name;
    private String count;

    public Source(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCount() {
        return count;
    }
}
