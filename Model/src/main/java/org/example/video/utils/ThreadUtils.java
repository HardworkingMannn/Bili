package org.example.video.utils;

public class ThreadUtils {
    private static ThreadLocal<Integer> local=new ThreadLocal<>();
    public static void set(int userId){
        local.set(userId);
    }
    public static Integer get(){
        return local.get();
    }
}
