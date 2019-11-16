package com.codeforcommunity.dto;

import java.time.LocalDateTime;

public class EventReturn {
  // doesnt make sense that we would ever set these values after construction.
  // favor immutibility.
  public final int id;
  public final String name;
  public final LocalDateTime date;
  public final boolean open;


  public EventReturn(int id, String name, LocalDateTime date, boolean open) {
    this.id = id;
    this.name = name;
    this.date = date;
    this.open = open;
    
  }

}