package com.zd.algorithm.letcode.week;

import java.util.*;
import java.util.stream.Collectors;

public class Twitter {

    private Map<Integer, Set<Integer>> followMap;

    private List<TwitterUser> twitterUsers;

    /**
     * Initialize your data structure here.
     */
    public Twitter() {
        followMap = new HashMap<>();
        twitterUsers = new ArrayList<>();
    }

    /**
     * Compose a new tweet.
     */
    public void postTweet(int userId, int tweetId) {
        if (!followMap.containsKey(userId)) {
            Set<Integer> set = new HashSet<>();
            set.add(userId);
            followMap.put(userId, set);
        }
        twitterUsers.add(0,new TwitterUser(userId, tweetId));
    }

    /**
     * Retrieve the 10 most recent tweet ids in the user's news feed. Each item in the news feed must be posted by users who the user followed or by the user herself. Tweets must be ordered from most recent to least recent.
     */
    public List<Integer> getNewsFeed(int userId) {
        Set<Integer> followeeIds = followMap.get(userId);
        return twitterUsers.stream()
                .filter(a ->followeeIds!=null&&followeeIds.contains(a.userId))
                .map(TwitterUser::getTweetId)
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Follower follows a followee. If the operation is invalid, it should be a no-op.
     */
    public void follow(int followerId, int followeeId) {
        if (!followMap.containsKey(followerId)) {
            Set<Integer> set = new HashSet<>();
            set.add(followerId);
            followMap.put(followerId, set);
        }
        followMap.computeIfPresent(followerId, (k, v) -> {
            v.add(followeeId);
            return v;
        });
    }

    /**
     * Follower unfollows a followee. If the operation is invalid, it should be a no-op.
     */
    public void unfollow(int followerId, int followeeId) {
        if (followerId!=followeeId&&followMap.containsKey(followerId)){
            followMap.computeIfPresent(followerId, (k, v) -> {
                v.remove(followeeId);
                return v;
            });
        }
    }

    class TwitterUser {

        private Integer userId;
        private Integer tweetId;

        public Integer getTweetId() {
            return tweetId;
        }

        public TwitterUser(Integer userId, Integer tweetId) {
            this.userId = userId;
            this.tweetId = tweetId;
        }
    }
}
