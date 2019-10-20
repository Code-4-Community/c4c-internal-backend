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
