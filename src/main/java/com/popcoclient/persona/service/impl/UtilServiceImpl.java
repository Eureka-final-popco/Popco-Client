package com.popcoclient.persona.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class UtilServiceImpl {
    public static Map<String, Double> calculatePercentages(BigDecimal score1, BigDecimal score2) {
        Map<String, Double> resultMap = new HashMap<>();

        BigDecimal totalScore = score1.add(score2);

        int finalScale = 2; // 최종적으로 보여줄 소수점 둘째 자리
        RoundingMode roundingMode = RoundingMode.HALF_UP;

        // 합계가 0인 경우 (0으로 나누기 방지)
        if (totalScore.compareTo(BigDecimal.ZERO) == 0) {
            resultMap.put("main_percentage", 0.0);
            resultMap.put("sub_percentage", 0.0);
            return resultMap;
        }

        int calculationScale = 10;
        RoundingMode calculationRoundingMode = RoundingMode.HALF_UP;

        BigDecimal percentage1 = score1
                .divide(totalScore, calculationScale, calculationRoundingMode)
                .multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.DOWN);

        BigDecimal percentage2 = new BigDecimal("100").subtract(percentage1);

        resultMap.put("main_percentage", percentage1.doubleValue());
        resultMap.put("sub_percentage", percentage2.doubleValue());

        return resultMap;
    }
}
