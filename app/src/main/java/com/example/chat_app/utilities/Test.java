package com.example.chat_app.utilities;

import java.util.Random;

public class Test {
    public static String generateRandomNumberString() {
        // Tạo đối tượng Random
        Random random = new Random();

        // Tạo một chuỗi có 4 kí tự
        StringBuilder sb = new StringBuilder(4);

        // Thêm 4 số ngẫu nhiên vào chuỗi
        for (int i = 0; i < 4; i++) {
            sb.append(random.nextInt(10));
        }

        // Trả về chuỗi kết quả
        return sb.toString();
    }
}
