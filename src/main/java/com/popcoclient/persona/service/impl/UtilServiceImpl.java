package com.popcoclient.persona.service.impl;

import com.popcoclient.user.entity.UserDetail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class UtilServiceImpl {
    private UtilServiceImpl(){
        throw new IllegalStateException("Utility class");
    }

    public static Map<String, Double> calculatePercentages(BigDecimal score1, BigDecimal score2) {
        Map<String, Double> resultMap = new HashMap<>();

        BigDecimal totalScore = score1.add(score2);

        int finalScale = 2; // 소수점 둘째 자리
        RoundingMode roundingMode = RoundingMode.HALF_UP;

        // 0으로 나누기 방지
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

    public static List<Integer> calculateIntegerPercentages(Integer score1, Integer score2) {
        List<Integer> resultList = new ArrayList<>();

        int totalScore = score1 + score2;

        if (totalScore == 0) {
            resultList.add(0);
            resultList.add(0);
            return resultList;
        }

        double percentage1 = ((double) score1 / totalScore) * 100.0;

        double mainPercentage = Math.floor(percentage1);

        double subPercentage = 100.0 - mainPercentage;

        resultList.add((int) mainPercentage);
        resultList.add((int) subPercentage);

        return resultList;
    }

    public static List<Integer> calculateGenderPercent(List<UserDetail> userDetails) {
        int maleCount = 0;
        int femaleCount = 0;

        for (UserDetail details : userDetails) {
            if ("M".equalsIgnoreCase(details.getGender())) {
                maleCount++;
            } else if ("F".equalsIgnoreCase(details.getGender())) {
                femaleCount++;
            }
        }

        List<Integer> genderDistribution = Arrays.asList(maleCount, femaleCount);
        return genderDistribution;
    }

    public static List<Integer> calculateBirthPercent(List<UserDetail> userDetails) {
        int[] ageBrackets = new int[6];
        int currentYear = LocalDate.now().getYear();

        for (UserDetail details : userDetails) {
            int age = currentYear - details.getBirthdate().getYear();
            if (age < 20) {
                ageBrackets[0]++;
            } else if (age < 30) {
                ageBrackets[1]++;
            } else if (age < 40) {
                ageBrackets[2]++;
            } else if (age < 50) {
                ageBrackets[3]++;
            } else if (age < 60) {
                ageBrackets[4]++;
            } else {
                ageBrackets[5]++;
            }
        }

        int totalUsers = Arrays.stream(ageBrackets).sum();
        if (totalUsers == 0) {
            return Arrays.asList(0, 0, 0, 0, 0, 0);
        }

        double[] percentages = new double[6];
        int[] finalPercentages = new int[6];
        double[] remainders = new double[6];
        int sumOfPercentages = 0;

        for (int i = 0; i < ageBrackets.length; i++) {
            percentages[i] = (double) ageBrackets[i] / totalUsers * 100;
            finalPercentages[i] = (int) percentages[i];
            remainders[i] = percentages[i] - finalPercentages[i];
            sumOfPercentages += finalPercentages[i];
        }

        int diff = 100 - sumOfPercentages;
        Integer[] remainderIndices = new Integer[6];
        for (int i = 0; i < 6; i++) {
            remainderIndices[i] = i;
        }

        Arrays.sort(remainderIndices, (Integer a, Integer b) ->
                Double.compare(remainders[b], remainders[a]));

        for (int i = 0; i < diff; i++) {
            finalPercentages[remainderIndices[i]]++;
        }

        List<Integer> result = new ArrayList<>();
        for (int p : finalPercentages) {
            result.add(p);
        }

        return result;
    }

    public static List<Integer> calcPerAvg(Integer myAction, Integer personaAction, Integer max){
        List<Integer> distributionList = new ArrayList<>();

        if (personaAction == 0) {
            distributionList.add(0);
            distributionList.add(0);
            return distributionList;
        }

        double percentage = ((double) personaAction / max) * 100.0;
        long roundedValue = Math.round(percentage);

        distributionList.add(myAction);
        distributionList.add((int)roundedValue);

        return distributionList;
    }

    public static List<Integer> calcPerEventAvg(Integer myAction, Integer totalPersonaParticipations, Integer totalPersonaUsers, Integer maxEvents){
        List<Integer> distributionList = new ArrayList<>();

        if (totalPersonaUsers == 0) {
            distributionList.add(myAction);
            distributionList.add(0);
            return distributionList;
        }

        double avgParticipation = (double) totalPersonaParticipations / totalPersonaUsers;
        int roundedAvg = (int) Math.ceil(avgParticipation);

        int finalAvg = Math.min(roundedAvg, maxEvents);

        distributionList.add(myAction);
        distributionList.add(finalAvg);

        return distributionList;
    }
}
