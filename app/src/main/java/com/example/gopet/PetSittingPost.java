package com.example.gopet;

import java.util.List;

public class PetSittingPost {

    public String id;
    public String ownerId;
    public List<String> animalIds;
    public String startDate;
    public String endDate;
    public String aproximative_location, exact_location;
    public String notes;
    public String acceptedUserId;
    public String acceptedUsername;
    public boolean isActive = true;

    public String ownerUsername;
    public boolean isAcceptedByMe = false;

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String price;

    public PetSittingPost() {
    }

    public PetSittingPost(String id, String ownerId, List<String> animalIds, String startDate, String endDate,
                          String exact_location, String aproximative_location, String notes, String price) {
        this.id = id;
        this.ownerId = ownerId;
        this.animalIds = animalIds;
        this.startDate = startDate;
        this.endDate = endDate;
        this.exact_location = exact_location;
        this.notes = notes;
        this.isActive = true;
        this.aproximative_location=aproximative_location;
        this.price = price;
    }
}
