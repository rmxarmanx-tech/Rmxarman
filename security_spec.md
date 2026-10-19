# Security Specification — UGT StudyGuard

## 1. Data Invariants
1. Every document in `/focus_sessions/{sessionId}`, `/app_configs/{configId}`, and `/study_assessments/{assessmentId}` MUST belong to an authenticated user (`request.auth != null`) and have `userId == request.auth.uid`.
2. `userId` and `createdAt` are immutable across all updates (`incoming().userId == existing().userId && incoming().createdAt == existing().createdAt`).
3. Every `allow list` rule enforces `resource.data.userId == request.auth.uid` so unaligned queries or cross-user reads fail with `PERMISSION_DENIED`.
4. All strings have explicit `.size()` bounds, enums are strictly constrained, integers are bounded, and document IDs are validated with `isValidId()`.

## 2. The Dirty Dozen Payloads
1. Unauthenticated read/write to `/focus_sessions/s1`
2. Cross-user read where `alice` reads `bob`'s `/focus_sessions/s_bob`
3. Cross-user write where `alice` creates `/focus_sessions/s1` with `userId: "bob"`
4. Shadow field injection (`{"isVerified": true}` extra key) on `/focus_sessions/s1`
5. Invalid enum (`mode: "HACK_MODE"`) on `/focus_sessions/s1`
6. Out-of-bounds integer (`durationMinutes: 999999`) on `/focus_sessions/s1`
7. Oversized string (`subject` > 100 chars) on `/focus_sessions/s1`
8. Mutating immutable `userId` or `createdAt` during `update` on `/app_configs/c1`
9. Invalid category enum (`category: "malware"`) on `/app_configs/c1`
10. Out-of-bounds `scorePercent: 150` on `/study_assessments/a1`
11. Unfiltered `list` query on `/focus_sessions` without `.where("userId", "==", uid)`
12. Future timestamp injection (`createdAt > request.time`) on any collection
