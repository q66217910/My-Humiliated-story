package com.zd.algorithm.letcode.week;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class UndergroundSystem {

    Map<Integer, String> enter = new HashMap<>();
    Map<Integer, Integer> time = new HashMap<>();
    Map<String, Long> sum = new HashMap<>();
    Map<String, Integer> cnt = new HashMap<>();

    public UndergroundSystem() {

    }

    public void checkIn(int id, String stationName, int t) {
        enter.put(id, stationName);
        time.put(id, t);
    }

    private String concate(String a, String b) {
        return a + ".-." + b;
    }

    public void checkOut(int id, String stationName, int t) {
        String e = enter.get(id);
        String way = concate(e, stationName);
        Integer f = time.get(id);
        sum.put(way, sum.getOrDefault(way, 0L) + t - f);
        cnt.put(way, cnt.getOrDefault(way, 0) + 1);
    }

    public double getAverageTime(String startStation, String endStation) {
        String way = concate(startStation, endStation);
        return (double) sum.get(way) / cnt.get(way);
    }

    public int trap(int[] height) {
        int ans = 0, current = 0;
        Stack<Integer> st = new Stack<>();
        while (current < height.length) {
            while (!st.empty() && height[current] > height[st.peek()]) {
                int top = st.peek();
                st.pop();
                if (st.empty())
                    break;
                int distance = current - st.peek() - 1;
                int bounded_height = Math.min(height[current], height[st.peek()]) - height[top];
                ans += distance * bounded_height;
            }
            st.push(current++);
        }
        return ans;
    }

    public static void main(String[] args) {
        UndergroundSystem undergroundSystem = new UndergroundSystem();
        undergroundSystem.checkIn(45, "Leyton", 3);
        undergroundSystem.checkIn(32, "Paradise", 8);
        undergroundSystem.checkIn(27, "Leyton", 10);
        undergroundSystem.checkOut(45, "Waterloo", 15);
        undergroundSystem.checkOut(27, "Waterloo", 20);
        undergroundSystem.checkOut(32, "Cambridge", 22);
        undergroundSystem.getAverageTime("Paradise", "Cambridge");       // 返回 14.0。从 "Paradise"（时刻 8）到 "Cambridge"(时刻 22)的行程只有一趟
        undergroundSystem.getAverageTime("Leyton", "Waterloo");          // 返回 11.0。总共有 2 躺从 "Leyton" 到 "Waterloo" 的行程，编号为 id=45 的乘客出发于 time=3 到达于 time=15，编号为 id=27 的乘客于 time=10 出发于 time=20 到达。所以平均时间为 ( (15-3) + (20-10) ) / 2 = 11.0
        undergroundSystem.checkIn(10, "Leyton", 24);
        undergroundSystem.getAverageTime("Leyton", "Waterloo");          // 返回 11.0
        undergroundSystem.checkOut(10, "Waterloo", 38);
        undergroundSystem.getAverageTime("Leyton", "Waterloo");
    }
}
