const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfilePayload(uid, now = new Date(Date.now() - 5000)) {
  return {
    userId: uid,
    displayName: "Ayesha Rahman",
    dailyCalorieGoal: 2000,
    proteinGoalG: 100,
    carbsGoalG: 240,
    fatGoalG: 65,
    waterGoalMl: 2500,
    weightKg: 64.5,
    targetWeightKg: 60.0,
    heightCm: 165,
    age: 27,
    goalMode: "LOSE",
    preferredLang: "bn",
    createdAt: now,
    updatedAt: now,
  };
}

function validMealPayload(uid, now = new Date(Date.now() - 5000)) {
  return {
    userId: uid,
    dateKey: "2026-11-24",
    mealType: "LUNCH",
    dishNameBn: "সাদা ভাত ও ইলিশ ভাজা",
    dishNameEn: "Steamed Rice & Fried Hilsa",
    cuisineType: "Bangladeshi",
    calories: 620,
    proteinG: 28.5,
    carbsG: 72.0,
    fatG: 22.0,
    fiberG: 3.5,
    portionMultiplier: 1.0,
    healthScore: 8,
    source: "PHOTO_AI",
    itemsSummary: "ভাত (১.৫ কাপ) ৩১০ kcal • ইলিশ ভাজা (১ পিস) ৩১০ kcal",
    aiTipBn: "সাথে সালাদ যোগ করলে ফাইবার বাড়বে।",
    aiTipEn: "Add fresh salad for extra dietary fiber.",
    createdAt: now,
    updatedAt: now,
  };
}

function validDailyLogPayload(uid, dateKey = "2026-11-24", now = new Date(Date.now() - 5000)) {
  return {
    userId: uid,
    dateKey,
    waterMl: 1250,
    coachTip: "রাতের খাবারে হালকা প্রোটিন ও সবজি রাখুন।",
    createdAt: now,
    updatedAt: now,
  };
}

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

test("Unauthenticated requests are denied across all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("meals").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("meals")
      .doc("meal_1")
      .set(validMealPayload(ALICE_UID))
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("daily_logs")
      .doc("2026-11-24")
      .set(validDailyLogPayload(ALICE_UID))
  );
});

test("Authenticated user can create, read, partially update, and delete own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(profileRef.set(validProfilePayload(ALICE_UID)));
  await assertSucceeds(profileRef.get());

  // Partial update of goals & language
  await assertSucceeds(
    profileRef.update({
      dailyCalorieGoal: 1850,
      preferredLang: "en",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject ghost field ("Shadow Update Test")
  await assertFails(
    profileRef.update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject immutable createdAt modification
  await assertFails(
    profileRef.update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .set(validProfilePayload(BOB_UID, new Date(Date.now() + 60000)))
  );

  // Reject listing /users root collection
  await assertFails(aliceDb.collection("users").get());
});

test("Authenticated user can create, list, update, and delete own meals with strict schema checks", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const mealsCol = aliceDb.collection("users").doc(ALICE_UID).collection("meals");

  // List empty subcollection succeeds
  await assertSucceeds(mealsCol.get());

  // Create meal without @DocumentId id field
  await assertSucceeds(mealsCol.doc("meal_1").set(validMealPayload(ALICE_UID)));

  // Reject meal with extra id field
  await assertFails(
    mealsCol.doc("meal_2").set({
      ...validMealPayload(ALICE_UID),
      id: "meal_2",
    })
  );

  // Partial update of portion & calories
  await assertSucceeds(
    mealsCol.doc("meal_1").update({
      portionMultiplier: 1.5,
      calories: 930,
      proteinG: 42.75,
      carbsG: 108.0,
      fatG: 33.0,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Filtered query by dateKey matches repository query
  await assertSucceeds(mealsCol.where("dateKey", "==", "2026-11-24").get());

  // Reject invalid bounds
  await assertFails(
    mealsCol.doc("meal_1").update({
      calories: 50000,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(mealsCol.doc("meal_1").delete());
});

test("Authenticated user can manage own daily_logs and dateKey validation holds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const logsCol = aliceDb.collection("users").doc(ALICE_UID).collection("daily_logs");

  await assertSucceeds(logsCol.get());
  await assertSucceeds(
    logsCol.doc("2026-11-24").set(validDailyLogPayload(ALICE_UID, "2026-11-24"))
  );

  await assertSucceeds(
    logsCol.doc("2026-11-24").update({
      waterMl: 1500,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject mismatched dateKey document ID vs payload
  await assertFails(
    logsCol.doc("2026-11-25").set(validDailyLogPayload(ALICE_UID, "2026-11-24"))
  );

  // Reject invalid dateKey format
  await assertFails(
    logsCol.doc("bad-date").set(validDailyLogPayload(ALICE_UID, "bad-date"))
  );
});

test("Cross-user isolation: Alice cannot access or mutate Bob's data", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const adminDb = context.firestore();
    await adminDb.collection("users").doc(BOB_UID).set(validProfilePayload(BOB_UID));
    await adminDb
      .collection("users")
      .doc(BOB_UID)
      .collection("meals")
      .doc("bob_meal")
      .set(validMealPayload(BOB_UID));
    await adminDb
      .collection("users")
      .doc(BOB_UID)
      .collection("daily_logs")
      .doc("2026-11-24")
      .set(validDailyLogPayload(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).update({
      dailyCalorieGoal: 1200,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("meals").get()
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("meals")
      .doc("bob_meal")
      .delete()
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("daily_logs")
      .doc("2026-11-24")
      .get()
  );
});
