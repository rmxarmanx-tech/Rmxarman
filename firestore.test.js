const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or write focus_sessions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("focus_sessions").get());
  await assertFails(
    unauthDb.collection("focus_sessions").doc("s1").set({
      userId: ALICE_UID,
      mode: "FOCUS",
      durationMinutes: 25,
      subject: "Physics",
      completed: true,
    })
  );
});

test("Authenticated user: can create and query own focus_sessions with matching filter", async () => {
  const aliceContext = testEnv.authenticatedContext(ALICE_UID);
  const aliceDb = aliceContext.firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("focus_sessions").doc("session_1").set({
      userId: ALICE_UID,
      mode: "POMODORO",
      durationMinutes: 25,
      subject: "Mathematics Calculus",
      completed: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("focus_sessions").where("userId", "==", ALICE_UID).get()
  );
});

test("Authenticated user: cannot read another user's focus_sessions or query without filter", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("focus_sessions").doc("bob_session").set({
      userId: BOB_UID,
      mode: "FOCUS",
      durationMinutes: 45,
      subject: "Chemistry",
      completed: true,
      createdAt: now,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("focus_sessions").doc("bob_session").get());
  await assertFails(aliceDb.collection("focus_sessions").get());
});

test("Shadow field injection and invalid enum rejected on app_configs", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();

  // Valid create
  await assertSucceeds(
    aliceDb.collection("app_configs").doc("cfg_1").set({
      userId: ALICE_UID,
      packageName: "com.android.chrome",
      appName: "Google Chrome",
      category: "browser",
      isAllowed: true,
      dailyLimitMinutes: 120,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Shadow key injection
  await assertFails(
    aliceDb.collection("app_configs").doc("cfg_2").set({
      userId: ALICE_UID,
      packageName: "com.android.chrome",
      appName: "Google Chrome",
      category: "browser",
      isAllowed: true,
      dailyLimitMinutes: 120,
      isAdminOverride: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Invalid category enum
  await assertFails(
    aliceDb.collection("app_configs").doc("cfg_3").set({
      userId: ALICE_UID,
      packageName: "com.android.chrome",
      appName: "Google Chrome",
      category: "unknown_category",
      isAllowed: true,
      dailyLimitMinutes: 120,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Study assessments enforce score bounds and ownership", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("study_assessments").doc("exam_1").set({
      userId: ALICE_UID,
      scorePercent: 80,
      correctCount: 4,
      totalQuestions: 5,
      passed: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Invalid scorePercent > 100
  await assertFails(
    aliceDb.collection("study_assessments").doc("exam_2").set({
      userId: ALICE_UID,
      scorePercent: 150,
      correctCount: 4,
      totalQuestions: 5,
      passed: true,
      createdAt: now,
      updatedAt: now,
    })
  );
});
