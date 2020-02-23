package com.zd.algorithm.letcode.week;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Week177 {

    public static void main(String[] args) {
        System.out.println(daysBetweenDates("2019-06-29", "2019-06-30"));
    }

    public static int daysBetweenDates(String date1, String date2) {
        int ret = 0;
        int RNum = 0;
        int[] moth = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        String[] date3 = date1.split("-");
        String[] date4 = date2.split("-");
        int year1 = Integer.parseInt(date3[0]);
        int year2 = Integer.parseInt(date4[0]);
        int month1 = Integer.parseInt(date3[1]);
        int month2 = Integer.parseInt(date4[1]);
        int day1 = Integer.parseInt(date3[2]);
        int day2 = Integer.parseInt(date4[2]);
        RNum = countYear(year1, year2, month1, month2);
        ret = (year1 - year2) * 365;
        for (int i = 0; i < month1; i++) {
            ret += moth[i];
        }
        for (int i = 0; i < month2; i++) {
            ret -= moth[i];
        }
        return ret - day2 + day1 + RNum;
    }

    private static int countYear(int year1, int year2, int month1, int month2) {
        int ret = 0;
        int end = year1;
        int start = year2;
        if (month1 >= 2) {
            end += 1;
        }
        if (month2 >= 2) {
            start += 1;
        }
        for (int i = start; i < end; i++) {
            if (isRUN(i)) {
                ret++;
            }
        }
        return ret;
    }

    private static boolean isRUN(int year) {
        if (year % 400 == 0) {
            return true;
        }
        if (year % 100 == 0) {
            return true;
        }
        if (year % 4 == 0) {
            return true;
        }
        return false;
    }

    class Entry {

        Entry left;

        Entry right;

        int value;

        public Entry(int value) {
            this.value = value;
        }
    }

    public boolean validateBinaryTreeNodes(int n, int[] leftChild, int[] rightChild) {
        Entry parent = new Entry(0);
        for (int i = 0; i < Math.abs(leftChild.length - rightChild.length); i++) {
            if (leftChild[i] != -1) {
                parent.left = new Entry(0);
            }
        }
        return false;
    }
}
