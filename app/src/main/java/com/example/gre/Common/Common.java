package com.example.gre.Common;

import com.example.gre.Model.Question;
import com.example.gre.Model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Common {
    public static String category_id;
    public static User currentUser;
    public static List<Question> questionArrayList = new ArrayList<>();
    public static HashMap<String, Integer> scores_by_category = new HashMap<>();
}
