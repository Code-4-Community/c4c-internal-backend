package com.codeforcommunity.processor;

import com.codeforcommunity.api.IProcessor;
import com.codeforcommunity.dto.EventReturn;
import com.codeforcommunity.dto.UserReturn;
import com.codeforcommunity.dto.ApplicantReturn;
import com.codeforcommunity.dto.NewsReturn;
import org.jooq.Result;
import org.jooq.exception.NoDataFoundException;
import org.jooq.generated.tables.records.ApplicantsRecord;
import org.jooq.generated.tables.records.EventCheckInsRecord;
import org.jooq.generated.tables.records.EventsRecord;
import org.jooq.generated.tables.records.NewsRecord;
import org.jooq.generated.tables.records.UsersRecord;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.generated.Tables;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import com.codeforcommunity.util.UpdatableBCrypt;
import java.sql.Timestamp;

public class ProcessorImpl implements IProcessor {

  private final DSLContext db;

  public ProcessorImpl(DSLContext db) {
    this.db = db;
  }

  // News

  @Override
  public List<NewsReturn> getAllNews() {
    List<NewsReturn> news = db.selectFrom(Tables.NEWS).fetchInto(NewsReturn.class);
    return news;
  }

  @Override
  public Optional<NewsReturn> createNews(String title, String description, String imageUrl, String author,
      LocalDateTime date, String content) {
    try {

      NewsRecord newsToCreate = db.newRecord(Tables.NEWS);

      newsToCreate.setTitle(title);
      newsToCreate.setDescription(description);
      newsToCreate.setImageUrl(imageUrl);
      newsToCreate.setAuthor(author);
      newsToCreate.setDate(Timestamp.valueOf(date));
      newsToCreate.setContent(content);

      newsToCreate.store();

      return Optional.of(newsToCreate.into(NewsReturn.class));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<NewsReturn> getNews(int id) {
    try {
      NewsReturn result = db.select().from(Tables.NEWS).where(Tables.NEWS.ID.eq(id)).fetchSingleInto(NewsReturn.class);
      return Optional.of(result);
    } catch (NoDataFoundException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<NewsReturn> updateNews(int id, String title, String description, String imageUrl, String author,
      LocalDateTime date, String content) {
    try {

      NewsRecord newsToUpdate = db.fetchOne(Tables.NEWS, Tables.NEWS.ID.eq(id));
      newsToUpdate.setTitle(title);
      newsToUpdate.setDescription(description);
      newsToUpdate.setImageUrl(imageUrl);
      newsToUpdate.setAuthor(author);
      newsToUpdate.setDate(Timestamp.valueOf(date));
      newsToUpdate.setContent(content);

      newsToUpdate.store();

      return Optional.of(newsToUpdate.into(NewsReturn.class));

    } catch (Exception e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<NewsReturn> deleteNews(int id) {
    try {
      Optional<NewsReturn> ret = getNews(id);
      db.delete(Tables.NEWS).where(Tables.NEWS.ID.eq(id)).execute();
      return ret;
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  // Applicants

  public List<ApplicantReturn> getAllApplicants() {
    List<ApplicantReturn> applicants = db.selectFrom(Tables.APPLICANTS).fetchInto(ApplicantReturn.class);
    return applicants;
  }

  public Optional<ApplicantReturn> createApplicant(int userId, byte[] fileBLOB, String fileType, String[] interests,
      String priorInvolvement, String whyJoin) {
    try {

      ApplicantsRecord applicantToCreate = db.newRecord(Tables.APPLICANTS);

      applicantToCreate.setUserId(userId);
      applicantToCreate.setResume(fileBLOB);
      applicantToCreate.setFileType(fileType);
      applicantToCreate.setInterests((Object[]) interests);
      applicantToCreate.setPriorInvolvement(priorInvolvement);
      applicantToCreate.setWhyJoin(whyJoin);

      applicantToCreate.store();

      return Optional.of(applicantToCreate.into(ApplicantReturn.class));
    } catch (Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<ApplicantReturn> getApplicant(int userId) {
    try {
      ApplicantReturn result = db.select().from(Tables.APPLICANTS).where(Tables.APPLICANTS.USER_ID.eq(userId))
          .fetchSingleInto(ApplicantReturn.class);
      return Optional.of(result);
    } catch (NoDataFoundException e) {
      return Optional.empty();
    }
  }

  public Optional<ApplicantReturn> updateApplicant(int userId, byte[] fileBLOB, String fileType, String[] interests,
      String priorInvolvement, String whyJoin) {

    try {
      ApplicantsRecord applicantToUpdate = db.fetchOne(Tables.APPLICANTS, Tables.APPLICANTS.USER_ID.eq(userId));

      applicantToUpdate.setUserId(userId);
      applicantToUpdate.setResume(fileBLOB);
      applicantToUpdate.setFileType(fileType);
      applicantToUpdate.setInterests((Object[]) interests);
      applicantToUpdate.setPriorInvolvement(priorInvolvement);
      applicantToUpdate.setWhyJoin(whyJoin);

      applicantToUpdate.store();

      return Optional.of(applicantToUpdate.into(ApplicantReturn.class));
    } catch (Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<ApplicantReturn> deleteApplicant(int userId) {
    try {
      Optional<ApplicantReturn> ret = getApplicant(userId);
      db.delete(Tables.APPLICANTS).where(Tables.APPLICANTS.USER_ID.eq(userId)).execute();
      return ret;
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  // Event Check-ins

  @Override
  public List<UserReturn> getEventUsers(int eventId) {
    List<UserReturn> users = db
        .select().from(Tables.USERS).where(Tables.USERS.ID.in(db.select(Tables.EVENT_CHECK_INS.USER_ID)
            .from(Tables.EVENT_CHECK_INS).where(Tables.EVENT_CHECK_INS.EVENT_ID.eq(eventId))))
        .fetchInto(UserReturn.class);

    return users;
  }

  @Override
  public boolean attendEvent(String eventCode, int userId) {
    EventReturn event;
    try {
      event = db.select().from(Tables.EVENTS).where(Tables.EVENTS.CODE.eq(eventCode))
          .fetchSingleInto(EventReturn.class);
    } catch (NoDataFoundException e) {
      e.printStackTrace();
      return false;
    }

    if (!event.getOpen() || LocalDateTime.now().compareTo(LocalDateTime.parse(event.getDate())) >= 0) {
      return false;
    }

    try {
      EventCheckInsRecord checkinToCreate = db.fetchOne(Tables.EVENT_CHECK_INS,
          Tables.EVENT_CHECK_INS.USER_ID.eq(userId));

      checkinToCreate.setUserId(userId);
      checkinToCreate.setEventId(event.getId());
      checkinToCreate.store();

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  // Events

  @Override
  public List<EventReturn> getAllEvents() {
    List<EventReturn> events = db.selectFrom(Tables.EVENTS).fetchInto(EventReturn.class);
    return events;
  }

  @Override
  public Optional<EventReturn> createEvent(String name, String subtitle, String description, String imageUrl,
      LocalDateTime date, boolean open, String eventCode) {
    try {

      EventsRecord eventToCreate = db.newRecord(Tables.EVENTS);

      eventToCreate.setName(name);
      eventToCreate.setSubtitle(subtitle);
      eventToCreate.setDescription(description);
      eventToCreate.setImageUrl(imageUrl);
      eventToCreate.setDate(Timestamp.valueOf(date));
      eventToCreate.setOpen(open);
      eventToCreate.setCode(eventCode);

      eventToCreate.store();

      return Optional.of(eventToCreate.into(EventReturn.class));
    } catch (NoDataFoundException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  @Override
  public Optional<EventReturn> getEvent(int id) {
    try {
      EventReturn result = db.select().from(Tables.EVENTS).where(Tables.EVENTS.ID.eq(id))
          .fetchSingleInto(EventReturn.class);
      return Optional.of(result);
    } catch (NoDataFoundException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  @Override
  public Optional<EventReturn> updateEvent(int id, String name, String subtitle, String description, String imageUrl,
      LocalDateTime date, boolean open, String code) {
    try {

      EventsRecord eventToUpdate = db.fetchOne(Tables.EVENTS, Tables.EVENTS.ID.eq(id));

      eventToUpdate.setName(name);
      eventToUpdate.setSubtitle(subtitle);
      eventToUpdate.setDescription(description);
      eventToUpdate.setImageUrl(imageUrl);
      eventToUpdate.setDate(Timestamp.valueOf(date));
      eventToUpdate.setOpen(open);
      eventToUpdate.setCode(code);

      eventToUpdate.store();

      return Optional.of(eventToUpdate.into(EventReturn.class));
    } catch (Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  @Override
  public Optional<EventReturn> deleteEvent(int id) {
    try {

      Optional<EventReturn> ret = getEvent(id);
      db.delete(Tables.EVENTS).where(Tables.EVENTS.ID.eq(id)).execute();
      return ret;
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  // Users/Authorization

  @Override
  public List<UserReturn> getAllUsers() {
    List<UserReturn> users = db.selectFrom(Tables.USERS).fetchInto(UserReturn.class);
    return users;
  }

  @Override
  public Optional<UserReturn> addUser(String email, String first, String last, String hashedPassword, int currentYear,
      String major, int yearOfGraduation, String college, String gender) {
    try {
      UsersRecord userToCreate = db.newRecord(Tables.USERS);

      userToCreate.setEmail(email);
      userToCreate.setFirstName(first);
      userToCreate.setLastName(last);
      userToCreate.setHashedPassword(hashedPassword);
      userToCreate.setCurrentYear(currentYear);
      userToCreate.setMajor(major);
      userToCreate.setYearOfGraduation(yearOfGraduation);
      userToCreate.setCollege(college);
      userToCreate.setGender(gender);

      userToCreate.store();

      return Optional.of(userToCreate.into(UserReturn.class));

    } catch (Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  @Override
  public Optional<UserReturn> getUserByEmail(String email) {
    try {
      UserReturn result = db.selectFrom(Tables.USERS).where(Tables.USERS.EMAIL.eq(email))
          .fetchSingleInto(UserReturn.class);

      return Optional.of(result);
    } catch (NoDataFoundException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<UserReturn> getUser(int id) {
    try {
      UserReturn result = db.selectFrom(Tables.USERS).where(Tables.USERS.ID.eq(id)).fetchSingleInto(UserReturn.class);

      return Optional.of(result);
    } catch (NoDataFoundException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<UserReturn> updateUser(int id, String email, String first, String last, String hashedPassword,
      int currentYear, String major, int yearOfGraduation, String college, String gender) {
    try {
      UsersRecord userToUpdate = db.fetchOne(Tables.USERS, Tables.USERS.ID.eq(id));

      userToUpdate.setEmail(email);
      userToUpdate.setFirstName(first);
      userToUpdate.setLastName(last);
      userToUpdate.setHashedPassword(hashedPassword);
      userToUpdate.setCurrentYear(currentYear);
      userToUpdate.setMajor(major);
      userToUpdate.setYearOfGraduation(yearOfGraduation);
      userToUpdate.setCollege(college);
      userToUpdate.setGender(gender);

      userToUpdate.store();

      return Optional.of(userToUpdate.into(UserReturn.class));
    } catch (Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  @Override
  public Optional<UserReturn> deleteUser(int id) {
    try {
      Optional<UserReturn> ret = getUser(id);
      db.delete(Tables.USERS).where(Tables.USERS.ID.eq(id)).execute();
      return ret;
    } catch (Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  @Override
  public boolean validate(String email, String password) {
    try {
      String storedPassword = db.select(Tables.USERS.HASHED_PASSWORD).from(Tables.USERS)
          .where(Tables.USERS.EMAIL.eq(email)).fetchOneInto(String.class);

      return storedPassword != null && UpdatableBCrypt.verifyHash(password, storedPassword);
    } catch (NoDataFoundException e) {
      e.printStackTrace();
      return false;
    }
  }

  // JWT Blacklisting

  @Override
  public boolean isBlacklistedToken(String jti) {
    Result<Record> result = db.select().from(Tables.BLACKLISTED_TOKENS).where(Tables.BLACKLISTED_TOKENS.ID.eq(jti))
        .fetch();
    return !result.isEmpty();
  }

  @Override
  public boolean addBlacklistedToken(String jti) {
    try {
      db.insertInto(Tables.BLACKLISTED_TOKENS, Tables.BLACKLISTED_TOKENS.ID,
          Tables.BLACKLISTED_TOKENS.TIME_MILLISECONDS).values(jti, System.currentTimeMillis()).execute();
    } catch (Exception e) {
      e.printStackTrace();
      // If this fails this is a security risk as there exists a token that is still
      // technically "valid" even though the user logged out
      return false;
    }
    return true;
  }

  @Override
  public boolean clearBlacklistedTokens(long tokenDuration) {
    try {
      db.delete(Tables.BLACKLISTED_TOKENS)
          .where(Tables.BLACKLISTED_TOKENS.TIME_MILLISECONDS.le(System.currentTimeMillis() - tokenDuration)).execute();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }
}
