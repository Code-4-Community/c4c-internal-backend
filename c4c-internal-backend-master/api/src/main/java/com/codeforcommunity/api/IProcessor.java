package com.codeforcommunity.api;

import com.codeforcommunity.dto.MemberReturn;

import java.util.List;

public interface IProcessor {
  /**
   * Get all the members first and last names.
   */
  List<MemberReturn> getAllMembers();

  boolean addMember(String first, String last);
  boolean validate(String first, String last);
}
