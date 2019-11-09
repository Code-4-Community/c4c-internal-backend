package com.codeforcommunity.api;

import com.codeforcommunity.dto.MemberReturn;

import java.util.List;
import java.time.LocalDateTime;

public interface IProcessor {
  /**
   * Get all the members first and last names.
   */
  List<MemberReturn> getAllMembers();

  boolean attendMeeting(String meetingid, String username);
  boolean createMeeting(String id, String name, LocalDateTime date, boolean open);
  boolean addMember(String first, String last);
  boolean validate(String first, String last);
  boolean isBlacklistedToken(String jwt);
  boolean addBlacklistedToken(String jwt);
  boolean clearBlacklistedTokens(long tokenDuration);
}
