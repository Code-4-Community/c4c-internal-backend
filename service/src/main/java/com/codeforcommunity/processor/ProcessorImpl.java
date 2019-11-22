package com.codeforcommunity.processor;

import com.codeforcommunity.api.IProcessor;
import com.codeforcommunity.dto.EventReturn;
import com.codeforcommunity.dto.UserReturn;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.exception.NoDataFoundException;
import org.jooq.generated.tables.pojos.Users;

import antlr.debug.Event;
import io.vertx.core.cli.Option;

import org.jooq.generated.tables.pojos.Events;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.generated.Tables;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import java.io.Console;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.codeforcommunity.util.UpdatableBCrypt;
import java.sql.Timestamp;

public class ProcessorImpl implements IProcessor {

  private final DSLContext db;
  // id should really not be a static number! see down below for dynamic id, hash
  // of username
  // private int id = 10;

  public ProcessorImpl(DSLContext db) {
    this.db = db;
  }

  @Override
  public List<UserReturn> getAllUsers() {
    List<Users> users = db.selectFrom(Tables.USERS).fetchInto(Users.class);
    return users.stream()
        .map(user -> new UserReturn(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
            user.getGraduationYear().toString(), user.getMajor(), user.getPrivilegeLevel()))
        .collect(Collectors.toList());
  }

  @Override
  public List<UserReturn> getEventUsers(int eventId) {
    // List<Users> users = db.selectFrom(Tables.USERS).fetchInto(Users.class);
    List<Users> users = db
        .fetch("SELECT * FROM users CROSS JOIN (SELECT * FROM event_check_ins where id = ?) AS x;", eventId)
        .into(Users.class);
    return users.stream()
        .map(user -> new UserReturn(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
            user.getGraduationYear().toString(), user.getMajor(), user.getPrivilegeLevel()))
        .collect(Collectors.toList());
  }

  @Override
  public boolean attendEvent(String eventCode, int userid) {
    try {
      Result eventResult = db.fetch("select * from events where code = ?;", eventCode);

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
      LocalDateTime date = LocalDateTime.parse(eventResult.getValue(0, "date").toString(), formatter);
      boolean open = Boolean.parseBoolean(eventResult.getValue(0, "open").toString());
      int eventid = Integer.parseInt(eventResult.getValue(0, "id").toString());
      if (eventResult.isEmpty() || !open || LocalDateTime.now().compareTo(date) >= 0)
        return false;

      try {
        db.execute("insert into event_check_ins\n" + "  (id, user_id, event_id)\n" + "  values (DEFAULT, ?, ?);",
            userid, eventid);

      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  @Override
  public List<EventReturn> getAllEvents() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
    List<Events> events = db.selectFrom(Tables.EVENTS).fetchInto(Events.class);
    return events.stream()
        .map(event -> new EventReturn(event.getId(), event.getName(),
            LocalDateTime.parse(event.getDate().toString(), formatter), event.getOpen(), event.getCode()))
        .collect(Collectors.toList());
  }

  @Override
  public boolean createEvent(String name, LocalDateTime date, boolean open, String eventCode) {
    try {
      db.execute("insert into events\n" + "  (id, name, date, open, code)\n" + "  values (DEFAULT, ?, ?, ?, ?);", name,
          date, open, eventCode);
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
      db.execute("update events set \n" + " name = ?, date = ?, open = ?, code = ? \n" + "  where id = ?;", name, date,
          open, id, code);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public boolean deleteEvent(int id) {
    try {
      db.execute("delete from events \n" + "where id = ?;", id);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public boolean addUser(String email, String first, String last, String hashedPassword) {
    try {
      db.execute("insert into users\n"
          + "  (id, email, first_name, last_name, hashed_password, graduation_year, major, privilege_level)\n"
          + "  values (DEFAULT, ?, ?, ?, ?, \n" + "2020, 'CS Probably', 0);", email, first, last, hashedPassword);
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
          Integer.toString(result.getGraduationYear()), result.getMajor(), result.getPrivilegeLevel());
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
          Integer.toString(result.getGraduationYear()), result.getMajor(), result.getPrivilegeLevel());
      return Optional.of(ret);
    } catch (NoDataFoundException e) {
      return Optional.empty();
    }
  }

  @Override
  public boolean updateUser(int id, String email, String first, String last, String hashedPassword) {
    try {
      db.execute("update users set \n" + "  email = ?, first_name = ?, last_name = ?, hashed_password = ?\n"
          + "  where id = ?;", email, first, last, hashedPassword, id);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  @Override
  public boolean deleteUser(int id) {
    try {
      db.execute("delete from users \n" + "where id = ?;", id);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public boolean validate(String email, String password) {
    Result result = db.fetch("select hashed_password \n" + "   from users\n" + "   where email = ?;", email);

    try {
      return UpdatableBCrypt.verifyHash(password, result.getValue(0, "hashed_password").toString());
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public boolean isBlacklistedToken(String jwt) {
    Result result = db.fetch("select * \n" + "   from blacklisted_tokens\n" + "   where id = ?;", jwt);
    if (result.isEmpty())
      return false;
    return true;
  }

  @Override
  public boolean addBlacklistedToken(String jwt) {
    try {
      db.execute("insert into blacklisted_tokens\n" + "  (id, time_milliseconds)\n" + "  values (?, ?);", jwt,
          System.currentTimeMillis());
    } catch (Exception e) {
      // If this fails this is a security risk as there exists a token that is still
      // technically "valid" even though the user logged out
      return false;
    }
    return true;
  }

  @Override
  public boolean clearBlacklistedTokens(long tokenDuration) {
    try {
      db.execute("delete from blacklisted_tokens\n" + " where time_millisecond < ?;",
          System.currentTimeMillis() - tokenDuration);
    } catch (Exception e) {
      return false;
    }
    return true;
  }
}
