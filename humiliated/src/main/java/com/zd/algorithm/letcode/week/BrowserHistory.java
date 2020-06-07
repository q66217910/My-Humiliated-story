package com.zd.algorithm.letcode.week;

import java.util.ArrayList;
import java.util.List;

public class BrowserHistory {

    private List<String> list;

    private int index;

    public BrowserHistory(String homepage) {
        this.list = new ArrayList<>();
        list.add(homepage);
        this.index = 0;
    }

    /**
     * 执行此操作会把浏览历史前进的记录全部删除
     */
    public void visit(String url) {
        list.add(index+1,url);
        list = this.list.subList(0, index + 2);
        index = this.list.size() - 1;
    }


    /**
     * 在浏览历史中后退 steps 步。如果你只能在浏览历史中后退至多 x 步且 steps > x ，
     * 那么你只后退 x 步。请返回后退 至多 steps 步以后的 url 。
     */
    public String back(int steps) {
        int num = index - steps;
        index = Math.max(num, 0);
        return list.get(index);
    }


    /**
     * 在浏览历史中前进 steps 步。如果你只能在浏览历史中前进至多 x 步且 steps > x ，
     * 那么你只前进 x 步。请返回前进 至多 steps步以后的 url 。
     */
    public String forward(int steps) {
        int num = index + steps;
        if (num < list.size()) {
            index = num;
        } else {
            index = list.size() - 1;
        }
        return list.get(index);
    }

    public static void main(String[] args) {
        BrowserHistory browserHistory = new BrowserHistory("leetcode.com");
        browserHistory.visit("google.com");       // 你原本在浏览 "leetcode.com" 。访问 "google.com"
        browserHistory.visit("facebook.com");     // 你原本在浏览 "google.com" 。访问 "facebook.com"
        browserHistory.visit("youtube.com");      // 你原本在浏览 "facebook.com" 。访问 "youtube.com"
        browserHistory.back(1);                   // 你原本在浏览 "youtube.com" ，后退到 "facebook.com" 并返回 "facebook.com"
        browserHistory.back(1);                   // 你原本在浏览 "facebook.com" ，后退到 "google.com" 并返回 "google.com"
        browserHistory.forward(1);                // 你原本在浏览 "google.com" ，前进到 "facebook.com" 并返回 "facebook.com"
        browserHistory.visit("linkedin.com");     // 你原本在浏览 "facebook.com" 。 访问 "linkedin.com"
        browserHistory.forward(2);                // 你原本在浏览 "linkedin.com" ，你无法前进任何步数。
        browserHistory.back(2);                   // 你原本在浏览 "linkedin.com" ，后退两步依次先到 "facebook.com" ，然后到 "google.com" ，并返回 "google.com"
        browserHistory.back(7);
    }
}
