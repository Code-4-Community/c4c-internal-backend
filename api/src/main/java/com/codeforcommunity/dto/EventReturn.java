package com.codeforcommunity.dto;

import java.time.LocalDateTime;

public class EventReturn {
  // doesnt make sense that we would ever set these values after construction.
  // favor immutibility.
  private final int id;
  private final String name;
  private final String subtitle;
  private final String description;
  private final String imageUrl;
  private final LocalDateTime date;
  private final boolean open;
  private final String code;


  public EventReturn(int id, String name, String subtitle, String description, String imageUrl, LocalDateTime date, boolean open, String code) {
    this.id = id;
    this.name = name;
    this.subtitle = subtitle;
    this.description = description;
    this.imageUrl = imageUrl;
    this.date = date;
    this.open = open;
    this.code = code;
  }

  public int getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public String getSubtitle() {
    return this.subtitle;
  }

  public String getDescription() {
    return this.description;
  }

  public String getImageUrl() {
    return this.imageUrl;
  }

  public LocalDateTime getDate() {
    return this.date;
  }

  public boolean getOpen() {
    return this.open;
  }

  public String getCode() {
    return this.code;
  }

  @Override
  public String toString() {
    return this.id + " " + this.name + " " + this.date.toString() + " " + this.open + " " + this.code;
  }
}