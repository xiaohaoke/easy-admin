package com.laker.admin.framework.aop.trace;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Span {
    private String id;
    private String name;
    private String className;
    private String methodName;
    private long startTime;
    private long endTime;
    private String routeName;
    private long cost;
    private SpanType methodType;
    private int order;
    private int level = 0;
    private List<Span> childs = new ArrayList<>();
}
