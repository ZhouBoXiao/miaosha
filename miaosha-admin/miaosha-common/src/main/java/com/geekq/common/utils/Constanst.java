package com.geekq.miaosha.utils;

public class Constanst {
    public static String CLOSE_ORDER_INFO_TASK_LOCK = "CLOSE_ORDER_INFO_KEY";

    public static String COUNTLOGIN = "count:login";


    public enum orderStaus{
        ORDER_NOT_PAY("新建未支付");

        orderStaus(String name){
            this.name=name;
        }

        private  String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
