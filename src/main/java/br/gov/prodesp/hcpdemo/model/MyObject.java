package br.gov.prodesp.hcpdemo.model;

import lombok.Data;

import java.time.Instant;

@Data
public class MyObject {
    public static final String ANNOTATION = "my-object";

    private String owner;
    private Long id;
    private Instant signDate;
}
