package com.emiyaoj.judge.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestCaseGeneratorRunRequestDTO implements Serializable {

    private String generatorCode;
}
