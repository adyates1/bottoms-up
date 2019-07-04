package com.mishappstudios.bottomsup;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Class for representing a Theme
 */
public class Theme {
    private String name;
    private ArrayList<Category> categories;

    public Theme(String nameP) {
        this.name = nameP;
    }

    public String getName() {
        return name;
    }

    public void setCategories(InputStream is) {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );
        String line;
        ArrayList<Category> categoriesN = new ArrayList<>();
        try {
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                String[] categoryRaw = line.split(",");

                if (lineNo != 0) {
                    String name = categoryRaw[0];
                    String filePath = categoryRaw[1];
                    int cost = Integer.parseInt(categoryRaw[2]);
                    Category c = new Category(name, filePath, cost);
                    categoriesN.add(c);
                }
                lineNo ++;
            }
            this.categories = categoriesN;
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addCategory(Category category){
        this.categories.add(category);
    }

    public void setCategories(ArrayList<Category> categories){
        this.categories = categories;
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }
}