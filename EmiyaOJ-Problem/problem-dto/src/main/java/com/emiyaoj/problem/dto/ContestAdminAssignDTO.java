package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ContestAdminAssignDTO implements Serializable {

    private List<Long> userIds;
}
