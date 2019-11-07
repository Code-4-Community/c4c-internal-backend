package com.codeforcommunity.processor;

import com.codeforcommunity.api.IProcessor;
import com.codeforcommunity.dto.MemberReturn;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.generated.tables.pojos.Member;
import org.jooq.DSLContext;
import org.jooq.generated.Tables;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProcessorImpl implements IProcessor {

  private final DSLContext db;
  private int id = 10;

  public ProcessorImpl(DSLContext db) {
    this.db = db;
  }

  @Override
  public List<MemberReturn> getAllMembers() {
    List<Member> members = db.selectFrom(Tables.MEMBER).fetchInto(Member.class);
    return members.stream().map(member -> new MemberReturn(member.getFirstName(), member.getLastName()))
        .collect(Collectors.toList());
  }

  @Override
  public boolean attendMeeting(String meetingid, String username) {
    Result meetingResult = db.fetch("select * from meeting where id=?;", meetingid);
    // for now assume username is just the first name
    Result memberResult = db.fetch("select id from member where first_name=?;", username);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
    LocalDateTime date = LocalDateTime.parse(meetingResult.getValue(0, "date").toString(), formatter);
    boolean open = Boolean.parseBoolean(meetingResult.getValue(0, "open").toString());

    if (meetingResult.isEmpty() || memberResult.isEmpty() || !open || LocalDateTime.now().compareTo(date) >= 0)
      return false;

    try {
      String memberid = memberResult.getValue(0, "id").toString();
      db.execute("insert into member_attended_meeting\n" + "  (id, member_id, meeting_id)\n" + "  values (?, ?, ?);",
          (memberid + meetingid).hashCode(), memberid, meetingid);

    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public boolean createMeeting(String meetingid, String name, LocalDateTime date, boolean open) {
    try {
      db.execute("insert into meeting\n" + "  (id, name, date, open)\n" + "  values (?, ?, ?, ?);", meetingid, name,
          date, open);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public boolean addMember(String first, String last) {

    try {
      db.execute(
          "insert into member\n" + "  (id, email, first_name, last_name, graduation_year, major, privilege_level)\n"
              + "  values (?, 'N/A', '" + first + "', '" + last + "', \n" + "          2020, 'CS Probably', 0);",
          this.id);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Override

  public boolean validate(String first, String last){
    Result result = db.fetch("select first_name, last_name\n"
        + "   from member\n"
        + "   where first_name = ? and last_name = ?;", first, last);
    if (result.isEmpty())
      return false;
    return true;
  }
  
  public boolean attendedMeeting(String username /*,String meeting*/) { //will need to use meeting
    try {
    Result result = db.fetch("select id from member\n where first_name = '" + username + "'");
    String memberid = (String) result.getValue(0, 0);    
    Result result1 = db.fetch("select id from meeting"); // figure out which meeting
    String meetingid = (String) result.getValue(0, 0);     
    db.execute("insert into member_attended_meeting values "
        + "('1'," + memberid + "," + meetingid + ");");
    } catch(Exception e) {
      e.printStackTrace();
    return false;
    }
    return true;
  }
}
