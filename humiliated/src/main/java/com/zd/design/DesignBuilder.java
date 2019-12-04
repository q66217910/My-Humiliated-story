package com.zd.design;

import lombok.Builder;

public class DesignBuilder {

    public static void main(String[] args) {
        User.builder().userId("1").userName("江俊宽");
    }

    @Builder
    private static class User{

        private String userId;
        private String userName;

    }

}
