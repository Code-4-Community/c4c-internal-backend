package com.codeforcommunity.api;

import com.codeforcommunity.dto.ApplicantReturn;
import com.codeforcommunity.dto.EventReturn;
import com.codeforcommunity.dto.NewsReturn;
import com.codeforcommunity.dto.UserReturn;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IProcessor {

  List<NewsReturn> getAllNews();

  Optional<NewsReturn> createNews(
      String title,
      String description,
      String imageUrl,
      String author,
      LocalDateTime date,
      String content);

  Optional<NewsReturn> getNews(int id);

  Optional<NewsReturn> updateNews(
      int id,
      String title,
      String description,
      String imageUrl,
      String author,
      LocalDateTime date,
      String content);

  Optional<NewsReturn> deleteNews(int id);

  List<ApplicantReturn> getAllApplicants();

  Optional<ApplicantReturn> createApplicant(
      int userId,
      byte[] fileBLOB,
      String fileType,
      String[] interests,
      String priorInvolvement,
      String whyJoin);

  Optional<ApplicantReturn> getApplicant(int userId);

  Optional<ApplicantReturn> updateApplicant(
      int userId,
      byte[] fileBLOB,
      String fileType,
      String[] interests,
      String priorInvolvement,
      String whyJoin);

  Optional<ApplicantReturn> deleteApplicant(int userId);

  List<UserReturn> getEventUsers(int eventId);

  boolean attendEvent(String eventCode, int userId);

  List<EventReturn> getAllEvents();

  Optional<EventReturn> createEvent(
      String name,
      String subtitle,
      String description,
      String imageUrl,
      LocalDateTime date,
      boolean open,
      String eventCode);

  Optional<EventReturn> getEvent(int id);

  Optional<EventReturn> updateEvent(
      int id,
      String name,
      String subtitle,
      String description,
      String imageUrl,
      LocalDateTime date,
      boolean open,
      String code);

  Optional<EventReturn> deleteEvent(int id);

  List<UserReturn> getAllUsers();

  Optional<UserReturn> addUser(
      String email,
      String first,
      String last,
      String hashedPassword,
      int currentYear,
      String major,
      int yearOfGraduation,
      String college,
      String gender);

  Optional<UserReturn> getUserByEmail(String email);

  Optional<UserReturn> getUser(int id);

  Optional<UserReturn> updateUser(
      int id,
      String email,
      String first,
      String last,
      String hashedPassword,
      int currentYear,
      String major,
      int yearOfGraduation,
      String college,
      String gender);

  Optional<UserReturn> deleteUser(int id);

  boolean validate(String email, String password);

  boolean isBlacklistedToken(String jti);

  boolean addBlacklistedToken(String jti);

  boolean clearBlacklistedTokens(long tokenDuration);
}
