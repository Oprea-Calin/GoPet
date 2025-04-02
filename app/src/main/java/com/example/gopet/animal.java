package com.example.gopet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class animal {

    String name;
    String breed;
    String age;
<<<<<<< Updated upstream
    private String imageBase64;
=======
    String imageBase64;
>>>>>>> Stashed changes

    public animal(){

    }
<<<<<<< Updated upstream
    public Bitmap decodeImage() {
        byte[] decodedString = Base64.decode(this.imageBase64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
=======
>>>>>>> Stashed changes
    public animal(String name, String breed, String age, String imageBase64) {
        this.age = age;
        this.breed = breed;
        this.name = name;
        this.imageBase64 = imageBase64;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }



}
