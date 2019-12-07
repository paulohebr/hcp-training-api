package br.gov.prodesp.hcpdemo.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class MyObject {
    public static final String ANNOTATION = "my-object";

    private String owner;
    private Long id;

    private ZonedDateTime signDate;
}
