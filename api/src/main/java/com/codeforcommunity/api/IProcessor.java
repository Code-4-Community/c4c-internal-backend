package com.codeforcommunity.api;

import com.codeforcommunity.dto.UserReturn;
import com.codeforcommunity.dto.EventReturn;

import java.util.List;
import java.time.LocalDateTime;

public interface IProcessor {
  /**
   * Get all the users first and last names.
   */

  List<UserReturn> getEventUsers(int eventCode);

  boolean attendEvent(String eventCode, int userId);

  List<EventReturn> getAllEvents();

  boolean createEvent(String name, LocalDateTime date, boolean open, String code);
  
  EventReturn getEvent(int id);
  
  boolean updateEvent(int id, String name, LocalDateTime date, boolean open, String code);
  
  boolean deleteEvent(int id);

  List<UserReturn> getAllUsers();

  boolean addUser(String email, String first, String last, String hashedPassword);

  UserReturn getUserByEmail(String email);

  UserReturn getUser(int id);

  boolean updateUser(int id, String email, String first, String last, String hashedPassword);

  boolean deleteUser(int id);

  boolean validate(String email, String password);

  boolean isBlacklistedToken(String jwt);

  boolean addBlacklistedToken(String jwt);

  boolean clearBlacklistedTokens(long tokenDuration);
}
