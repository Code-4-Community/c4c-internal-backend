package com.codeforcommunity.processor;

import com.codeforcommunity.api.IProcessor;
import com.codeforcommunity.dto.EventReturn;
import com.codeforcommunity.dto.NewsReturn;
import com.codeforcommunity.dto.UserReturn;
import com.codeforcommunity.dto.ApplicantReturn;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.exception.NoDataFoundException;
import org.jooq.generated.tables.pojos.Users;

import antlr.debug.Event;
import io.vertx.core.cli.Option;

import org.jooq.generated.tables.pojos.Events;
import org.jooq.generated.tables.pojos.Applicants;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.generated.Tables;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.Optional;
import java.io.Console;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.codeforcommunity.util.UpdatableBCrypt;
import java.sql.Timestamp;

public class ProcessorImpl implements IProcessor {

  private final DSLContext db;

  public ProcessorImpl(DSLContext db) {
    this.db = db;
  }

  public List<ApplicantReturn> getAllApplicants() {
    List<Applicants> applicants = db.selectFrom(Tables.APPLICANTS).fetchInto(Applicants.class);
    return applicants.stream()
        .map(applicant -> new ApplicantReturn(applicant.getUserId(), applicant.getResume(), applicant.getFileType(),
            Arrays.copyOf(applicant.getInterests(), applicant.getInterests().length, String[].class),
            applicant.getPriorInvolvement(), applicant.getWhyJoin()))
        .collect(Collectors.toList());
  }

  public boolean createApplicant(int userId, byte[] fileBLOB, String fileType, String[] interests,
      String priorInvolvement, String whyJoin) {
    try {
      db.insertInto(Tables.APPLICANTS, Tables.APPLICANTS.USER_ID, Tables.APPLICANTS.RESUME, Tables.APPLICANTS.FILE_TYPE,
          Tables.APPLICANTS.INTERESTS, Tables.APPLICANTS.PRIOR_INVOLVEMENT, Tables.APPLICANTS.WHY_JOIN)
          .values(userId, fileBLOB, fileType, interests, priorInvolvement, whyJoin).execute();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public Optional<ApplicantReturn> getApplicant(int userId) {
    try {
      Applicants result = db.select().from(Tables.APPLICANTS).where(Tables.APPLICANTS.USER_ID.eq(userId))
          .fetchSingleInto(Applicants.class);

      ApplicantReturn ret = new ApplicantReturn(result.getUserId(), result.getResume(), result.getFileType(),
          Arrays.copyOf(result.getInterests(), result.getInterests().length, String[].class),
          result.getPriorInvolvement(), result.getWhyJoin());

      return Optional.of(ret);
    } catch (NoDataFoundException e) {
      return Optional.empty();
    }
  }

  public boolean updateApplicant(int userId, byte[] fileBLOB, String fileType, String[] interests,
      String priorInvolvement, String whyJoin) {

    try {
      db.update(Tables.APPLICANTS).set(Tables.APPLICANTS.RESUME, fileBLOB).set(Tables.APPLICANTS.FILE_TYPE, fileType)
          .set(Tables.APPLICANTS.INTERESTS, interests).set(Tables.APPLICANTS.PRIOR_INVOLVEMENT, priorInvolvement)
          .set(Tables.APPLICANTS.WHY_JOIN, whyJoin).where(Tables.APPLICANTS.USER_ID.eq(userId)).execute();

    } catch (Exception e) {
      return false;
    }
    return true;
  }

  public boolean deleteApplicant(int userId) {
    try {
      db.delete(Tables.APPLICANTS).where(Tables.APPLICANTS.USER_ID.eq(userId)).execute();
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public List<UserReturn> getAllUsers() {
    List<Users> users = db.selectFrom(Tables.USERS).fetchInto(Users.class);
    return users.stream().map(user -> new UserReturn(user.getId(), user.getEmail(), user.getFirstName(),
        user.getLastName(), user.getGraduationYear(), user.getMajor(), user.getPrivilegeLevel()))
        .collect(Collectors.toList());
  }

  @Override
  public List<UserReturn> getEventUsers(int eventId) {
    // List<Users> users = db.selectFrom(Tables.USERS).fetchInto(Users.class);

    // I dont know how to turn this SQL statement into JOOQ
    // db.fetch(
    // "SELECT * FROM USERS INNER JOIN (SELECT user_id FROM event_check_ins where
    // event_id = ?) as Z ON USERS.id = Z.user_id;",
    // eventId).into(Users.class);
    List<Users> users = db.select().from(Tables.USERS)
        .where(Tables.USERS.ID.in(db.select(Tables.EVENT_CHECK_INS.USER_ID).from(Tables.EVENT_CHECK_INS)
            .where(Tables.EVENT_CHECK_INS.EVENT_ID.eq(eventId))))
        .fetchInto(Users.class);

    return users.stream().map(user -> new UserReturn(user.getId(), user.getEmail(), user.getFirstName(),
        user.getLastName(), user.getGraduationYear(), user.getMajor(), user.getPrivilegeLevel()))
        .collect(Collectors.toList());
  }

  @Override
  public boolean attendEvent(String eventCode, int userid) {
    EventReturn result;
    try {
      result = db.select().from(Tables.EVENTS).where(Tables.EVENTS.CODE.eq(eventCode))
          .fetchSingleInto(EventReturn.class);
    } catch (NoDataFoundException e) {
      return false;
    }

    if (result == null || !result.getOpen() || LocalDateTime.now().compareTo(result.getDate()) >= 0)
      return false;

    try {
      db.insertInto(Tables.EVENT_CHECK_INS, Tables.EVENT_CHECK_INS.USER_ID, Tables.EVENT_CHECK_INS.EVENT_ID)
          .values(userid, result.getId()).execute();

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  @Override
  public List<EventReturn> getAllEvents() {
    // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
    // HH:mm:ss.S");
    List<EventReturn> events = db.selectFrom(Tables.EVENTS).fetchInto(EventReturn.class);
    return events;
  }

  @Override
  public boolean createEvent(String name, LocalDateTime date, boolean open, String eventCode) {
    try {
      db.insertInto(Tables.EVENTS, Tables.EVENTS.NAME, Tables.EVENTS.DATE, Tables.EVENTS.OPEN, Tables.EVENTS.CODE)
          .values(name, Timestamp.valueOf(date), open, eventCode).execute();
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public Optional<EventReturn> getEvent(int id) {
    try {
      EventReturn result = db.select().from(Tables.EVENTS).where(Tables.EVENTS.ID.eq(id))
          .fetchSingleInto(EventReturn.class);
      return Optional.of(result);
    } catch (NoDataFoundException e) {
      return Optional.empty();
    }
  }

  @Override
  public boolean updateEvent(int id, String name, LocalDateTime date, boolean open, String code) {
    try {
      db.update(Tables.EVENTS).set(Tables.EVENTS.NAME, name).set(Tables.EVENTS.DATE, Timestamp.valueOf(date))
          .set(Tables.EVENTS.OPEN, open).set(Tables.EVENTS.CODE, code).where(Tables.EVENTS.ID.eq(id)).execute();

    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public boolean deleteEvent(int id) {
    try {
      db.delete(Tables.EVENTS).where(Tables.EVENTS.ID.eq(id)).execute();
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public List<NewsReturn> getAllNews() {
    // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
    // HH:mm:ss.S");
    List<NewsReturn> news = db.selectFrom(Tables.NEWS).fetchInto(NewsReturn.class);
    return news;
  }

  @Override
  public boolean createNews(String title, String description, String author, LocalDateTime date, String content) {
    try {
      /*
       * db.execute("insert into events\n" + "  (id, name, date, open, code)\n" +
       * "  values (DEFAULT, ?, ?, ?, ?);", name, date, open, eventCode);
       */

      db.insertInto(Tables.NEWS, Tables.NEWS.TITLE, Tables.NEWS.DESCRIPTION, Tables.NEWS.AUTHOR, Tables.NEWS.DATE, Tables.NEWS.CONTENT)
              .values(title, description, author, Timestamp.valueOf(date), content).execute();
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public Optional<NewsReturn> getNews(int id) {
    try {
      NewsReturn result = db.select().from(Tables.NEWS).where(Tables.NEWS.ID.eq(id))
              .fetchSingleInto(NewsReturn.class);
      return Optional.of(result);
    } catch (NoDataFoundException e) {
      return Optional.empty();
    }
  }

  @Override
  public boolean updateNews(int id, String title, String description, String author, LocalDateTime date, String content) {
    try {
      // db.execute("update events set \n" + " name = ?, date = ?, open = ?, code = ?
      // \n" + " where id = ?;", name, date,
      // open, id, code);

      db.update(Tables.NEWS).set(Tables.NEWS.TITLE, title).set(Tables.NEWS.DESCRIPTION, description)
              .set(Tables.NEWS.AUTHOR, author).set(Tables.NEWS.DATE, Timestamp.valueOf(date))
              .set(Tables.NEWS.CONTENT, content).where(Tables.NEWS.ID.eq(id)).execute();

    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public boolean deleteNews(int id) {
    try {
      // db.execute("delete from events \n" + "where id = ?;", id);

      db.delete(Tables.NEWS).where(Tables.NEWS.ID.eq(id)).execute();

    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public boolean addUser(String email, String first, String last, String hashedPassword) {
    try {
      // dont know if we want the graduation year and major yet so in the meantime
      // just dont change what was here before
      // db.execute("insert into users\n"
      // + " (id, email, first_name, last_name, hashed_password, graduation_year,
      // major, privilege_level)\n"
      // + " values (DEFAULT, ?, ?, ?, ?, \n" + "2020, 'CS Probably', 0);", email,
      // first, last, hashedPassword);

      db.insertInto(Tables.USERS, Tables.USERS.EMAIL, Tables.USERS.FIRST_NAME, Tables.USERS.LAST_NAME,
          Tables.USERS.HASHED_PASSWORD, Tables.USERS.GRADUATION_YEAR, Tables.USERS.MAJOR, Tables.USERS.PRIVILEGE_LEVEL)
          .values(email, first, last, hashedPassword, 2020, "Computer Science", 0).execute();

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  @Override
  public Optional<UserReturn> getUserByEmail(String email) {
    try {
      Users result = db.select().from(Tables.USERS).where(Tables.USERS.EMAIL.eq(email)).fetchSingleInto(Users.class);
      UserReturn ret = new UserReturn(result.getId(), result.getEmail(), result.getFirstName(), result.getLastName(),
          result.getGraduationYear(), result.getMajor(), result.getPrivilegeLevel());
      return Optional.of(ret);
    } catch (NoDataFoundException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<UserReturn> getUser(int id) {
    try {
      Users result = db.select().from(Tables.USERS).where(Tables.USERS.ID.eq(id)).fetchSingleInto(Users.class);
      UserReturn ret = new UserReturn(result.getId(), result.getEmail(), result.getFirstName(), result.getLastName(),
          result.getGraduationYear(), result.getMajor(), result.getPrivilegeLevel());
      return Optional.of(ret);
    } catch (NoDataFoundException e) {
      return Optional.empty();
    }
  }

  @Override
  public boolean updateUser(int id, String email, String first, String last, String hashedPassword) {
    try {
      db.update(Tables.USERS).set(Tables.USERS.EMAIL, email).set(Tables.USERS.FIRST_NAME, first)
          .set(Tables.USERS.LAST_NAME, last).set(Tables.USERS.HASHED_PASSWORD, hashedPassword)
          .where(Tables.USERS.ID.eq(id)).execute();

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  @Override
  public boolean deleteUser(int id) {
    try {
      db.delete(Tables.USERS).where(Tables.USERS.ID.eq(id)).execute();
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public boolean validate(String email, String password) {
    try {
      String storedPassword = db.select(Tables.USERS.HASHED_PASSWORD).from(Tables.USERS)
          .where(Tables.USERS.EMAIL.eq(email)).fetchOneInto(String.class);

      return UpdatableBCrypt.verifyHash(password, storedPassword);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public boolean isBlacklistedToken(String jti) {
    Result result = db.select().from(Tables.BLACKLISTED_TOKENS).where(Tables.BLACKLISTED_TOKENS.ID.eq(jti)).fetch();
    if (result.isEmpty())
      return false;
    return true;
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
      /*
       * db.execute("delete from blacklisted_tokens\n" +
       * " where time_millisecond < ?;", System.currentTimeMillis() - tokenDuration);
       */
      db.delete(Tables.BLACKLISTED_TOKENS)
          .where(Tables.BLACKLISTED_TOKENS.TIME_MILLISECONDS.le(System.currentTimeMillis() - tokenDuration)).execute();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }
}
