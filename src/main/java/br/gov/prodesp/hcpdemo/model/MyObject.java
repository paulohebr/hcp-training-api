package br.gov.prodesp.hcpdemo.model;

import lombok.Data;

import java.time.Instant;

@Data
public class MyObject {
    private String owner;
    private Long id;
    private Instant signDate;
}
