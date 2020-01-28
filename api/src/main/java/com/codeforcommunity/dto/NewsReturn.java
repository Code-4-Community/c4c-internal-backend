package com.codeforcommunity.dto;

import java.time.LocalDateTime;

public class NewsReturn {
  // doesnt make sense that we would ever set these values after construction.
  // favor immutibility.
  private final int id;
  private final String title;
  private final String description;
  private final String imageUrl;
  private final String author;
  private final LocalDateTime date;
  private final String content;

  public NewsReturn(int id, String title, String description, String author, LocalDateTime date, String content, String imageUrl) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.imageUrl = imageUrl;
    this.author = author;
    this.date = date;
    this.content = content;
  }

  public int getId() {
    return this.id;
  }

  public String getTitle() {
    return this.title;
  }

  public String getDescription() {
    return this.description;
  }

  public String getImageUrl(){
    return this.imageUrl;
  }

  public String getAuthor() {
    return this.author;
  }

  public LocalDateTime getDate() {
    return this.date;
  }

  public String getContent() {
    return this.content;
  }

  @Override
  public String toString() {
    return this.id + " " + this.title + " " + this.description + " " + this.author + " " + this.date.toString() + " "
        + this.content;
  }
}
