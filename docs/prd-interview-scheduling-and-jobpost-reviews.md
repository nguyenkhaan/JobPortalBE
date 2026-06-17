# PRD: Interview Scheduling and JobPost Reviews

## Problem Statement

Hệ thống JobPortal hiện đã hỗ trợ `JobSeeker` gửi `JobApplication`, `Employer` quản lý ứng viên theo trạng thái, và notification qua `IN_APP`/`DEVICE`. Tuy nhiên luồng phỏng vấn vẫn chưa có cách đặt lịch hẹn rõ ràng giữa `Employer` và `JobSeeker`.

Từ góc nhìn người dùng:

- `Employer` muốn chuyển một `JobApplication` sang giai đoạn xem xét/phỏng vấn và đưa ra nhiều khung thời gian có thể gặp ứng viên.
- `JobSeeker` muốn nhận thông báo, chọn một thời gian phù hợp, hoặc được hệ thống xem là từ chối nếu không phản hồi sau 24 giờ.
- `Employer` muốn được thông báo khi `JobSeeker` đã chọn lịch, sau đó tiếp tục đưa ra `Application Decision` bằng cách chuyển ứng viên sang `ACCEPTED` hoặc `REJECTED`.
- `JobSeeker` muốn đánh giá `JobPost` sau khi đã có phiên phỏng vấn, bao gồm rating 1-5 sao và comment.
- Người xem `JobPost` muốn thấy review/comment để có thêm tín hiệu về chất lượng bài đăng và trải nghiệm tuyển dụng.

## Solution

Bổ sung một luồng `Interview Session` gắn với `JobApplication`, cho phép `Employer` tạo đề xuất lịch phỏng vấn gồm nhiều time slots khi chuyển application sang `REVIEWING` hoặc `INTERVIEW`. Hệ thống gửi notification cho `JobSeeker`; `JobSeeker` mở form chọn một khung giờ trong danh sách. Nếu `JobSeeker` không chọn trong 24 giờ kể từ thời điểm lời mời được gửi, phiên hẹn được xem là từ chối/expired. Khi `JobSeeker` chọn lịch, hệ thống xác nhận phiên phỏng vấn và gửi notification lại cho `Employer`.

Bổ sung `JobPost Review` cho phép `JobSeeker` đã apply vào `JobPost` và đã có phiên phỏng vấn hoàn tất được gửi rating 1-5 sao và comment. Review được hiển thị trong Job Detail, kèm thống kê average rating và review count.

## User Stories

1. As an Employer, I want to see a screen for managing interview sessions, so that I can track scheduled interviews separately from the general candidate list.
2. As an Employer, I want to open a scheduling dialog when moving a Job Application to reviewing, so that I can propose interview times during the candidate review flow.
3. As an Employer, I want to select multiple available date-time options, so that the JobSeeker can choose the slot that works best.
4. As an Employer, I want each proposed slot to include date and time, so that there is no ambiguity about the interview schedule.
5. As an Employer, I want to add an optional meeting note or location/link, so that the JobSeeker knows how the interview will happen.
6. As an Employer, I want to submit the interview proposal from the status change flow, so that scheduling is part of the normal hiring workflow.
7. As an Employer, I want the system to validate that proposed interview times are in the future, so that invalid sessions are not sent.
8. As an Employer, I want the system to prevent empty slot lists, so that the JobSeeker always has at least one option.
9. As an Employer, I want to see the pending response deadline, so that I know when a JobSeeker will be considered declined.
10. As an Employer, I want to receive a notification when the JobSeeker chooses an interview time, so that I can prepare for the confirmed interview.
11. As an Employer, I want to see which slot the JobSeeker selected, so that I can attend the correct interview time.
12. As an Employer, I want to see interview sessions filtered by pending, confirmed, declined, expired, completed, or cancelled status, so that I can manage the pipeline efficiently.
13. As an Employer, I want to cancel or reschedule a pending interview proposal, so that I can correct mistakes before the JobSeeker confirms.
14. As an Employer, I want to reschedule a confirmed interview by sending a new set of slots, so that schedule changes can be handled without manual coordination.
15. As an Employer, I want cancelled or expired sessions to be visible in history, so that the hiring audit trail remains clear.
16. As an Employer, I want to mark an interview session as completed, so that the JobSeeker can become eligible to review the JobPost.
17. As an Employer, I want to move a Job Application to ACCEPTED after the interview, so that I can approve the candidate.
18. As an Employer, I want to move a Job Application to REJECTED after the interview, so that I can close the candidate's application.
19. As an Employer, I want interview session data to appear in Job Application detail, so that I can understand the application timeline.
20. As an Employer, I want the management screen to show candidate name, JobPost title, application status, interview status, selected slot, and response deadline, so that I can scan work quickly.
21. As a JobSeeker, I want to receive a notification when an Employer proposes interview times, so that I know action is required.
22. As a JobSeeker, I want the notification to open the interview selection form, so that I can respond with minimal friction.
23. As a JobSeeker, I want to see all available interview slots from the Employer, so that I can choose the best time.
24. As a JobSeeker, I want to confirm exactly one interview slot, so that the schedule is clear to both sides.
25. As a JobSeeker, I want to see the 24-hour response deadline, so that I understand when the proposal will expire.
26. As a JobSeeker, I want the system to treat no response after 24 hours as declined, so that stale proposals do not remain pending forever.
27. As a JobSeeker, I want to receive confirmation after selecting a slot, so that I know the interview has been scheduled.
28. As a JobSeeker, I want to see my scheduled interview sessions in my application detail, so that I can track upcoming interviews.
29. As a JobSeeker, I want to see whether an interview is pending, confirmed, expired, declined, completed, or cancelled, so that I understand the current state.
30. As a JobSeeker, I want to be prevented from selecting an expired slot, so that I do not confirm an invalid interview.
31. As a JobSeeker, I want to be prevented from selecting a slot from another user's application, so that private hiring data remains protected.
32. As a JobSeeker, I want to be notified if the Employer cancels or reschedules the interview, so that I do not attend an outdated session.
33. As a JobSeeker, I want to rate a JobPost after I have completed an interview for that JobPost, so that I can share my hiring experience.
34. As a JobSeeker, I want to write a comment when reviewing a JobPost, so that I can explain the reason behind my rating.
35. As a JobSeeker, I want rating to be limited from 1 to 5 stars, so that review data is consistent.
36. As a JobSeeker, I want comment length to be validated, so that reviews stay readable and safe for display.
37. As a JobSeeker, I want to review only JobPosts that I applied to, so that reviews come from real candidates.
38. As a JobSeeker, I want to review only after a completed interview session, so that reviews reflect an actual interview experience.
39. As a JobSeeker, I want to edit my review if I made a mistake, so that my feedback remains accurate.
40. As a JobSeeker, I want to delete my review if needed, so that I retain control over my feedback.
41. As a JobSeeker, I want to be limited to one active review per JobPost, so that duplicate reviews do not distort ratings.
42. As a visitor, I want to see JobPost reviews on Job Detail, so that I can evaluate candidate experience before applying.
43. As a visitor, I want to see average rating and review count on Job Detail, so that I can understand the overall feedback quickly.
44. As a visitor, I want to see review author display name, rating, comment, and created date, so that reviews feel trustworthy and useful.
45. As a visitor, I want reviews to be paginated, so that Job Detail remains performant for popular JobPosts.
46. As an Admin, I want review data to be auditable, so that inappropriate or abusive content can be investigated later.
47. As an Admin, I want soft-deleted reviews and sessions to preserve history, so that moderation and audit use cases remain possible.
48. As a Backend developer, I want interview scheduling to reuse existing Job Application ownership rules, so that authorization behavior stays consistent.
49. As a Backend developer, I want notifications to be published from service-layer business actions, so that controllers stay thin.
50. As a Frontend developer, I want stable API contracts for interview sessions and JobPost reviews, so that FE screens can be implemented independently.

## Implementation Decisions

- Build a new `Interview Session` domain concept linked to exactly one `JobApplication`.
- A `JobApplication` may have multiple interview sessions over time, but only one active pending/confirmed session at a time.
- Add an interview session status model with at least: `PENDING_SELECTION`, `CONFIRMED`, `DECLINED`, `EXPIRED`, `CANCELLED`, `COMPLETED`.
- Store proposed interview slots as child records of the interview session, not as a free-form string, so each option can be selected, validated, and audited.
- Each interview slot stores its scheduled date-time and optional metadata needed by FE display.
- Store the selected slot on the interview session after `JobSeeker` confirms.
- Store response deadline as `expiresAt = createdAt + 24 hours` by default.
- Treat proposals that pass `expiresAt` without a selected slot as expired/declined from the JobSeeker side.
- Implement expiration through a scheduled backend job and also guard all read/selection operations against expired pending sessions.
- When `Employer` moves a `JobApplication` to `REVIEWING` and submits slots, create an interview session and send notification to the `JobSeeker`.
- If the team wants a stricter state model, use `INTERVIEW` for confirmed interview stage and reserve `REVIEWING` for pre-interview review; otherwise allow scheduling from `REVIEWING` and transition to `INTERVIEW` when a slot is confirmed.
- Existing `JobApplicationStatus` values should remain compatible: `PENDING`, `REVIEWING`, `INTERVIEW`, `OFFER`, `REJECTED`, `ACCEPTED`.
- `Employer` can make the final `Application Decision` by changing the application to `ACCEPTED` or `REJECTED` after interview.
- Notification for interview proposal should use both `IN_APP` and `DEVICE` channels.
- Notification copy should be in English to match prior notification PRD decisions.
- Add notification types such as `INTERVIEW_PROPOSED`, `INTERVIEW_CONFIRMED`, `INTERVIEW_DECLINED`, `INTERVIEW_EXPIRED`, `INTERVIEW_CANCELLED`, and `INTERVIEW_COMPLETED`.
- Notification target URLs should deep-link to FE screens:
  - JobSeeker selection form: `/job-seeker/interviews/{sessionId}`
  - Employer session detail: `/employer/interviews/{sessionId}`
- FE Employer must add an interview/session management screen with filters, pagination, and session detail.
- FE Employer candidate status drag/drop or status update flow must open a date-time selection dialog when moving an application into the interview scheduling path.
- FE JobSeeker must show a slot selection form from notification and from application detail.
- FE Job Detail must show rating summary and paginated reviews.
- FE Job Detail must show a review form only when the authenticated JobSeeker is eligible.
- Add `JobPost Review` linked to `JobPost`, `JobSeekerProfile`, and optionally the qualifying `JobApplication`/`Interview Session`.
- Review requires `rating` from 1 to 5 and `comment`.
- A JobSeeker can create a review only if they have a `JobApplication` for that `JobPost`.
- Final eligibility requires a completed interview session for that application.
- Enforce one active review per JobSeeker per JobPost.
- Review updates should be allowed only by the author.
- Review delete should be soft delete.
- JobPost detail response should include `averageRating`, `reviewCount`, and a paginated review list or a separate review endpoint if keeping detail payload small is preferred.
- Preferred API contracts:
  - `POST /interview-sessions` for Employer to create a session with proposed slots.
  - `GET /interview-sessions` for Employer/JobSeeker to list sessions according to role.
  - `GET /interview-sessions/{id}` for authorized session detail.
  - `POST /interview-sessions/{id}/select-slot` for JobSeeker to confirm one slot.
  - `POST /interview-sessions/{id}/cancel` for Employer cancellation.
  - `POST /interview-sessions/{id}/complete` for Employer completion.
  - `POST /jobpost/{jobPostId}/reviews` for JobSeeker review creation.
  - `GET /jobpost/{jobPostId}/reviews` for public paginated review list.
  - `PATCH /jobpost/{jobPostId}/reviews/{reviewId}` for author update.
  - `DELETE /jobpost/{jobPostId}/reviews/{reviewId}` for author soft delete.
- Suggested create interview request shape:

```json
{
  "applicationId": 123,
  "message": "Please choose one interview time.",
  "meetingLocation": "Google Meet",
  "meetingUrl": "https://meet.google.com/...",
  "slots": [
    { "startsAt": "2026-06-20T09:00:00" },
    { "startsAt": "2026-06-21T14:30:00" }
  ]
}
```

- Suggested select slot request shape:

```json
{
  "slotId": 456
}
```

- Suggested create review request shape:

```json
{
  "rating": 5,
  "comment": "The interview process was clear and professional."
}
```

- Return API responses using existing `ApiResponse` and `PageResponse` conventions where applicable.
- Keep authorization role-based:
  - `EMPLOYER` can create/manage sessions only for JobApplications belonging to their JobPosts.
  - `SEEKER` can select slots and review only for their own JobApplications.
  - Public users can read review summaries/list if JobPost detail is public.
- Add DB indexes for common queries:
  - Interview sessions by application, employer owner, job seeker user, status, and selected slot date.
  - JobPost reviews by job post, job seeker, rating, created date, and soft-delete marker.
- Keep FE timezone handling explicit. Backend currently uses `LocalDateTime`; FE should display and submit consistent local times until the project standardizes timezone storage.

## Testing Decisions

- Tests should assert externally visible behavior: API responses, state transitions, authorization, notification side effects, expiration behavior, and review eligibility. Avoid tests that depend on private helper implementation.
- Highest-value BE seam for interview scheduling is the service layer around `JobApplication` ownership, interview session creation, slot selection, session completion, and scheduled expiration.
- Add controller/API tests where endpoint authorization or request validation is easy to regress.
- Add repository tests for query-heavy review summaries and interview session filters only when service tests cannot cover the behavior reliably.
- Existing prior art:
  - `JobApplicationServiceTest` already tests application status transitions and notification publishing.
  - `JobPostServiceTest` already tests service validation and response mapping.
  - `JobPostRepositoryTest` can be used as a model for persistence/query behavior.
- Interview scheduling tests should cover:
  - Employer can create a session only for their own JobApplication.
  - Employer cannot create a session for another Employer's JobApplication.
  - Create session requires at least one future slot.
  - Creating a session sends `INTERVIEW_PROPOSED` notification to JobSeeker through `IN_APP` and `DEVICE`.
  - JobSeeker can select exactly one slot before `expiresAt`.
  - JobSeeker cannot select another user's session.
  - JobSeeker cannot select an expired session.
  - Selecting a slot marks session `CONFIRMED`, stores selected slot, optionally moves application to `INTERVIEW`, and notifies Employer.
  - Pending sessions become `EXPIRED` after 24 hours without selection.
  - Employer can mark a confirmed session as `COMPLETED`.
  - Employer can move application to `ACCEPTED` or `REJECTED` after interview.
- JobPost review tests should cover:
  - JobSeeker cannot review without applying to the JobPost.
  - JobSeeker cannot review before a completed interview session.
  - JobSeeker can create a review with rating 1-5 and comment.
  - Rating outside 1-5 is rejected.
  - Duplicate active review for the same JobSeeker and JobPost is rejected.
  - Review author can update and soft-delete their own review.
  - Another JobSeeker cannot update/delete the review.
  - JobPost detail or review list returns average rating, review count, and paginated reviews correctly.
- FE tests should cover:
  - Employer scheduling dialog appears when moving a candidate to the scheduling path.
  - Employer cannot submit with no slots or past slots.
  - JobSeeker notification route opens the slot selection form.
  - JobSeeker selection form handles pending, confirmed, expired, cancelled, and completed states.
  - Job Detail shows rating summary and review list.
  - Review form appears only for eligible authenticated JobSeekers.
  - Review form validates rating and comment before submit.

## Out of Scope

- Calendar provider integration such as Google Calendar, Outlook Calendar, or ICS export.
- Video meeting creation automation.
- Real-time chat between Employer and JobSeeker.
- Interview feedback forms for Employer.
- Multi-round interview workflows with named stages.
- Admin review moderation UI, beyond preserving auditable review data.
- Personalized recommendation based on review scores.
- Anonymous reviews.
- Payment/subscription changes for interview scheduling.
- Changing existing authentication, role, or subscription rules.

## Further Notes

- This PRD covers both FE and BE. The current repository is the backend, so FE work should consume the API contracts and state model described here.
- The phrase "khi chuyển jobseeker sang reviewing" should be implemented as updating the related `JobApplication`, not changing the `JobSeekerProfile`.
- To avoid stale state, FE should refresh application/session detail after each status update, slot selection, cancel, complete, or review mutation.
- The 24-hour timeout should be shown using an absolute deadline in UI, not only a relative countdown.
- Because `gh` CLI is not installed in this environment, this PRD is published as a local project document under `docs/` rather than as a GitHub issue.
