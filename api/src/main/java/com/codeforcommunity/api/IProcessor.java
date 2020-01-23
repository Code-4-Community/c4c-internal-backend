package com.codeforcommunity.api;

import com.codeforcommunity.dto.UserReturn;
import com.codeforcommunity.dto.EventReturn;
import com.codeforcommunity.dto.ApplicantReturn;
import com.codeforcommunity.dto.NewsReturn;

import java.util.List;
import java.util.Optional;

import java.time.LocalDateTime;

public interface IProcessor {

  List<NewsReturn> getAllNews();

  boolean createNews(String title, String description, String author, LocalDateTime date, String content);

  Optional<NewsReturn> getNews(int id);

  boolean updateNews(int id, String title, String description, String author, LocalDateTime date, String content);

  boolean deleteNews(int id);
  
  List<ApplicantReturn> getAllApplicants();

  boolean createApplicant(int userId, byte[] fileBLOB, String fileType, String[] interests, String priorInvolvement,
      String whyJoin);

  Optional<ApplicantReturn> getApplicant(int userId);

  boolean updateApplicant(int userId, byte[] fileBLOB, String fileType, String[] interests, String priorInvolvement, String whyJoin);

  boolean deleteApplicant(int userId);

  List<UserReturn> getEventUsers(int eventId);

  boolean attendEvent(String eventCode, int userId);

  List<EventReturn> getAllEvents();

  boolean createEvent(String name, LocalDateTime date, boolean open, String eventCode);
  
  Optional<EventReturn> getEvent(int id);
  
  boolean updateEvent(int id, String name, LocalDateTime date, boolean open, String code);
  
  boolean deleteEvent(int id);

  List<UserReturn> getAllUsers();

  boolean addUser(String email, String first, String last, String hashedPassword, int currentYear, String major);

  Optional<UserReturn> getUserByEmail(String email);

  Optional<UserReturn> getUser(int id);

  boolean updateUser(int id, String email, String first, String last, String hashedPassword, int currentYear,
      String major);

  boolean deleteUser(int id);

  boolean validate(String email, String password);

  boolean isBlacklistedToken(String jti);

  boolean addBlacklistedToken(String jti);

  boolean clearBlacklistedTokens(long tokenDuration);

  boolean validateEmail(String email);
}
