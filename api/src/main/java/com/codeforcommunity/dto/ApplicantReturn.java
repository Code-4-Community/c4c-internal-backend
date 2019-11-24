package com.codeforcommunity.dto;

import java.time.LocalDateTime;

public class ApplicantReturn {
  // doesnt make sense that we would ever set these values after construction.
  // favor immutibility.
  private final int userId;
  private final byte[] fileBLOB;
  private final String fileType;
  private final String[] interests;
  private final String priorInvolvement;
  private final String whyJoin;

  public ApplicantReturn(int userId, byte[] fileBLOB, String fileType, String[] interests, String priorInvolvement,
      String whyJoin) {
    this.userId = userId;
    this.fileBLOB = fileBLOB;
    this.fileType = fileType;
    this.interests = interests;
    this.priorInvolvement = priorInvolvement;
    this.whyJoin = whyJoin;

  }

  public int getUserId() {
    return this.userId;
  }

  public byte[] getFileBLOB() {
    return this.fileBLOB;
  }

  public String getFileType() {
    return this.fileType;
  }

  public String[] getInterests() {
    return this.interests;
  }

  public String getPriorInvolvement() {
    return this.priorInvolvement;
  }

  public String getWhyJoin() {
    return this.whyJoin;
  }

  @Override
  public String toString() {
    return this.userId + " " + this.fileType + " " + this.interests + " " + this.priorInvolvement + " " + this.whyJoin;
  }
}