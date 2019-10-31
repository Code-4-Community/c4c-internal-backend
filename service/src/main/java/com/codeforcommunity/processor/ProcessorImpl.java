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
import java.time.LocalDate;

public class ProcessorImpl implements IProcessor {

  private final DSLContext db;
  private int id = 10;

  public ProcessorImpl(DSLContext db) {
    this.db = db;
  }

  @Override
  public List<MemberReturn> getAllMembers() {
    List<Member> members = db.selectFrom(Tables.MEMBER).fetchInto(Member.class);
    return members.stream()
        .map(member -> new MemberReturn(member.getFirstName(), member.getLastName()))
        .collect(Collectors.toList());
  }

  @Override
  public boolean attendMeeting(String meetingid, String username) {
    Result meetingResult = db.fetch("select * from meeting where id=?;", meetingid);
    // for now assume username is just the first name
    Result memberResult = db.fetch("select id from member where first_name=?;", username);

    if(meetingResult.isEmpty() || memberResult.isEmpty())
      return false;
    

    try {
      String memberid = memberResult.getValue(0, "id").toString();
      System.out.println(memberid);
      db.execute("insert into member_attended_meeting\n"
        + "  (id, member_id, meeting_id)\n"
        + "  values (?, ?, ?);",
          (memberid+meetingid).hashCode(), memberid, meetingid);
      
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public boolean createMeeting(String meetingid, String name, String date, boolean open) {
    try {
      System.out.println(meetingid);
      System.out.println(name);
      System.out.println(date);
      System.out.println(open);
      db.execute(
          "insert into meeting\n" 
              + "  (id, name, date, open)\n"
              + "  values (?, ?, ?, ?);",
          meetingid, name, LocalDate.parse(date), open);
    } catch (Exception e) {
      System.out.println(e);
      return false;
    }
    return true;
  }

  @Override
  public boolean addMember(String first, String last) {

    try {
      db.execute("insert into member\n"
          + "  (id, email, first_name, last_name, graduation_year, major, privilege_level)\n"
          + "  values (?, 'N/A', '" + first + "', '" + last + "', \n"
          + "          2020, 'CS Probably', 0);", this.id);
    } catch(Exception e) {
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
}
