package com.codeforcommunity.api;

import com.codeforcommunity.dto.NewsReturn;
import com.codeforcommunity.dto.UserReturn;
import com.codeforcommunity.dto.EventReturn;

import java.util.List;
import java.util.Optional;

import java.time.LocalDateTime;

public interface IProcessor {

  List<UserReturn> getEventUsers(int eventId);

  boolean attendEvent(String eventCode, int userId);

  List<EventReturn> getAllEvents();

  boolean createEvent(String name, LocalDateTime date, boolean open, String eventCode);
  
  Optional<EventReturn> getEvent(int id);
  
  boolean updateEvent(int id, String name, LocalDateTime date, boolean open, String code);
  
  boolean deleteEvent(int id);

  List<UserReturn> getAllUsers();

  boolean addUser(String email, String first, String last, String hashedPassword);

  Optional<UserReturn> getUserByEmail(String email);

  Optional<UserReturn> getUser(int id);

  boolean updateUser(int id, String email, String first, String last, String hashedPassword);

  boolean deleteUser(int id);

  boolean validate(String email, String password);

  boolean isBlacklistedToken(String jti);

  boolean addBlacklistedToken(String jti);

  boolean clearBlacklistedTokens(long tokenDuration);

  List<NewsReturn> getAllNews();

  boolean createNews(String title, String description, String author, LocalDateTime date, String content);

  Optional<NewsReturn> getNews(int id);

  boolean updateNews(int id, String title, String description, String author, LocalDateTime date, String content);

  boolean deleteNews(int id);
}
