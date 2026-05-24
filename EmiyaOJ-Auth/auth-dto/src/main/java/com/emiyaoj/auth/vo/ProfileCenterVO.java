package com.emiyaoj.auth.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileCenterVO implements Serializable {

    private PublicUserVO user;

    private Integer solvedCount;

    private Integer totalSubmitCount;

    private Integer acceptedSubmitCount;

    private BigDecimal passRate;

    private List<DifficultySolvedStatsVO> difficultyStats;

    private Integer blogCount;

    private Integer starCount;

    private Integer likedBlogCount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PublicUserVO implements Serializable {

        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        private String username;

        private String nickname;

        private String avatar;

        private LocalDateTime createTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DifficultySolvedStatsVO implements Serializable {

        private Integer difficulty;

        private String difficultyDesc;

        private Integer solvedCount;
    }
}
