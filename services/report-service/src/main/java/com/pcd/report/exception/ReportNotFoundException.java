package com.pcd.report.exception;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=true)
@Data
public class ReportNotFoundException extends RuntimeException {
    private final String msg ;
}